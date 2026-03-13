package com.apu.asc.ui.shared;

import com.apu.asc.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JDialog {

    private final AuthService authService = new AuthService();
    private final LoginFrame parent;

    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField contactField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel messageLabel;

    public RegisterFrame(LoginFrame parent) {
        super(parent, "Register — APU-ASC", true);
        this.parent = parent;
        setSize(440, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(45, 45, 45));
        root.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));

        JLabel title = new JLabel("Create Customer Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(45, 45, 45));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(18);
        fullNameField = new JTextField(18);
        emailField    = new JTextField(18);
        contactField  = new JTextField(18);
        passwordField = new JPasswordField(18);
        confirmField  = new JPasswordField(18);

        Object[][] rows = {
            {"Username:", usernameField},
            {"Full Name:", fullNameField},
            {"Email:", emailField},
            {"Contact No:", contactField},
            {"Password:", passwordField},
            {"Confirm Password:", confirmField}
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35;
            form.add(styledLabel((String) rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 0.65;
            form.add((Component) rows[i][1], gc);
        }

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(new Color(220, 80, 80));
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gc.gridx = 0; gc.gridy = rows.length; gc.gridwidth = 2;
        form.add(messageLabel, gc);

        JButton submitBtn = new JButton("Register");
        submitBtn.setBackground(new Color(0, 160, 80));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gc.gridy = rows.length + 1;
        form.add(submitBtn, gc);

        root.add(form, BorderLayout.CENTER);
        add(root);

        submitBtn.addActionListener(e -> handleRegister());
    }

    private void handleRegister() {
        String username  = usernameField.getText().trim();
        String fullName  = fullNameField.getText().trim();
        String email     = emailField.getText().trim();
        String contact   = contactField.getText().trim();
        String password  = new String(passwordField.getPassword());
        String confirm   = new String(confirmField.getPassword());

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()
                || contact.isEmpty() || password.isEmpty()) {
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

        var result = authService.register(username, password, fullName, email, contact);
        if (result == null) {
            messageLabel.setText("Username already taken. Choose another.");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Account created! You can now log in.", "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.LIGHT_GRAY);
        return lbl;
    }
}
