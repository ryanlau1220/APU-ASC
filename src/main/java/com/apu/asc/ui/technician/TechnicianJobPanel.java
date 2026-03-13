package com.apu.asc.ui.technician;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TechnicianJobPanel extends JPanel {

    private final User technician;
    private final AppointmentService apptService = new AppointmentService();
    private final ServiceDAO serviceDAO          = ServiceDAO.getInstance();
    private final UserDAO userDAO                = UserDAO.getInstance();

    private JTable table;
    private DefaultTableModel model;

    public TechnicianJobPanel(User technician) {
        this.technician = technician;
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        buildUI();
        loadData();
    }

    private void buildUI() {
        String[] cols = {"Appt ID", "Customer", "Vehicle", "Service", "Date & Time", "Status", "Tech Notes"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(new Color(45, 45, 45));

        JButton completeBtn = new JButton("Mark as Completed");
        JButton notesBtn    = new JButton("Add/Edit Notes");
        JButton refreshBtn  = new JButton("Refresh");

        top.add(completeBtn);
        top.add(notesBtn);
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        completeBtn.addActionListener(e -> handleComplete());
        notesBtn.addActionListener(e -> handleEditNotes());
        refreshBtn.addActionListener(e -> loadData());
    }

    private void loadData() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

        List<Appointment> assigned = apptService.getByTechnician(technician.getId()).stream()
                .filter(a -> a.getStatus() == ApptStatus.ASSIGNED)
                .toList();

        for (Appointment a : assigned) {
            User customer   = userDAO.findById(a.getCustomerId());
            String custName = customer != null ? customer.getFullName() : a.getCustomerId();
            Service svc     = serviceDAO.findById(a.getServiceId());
            String svcName  = svc != null ? svc.getServiceName() : a.getServiceId();

            model.addRow(new Object[]{
                a.getAppointmentId(), custName, a.getVehiclePlate(),
                svcName, a.getAppointmentDateTime().format(fmt),
                a.getStatus().name(), a.getTechnicianNotes()
            });
        }
    }

    private void handleComplete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }

        String apptId = (String) model.getValueAt(row, 0);
        String notes  = (String) model.getValueAt(row, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment " + apptId + " as COMPLETED?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = apptService.completeAppointment(apptId, notes == null ? "" : notes);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to complete. Check appointment status.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Appointment marked as COMPLETED.");
            loadData();
        }
    }

    private void handleEditNotes() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }

        String apptId       = (String) model.getValueAt(row, 0);
        String currentNotes = (String) model.getValueAt(row, 6);

        JTextArea area = new JTextArea(currentNotes == null ? "" : currentNotes, 6, 30);
        area.setLineWrap(true);

        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(area),
                "Technician Notes for " + apptId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        Appointment appt = apptService.findById(apptId);
        if (appt == null) return;
        appt.setTechnicianNotes(area.getText().trim());

        com.apu.asc.dao.AppointmentDAO.getInstance().save(appt);
        loadData();
    }
}
