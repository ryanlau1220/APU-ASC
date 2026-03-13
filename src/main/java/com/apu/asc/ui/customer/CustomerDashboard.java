package com.apu.asc.ui.customer;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.shared.EditProfilePanel;
import com.apu.asc.ui.shared.LoginFrame;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class CustomerDashboard extends JFrame {

  private final User user;
  private final AuthService authService = new AuthService();

  public CustomerDashboard(User user) {
    this.user = user;
    setTitle("APU-ASC - " + user.getDashboardTitle() + " (" + user.getFullName() + ")");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setSize(900, 620);
    setLocationRelativeTo(null);
    buildUI();

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
          }
        });
  }

  private void buildUI() {
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

    tabs.addTab("Appointment History", new CustomerHistoryPanel(user));
    tabs.addTab("Submit Feedback", new CustomerFeedbackPanel(user));
    tabs.addTab("My Profile", new EditProfilePanel(user));

    add(tabs);
  }
}
