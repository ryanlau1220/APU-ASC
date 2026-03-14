package com.apu.asc.ui;

import com.apu.asc.model.User;
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

  private void buildNavItems(JPanel nav, User user) {
    switch (user.getRole()) {
      case CUSTOMER:
        addNav(nav, "Appointment History", "history", AppShell.CARD_CUST_HISTORY);
        addNav(nav, "Submit Feedback", "feedback", AppShell.CARD_CUST_FEEDBACK);
        addNav(nav, "My Profile", "profile", AppShell.CARD_PROFILE);
        break;

      case TECHNICIAN:
        addNav(nav, "My Job Queue", "jobs", AppShell.CARD_TECH_JOBS);
        addNav(nav, "Feedback Received", "feedback", AppShell.CARD_TECH_FEEDBACK);
        addNav(nav, "My Profile", "profile", AppShell.CARD_PROFILE);
        break;

      case STAFF:
        addNav(nav, "Customer Management", "customers", AppShell.CARD_STAFF_CUSTOMERS);
        addNav(nav, "Book Appointment", "book", AppShell.CARD_STAFF_BOOK);
        addNav(nav, "Assign Technician", "assign", AppShell.CARD_STAFF_ASSIGN);
        addNav(nav, "Process Payment", "payment", AppShell.CARD_STAFF_PAYMENT);
        addNav(nav, "My Profile", "profile", AppShell.CARD_PROFILE);
        break;

      case MANAGER:
      default:
        addNav(nav, "User Management", "users", AppShell.CARD_MGR_USERS);
        addNav(nav, "Service Pricing", "services", AppShell.CARD_MGR_SERVICES);
        addNav(nav, "All Feedback", "feedback", AppShell.CARD_MGR_FEEDBACK);
        addNav(nav, "Audit Log", "audit", AppShell.CARD_MGR_AUDIT);
        addNav(nav, "Reports", "reports", AppShell.CARD_MGR_REPORTS);
        addNav(nav, "My Profile", "profile", AppShell.CARD_PROFILE);
        break;
    }
  }

  private void addNav(JPanel nav, String label, String iconName, String cardName) {
    NavButton btn = new NavButton(label, iconName, cardName);
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

  private class NavButton extends JButton {
    final String cardName;

    NavButton(String text, String iconName, String cardName) {
      super(text);
      this.cardName = cardName;

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
