package com.apu.asc.ui.shared;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.customer.CustomerDashboard;
import com.apu.asc.ui.manager.ManagerDashboard;
import com.apu.asc.ui.staff.StaffDashboard;
import com.apu.asc.ui.technician.TechnicianDashboard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {

  private final AuthService authService = new AuthService();

  private JTextField usernameField;
  private JPasswordField passwordField;
  private JLabel messageLabel;

  public LoginFrame() {
    setTitle("APU Automotive Service Centre — Login");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(420, 300);
    setLocationRelativeTo(null);
    setResizable(false);
    buildUI();
  }

  private void buildUI() {
    JPanel root = new JPanel(new BorderLayout(0, 0));
    root.setBackground(new Color(30, 30, 30));

    JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
    header.setBackground(new Color(30, 30, 30));
    JLabel title = new JLabel("APU-ASC");
    title.setFont(new Font("SansSerif", Font.BOLD, 22));
    title.setForeground(Color.WHITE);
    header.add(title);
    header.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
    root.add(header, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(new Color(45, 45, 45));
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
    messageLabel.setForeground(new Color(220, 80, 80));
    messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
    gc.gridx = 0;
    gc.gridy = 2;
    gc.gridwidth = 2;
    form.add(messageLabel, gc);

    JButton loginBtn = new JButton("Login");
    loginBtn.setBackground(new Color(0, 120, 215));
    loginBtn.setForeground(Color.WHITE);
    loginBtn.setFocusPainted(false);
    loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 3;
    form.add(loginBtn, gc);

    JButton registerBtn = new JButton("Register as Customer");
    registerBtn.setForeground(new Color(100, 160, 255));
    registerBtn.setBorderPainted(false);
    registerBtn.setContentAreaFilled(false);
    registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 4;
    form.add(registerBtn, gc);

    root.add(form, BorderLayout.CENTER);
    add(root);

    loginBtn.addActionListener(e -> handleLogin());
    passwordField.addActionListener(e -> handleLogin());
    registerBtn.addActionListener(e -> openRegister());
  }

  private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
      messageLabel.setText("Username and password are required.");
      return;
    }

    User user = authService.login(username, password);
    if (user == null) {
      messageLabel.setText("Invalid credentials or account is deactivated.");
      passwordField.setText("");
      return;
    }

    dispose();
    openDashboard(user);
  }

  private void openDashboard(User user) {
    switch (user.getRole()) {
      case CUSTOMER:
        new CustomerDashboard(user).setVisible(true);
        break;
      case STAFF:
        new StaffDashboard(user).setVisible(true);
        break;
      case TECHNICIAN:
        new TechnicianDashboard(user).setVisible(true);
        break;
      case MANAGER:
      default:
        new ManagerDashboard(user).setVisible(true);
        break;
    }
  }

  private void openRegister() {
    new RegisterFrame(this).setVisible(true);
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Color.LIGHT_GRAY);
    return lbl;
  }
}
