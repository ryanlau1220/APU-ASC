package com.apu.asc.ui.shared;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.Result;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class EditProfilePanel extends JPanel implements Refreshable {

  private final User user;
  private final UserService userService = new UserService();
  private final AuthService authService = new AuthService();

  private JTextField fullNameField;
  private JTextField emailField;
  private JTextField contactField;

  private JPasswordField currentPassField;
  private JPasswordField newPassField;
  private JPasswordField confirmPassField;

  private JLabel profileErrorLabel;
  private JLabel passErrorLabel;

  private JLabel profileInfoTitle;
  private JLabel changePasswordTitle;
  private JLabel fullNameLabel;
  private JLabel emailLabel;
  private JLabel contactLabel;
  private JLabel currentPasswordLabel;
  private JLabel newPasswordLabel;
  private JLabel confirmNewLabel;
  private JButton saveBtn;
  private JButton changeBtn;

  public EditProfilePanel(User user) {
    this.user = user;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    buildUI();
  }

  private void buildUI() {
    profileInfoTitle = sectionTitle(LanguageManager.t("title.profileInfo"));
    add(profileInfoTitle);
    add(Box.createVerticalStrut(8));

    JPanel infoForm = new JPanel(new GridBagLayout());
    infoForm.setBackground(Theme.BG_PANEL);
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(5, 4, 5, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;

    fullNameField = new JTextField(user.getFullName(), 22);
    emailField = new JTextField(user.getEmail(), 22);
    contactField = new JTextField(user.getContactNumber(), 22);

    fullNameLabel = styledLabel(LanguageManager.t("label.fullName"));
    emailLabel = styledLabel(LanguageManager.t("label.email"));
    contactLabel = styledLabel(LanguageManager.t("label.contact"));

    addFormRow(infoForm, gc, 0, fullNameLabel, fullNameField);
    addFormRow(infoForm, gc, 1, emailLabel, emailField);
    addFormRow(infoForm, gc, 2, contactLabel, contactField);

    profileErrorLabel = new JLabel(LanguageManager.t("msg.empty"));
    profileErrorLabel.setForeground(Theme.TEXT_ERROR);
    profileErrorLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 2;
    infoForm.add(profileErrorLabel, gc);

    saveBtn = new JButton(LanguageManager.t("btn.saveProfile"));
    styleButton(saveBtn, Theme.BTN_PRIMARY);
    gc.gridy = 4;
    infoForm.add(saveBtn, gc);

    add(infoForm);
    add(Box.createVerticalStrut(20));
    changePasswordTitle = sectionTitle(LanguageManager.t("title.changePassword"));
    add(changePasswordTitle);
    add(Box.createVerticalStrut(8));

    JPanel passForm = new JPanel(new GridBagLayout());
    passForm.setBackground(Theme.BG_PANEL);
    GridBagConstraints gc2 = new GridBagConstraints();
    gc2.insets = new Insets(5, 4, 5, 4);
    gc2.fill = GridBagConstraints.HORIZONTAL;

    currentPassField = new JPasswordField(22);
    newPassField = new JPasswordField(22);
    confirmPassField = new JPasswordField(22);

    currentPasswordLabel = styledLabel(LanguageManager.t("label.currentPassword"));
    newPasswordLabel = styledLabel(LanguageManager.t("label.newPassword"));
    confirmNewLabel = styledLabel(LanguageManager.t("label.confirmNew"));

    addFormRow(passForm, gc2, 0, currentPasswordLabel, currentPassField);
    addFormRow(passForm, gc2, 1, newPasswordLabel, newPassField);
    addFormRow(passForm, gc2, 2, confirmNewLabel, confirmPassField);

    passErrorLabel = new JLabel(LanguageManager.t("msg.empty"));
    passErrorLabel.setForeground(Theme.TEXT_ERROR);
    passErrorLabel.setFont(Theme.FONT_BODY);
    gc2.gridx = 0;
    gc2.gridy = 3;
    gc2.gridwidth = 2;
    passForm.add(passErrorLabel, gc2);

    changeBtn = new JButton(LanguageManager.t("btn.changePassword"));
    styleButton(changeBtn, Theme.BTN_DANGER);
    gc2.gridy = 4;
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
      profileErrorLabel.setText(LanguageManager.t("msg.fieldsRequired"));
      return;
    }

    Result<User> result = userService.updateProfile(user, name, email, contact);
    if (!result.isSuccess()) {
      profileErrorLabel.setText(LanguageManager.resolveError(result.getError()));
      return;
    }

    profileErrorLabel.setText(LanguageManager.t("msg.empty"));
    Toast.success(SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.profile.updated"));
  }

  private void handleChangePassword() {
    String current = new String(currentPassField.getPassword());
    String newPass = new String(newPassField.getPassword());
    String confirm = new String(confirmPassField.getPassword());

    if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
      passErrorLabel.setText(LanguageManager.t("msg.password.fieldsRequired"));
      return;
    }
    if (!newPass.equals(confirm)) {
      passErrorLabel.setText(LanguageManager.t("msg.password.mismatch"));
      return;
    }
    if (newPass.length() < 6) {
      passErrorLabel.setText(LanguageManager.t("msg.password.minLength"));
      return;
    }

    boolean ok = authService.changePassword(user, current, newPass);
    if (!ok) {
      passErrorLabel.setText(LanguageManager.t("msg.password.currentIncorrect"));
      return;
    }

    currentPassField.setText("");
    newPassField.setText("");
    confirmPassField.setText("");
    passErrorLabel.setText(LanguageManager.t("msg.empty"));
    Toast.success(
        SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.password.changed"));
  }

  private void addFormRow(
      JPanel panel, GridBagConstraints gc, int row, JLabel label, JComponent field) {
    gc.gridx = 0;
    gc.gridy = row;
    gc.weightx = 0.35;
    gc.gridwidth = 1;
    panel.add(label, gc);
    gc.gridx = 1;
    gc.weightx = 0.65;
    panel.add(field, gc);
  }

  private JLabel sectionTitle(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setFont(Theme.FONT_HEADING);
    lbl.setForeground(Theme.TEXT_HEADING);
    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    return lbl;
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    return lbl;
  }

  @Override
  public void refresh() {
    profileInfoTitle.setText(LanguageManager.t("title.profileInfo"));
    changePasswordTitle.setText(LanguageManager.t("title.changePassword"));
    fullNameLabel.setText(LanguageManager.t("label.fullName"));
    emailLabel.setText(LanguageManager.t("label.email"));
    contactLabel.setText(LanguageManager.t("label.contact"));
    currentPasswordLabel.setText(LanguageManager.t("label.currentPassword"));
    newPasswordLabel.setText(LanguageManager.t("label.newPassword"));
    confirmNewLabel.setText(LanguageManager.t("label.confirmNew"));
    saveBtn.setText(LanguageManager.t("btn.saveProfile"));
    changeBtn.setText(LanguageManager.t("btn.changePassword"));
  }

  private void styleButton(JButton btn, java.awt.Color bg) {
    btn.setBackground(bg);
    btn.setForeground(Theme.TEXT_PRIMARY);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }
}
