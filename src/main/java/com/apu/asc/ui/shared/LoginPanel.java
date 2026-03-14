package com.apu.asc.ui.shared;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.AppShell;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginPanel extends JPanel {

  private final AuthService authService = new AuthService();
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

    card.add(form, BorderLayout.CENTER);

    GridBagConstraints wc = new GridBagConstraints();
    wrapper.add(card, wc);
    add(wrapper, BorderLayout.CENTER);

    loginBtn.addActionListener(e -> handleLogin());
    passwordField.addActionListener(e -> handleLogin());
    registerBtn.addActionListener(e -> shell.showCard(AppShell.CARD_REGISTER));
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

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    return lbl;
  }
}
