package com.apu.asc.ui.manager;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.shared.EditProfilePanel;
import com.apu.asc.ui.shared.LoginFrame;
import java.awt.*;
import javax.swing.*;

public class ManagerDashboard extends JFrame {

  private final User user;
  private final AuthService authService = new AuthService();

  public ManagerDashboard(User user) {
    this.user = user;
    setTitle("APU-ASC — " + user.getDashboardTitle() + " (" + user.getFullName() + ")");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setSize(1100, 700);
    setLocationRelativeTo(null);
    buildUI();

    addWindowListener(
        new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
          }
        });
  }

  private void buildUI() {
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

    tabs.addTab("User Management", new ManagerUserPanel());
    tabs.addTab("Service Pricing", new ManagerServicePanel());
    tabs.addTab("All Feedback", new ManagerFeedbackPanel());
    tabs.addTab("Audit Log", new ManagerAuditPanel());
    tabs.addTab("Reports", new ManagerReportPanel());
    tabs.addTab("My Profile", new EditProfilePanel(user));

    add(tabs);
  }
}
