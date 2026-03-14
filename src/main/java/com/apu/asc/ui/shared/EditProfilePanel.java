package com.apu.asc.ui.shared;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.service.UserService;
import com.apu.asc.util.Result;
import java.awt.*;
import javax.swing.*;

public class EditProfilePanel extends JPanel {

  private final User user;
  private final UserService userService = new UserService();
  private final AuthService authService = new AuthService();

  private JTextField fullNameField;
  private JTextField emailField;
  private JTextField contactField;

  private JPasswordField currentPassField;
  private JPasswordField newPassField;
  private JPasswordField confirmPassField;

  public EditProfilePanel(User user) {
    this.user = user;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    buildUI();
  }

  private void buildUI() {
    add(sectionTitle("Profile Information"));
    add(Box.createVerticalStrut(8));

    JPanel infoForm = new JPanel(new GridBagLayout());
    infoForm.setBackground(new Color(45, 45, 45));
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(5, 4, 5, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;

    fullNameField = new JTextField(user.getFullName(), 22);
    emailField = new JTextField(user.getEmail(), 22);
    contactField = new JTextField(user.getContactNumber(), 22);

    addFormRow(infoForm, gc, 0, "Full Name:", fullNameField);
    addFormRow(infoForm, gc, 1, "Email:", emailField);
    addFormRow(infoForm, gc, 2, "Contact:", contactField);

    JButton saveBtn = new JButton("Save Profile");
    styleButton(saveBtn, new Color(0, 120, 215));
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 2;
    infoForm.add(saveBtn, gc);

    add(infoForm);
    add(Box.createVerticalStrut(20));
    add(sectionTitle("Change Password"));
    add(Box.createVerticalStrut(8));

    JPanel passForm = new JPanel(new GridBagLayout());
    passForm.setBackground(new Color(45, 45, 45));
    GridBagConstraints gc2 = new GridBagConstraints();
    gc2.insets = new Insets(5, 4, 5, 4);
    gc2.fill = GridBagConstraints.HORIZONTAL;

    currentPassField = new JPasswordField(22);
    newPassField = new JPasswordField(22);
    confirmPassField = new JPasswordField(22);

    addFormRow(passForm, gc2, 0, "Current Password:", currentPassField);
    addFormRow(passForm, gc2, 1, "New Password:", newPassField);
    addFormRow(passForm, gc2, 2, "Confirm New:", confirmPassField);

    JButton changeBtn = new JButton("Change Password");
    styleButton(changeBtn, new Color(180, 80, 0));
    gc2.gridx = 0;
    gc2.gridy = 3;
    gc2.gridwidth = 2;
    passForm.add(changeBtn, gc2);

    add(passForm);

    saveBtn.addActionListener(e -> handleSaveProfile());
    changeBtn.addActionListener(e -> handleChangePassword());
  }

  private void handleSaveProfile() {
    String name = fullNameField.getText().trim();
    String email = emailField.getText().trim();
    String contact = contactField.getText().trim();

    if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
      showError("All fields are required.");
      return;
    }

    Result<User> result = userService.updateProfile(user, name, email, contact);
    if (!result.isSuccess()) {
      showError(result.getError());
      return;
    }
    JOptionPane.showMessageDialog(
        this, "Profile updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
  }

  private void handleChangePassword() {
    String current = new String(currentPassField.getPassword());
    String newPass = new String(newPassField.getPassword());
    String confirm = new String(confirmPassField.getPassword());

    if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
      showError("All password fields are required.");
      return;
    }
    if (!newPass.equals(confirm)) {
      showError("New passwords do not match.");
      return;
    }
    if (newPass.length() < 6) {
      showError("New password must be at least 6 characters.");
      return;
    }

    boolean ok = authService.changePassword(user, current, newPass);
    if (!ok) {
      showError("Current password is incorrect.");
      return;
    }

    currentPassField.setText("");
    newPassField.setText("");
    confirmPassField.setText("");
    JOptionPane.showMessageDialog(
        this, "Password changed successfully.", "Done", JOptionPane.INFORMATION_MESSAGE);
  }

  private void addFormRow(
      JPanel panel, GridBagConstraints gc, int row, String label, JComponent field) {
    gc.gridx = 0;
    gc.gridy = row;
    gc.weightx = 0.35;
    gc.gridwidth = 1;
    panel.add(styledLabel(label), gc);
    gc.gridx = 1;
    gc.weightx = 0.65;
    panel.add(field, gc);
  }

  private JLabel sectionTitle(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
    lbl.setForeground(new Color(180, 180, 180));
    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    return lbl;
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Color.LIGHT_GRAY);
    return lbl;
  }

  private void styleButton(JButton btn, Color bg) {
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  private void showError(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
