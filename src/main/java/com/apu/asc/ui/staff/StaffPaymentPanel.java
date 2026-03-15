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
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class StaffPaymentPanel extends JPanel implements Refreshable {

  private final AppointmentService apptService = new AppointmentService();
  private final PaymentService paymentService = new PaymentService();
  private final UserDAO userDAO = UserDAO.getInstance();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public StaffPaymentPanel(User staffUser) {
    Objects.requireNonNull(staffUser, "staffUser must not be null");
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
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

    JButton processBtn = makeBtn("Process Payment for Selected", Theme.BTN_SUCCESS);
    JButton receiptBtn = makeBtn("Send Receipt", Theme.BTN_PRIMARY);
    JButton refreshBtn = makeBtn("Refresh", Theme.BTN_PRIMARY);

    top.add(processBtn);
    top.add(receiptBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    processBtn.addActionListener(e -> handleProcess());
    receiptBtn.addActionListener(e -> handleSendReceipt());
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
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);

    if (!"UNPAID".equals(model.getValueAt(row, 4))) {
      Toast.error(owner, "Payment already processed for this appointment.");
      return;
    }

    Result<Payment> result = paymentService.processPayment(apptId);
    if (!result.isSuccess()) {
      Toast.error(owner, result.getError());
    } else {
      Payment p = result.getValue();
      loadData();
      Toast.success(
          owner,
          "Payment recorded: "
              + p.getPaymentId()
              + "  RM "
              + String.format("%.2f", p.getAmountPaid()));
      dispatchReceiptAsync(p.getPaymentId(), owner);
    }
  }

  private void handleSendReceipt() {
    int row = table.getSelectedRow();
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, "Select an appointment first.");
      return;
    }

    String apptId = (String) model.getValueAt(row, 0);
    Payment p = paymentService.findByAppointment(apptId);

    if (p == null) {
      Toast.error(owner, "Process payment first before sending receipt.");
      return;
    }
    if (p.isReceiptSent()) {
      Toast.error(owner, "Receipt already sent for this appointment.");
      return;
    }

    dispatchReceiptAsync(p.getPaymentId(), owner);
  }

  private void dispatchReceiptAsync(String paymentId, Window owner) {
    new SwingWorker<Boolean, Void>() {
      @Override
      protected Boolean doInBackground() {
        return paymentService.sendReceipt(paymentId);
      }

      @Override
      protected void done() {
        try {
          boolean ok = get();
          if (ok) {
            loadData();
            Toast.success(owner, "Receipt emailed to customer.");
          } else {
            Toast.error(
                owner, "Failed to send receipt. Check config.properties for SMTP settings.");
          }
        } catch (InterruptedException | ExecutionException e) {
          Toast.error(owner, "Failed to send receipt: " + e.getMessage());
        }
      }
    }.execute();
  }

  @Override
  public void refresh() {
    loadData();
  }
}
