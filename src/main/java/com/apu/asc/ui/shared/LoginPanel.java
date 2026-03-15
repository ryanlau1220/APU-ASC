package com.apu.asc.ui.shared;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.service.OtpService;
import com.apu.asc.ui.AppShell;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class LoginPanel extends JPanel {

  private final AuthService authService = new AuthService();
  private final OtpService otpService = new OtpService();
  private final UserDAO userDAO = UserDAO.getInstance();
  private final AppShell shell;

  private JTextField usernameField;
  private JPasswordField passwordField;
  private JLabel messageLabel;

  public LoginPanel(AppShell shell) {
    this.shell = shell;
    setLayout(new BorderLayout());
    setBackground(Theme.BG_ROOT);
    buildUI();
  }

  private void buildUI() {
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setBackground(Theme.BG_ROOT);

    JPanel card = new JPanel(new BorderLayout(0, 0));
    card.setBackground(Theme.BG_ROOT);

    JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
    header.setBackground(Theme.BG_ROOT);
    JLabel title = new JLabel("APU-ASC");
    title.setFont(Theme.FONT_DISPLAY);
    title.setForeground(Theme.TEXT_PRIMARY);
    header.add(title);
    header.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
    card.add(header, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Theme.BG_PANEL);
    form.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));

    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(6, 4, 6, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.weightx = 0.3;
    form.add(styledLabel("Username:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.7;
    usernameField = new JTextField(18);
    form.add(usernameField, gc);

    gc.gridx = 0;
    gc.gridy = 1;
    gc.weightx = 0.3;
    form.add(styledLabel("Password:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.7;
    passwordField = new JPasswordField(18);
    form.add(passwordField, gc);

    messageLabel = new JLabel(" ");
    messageLabel.setForeground(Theme.TEXT_ERROR);
    messageLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 2;
    form.add(messageLabel, gc);

    JButton loginBtn = new JButton("Login");
    loginBtn.setBackground(Theme.BTN_PRIMARY);
    loginBtn.setForeground(Theme.TEXT_PRIMARY);
    loginBtn.setFocusPainted(false);
    loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 3;
    form.add(loginBtn, gc);

    JButton registerBtn = new JButton("Register as Customer");
    registerBtn.setForeground(Theme.TEXT_LINK);
    registerBtn.setBorderPainted(false);
    registerBtn.setContentAreaFilled(false);
    registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 4;
    form.add(registerBtn, gc);

    JButton forgotBtn = new JButton("Forgot Password?");
    forgotBtn.setForeground(Theme.TEXT_LINK);
    forgotBtn.setBorderPainted(false);
    forgotBtn.setContentAreaFilled(false);
    forgotBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 5;
    form.add(forgotBtn, gc);

    card.add(form, BorderLayout.CENTER);

    GridBagConstraints wc = new GridBagConstraints();
    wrapper.add(card, wc);
    add(wrapper, BorderLayout.CENTER);

    loginBtn.addActionListener(e -> handleLogin());
    passwordField.addActionListener(e -> handleLogin());
    registerBtn.addActionListener(e -> shell.showCard(AppShell.CARD_REGISTER));
    forgotBtn.addActionListener(e -> showForgotStep1());
  }

  private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
      messageLabel.setText("Username and password are required.");
      return;
    }

    Result<User> result = authService.login(username, password);
    if (!result.isSuccess()) {
      messageLabel.setText(result.getError());
      passwordField.setText("");
      return;
    }

    messageLabel.setText(" ");
    usernameField.setText("");
    passwordField.setText("");
    shell.onLoginSuccess(result.getValue());
  }

  private void showForgotStep1() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(owner, "Forgot Password", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(400, 220);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Theme.BG_PANEL);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(6, 4, 6, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.gridwidth = 2;

    JLabel instruction = new JLabel("Enter your username to receive a reset OTP.");
    instruction.setForeground(Theme.TEXT_SECONDARY);
    instruction.setFont(Theme.FONT_BODY);
    gc.gridy = 0;
    panel.add(instruction, gc);

    gc.gridwidth = 1;
    gc.weightx = 0.35;
    gc.gridx = 0;
    gc.gridy = 1;
    panel.add(styledLabel("Username:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.65;
    JTextField usernameInput = new JTextField(16);
    panel.add(usernameInput, gc);

    JLabel statusLabel = new JLabel(" ");
    statusLabel.setForeground(Theme.TEXT_ERROR);
    statusLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 2;
    panel.add(statusLabel, gc);

    JButton sendBtn = new JButton("Send OTP");
    sendBtn.setBackground(Theme.BTN_PRIMARY);
    sendBtn.setForeground(Theme.TEXT_PRIMARY);
    sendBtn.setFocusPainted(false);
    sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 3;
    panel.add(sendBtn, gc);

    dialog.add(panel);
    dialog.setBackground(Theme.BG_PANEL);

    sendBtn.addActionListener(
        e -> {
          String username = usernameInput.getText().trim();
          if (username.isEmpty()) {
            statusLabel.setText("Username is required.");
            return;
          }

          User user = userDAO.findByUsername(username);
          if (user == null) {
            statusLabel.setText("No account found with that username.");
            return;
          }
          if (user.getEmail() == null || user.getEmail().isBlank()) {
            statusLabel.setText("No email address on file for this account.");
            return;
          }

          sendBtn.setEnabled(false);
          statusLabel.setForeground(Theme.TEXT_MUTED);
          statusLabel.setText("Sending OTP...");

          String otp = otpService.generateOtp(username);

          new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
              otpService.sendOtpEmail(user.getEmail(), username, otp);
              return true;
            }

            @Override
            protected void done() {
              boolean success;
              try {
                success = get();
              } catch (InterruptedException | ExecutionException ex) {
                success = false;
              }
              if (success) {
                dialog.dispose();
                showForgotStep2(owner, username, user.getEmail());
              } else {
                sendBtn.setEnabled(true);
                statusLabel.setForeground(Theme.TEXT_ERROR);
                statusLabel.setText("Failed to send OTP. Check SMTP config.");
              }
            }
          }.execute();
        });

    dialog.setVisible(true);
  }

  private void showForgotStep2(Window owner, String username, String maskedEmail) {
    JDialog dialog =
        new JDialog(owner, "Enter OTP", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(400, 240);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Theme.BG_PANEL);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(6, 4, 6, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.gridwidth = 2;

    JLabel instruction = new JLabel("Enter the 6-digit OTP sent to " + maskedEmail + ".");
    instruction.setForeground(Theme.TEXT_SECONDARY);
    instruction.setFont(Theme.FONT_BODY);
    gc.gridy = 0;
    panel.add(instruction, gc);

    JLabel expiry = new JLabel("The code expires in 5 minutes.");
    expiry.setForeground(Theme.TEXT_MUTED);
    expiry.setFont(Theme.FONT_BODY);
    gc.gridy = 1;
    panel.add(expiry, gc);

    gc.gridwidth = 1;
    gc.weightx = 0.35;
    gc.gridx = 0;
    gc.gridy = 2;
    panel.add(styledLabel("OTP Code:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.65;
    JTextField otpInput = new JTextField(10);
    panel.add(otpInput, gc);

    JLabel statusLabel = new JLabel(" ");
    statusLabel.setForeground(Theme.TEXT_ERROR);
    statusLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 2;
    panel.add(statusLabel, gc);

    JButton verifyBtn = new JButton("Verify OTP");
    verifyBtn.setBackground(Theme.BTN_PRIMARY);
    verifyBtn.setForeground(Theme.TEXT_PRIMARY);
    verifyBtn.setFocusPainted(false);
    verifyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 4;
    panel.add(verifyBtn, gc);

    dialog.add(panel);

    verifyBtn.addActionListener(
        e -> {
          String code = otpInput.getText().trim();
          if (code.isEmpty()) {
            statusLabel.setText("OTP code is required.");
            return;
          }
          if (!otpService.validateOtp(username, code)) {
            statusLabel.setText("Invalid or expired OTP. Please try again.");
            return;
          }
          otpService.clearOtp(username);
          dialog.dispose();
          showForgotStep3(owner, username);
        });

    dialog.setVisible(true);
  }

  private void showForgotStep3(Window owner, String username) {
    JDialog dialog =
        new JDialog(owner, "Set New Password", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(420, 270);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Theme.BG_PANEL);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(6, 4, 6, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.gridwidth = 2;

    JLabel instruction = new JLabel("Enter a new password for \"" + username + "\".");
    instruction.setForeground(Theme.TEXT_SECONDARY);
    instruction.setFont(Theme.FONT_BODY);
    gc.gridy = 0;
    panel.add(instruction, gc);

    gc.gridwidth = 1;
    gc.weightx = 0.4;
    gc.gridx = 0;
    gc.gridy = 1;
    panel.add(styledLabel("New Password:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.6;
    JPasswordField newPassField = new JPasswordField(16);
    panel.add(newPassField, gc);

    gc.gridx = 0;
    gc.gridy = 2;
    gc.weightx = 0.4;
    panel.add(styledLabel("Confirm Password:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.6;
    JPasswordField confirmPassField = new JPasswordField(16);
    panel.add(confirmPassField, gc);

    JLabel statusLabel = new JLabel(" ");
    statusLabel.setForeground(Theme.TEXT_ERROR);
    statusLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 2;
    panel.add(statusLabel, gc);

    JButton resetBtn = new JButton("Reset Password");
    resetBtn.setBackground(Theme.BTN_PRIMARY);
    resetBtn.setForeground(Theme.TEXT_PRIMARY);
    resetBtn.setFocusPainted(false);
    resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 4;
    panel.add(resetBtn, gc);

    dialog.add(panel);

    resetBtn.addActionListener(
        e -> {
          String newPass = new String(newPassField.getPassword());
          String confirmPass = new String(confirmPassField.getPassword());

          if (newPass.isEmpty()) {
            statusLabel.setText("New password is required.");
            return;
          }
          if (newPass.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters.");
            return;
          }
          if (!newPass.equals(confirmPass)) {
            statusLabel.setText("Passwords do not match.");
            return;
          }

          User user = userDAO.findByUsername(username);
          if (user == null) {
            statusLabel.setText("Account not found. Please try again.");
            return;
          }

          user.setPasswordHash(PasswordUtil.hash(newPass));
          userDAO.save(user);

          dialog.dispose();
          messageLabel.setForeground(Theme.TEXT_SUCCESS);
          messageLabel.setText("Password reset successfully. Please log in.");
        });

    dialog.setVisible(true);
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    return lbl;
  }
}
