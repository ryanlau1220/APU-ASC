package com.apu.asc.ui;

import com.apu.asc.model.User;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class SidebarPanel extends JPanel {

  private static final int ICON_SIZE = 18;

  private final AppShell shell;
  private final List<NavButton> navButtons = new ArrayList<>();

  public SidebarPanel(AppShell shell, User user) {
    this.shell = shell;
    setBackground(Theme.BG_SIDEBAR);
    setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BG_HEADER));

    JPanel nav = new JPanel();
    nav.setBackground(Theme.BG_SIDEBAR);
    nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
    nav.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

    buildNavItems(nav, user);

    add(nav, BorderLayout.NORTH);
  }

  public void setActiveCard(String cardName) {
    for (NavButton nb : navButtons) {
      nb.setActive(nb.cardName.equals(cardName));
    }
  }

  public void refreshTexts() {
    for (NavButton nb : navButtons) {
      nb.refreshText();
    }
  }

  private void buildNavItems(JPanel nav, User user) {
    switch (user.getRole()) {
      case CUSTOMER:
        addNav(nav, "nav.cust.history", "history", AppShell.CARD_CUST_HISTORY);
        addNav(nav, "nav.cust.feedback", "feedback", AppShell.CARD_CUST_FEEDBACK);
        addNav(nav, "nav.profile", "profile", AppShell.CARD_PROFILE);
        break;

      case TECHNICIAN:
        addNav(nav, "nav.tech.jobs", "jobs", AppShell.CARD_TECH_JOBS);
        addNav(nav, "nav.tech.feedback", "feedback", AppShell.CARD_TECH_FEEDBACK);
        addNav(nav, "nav.profile", "profile", AppShell.CARD_PROFILE);
        break;

      case STAFF:
        addNav(nav, "nav.staff.customers", "customers", AppShell.CARD_STAFF_CUSTOMERS);
        addNav(nav, "nav.staff.book", "book", AppShell.CARD_STAFF_BOOK);
        addNav(nav, "nav.staff.assign", "assign", AppShell.CARD_STAFF_ASSIGN);
        addNav(nav, "nav.staff.payment", "payment", AppShell.CARD_STAFF_PAYMENT);
        addNav(nav, "nav.profile", "profile", AppShell.CARD_PROFILE);
        break;

      case MANAGER:
      default:
        addNav(nav, "nav.mgr.users", "users", AppShell.CARD_MGR_USERS);
        addNav(nav, "nav.mgr.services", "services", AppShell.CARD_MGR_SERVICES);
        addNav(nav, "nav.mgr.feedback", "feedback", AppShell.CARD_MGR_FEEDBACK);
        addNav(nav, "nav.mgr.audit", "audit", AppShell.CARD_MGR_AUDIT);
        addNav(nav, "nav.mgr.reports", "reports", AppShell.CARD_MGR_REPORTS);
        addNav(nav, "nav.mgr.settings", "settings", AppShell.CARD_MGR_SETTINGS);
        addNav(nav, "nav.profile", "profile", AppShell.CARD_PROFILE);
        break;
    }
  }

  private void addNav(JPanel nav, String labelKey, String iconName, String cardName) {
    NavButton btn = new NavButton(labelKey, iconName, cardName);
    navButtons.add(btn);
    nav.add(btn);
    nav.add(Box.createVerticalStrut(2));
    btn.addActionListener(e -> shell.showCard(cardName));
  }

  private static ImageIcon loadIcon(String name) {
    URL url = SidebarPanel.class.getResource("/images/icons/" + name + ".png");
    if (url == null) return null;
    return new ImageIcon(url);
  }

  private static class NavButton extends JButton {
    final String cardName;
    final String labelKey;

    NavButton(String labelKey, String iconName, String cardName) {
      super(LanguageManager.t(labelKey));
      this.cardName = cardName;
      this.labelKey = labelKey;

      ImageIcon icon = loadIcon(iconName);
      if (icon != null) setIcon(scaleIcon(icon));

      setHorizontalAlignment(JButton.LEFT);
      setIconTextGap(10);
      setFont(Theme.FONT_LABEL);
      setForeground(Theme.TEXT_SECONDARY);
      setBackground(Theme.BTN_SIDEBAR_DEFAULT);
      setOpaque(true);
      setBorderPainted(false);
      setFocusPainted(false);
      setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 40));
      setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 40));
      setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void refreshText() {
      setText(LanguageManager.t(labelKey));
    }

    void setActive(boolean active) {
      setBackground(active ? Theme.BTN_SIDEBAR_ACTIVE : Theme.BTN_SIDEBAR_DEFAULT);
      setForeground(active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.dispose();
      super.paintComponent(g);
    }
  }

  private static ImageIcon scaleIcon(ImageIcon src) {
    java.awt.Image img =
        src.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, java.awt.Image.SCALE_SMOOTH);
    return new ImageIcon(img);
  }
}
