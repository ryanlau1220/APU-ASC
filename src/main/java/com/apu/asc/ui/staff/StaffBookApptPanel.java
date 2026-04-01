package com.apu.asc.ui.staff;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.Role;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class StaffBookApptPanel extends JPanel implements Refreshable {

  private final User staffUser;
  private final AppointmentService apptService = new AppointmentService();
  private final UserService userService = new UserService();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();

  private JComboBox<UserItem> customerCombo;
  private JComboBox<ServiceItem> serviceCombo;
  private JTextField vehicleField;
  private JTextField dateTimeField;
  private JLabel messageLabel;

  private JLabel titleLabel;
  private JLabel customerLabel;
  private JLabel serviceLabel;
  private JLabel vehiclePlateLabel;
  private JLabel dateTimeIsoLabel;
  private JLabel hintLabel;
  private JButton bookBtn;
  private JButton refreshBtn;

  public StaffBookApptPanel(User staffUser) {
    this.staffUser = staffUser;
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    buildUI();
  }

  private void buildUI() {
    titleLabel = new JLabel(LanguageManager.t("title.bookAppt"));
    titleLabel.setFont(Theme.FONT_TITLE);
    titleLabel.setForeground(Theme.TEXT_PRIMARY);
    add(titleLabel, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Theme.BG_PANEL);
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(8, 4, 8, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;

    customerCombo = new JComboBox<>();
    serviceCombo = new JComboBox<>();
    vehicleField = new JTextField(16);
    dateTimeField = new JTextField("2026-06-01T09:00", 16);

    loadCustomers();
    loadServices();

    customerLabel = styledLabel(LanguageManager.t("label.customer"));
    serviceLabel = styledLabel(LanguageManager.t("label.service"));
    vehiclePlateLabel = styledLabel(LanguageManager.t("label.vehiclePlate"));
    dateTimeIsoLabel = styledLabel(LanguageManager.t("label.dateTimeIso"));

    addRow(form, gc, 0, customerLabel, customerCombo);
    addRow(form, gc, 1, serviceLabel, serviceCombo);
    addRow(form, gc, 2, vehiclePlateLabel, vehicleField);
    addRow(form, gc, 3, dateTimeIsoLabel, dateTimeField);

    hintLabel = new JLabel(LanguageManager.t("hint.dateFormat"));
    hintLabel.setForeground(Theme.TEXT_MUTED);
    hintLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 1;
    gc.gridy = 4;
    gc.gridwidth = 1;
    form.add(hintLabel, gc);

    messageLabel = new JLabel(LanguageManager.t("msg.empty"));
    messageLabel.setForeground(Theme.TEXT_ERROR);
    messageLabel.setFont(Theme.FONT_BODY);
    gc.gridx = 0;
    gc.gridy = 5;
    gc.gridwidth = 2;
    form.add(messageLabel, gc);

    bookBtn = new JButton(LanguageManager.t("btn.bookAppt"));
    bookBtn.setBackground(Theme.BTN_PRIMARY);
    bookBtn.setForeground(Theme.TEXT_PRIMARY);
    bookBtn.setFocusPainted(false);
    bookBtn.setFont(Theme.FONT_BODY_BOLD);
    bookBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 6;
    form.add(bookBtn, gc);

    refreshBtn = new JButton(LanguageManager.t("btn.refreshCustomers"));
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadCustomers());
    gc.gridy = 7;
    gc.gridwidth = 1;
    form.add(refreshBtn, gc);

    add(form, BorderLayout.CENTER);
    bookBtn.addActionListener(e -> handleBook());
  }

  private void loadCustomers() {
    customerCombo.removeAllItems();
    List<User> customers = userService.getAllByRole(Role.CUSTOMER);
    for (User c : customers) {
      customerCombo.addItem(
          new UserItem(c.getId(), c.getFullName() + " (" + c.getUsername() + ")"));
    }
  }

  private void loadServices() {
    serviceCombo.removeAllItems();
    for (Service s : serviceDAO.getActiveServices()) {
      serviceCombo.addItem(
          new ServiceItem(
              s.getServiceId(),
              String.format(
                  LanguageManager.t("fmt.serviceItem"),
                  s.getServiceName(),
                  String.format("%.2f", s.getPrice()))));
    }
  }

  private void handleBook() {
    UserItem customer = (UserItem) customerCombo.getSelectedItem();
    ServiceItem service = (ServiceItem) serviceCombo.getSelectedItem();
    String plate = vehicleField.getText().trim();
    String dtStr = dateTimeField.getText().trim();

    if (customer == null || service == null || plate.isEmpty() || dtStr.isEmpty()) {
      messageLabel.setForeground(Theme.TEXT_ERROR);
      messageLabel.setText(LanguageManager.t("msg.fieldsRequired"));
      return;
    }

    LocalDateTime dt;
    try {
      dt = LocalDateTime.parse(dtStr);
    } catch (DateTimeParseException ex) {
      messageLabel.setForeground(Theme.TEXT_ERROR);
      messageLabel.setText(LanguageManager.t("msg.date.invalidFormat"));
      return;
    }

    Result<Appointment> result =
        apptService.createAppointment(customer.id, staffUser.getId(), service.id, plate, dt);
    if (!result.isSuccess()) {
      messageLabel.setForeground(Theme.TEXT_ERROR);
      messageLabel.setText(LanguageManager.resolveError(result.getError()));
      return;
    }

    messageLabel.setForeground(Theme.TEXT_SUCCESS);
    messageLabel.setText(
        String.format(LanguageManager.t("msg.appt.created"), result.getValue().getAppointmentId()));
    vehicleField.setText("");
  }

  private void addRow(
      JPanel panel, GridBagConstraints gc, int row, JLabel label, JComponent field) {
    gc.gridx = 0;
    gc.gridy = row;
    gc.weightx = 0.35;
    gc.gridwidth = 1;
    panel.add(label, gc);
    gc.gridx = 1;
    gc.weightx = 0.65;
    panel.add(field, gc);
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_BODY);
    return lbl;
  }

  private static class UserItem {
    final String id, label;

    UserItem(String id, String label) {
      this.id = id;
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  @Override
  public void refresh() {
    titleLabel.setText(LanguageManager.t("title.bookAppt"));
    customerLabel.setText(LanguageManager.t("label.customer"));
    serviceLabel.setText(LanguageManager.t("label.service"));
    vehiclePlateLabel.setText(LanguageManager.t("label.vehiclePlate"));
    dateTimeIsoLabel.setText(LanguageManager.t("label.dateTimeIso"));
    hintLabel.setText(LanguageManager.t("hint.dateFormat"));
    bookBtn.setText(LanguageManager.t("btn.bookAppt"));
    refreshBtn.setText(LanguageManager.t("btn.refreshCustomers"));
    loadCustomers();
    loadServices();
  }

  private static class ServiceItem {
    final String id, label;

    ServiceItem(String id, String label) {
      this.id = id;
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
