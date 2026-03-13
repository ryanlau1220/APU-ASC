package com.apu.asc.ui.technician;

import com.apu.asc.model.User;
import com.apu.asc.service.AuthService;
import com.apu.asc.ui.shared.EditProfilePanel;
import com.apu.asc.ui.shared.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class TechnicianDashboard extends JFrame {

    private final User user;
    private final AuthService authService = new AuthService();

    public TechnicianDashboard(User user) {
        this.user = user;
        setTitle("APU-ASC — " + user.getDashboardTitle() + " (" + user.getFullName() + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 640);
        setLocationRelativeTo(null);
        buildUI();

        addWindowListener(new java.awt.event.WindowAdapter() {
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

        tabs.addTab("My Job Queue",    new TechnicianJobPanel(user));
        tabs.addTab("Feedback Received", new TechnicianFeedbackPanel(user));
        tabs.addTab("My Profile",      new EditProfilePanel(user));

        add(tabs);
    }
}
