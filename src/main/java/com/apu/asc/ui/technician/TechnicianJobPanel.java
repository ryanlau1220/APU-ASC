package com.apu.asc.ui.technician;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TechnicianJobPanel extends JPanel implements Refreshable {

  private final User technician;
  private final AppointmentService apptService = new AppointmentService();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();
  private final UserDAO userDAO = UserDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;
  private TableRowSorter<DefaultTableModel> sorter;
  private JTextField searchField;

  private JLabel searchLabel;
  private JButton completeBtn;
  private JButton notesBtn;
  private JButton refreshBtn;

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
      LanguageManager.t("col.apptId"),
      LanguageManager.t("col.customer"),
      LanguageManager.t("col.vehicle"),
      LanguageManager.t("col.service"),
      LanguageManager.t("col.dateTime"),
      LanguageManager.t("col.status"),
      LanguageManager.t("col.techNotes")
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

    completeBtn = new JButton(LanguageManager.t("btn.markCompleted"));
    notesBtn = new JButton(LanguageManager.t("btn.addEditNotes"));
    refreshBtn = new JButton(LanguageManager.t("btn.refresh"));

    completeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    notesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    top.add(searchLabel);
    top.add(searchField);
    top.add(completeBtn);
    top.add(notesBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    completeBtn.addActionListener(e -> handleComplete());
    notesBtn.addActionListener(e -> handleEditNotes());
    refreshBtn.addActionListener(e -> loadData());
  }

  private void applySearch() {
    String text = searchField.getText().trim();
    if (text.isEmpty()) sorter.setRowFilter(null);
    else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
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
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.appt.selectFirst"));
      return;
    }

    int modelRow = table.convertRowIndexToModel(row);
    String apptId = (String) model.getValueAt(modelRow, 0);
    String notes = (String) model.getValueAt(modelRow, 6);

    int confirm =
        JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            String.format(LanguageManager.t("msg.appt.completeConfirm"), apptId),
            LanguageManager.t("dialog.confirm.title"),
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    boolean ok = apptService.completeAppointment(apptId, notes == null ? "" : notes);
    if (!ok) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.appt.completeFailed"));
    } else {
      loadData();
      Toast.success(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.appt.completed"));
    }
  }

  private void handleEditNotes() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.appt.selectFirst"));
      return;
    }

    int modelRow = table.convertRowIndexToModel(row);
    String apptId = (String) model.getValueAt(modelRow, 0);
    String currentNotes = (String) model.getValueAt(modelRow, 6);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(
            owner,
            String.format(LanguageManager.t("dialog.techNotes.title"), apptId),
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(420, 280);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    panel.setBackground(Theme.BG_PANEL);

    JLabel lbl = new JLabel(String.format(LanguageManager.t("label.notesFor"), apptId));
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_LABEL);
    panel.add(lbl, BorderLayout.NORTH);

    JTextArea area = new JTextArea(currentNotes == null ? "" : currentNotes, 6, 30);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    panel.add(new JScrollPane(area), BorderLayout.CENTER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnRow.setBackground(Theme.BG_PANEL);
    JButton saveBtn = new JButton(LanguageManager.t("btn.save"));
    JButton cancelBtn = new JButton(LanguageManager.t("btn.cancel"));
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
            Toast.error(
                SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.notes.saveFailed"));
          } else {
            loadData();
            Toast.success(
                SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.notes.saved"));
          }
        });

    dialog.setVisible(true);
  }

  @Override
  public void refresh() {
    searchLabel.setText(LanguageManager.t("label.search"));
    completeBtn.setText(LanguageManager.t("btn.markCompleted"));
    notesBtn.setText(LanguageManager.t("btn.addEditNotes"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.apptId",
      "col.customer",
      "col.vehicle",
      "col.service",
      "col.dateTime",
      "col.status",
      "col.techNotes"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
