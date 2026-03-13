package com.apu.asc.ui.staff;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.shared.EditProfilePanel;
import com.apu.asc.ui.shared.LoginFrame;
import java.awt.*;
import javax.swing.*;

public class StaffDashboard extends JFrame {

  private final User user;
  private final AuthService authService = new AuthService();

  public StaffDashboard(User user) {
    this.user = user;
    setTitle("APU-ASC — " + user.getDashboardTitle() + " (" + user.getFullName() + ")");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setSize(1050, 680);
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

    tabs.addTab("Customer Management", new StaffCustomerPanel());
    tabs.addTab("Book Appointment", new StaffBookApptPanel(user));
    tabs.addTab("Assign Technician", new StaffAssignPanel(user));
    tabs.addTab("Process Payment", new StaffPaymentPanel(user));
    tabs.addTab("My Profile", new EditProfilePanel(user));

    add(tabs);
  }
}
