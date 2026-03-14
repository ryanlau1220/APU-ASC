package com.apu.asc.ui;

import com.apu.asc.model.User;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HeaderPanel extends JPanel {

  private final AppShell shell;

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

    JLabel title = new JLabel("APU Automotive Service Centre");
    title.setFont(Theme.FONT_TITLE);
    title.setForeground(Theme.TEXT_PRIMARY);
    left.add(title);

    return left;
  }

  private JPanel buildRight(User user) {
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
    right.setBackground(Theme.BG_HEADER);

    JLabel userInfo = new JLabel(user.getFullName() + "  |  " + user.getRole().name());
    userInfo.setFont(Theme.FONT_BODY);
    userInfo.setForeground(Theme.TEXT_SECONDARY);
    right.add(userInfo);

    JButton logout = new JButton("Logout");
    logout.setFont(Theme.FONT_BODY_BOLD);
    logout.setBackground(Theme.BTN_DANGER);
    logout.setForeground(Theme.TEXT_PRIMARY);
    logout.setFocusPainted(false);
    logout.setBorderPainted(false);
    logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logout.addActionListener(e -> shell.onLogout());
    right.add(logout);

    return right;
  }
}
