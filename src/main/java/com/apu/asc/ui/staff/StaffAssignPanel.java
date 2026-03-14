package com.apu.asc.ui.staff;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class StaffAssignPanel extends JPanel {

  private final AppointmentService apptService = new AppointmentService();
  private final UserService userService = new UserService();
  private final UserDAO userDAO = UserDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public StaffAssignPanel(User staffUser) {
    Objects.requireNonNull(staffUser, "staffUser must not be null");
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
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
    table.setRowHeight(Theme.TABLE_ROW_HEIGHT);
    table.setFont(Theme.FONT_BODY);
    table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowGrid(false);
    table.setIntercellSpacing(new java.awt.Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);

    JButton assignBtn = makeBtn("Assign Technician to Selected", Theme.BTN_PRIMARY);
    JButton refreshBtn = makeBtn("Refresh", Theme.BTN_PRIMARY);

    top.add(assignBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    assignBtn.addActionListener(e -> handleAssign());
    refreshBtn.addActionListener(e -> loadData());
  }

  private JButton makeBtn(String text, java.awt.Color bg) {
    JButton btn = new JButton(text);
    btn.setBackground(bg);
    btn.setForeground(Theme.TEXT_PRIMARY);
    btn.setFocusPainted(false);
    btn.setFont(Theme.FONT_BODY_BOLD);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
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
      Toast.error(SwingUtilities.getWindowAncestor(this), "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);

    List<User> technicians = userService.getAllByRole(Role.TECHNICIAN);
    if (technicians.isEmpty()) {
      Toast.error(SwingUtilities.getWindowAncestor(this), "No technicians available.");
      return;
    }

    String[] names =
        technicians.stream()
            .map(t -> t.getFullName() + " (" + t.getId() + ")")
            .toArray(String[]::new);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog = new JDialog(owner, "Assign Technician", Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(320, 260);
    dialog.setLocationRelativeTo(this);
    dialog.setResizable(false);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    for (String name : names) listModel.addElement(name);
    JList<String> techList = new JList<>(listModel);
    techList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    techList.setSelectedIndex(0);
    techList.setFont(Theme.FONT_BODY);

    JButton assignBtn = makeBtn("Assign", Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn("Cancel", Theme.BTN_DANGER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    btnRow.setBackground(Theme.BG_PANEL);
    btnRow.add(cancelBtn);
    btnRow.add(assignBtn);

    JLabel label = new JLabel("Select Technician:");
    label.setForeground(Theme.TEXT_SECONDARY);
    label.setFont(Theme.FONT_BODY);
    label.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Theme.BG_PANEL);
    root.add(label, BorderLayout.NORTH);
    root.add(new JScrollPane(techList), BorderLayout.CENTER);
    root.add(btnRow, BorderLayout.SOUTH);
    dialog.setContentPane(root);

    cancelBtn.addActionListener(e -> dialog.dispose());
    assignBtn.addActionListener(
        e -> {
          int idx = techList.getSelectedIndex();
          if (idx < 0) return;
          String techId = technicians.get(idx).getId();
          dialog.dispose();
          boolean ok = apptService.assignTechnician(apptId, techId);
          if (!ok) {
            Toast.error(owner, "Assignment failed: technician has a schedule conflict.");
          } else {
            loadData();
            Toast.success(owner, "Technician assigned successfully.");
          }
        });

    dialog.setVisible(true);
  }
}
