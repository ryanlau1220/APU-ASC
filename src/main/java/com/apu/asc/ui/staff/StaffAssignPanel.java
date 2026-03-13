package com.apu.asc.ui.staff;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.UserService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class StaffAssignPanel extends JPanel {

  private final AppointmentService apptService = new AppointmentService();
  private final UserService userService = new UserService();
  private final UserDAO userDAO = UserDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public StaffAssignPanel(User staffUser) {
    setLayout(new BorderLayout(8, 8));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {"Appt ID", "Customer", "Vehicle", "Service ID", "Date & Time", "Status"};
    model =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    table = new JTable(model);
    table.setRowHeight(24);
    table.setFont(new Font("SansSerif", Font.PLAIN, 12));
    table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(new Color(45, 45, 45));

    JButton assignBtn = new JButton("Assign Technician to Selected");
    JButton refreshBtn = new JButton("Refresh");

    top.add(assignBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    assignBtn.addActionListener(e -> handleAssign());
    refreshBtn.addActionListener(e -> loadData());
  }

  private void loadData() {
    model.setRowCount(0);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    List<Appointment> pending = apptService.getByStatus(ApptStatus.PENDING);

    for (Appointment a : pending) {
      User customer = userDAO.findById(a.getCustomerId());
      String custName = customer != null ? customer.getFullName() : a.getCustomerId();
      model.addRow(
          new Object[] {
            a.getAppointmentId(),
            custName,
            a.getVehiclePlate(),
            a.getServiceId(),
            a.getAppointmentDateTime().format(fmt),
            a.getStatus().name()
          });
    }
  }

  private void handleAssign() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);

    List<User> technicians = userService.getAllByRole(Role.TECHNICIAN);
    if (technicians.isEmpty()) {
      JOptionPane.showMessageDialog(this, "No technicians available.");
      return;
    }

    String[] names =
        technicians.stream()
            .map(t -> t.getFullName() + " (" + t.getId() + ")")
            .toArray(String[]::new);

    String chosen =
        (String)
            JOptionPane.showInputDialog(
                this,
                "Select Technician:",
                "Assign Technician",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names,
                names[0]);
    if (chosen == null) return;

    int idx = java.util.Arrays.asList(names).indexOf(chosen);
    String techId = technicians.get(idx).getId();

    boolean ok = apptService.assignTechnician(apptId, techId);
    if (!ok) {
      JOptionPane.showMessageDialog(
          this,
          "Assignment failed: technician has a schedule conflict.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(this, "Technician assigned successfully.");
      loadData();
    }
  }
}
