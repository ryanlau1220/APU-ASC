package com.apu.asc.ui.technician;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.User;
import com.apu.asc.service.FeedbackService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public final class TechnicianFeedbackPanel extends JPanel implements Refreshable {

  private final User technician;
  private final FeedbackService feedbackService = new FeedbackService();
  private final AppointmentDAO appointmentDAO = AppointmentDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  private JButton refreshBtn;

  public TechnicianFeedbackPanel(User technician) {
    this.technician = technician;
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.feedbackId"),
      LanguageManager.t("col.appointment"),
      LanguageManager.t("col.vehicle"),
      LanguageManager.t("col.rating"),
      LanguageManager.t("col.comments")
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
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    refreshBtn = new JButton(LanguageManager.t("btn.refresh"));
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadData());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);
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
      model.addRow(
          new Object[] {
            f.getFeedbackId(),
            f.getAppointmentId(),
            vehicle,
            f.getRating() + " / 5",
            f.getComments()
          });
    }
  }

  @Override
  public void refresh() {
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.feedbackId", "col.appointment", "col.vehicle", "col.rating", "col.comments"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
