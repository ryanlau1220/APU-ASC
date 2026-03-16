package com.apu.asc.ui.staff;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.FeedbackDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class StaffAssignPanel extends JPanel implements Refreshable {

  private final AppointmentService apptService = new AppointmentService();
  private final UserService userService = new UserService();
  private final UserDAO userDAO = UserDAO.getInstance();
  private final AppointmentDAO appointmentDAO = AppointmentDAO.getInstance();
  private final FeedbackDAO feedbackDAO = FeedbackDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;
  private TableRowSorter<DefaultTableModel> sorter;
  private JTextField searchField;

  private JLabel searchLabel;
  private JButton assignBtn;
  private JButton suggestBtn;
  private JButton refreshBtn;

  public StaffAssignPanel(User staffUser) {
    Objects.requireNonNull(staffUser, "staffUser must not be null");
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.apptId"),
      LanguageManager.t("col.customer"),
      LanguageManager.t("col.vehicle"),
      LanguageManager.t("col.serviceIdShort"),
      LanguageManager.t("col.dateTime"),
      LanguageManager.t("col.status")
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
    table.setIntercellSpacing(new java.awt.Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);

    searchLabel = new JLabel(LanguageManager.t("label.search"));
    searchLabel.setForeground(Theme.TEXT_SECONDARY);
    searchLabel.setFont(Theme.FONT_BODY);

    searchField = new JTextField(20);
    searchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                applySearch();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                applySearch();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                applySearch();
              }
            });

    assignBtn = makeBtn(LanguageManager.t("btn.assignTechnician"), Theme.BTN_PRIMARY);
    suggestBtn = makeBtn(LanguageManager.t("btn.suggestTechnician"), Theme.BTN_SUCCESS);
    refreshBtn = makeBtn(LanguageManager.t("btn.refresh"), Theme.BTN_PRIMARY);

    top.add(searchLabel);
    top.add(searchField);
    top.add(assignBtn);
    top.add(suggestBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    assignBtn.addActionListener(e -> handleAssign(false));
    suggestBtn.addActionListener(e -> handleAssign(true));
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

  private void applySearch() {
    String text = searchField.getText().trim();
    if (text.isEmpty()) {
      sorter.setRowFilter(null);
    } else {
      sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }
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

  private void handleAssign(boolean withSuggestion) {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.appt.selectFirst"));
      return;
    }

    int modelRow = table.convertRowIndexToModel(row);
    String apptId = (String) model.getValueAt(modelRow, 0);
    Appointment appt = apptService.findById(apptId);

    List<User> technicians =
        userService.getAllByRole(Role.TECHNICIAN).stream()
            .filter(u -> u.getStatus() == UserStatus.ACTIVE)
            .toList();
    if (technicians.isEmpty()) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.tech.noneAvailable"));
      return;
    }

    String toastMessage = null;

    if (withSuggestion && appt != null) {
      LocalDate apptDate = appt.getAppointmentDateTime().toLocalDate();

      List<User> available =
          technicians.stream()
              .filter(tech -> !apptService.hasTechnicianConflict(tech.getId(), appt))
              .toList();
      if (available.isEmpty()) {
        Toast.error(
            SwingUtilities.getWindowAncestor(this),
            LanguageManager.t("msg.tech.noneAvailableNoConflicts"));
        return;
      }

      record TechScore(User tech, int workload, double avgRating) {}

      List<TechScore> scores =
          available.stream()
              .map(
                  tech -> {
                    long workload =
                        appointmentDAO.findByTechnician(tech.getId()).stream()
                            .filter(a -> a.getStatus() == ApptStatus.ASSIGNED)
                            .filter(a -> a.getAppointmentDateTime().toLocalDate().equals(apptDate))
                            .count();
                    double avgRating =
                        feedbackDAO.findByTechnician(tech.getId(), appointmentDAO).stream()
                            .mapToInt(f -> f.getRating())
                            .average()
                            .orElse(0.0);
                    return new TechScore(tech, (int) workload, avgRating);
                  })
              .sorted(
                  Comparator.comparingInt(TechScore::workload)
                      .thenComparingDouble(ts -> -ts.avgRating()))
              .toList();

      TechScore best = scores.get(0);
      toastMessage =
          String.format(
              LanguageManager.t("msg.tech.recommended"),
              best.tech().getFullName(),
              best.workload(),
              best.avgRating());
      technicians = scores.stream().map(TechScore::tech).toList();
    }

    String[] names =
        technicians.stream()
            .map(t -> t.getFullName() + " (" + t.getId() + ")")
            .toArray(String[]::new);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(
            owner,
            LanguageManager.t("dialog.assignTech.title"),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(320, 260);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    for (String name : names) listModel.addElement(name);
    JList<String> techList = new JList<>(listModel);
    techList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    techList.setSelectedIndex(0);
    techList.setFont(Theme.FONT_BODY);

    JButton assignBtn = makeBtn(LanguageManager.t("btn.assign"), Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn(LanguageManager.t("btn.cancel"), Theme.BTN_DANGER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    btnRow.setBackground(Theme.BG_PANEL);
    btnRow.add(cancelBtn);
    btnRow.add(assignBtn);

    JLabel label = new JLabel(LanguageManager.t("label.selectTechnician"));
    label.setForeground(Theme.TEXT_SECONDARY);
    label.setFont(Theme.FONT_BODY);
    label.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Theme.BG_PANEL);
    root.add(label, BorderLayout.NORTH);
    root.add(new JScrollPane(techList), BorderLayout.CENTER);
    root.add(btnRow, BorderLayout.SOUTH);
    dialog.setContentPane(root);

    if (toastMessage != null) {
      Toast.success(owner, toastMessage);
    }

    final List<User> finalTechnicians = technicians;
    cancelBtn.addActionListener(e -> dialog.dispose());
    assignBtn.addActionListener(
        e -> {
          int idx = techList.getSelectedIndex();
          if (idx < 0) return;
          String techId = finalTechnicians.get(idx).getId();
          dialog.dispose();
          boolean ok = apptService.assignTechnician(apptId, techId);
          if (!ok) {
            Toast.error(owner, LanguageManager.t("msg.assign.conflict"));
          } else {
            loadData();
            Toast.success(owner, LanguageManager.t("msg.assign.success"));
          }
        });

    dialog.setVisible(true);
  }

  @Override
  public void refresh() {
    searchLabel.setText(LanguageManager.t("label.search"));
    assignBtn.setText(LanguageManager.t("btn.assignTechnician"));
    suggestBtn.setText(LanguageManager.t("btn.suggestTechnician"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.apptId",
      "col.customer",
      "col.vehicle",
      "col.serviceIdShort",
      "col.dateTime",
      "col.status"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
