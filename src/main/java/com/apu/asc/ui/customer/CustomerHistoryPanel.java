package com.apu.asc.ui.customer;

import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Payment;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CustomerHistoryPanel extends JPanel {

  private final User user;
  private final AppointmentService apptService = new AppointmentService();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();
  private final PaymentDAO paymentDAO = PaymentDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public CustomerHistoryPanel(User user) {
    this.user = user;
    setLayout(new BorderLayout(8, 8));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {"Appt ID", "Vehicle Plate", "Service", "Date & Time", "Status", "Amount Paid"};
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

    JScrollPane scroll = new JScrollPane(table);

    JButton refreshBtn = new JButton("Refresh");
    refreshBtn.addActionListener(e -> loadData());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(new Color(45, 45, 45));
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
}
