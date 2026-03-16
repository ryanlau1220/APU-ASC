package com.apu.asc.ui.customer;

import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Payment;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class CustomerHistoryPanel extends JPanel implements Refreshable {

  private final User user;
  private final AppointmentService apptService = new AppointmentService();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();
  private final PaymentDAO paymentDAO = PaymentDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  private JButton refreshBtn;

  public CustomerHistoryPanel(User user) {
    this.user = user;
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.apptId"),
      LanguageManager.t("col.vehiclePlate"),
      LanguageManager.t("col.service"),
      LanguageManager.t("col.dateTime"),
      LanguageManager.t("col.status"),
      LanguageManager.t("col.amountPaid")
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

    JScrollPane scroll = new JScrollPane(table);

    refreshBtn = new JButton(LanguageManager.t("btn.refresh"));
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadData());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
  }

  private void loadData() {
    model.setRowCount(0);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    List<Appointment> list =
        apptService.getByCustomer(user.getId()).stream()
            .filter(a -> a.getStatus() == ApptStatus.COMPLETED)
            .toList();

    for (Appointment a : list) {
      Service svc = serviceDAO.findById(a.getServiceId());
      String svcName = svc != null ? svc.getServiceName() : a.getServiceId();

      Payment payment = paymentDAO.findByAppointment(a.getAppointmentId());
      String amount = payment != null ? String.format("RM %.2f", payment.getAmountPaid()) : "-";

      model.addRow(
          new Object[] {
            a.getAppointmentId(),
            a.getVehiclePlate(),
            svcName,
            a.getAppointmentDateTime().format(fmt),
            a.getStatus().name(),
            amount
          });
    }
  }

  @Override
  public void refresh() {
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.apptId",
      "col.vehiclePlate",
      "col.service",
      "col.dateTime",
      "col.status",
      "col.amountPaid"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
