package com.apu.asc.ui.manager;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.User;
import com.apu.asc.service.FeedbackService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ManagerFeedbackPanel extends JPanel implements Refreshable {

  private final FeedbackService feedbackService = new FeedbackService();
  private final AppointmentDAO appointmentDAO = AppointmentDAO.getInstance();
  private final UserDAO userDAO = UserDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;
  private JButton refreshBtn;

  public ManagerFeedbackPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.feedbackId"),
      LanguageManager.t("col.apptId"),
      LanguageManager.t("col.customer"),
      LanguageManager.t("col.technician"),
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
    table.setIntercellSpacing(new java.awt.Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    refreshBtn = new JButton(LanguageManager.t("btn.refresh"));
    refreshBtn.setFont(Theme.FONT_BODY_BOLD);
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
    List<Feedback> list = feedbackService.getAll();

    for (Feedback f : list) {
      Appointment a = appointmentDAO.findById(f.getAppointmentId());
      String custName = "";
      String techName = "";
      if (a != null) {
        User cust = userDAO.findById(a.getCustomerId());
        User tech = userDAO.findById(a.getTechnicianId());
        custName = cust != null ? cust.getFullName() : a.getCustomerId();
        techName = tech != null ? tech.getFullName() : a.getTechnicianId();
      }
      model.addRow(
          new Object[] {
            f.getFeedbackId(),
            f.getAppointmentId(),
            custName,
            techName,
            f.getRating() + " / 5",
            f.getComments()
          });
    }
  }

  @Override
  public void refresh() {
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.feedbackId", "col.apptId", "col.customer", "col.technician", "col.rating", "col.comments"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
