package com.apu.asc.ui.staff;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Payment;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.PaymentService;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class StaffPaymentPanel extends JPanel {

  private final AppointmentService apptService = new AppointmentService();
  private final PaymentService paymentService = new PaymentService();
  private final UserDAO userDAO = UserDAO.getInstance();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public StaffPaymentPanel(User staffUser) {
    Objects.requireNonNull(staffUser, "staffUser must not be null");
    setLayout(new BorderLayout(8, 8));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {"Appt ID", "Customer", "Vehicle", "Service", "Amount", "Receipt Sent"};
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

    JButton processBtn = new JButton("Process Payment for Selected");
    JButton receiptBtn = new JButton("Send Receipt");
    JButton refreshBtn = new JButton("Refresh");

    top.add(processBtn);
    top.add(receiptBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    processBtn.addActionListener(e -> handleProcess());
    receiptBtn.addActionListener(e -> handleSendReceipt());
    refreshBtn.addActionListener(e -> loadData());
  }

  private void loadData() {
    model.setRowCount(0);
    List<Appointment> completed = apptService.getByStatus(ApptStatus.COMPLETED);

    for (Appointment a : completed) {
      User customer = userDAO.findById(a.getCustomerId());
      String custName = customer != null ? customer.getFullName() : a.getCustomerId();
      Service svc = serviceDAO.findById(a.getServiceId());
      String svcName = svc != null ? svc.getServiceName() : a.getServiceId();

      Payment p = paymentService.findByAppointment(a.getAppointmentId());
      String amount = p != null ? String.format("RM %.2f", p.getAmountPaid()) : "UNPAID";
      String receiptSent = p != null ? p.isReceiptSent() ? "Yes" : "No" : "-";

      model.addRow(
          new Object[] {
            a.getAppointmentId(), custName, a.getVehiclePlate(), svcName, amount, receiptSent
          });
    }
  }

  private void handleProcess() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);

    if (!"UNPAID".equals(model.getValueAt(row, 4))) {
      JOptionPane.showMessageDialog(this, "Payment already processed for this appointment.");
      return;
    }

    Result<Payment> result = paymentService.processPayment(apptId);
    if (!result.isSuccess()) {
      JOptionPane.showMessageDialog(this, result.getError(), "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      Payment p = result.getValue();
      JOptionPane.showMessageDialog(
          this,
          "Payment recorded: "
              + p.getPaymentId()
              + "\nAmount: RM "
              + String.format("%.2f", p.getAmountPaid()));
      loadData();
    }
  }

  private void handleSendReceipt() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);
    Payment p = paymentService.findByAppointment(apptId);

    if (p == null) {
      JOptionPane.showMessageDialog(this, "Process payment first before sending receipt.");
      return;
    }
    if (p.isReceiptSent()) {
      JOptionPane.showMessageDialog(this, "Receipt already sent for this appointment.");
      return;
    }

    boolean ok = paymentService.sendReceipt(p.getPaymentId());
    if (!ok) {
      JOptionPane.showMessageDialog(
          this,
          "Failed to send receipt. Check config.properties for SMTP settings.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(this, "Receipt emailed successfully.");
      loadData();
    }
  }
}
