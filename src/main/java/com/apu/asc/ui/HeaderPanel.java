package com.apu.asc.ui;

import com.apu.asc.model.User;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URL;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HeaderPanel extends JPanel {

  private final AppShell shell;

  private JLabel titleLabel;
  private JButton logoutBtn;

  public HeaderPanel(AppShell shell, User user) {
    this.shell = shell;
    setBackground(Theme.BG_HEADER);
    setPreferredSize(new Dimension(0, Theme.HEADER_HEIGHT));
    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_SIDEBAR));
    setLayout(new BorderLayout());

    add(buildLeft(), BorderLayout.WEST);
    add(buildRight(user), BorderLayout.EAST);
  }

  private JPanel buildLeft() {
    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
    left.setBackground(Theme.BG_HEADER);

    URL logoUrl = HeaderPanel.class.getResource("/images/apuLogo.png");
    if (logoUrl != null) {
      ImageIcon raw = new ImageIcon(logoUrl);
      java.awt.Image scaled = raw.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
      left.add(new JLabel(new ImageIcon(scaled)));
    }

    titleLabel = new JLabel(LanguageManager.t("header.title"));
    titleLabel.setFont(Theme.FONT_TITLE);
    titleLabel.setForeground(Theme.TEXT_PRIMARY);
    left.add(titleLabel);

    return left;
  }

  private JPanel buildRight(User user) {
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
    right.setBackground(Theme.BG_HEADER);

    JComboBox<String> langCombo = new JComboBox<>(new String[] {"English", "Malay"});
    langCombo.setSelectedIndex(
        LanguageManager.getInstance().getLocale().getLanguage().equals("ms") ? 1 : 0);
    langCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    langCombo.addActionListener(
        e -> {
          String selected = (String) langCombo.getSelectedItem();
          Locale locale = "Malay".equals(selected) ? new Locale("ms") : Locale.ENGLISH;
          LanguageManager.getInstance().setLocale(locale);
          shell.refreshCurrentPanel();
        });
    right.add(langCombo);

    JLabel userInfo = new JLabel(user.getFullName() + "  |  " + user.getRole().name());
    userInfo.setFont(Theme.FONT_BODY);
    userInfo.setForeground(Theme.TEXT_SECONDARY);
    right.add(userInfo);

    logoutBtn = new JButton(LanguageManager.t("btn.logout"));
    logoutBtn.setFont(Theme.FONT_BODY_BOLD);
    logoutBtn.setBackground(Theme.BTN_DANGER);
    logoutBtn.setForeground(Theme.TEXT_PRIMARY);
    logoutBtn.setFocusPainted(false);
    logoutBtn.setBorderPainted(false);
    logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logoutBtn.addActionListener(e -> shell.onLogout());
    right.add(logoutBtn);

    return right;
  }

  public void refreshTexts() {
    titleLabel.setText(LanguageManager.t("header.title"));
    logoutBtn.setText(LanguageManager.t("btn.logout"));
  }
}
