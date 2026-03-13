package com.apu.asc.ui.technician;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.User;
import com.apu.asc.service.FeedbackService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TechnicianFeedbackPanel extends JPanel {

    private final User technician;
    private final FeedbackService feedbackService = new FeedbackService();
    private final AppointmentDAO appointmentDAO   = AppointmentDAO.getInstance();

    private JTable table;
    private DefaultTableModel model;

    public TechnicianFeedbackPanel(User technician) {
        this.technician = technician;
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        buildUI();
        loadData();
    }

    private void buildUI() {
        String[] cols = {"Feedback ID", "Appointment", "Vehicle", "Rating", "Comments"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(new Color(45, 45, 45));
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0);
        List<Feedback> feedbacks = feedbackService.getByTechnician(technician.getId());

        for (Feedback f : feedbacks) {
            Appointment a = appointmentDAO.findById(f.getAppointmentId());
            String vehicle = a != null ? a.getVehiclePlate() : "-";
            model.addRow(new Object[]{
                f.getFeedbackId(), f.getAppointmentId(), vehicle,
                f.getRating() + " / 5", f.getComments()
            });
        }
    }
}
