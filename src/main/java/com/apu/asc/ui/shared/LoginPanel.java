package com.apu.asc.ui.shared;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.service.OtpService;
import com.apu.asc.ui.AppShell;
import com.apu.asc.ui.util.LanguageManager;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

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
    JLabel title = new JLabel(LanguageManager.t("app.shortTitle"));
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
    form.add(styledLabel(LanguageManager.t("label.username")), gc);
    gc.gridx = 1;
    gc.weightx = 0.7;
    usernameField = new JTextField(18);
    form.add(usernameField, gc);

    gc.gridx = 0;
    gc.gridy = 1;
    gc.weightx = 0.3;
    form.add(styledLabel(LanguageManager.t("label.password")), gc);
    gc.gridx = 1;
    gc.weightx = 0.7;
    passwordField = new JPasswordField(18);
    JPanel pwRow = new JPanel(new BorderLayout(4, 0));
    pwRow.setBackground(Theme.BG_PANEL);
    JToggleButton pwToggle = new JToggleButton(LanguageManager.t("toggle.show"));
    pwToggle.setFocusPainted(false);
    pwToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    pwToggle.setFont(Theme.FONT_BODY);
    pwToggle.addActionListener(
        e -> {
          if (pwToggle.isSelected()) {
            passwordField.setEchoChar((char) 0);
            pwToggle.setText(LanguageManager.t("toggle.hide"));
          } else {
            passwordField.setEchoChar('\u2022');
            pwToggle.setText(LanguageManager.t("toggle.show"));
          }
        });
    pwRow.add(passwordField, BorderLayout.CENTER);
    pwRow.add(pwToggle, BorderLayout.EAST);
    form.add(pwRow, gc);

    messageLabel = new JLabel(" ");
    messageLabel.setForeground(Theme.TEXT_ERROR);
    messageLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 2;
    form.add(messageLabel, gc);

    JButton loginBtn = new JButton(LanguageManager.t("btn.login"));
    loginBtn.setBackground(Theme.BTN_PRIMARY);
    loginBtn.setForeground(Theme.TEXT_PRIMARY);
    loginBtn.setFocusPainted(false);
    loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 3;
    form.add(loginBtn, gc);

    JButton registerBtn = new JButton(LanguageManager.t("btn.registerCustomer"));
    registerBtn.setForeground(Theme.TEXT_LINK);
    registerBtn.setBorderPainted(false);
    registerBtn.setContentAreaFilled(false);
    registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 4;
    form.add(registerBtn, gc);

    JButton forgotBtn = new JButton(LanguageManager.t("btn.forgotPassword"));
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
      messageLabel.setText(LanguageManager.t("msg.login.required"));
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
        new JDialog(
            owner,
            LanguageManager.t("dialog.forgotPassword.title"),
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
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

    JLabel instruction = new JLabel(LanguageManager.t("msg.otp.usernamePrompt"));
    instruction.setForeground(Theme.TEXT_SECONDARY);
    instruction.setFont(Theme.FONT_BODY);
    gc.gridy = 0;
    panel.add(instruction, gc);

    gc.gridwidth = 1;
    gc.weightx = 0.35;
    gc.gridx = 0;
    gc.gridy = 1;
    panel.add(styledLabel(LanguageManager.t("label.username")), gc);
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

    JButton sendBtn = new JButton(LanguageManager.t("btn.sendOtp"));
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
            statusLabel.setText(LanguageManager.t("msg.otp.usernameRequired"));
            return;
          }

          User user = userDAO.findByUsername(username);
          if (user == null) {
            statusLabel.setText(LanguageManager.t("msg.otp.noAccount"));
            return;
          }
          if (user.getEmail() == null || user.getEmail().isBlank()) {
            statusLabel.setText(LanguageManager.t("msg.otp.noEmail"));
            return;
          }

          sendBtn.setEnabled(false);
          statusLabel.setForeground(Theme.TEXT_MUTED);
          statusLabel.setText(LanguageManager.t("msg.otp.sending"));

          String otp = otpService.generateOtp(username);

          otpService
              .sendOtpEmail(user.getEmail(), username, otp)
              .thenRun(
                  () ->
                      SwingUtilities.invokeLater(
                          () -> {
                            dialog.dispose();
                            showForgotStep2(owner, username, user.getEmail());
                          }))
              .exceptionally(
                  ex -> {
                    SwingUtilities.invokeLater(
                        () -> {
                          sendBtn.setEnabled(true);
                          statusLabel.setForeground(Theme.TEXT_ERROR);
                          statusLabel.setText(LanguageManager.t("msg.otp.sendFailed"));
                        });
                    return null;
                  });
        });

    dialog.setVisible(true);
  }

  private void showForgotStep2(Window owner, String username, String maskedEmail) {
    JDialog dialog =
        new JDialog(
            owner,
            LanguageManager.t("dialog.otp.title"),
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
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

    JLabel instruction =
        new JLabel(String.format(LanguageManager.t("msg.otp.sentTo"), maskedEmail));
    instruction.setForeground(Theme.TEXT_SECONDARY);
    instruction.setFont(Theme.FONT_BODY);
    gc.gridy = 0;
    panel.add(instruction, gc);

    JLabel expiry = new JLabel(LanguageManager.t("msg.otp.expires"));
    expiry.setForeground(Theme.TEXT_MUTED);
    expiry.setFont(Theme.FONT_BODY);
    gc.gridy = 1;
    panel.add(expiry, gc);

    gc.gridwidth = 1;
    gc.weightx = 0.35;
    gc.gridx = 0;
    gc.gridy = 2;
    panel.add(styledLabel(LanguageManager.t("label.otpCode")), gc);
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

    JButton verifyBtn = new JButton(LanguageManager.t("btn.verifyOtp"));
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
            statusLabel.setText(LanguageManager.t("msg.otp.codeRequired"));
            return;
          }
          if (!otpService.validateOtp(username, code)) {
            statusLabel.setText(LanguageManager.t("msg.otp.invalid"));
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
        new JDialog(
            owner,
            LanguageManager.t("dialog.resetPassword.title"),
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
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

    JLabel instruction =
        new JLabel(String.format(LanguageManager.t("msg.resetPassword.prompt"), username));
    instruction.setForeground(Theme.TEXT_SECONDARY);
    instruction.setFont(Theme.FONT_BODY);
    gc.gridy = 0;
    panel.add(instruction, gc);

    gc.gridwidth = 1;
    gc.weightx = 0.4;
    gc.gridx = 0;
    gc.gridy = 1;
    panel.add(styledLabel(LanguageManager.t("label.newPassword")), gc);
    gc.gridx = 1;
    gc.weightx = 0.6;
    JPasswordField newPassField = new JPasswordField(16);
    panel.add(newPassField, gc);

    gc.gridx = 0;
    gc.gridy = 2;
    gc.weightx = 0.4;
    panel.add(styledLabel(LanguageManager.t("label.confirmPassword")), gc);
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

    JButton resetBtn = new JButton(LanguageManager.t("btn.resetPassword"));
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
            statusLabel.setText(LanguageManager.t("msg.resetPassword.required"));
            return;
          }
          if (newPass.length() < 6) {
            statusLabel.setText(LanguageManager.t("msg.password.minLength"));
            return;
          }
          if (!newPass.equals(confirmPass)) {
            statusLabel.setText(LanguageManager.t("msg.password.mismatch"));
            return;
          }

          User user = userDAO.findByUsername(username);
          if (user == null) {
            statusLabel.setText(LanguageManager.t("msg.account.notFound"));
            return;
          }

          user.setPasswordHash(PasswordUtil.hash(newPass));
          userDAO.save(user);

          dialog.dispose();
          messageLabel.setForeground(Theme.TEXT_SUCCESS);
          messageLabel.setText(LanguageManager.t("msg.password.resetSuccess"));
        });

    dialog.setVisible(true);
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    return lbl;
  }
}
