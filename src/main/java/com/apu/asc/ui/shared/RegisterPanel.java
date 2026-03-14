package com.apu.asc.ui.shared;

import com.apu.asc.service.AuthService;
import com.apu.asc.ui.AppShell;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class RegisterPanel extends JPanel {

  private final AuthService authService = new AuthService();
  private final AppShell shell;

  private JTextField usernameField;
  private JTextField fullNameField;
  private JTextField emailField;
  private JTextField contactField;
  private JPasswordField passwordField;
  private JPasswordField confirmField;
  private JLabel messageLabel;

  public RegisterPanel(AppShell shell) {
    this.shell = shell;
    setLayout(new BorderLayout());
    setBackground(Theme.BG_ROOT);
    buildUI();
  }

  private void buildUI() {
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setBackground(Theme.BG_ROOT);

    JPanel card = new JPanel(new BorderLayout(0, 0));
    card.setBackground(Theme.BG_PANEL);
    card.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));

    JLabel title = new JLabel("Create Customer Account");
    title.setFont(Theme.FONT_TITLE);
    title.setForeground(Theme.TEXT_PRIMARY);
    title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    card.add(title, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Theme.BG_PANEL);
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(5, 4, 5, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;

    usernameField = new JTextField(18);
    fullNameField = new JTextField(18);
    emailField = new JTextField(18);
    contactField = new JTextField(18);
    passwordField = new JPasswordField(18);
    confirmField = new JPasswordField(18);

    Object[][] rows = {
      {"Username:", usernameField},
      {"Full Name:", fullNameField},
      {"Email:", emailField},
      {"Contact No:", contactField},
      {"Password:", passwordField},
      {"Confirm Password:", confirmField}
    };

    for (int i = 0; i < rows.length; i++) {
      gc.gridx = 0;
      gc.gridy = i;
      gc.weightx = 0.35;
      form.add(styledLabel((String) rows[i][0]), gc);
      gc.gridx = 1;
      gc.weightx = 0.65;
      form.add((Component) rows[i][1], gc);
    }

    messageLabel = new JLabel(" ");
    messageLabel.setForeground(Theme.TEXT_ERROR);
    messageLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = rows.length;
    gc.gridwidth = 2;
    form.add(messageLabel, gc);

    JButton submitBtn = new JButton("Register");
    submitBtn.setBackground(Theme.BTN_SUCCESS);
    submitBtn.setForeground(Theme.TEXT_PRIMARY);
    submitBtn.setFocusPainted(false);
    submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = rows.length + 1;
    form.add(submitBtn, gc);

    JButton backBtn = new JButton("Back to Login");
    backBtn.setForeground(Theme.TEXT_LINK);
    backBtn.setBorderPainted(false);
    backBtn.setContentAreaFilled(false);
    backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = rows.length + 2;
    form.add(backBtn, gc);

    card.add(form, BorderLayout.CENTER);

    GridBagConstraints wc = new GridBagConstraints();
    wrapper.add(card, wc);
    add(wrapper, BorderLayout.CENTER);

    submitBtn.addActionListener(e -> handleRegister());
    backBtn.addActionListener(e -> shell.showCard(AppShell.CARD_LOGIN));
  }

  private void handleRegister() {
    String username = usernameField.getText().trim();
    String fullName = fullNameField.getText().trim();
    String email = emailField.getText().trim();
    String contact = contactField.getText().trim();
    String password = new String(passwordField.getPassword());
    String confirm = new String(confirmField.getPassword());

    if (username.isEmpty()
        || fullName.isEmpty()
        || email.isEmpty()
        || contact.isEmpty()
        || password.isEmpty()) {
      messageLabel.setText("All fields are required.");
      return;
    }

    if (!password.equals(confirm)) {
      messageLabel.setText("Passwords do not match.");
      return;
    }

    if (password.length() < 6) {
      messageLabel.setText("Password must be at least 6 characters.");
      return;
    }

    Result<?> result = authService.register(username, password, fullName, email, contact);
    if (!result.isSuccess()) {
      messageLabel.setText(result.getError());
      return;
    }

    clearFields();
    Toast.success(SwingUtilities.getWindowAncestor(this), "Account created! You can now log in.");
    shell.showCard(AppShell.CARD_LOGIN);
  }

  private void clearFields() {
    usernameField.setText("");
    fullNameField.setText("");
    emailField.setText("");
    contactField.setText("");
    passwordField.setText("");
    confirmField.setText("");
    messageLabel.setText(" ");
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    return lbl;
  }
}
