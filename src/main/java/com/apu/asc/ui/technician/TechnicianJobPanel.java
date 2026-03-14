package com.apu.asc.ui.technician;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class TechnicianJobPanel extends JPanel {

  private final User technician;
  private final AppointmentService apptService = new AppointmentService();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();
  private final UserDAO userDAO = UserDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public TechnicianJobPanel(User technician) {
    this.technician = technician;
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      "Appt ID", "Customer", "Vehicle", "Service", "Date & Time", "Status", "Tech Notes"
    };
    model =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    table = new JTable(model);
    table.setRowHeight(Theme.TABLE_ROW_HEIGHT);
    table.setFont(Theme.FONT_BODY);
    table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);

    JButton completeBtn = new JButton("Mark as Completed");
    JButton notesBtn = new JButton("Add/Edit Notes");
    JButton refreshBtn = new JButton("Refresh");

    completeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    notesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

    List<Appointment> assigned =
        apptService.getByTechnician(technician.getId()).stream()
            .filter(a -> a.getStatus() == ApptStatus.ASSIGNED)
            .toList();

    for (Appointment a : assigned) {
      User customer = userDAO.findById(a.getCustomerId());
      String custName = customer != null ? customer.getFullName() : a.getCustomerId();
      Service svc = serviceDAO.findById(a.getServiceId());
      String svcName = svc != null ? svc.getServiceName() : a.getServiceId();

      model.addRow(
          new Object[] {
            a.getAppointmentId(),
            custName,
            a.getVehiclePlate(),
            svcName,
            a.getAppointmentDateTime().format(fmt),
            a.getStatus().name(),
            a.getTechnicianNotes()
          });
    }
  }

  private void handleComplete() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(SwingUtilities.getWindowAncestor(this), "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);
    String notes = (String) model.getValueAt(row, 6);

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Mark appointment " + apptId + " as COMPLETED?",
            "Confirm",
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    boolean ok = apptService.completeAppointment(apptId, notes == null ? "" : notes);
    if (!ok) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), "Failed to complete. Check appointment status.");
    } else {
      loadData();
      Toast.success(SwingUtilities.getWindowAncestor(this), "Appointment marked as COMPLETED.");
    }
  }

  private void handleEditNotes() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(SwingUtilities.getWindowAncestor(this), "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);
    String currentNotes = (String) model.getValueAt(row, 6);

    JDialog dialog =
        new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Technician Notes — " + apptId,
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(420, 280);
    dialog.setLocationRelativeTo(this);
    dialog.setResizable(false);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    panel.setBackground(Theme.BG_PANEL);

    JLabel lbl = new JLabel("Notes for " + apptId + ":");
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_LABEL);
    panel.add(lbl, BorderLayout.NORTH);

    JTextArea area = new JTextArea(currentNotes == null ? "" : currentNotes, 6, 30);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    panel.add(new JScrollPane(area), BorderLayout.CENTER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnRow.setBackground(Theme.BG_PANEL);
    JButton saveBtn = new JButton("Save");
    JButton cancelBtn = new JButton("Cancel");
    saveBtn.setBackground(Theme.BTN_PRIMARY);
    saveBtn.setForeground(Theme.TEXT_PRIMARY);
    saveBtn.setFocusPainted(false);
    saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btnRow.add(cancelBtn);
    btnRow.add(saveBtn);
    panel.add(btnRow, BorderLayout.SOUTH);

    dialog.add(panel);

    cancelBtn.addActionListener(ev -> dialog.dispose());
    saveBtn.addActionListener(
        ev -> {
          boolean ok = apptService.updateNotes(apptId, area.getText().trim());
          dialog.dispose();
          if (!ok) {
            Toast.error(SwingUtilities.getWindowAncestor(this), "Failed to save notes.");
          } else {
            loadData();
            Toast.success(SwingUtilities.getWindowAncestor(this), "Notes saved.");
          }
        });

    dialog.setVisible(true);
  }
}
