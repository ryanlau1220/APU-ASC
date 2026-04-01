package com.apu.asc.ui;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.customer.CustomerFeedbackPanel;
import com.apu.asc.ui.customer.CustomerHistoryPanel;
import com.apu.asc.ui.manager.ManagerAuditPanel;
import com.apu.asc.ui.manager.ManagerFeedbackPanel;
import com.apu.asc.ui.manager.ManagerReportPanel;
import com.apu.asc.ui.manager.ManagerServicePanel;
import com.apu.asc.ui.manager.ManagerSettingsPanel;
import com.apu.asc.ui.manager.ManagerUserPanel;
import com.apu.asc.ui.shared.EditProfilePanel;
import com.apu.asc.ui.shared.LoginPanel;
import com.apu.asc.ui.shared.RegisterPanel;
import com.apu.asc.ui.staff.StaffAssignPanel;
import com.apu.asc.ui.staff.StaffBookApptPanel;
import com.apu.asc.ui.staff.StaffCustomerPanel;
import com.apu.asc.ui.staff.StaffPaymentPanel;
import com.apu.asc.ui.technician.TechnicianFeedbackPanel;
import com.apu.asc.ui.technician.TechnicianJobPanel;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class AppShell extends JFrame {

  public static final String CARD_LOGIN = "LOGIN";
  public static final String CARD_REGISTER = "REGISTER";
  public static final String CARD_PROFILE = "PROFILE";

  public static final String CARD_CUST_HISTORY = "CUST_HISTORY";
  public static final String CARD_CUST_FEEDBACK = "CUST_FEEDBACK";

  public static final String CARD_TECH_JOBS = "TECH_JOBS";
  public static final String CARD_TECH_FEEDBACK = "TECH_FEEDBACK";

  public static final String CARD_STAFF_CUSTOMERS = "STAFF_CUSTOMERS";
  public static final String CARD_STAFF_BOOK = "STAFF_BOOK";
  public static final String CARD_STAFF_ASSIGN = "STAFF_ASSIGN";
  public static final String CARD_STAFF_PAYMENT = "STAFF_PAYMENT";

  public static final String CARD_MGR_USERS = "MGR_USERS";
  public static final String CARD_MGR_SERVICES = "MGR_SERVICES";
  public static final String CARD_MGR_FEEDBACK = "MGR_FEEDBACK";
  public static final String CARD_MGR_AUDIT = "MGR_AUDIT";
  public static final String CARD_MGR_REPORTS = "MGR_REPORTS";
  public static final String CARD_MGR_SETTINGS = "MGR_SETTINGS";

  private final AuthService authService = new AuthService();

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel contentPane = new JPanel(cardLayout);

  private HeaderPanel headerPanel;
  private SidebarPanel sidebarPanel;

  public AppShell() {
    setTitle("APU Automotive Service Centre");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setMinimumSize(new java.awt.Dimension(1200, 800));
    setResizable(false);
    setLocationRelativeTo(null);

    contentPane.setBackground(Theme.BG_ROOT);
    contentPane.add(new LoginPanel(this), CARD_LOGIN);
    contentPane.add(new RegisterPanel(this), CARD_REGISTER);

    setLayout(new BorderLayout());
    add(contentPane, BorderLayout.CENTER);

    cardLayout.show(contentPane, CARD_LOGIN);
  }

  public void onLoginSuccess(User user) {
    if (headerPanel != null) remove(headerPanel);
    if (sidebarPanel != null) remove(sidebarPanel);

    headerPanel = new HeaderPanel(this, user);
    sidebarPanel = new SidebarPanel(this, user);

    add(headerPanel, BorderLayout.NORTH);
    add(sidebarPanel, BorderLayout.WEST);

    registerCards(user);

    String firstCard = firstCardFor(user);
    cardLayout.show(contentPane, firstCard);
    sidebarPanel.setActiveCard(firstCard);

    revalidate();
    repaint();
  }

  public void onLogout() {
    authService.logout();

    if (headerPanel != null) {
      remove(headerPanel);
      headerPanel = null;
    }
    if (sidebarPanel != null) {
      remove(sidebarPanel);
      sidebarPanel = null;
    }

    contentPane.removeAll();
    contentPane.add(new LoginPanel(this), CARD_LOGIN);
    contentPane.add(new RegisterPanel(this), CARD_REGISTER);

    cardLayout.show(contentPane, CARD_LOGIN);
    revalidate();
    repaint();
  }

  public void showCard(String cardName) {
    cardLayout.show(contentPane, cardName);
    if (sidebarPanel != null) sidebarPanel.setActiveCard(cardName);
    for (Component c : contentPane.getComponents()) {
      if (c.isVisible() && c instanceof Refreshable) {
        ((Refreshable) c).refresh();
        break;
      }
    }
  }

  public void refreshCurrentPanel() {
    if (headerPanel != null) headerPanel.refreshTexts();
    if (sidebarPanel != null) sidebarPanel.refreshTexts();
    for (Component c : contentPane.getComponents()) {
      if (c.isVisible() && c instanceof Refreshable) {
        ((Refreshable) c).refresh();
        break;
      }
    }
  }

  private void registerCards(User user) {
    contentPane.add(new EditProfilePanel(user), CARD_PROFILE);

    switch (user.getRole()) {
      case CUSTOMER:
        contentPane.add(new CustomerHistoryPanel(user), CARD_CUST_HISTORY);
        contentPane.add(new CustomerFeedbackPanel(user), CARD_CUST_FEEDBACK);
        break;

      case TECHNICIAN:
        contentPane.add(new TechnicianJobPanel(user), CARD_TECH_JOBS);
        contentPane.add(new TechnicianFeedbackPanel(user), CARD_TECH_FEEDBACK);
        break;

      case STAFF:
        contentPane.add(new StaffCustomerPanel(), CARD_STAFF_CUSTOMERS);
        contentPane.add(new StaffBookApptPanel(user), CARD_STAFF_BOOK);
        contentPane.add(new StaffAssignPanel(user), CARD_STAFF_ASSIGN);
        contentPane.add(new StaffPaymentPanel(user), CARD_STAFF_PAYMENT);
        break;

      case MANAGER:
      default:
        contentPane.add(new ManagerUserPanel(), CARD_MGR_USERS);
        contentPane.add(new ManagerServicePanel(), CARD_MGR_SERVICES);
        contentPane.add(new ManagerFeedbackPanel(), CARD_MGR_FEEDBACK);
        contentPane.add(new ManagerAuditPanel(), CARD_MGR_AUDIT);
        contentPane.add(new ManagerReportPanel(), CARD_MGR_REPORTS);
        contentPane.add(new ManagerSettingsPanel(), CARD_MGR_SETTINGS);
        break;
    }
  }

  private String firstCardFor(User user) {
    switch (user.getRole()) {
      case CUSTOMER:
        return CARD_CUST_HISTORY;
      case TECHNICIAN:
        return CARD_TECH_JOBS;
      case STAFF:
        return CARD_STAFF_CUSTOMERS;
      case MANAGER:
      default:
        return CARD_MGR_USERS;
    }
  }
}
