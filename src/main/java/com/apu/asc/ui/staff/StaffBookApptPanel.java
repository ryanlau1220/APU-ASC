package com.apu.asc.ui.staff;

import com.apu.asc.model.Role;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.UserService;
import com.apu.asc.dao.ServiceDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class StaffBookApptPanel extends JPanel {

    private final User staffUser;
    private final AppointmentService apptService = new AppointmentService();
    private final UserService userService        = new UserService();
    private final ServiceDAO serviceDAO          = ServiceDAO.getInstance();

    private JComboBox<UserItem> customerCombo;
    private JComboBox<ServiceItem> serviceCombo;
    private JTextField vehicleField;
    private JTextField dateTimeField;
    private JLabel messageLabel;

    public StaffBookApptPanel(User staffUser) {
        this.staffUser = staffUser;
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Book New Appointment");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(45, 45, 45));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        customerCombo = new JComboBox<>();
        serviceCombo  = new JComboBox<>();
        vehicleField  = new JTextField(16);
        dateTimeField = new JTextField("2026-06-01T09:00", 16);

        loadCustomers();
        loadServices();

        addRow(form, gc, 0, "Customer:",           customerCombo);
        addRow(form, gc, 1, "Service:",            serviceCombo);
        addRow(form, gc, 2, "Vehicle Plate:",      vehicleField);
        addRow(form, gc, 3, "Date & Time (ISO):",  dateTimeField);

        JLabel hint = new JLabel("Format: yyyy-MM-ddTHH:mm  (e.g. 2026-06-01T09:00)");
        hint.setForeground(new Color(140, 140, 140));
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gc.gridx = 1; gc.gridy = 4; gc.gridwidth = 1;
        form.add(hint, gc);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(new Color(220, 80, 80));
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        form.add(messageLabel, gc);

        JButton bookBtn = new JButton("Book Appointment");
        bookBtn.setBackground(new Color(0, 120, 215));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFocusPainted(false);
        bookBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gc.gridy = 6;
        form.add(bookBtn, gc);

        JButton refreshBtn = new JButton("Refresh Customers");
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
            customerCombo.addItem(new UserItem(c.getId(),
                    c.getFullName() + " (" + c.getUsername() + ")"));
        }
    }

    private void loadServices() {
        serviceCombo.removeAllItems();
        for (Service s : serviceDAO.getActiveServices()) {
            serviceCombo.addItem(new ServiceItem(s.getServiceId(),
                    s.getServiceName() + " — RM " + String.format("%.2f", s.getPrice())));
        }
    }

    private void handleBook() {
        UserItem customer    = (UserItem) customerCombo.getSelectedItem();
        ServiceItem service  = (ServiceItem) serviceCombo.getSelectedItem();
        String plate         = vehicleField.getText().trim();
        String dtStr         = dateTimeField.getText().trim();

        if (customer == null || service == null || plate.isEmpty() || dtStr.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }

        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(dtStr);
        } catch (DateTimeParseException ex) {
            messageLabel.setText("Invalid date format. Use yyyy-MM-ddTHH:mm.");
            return;
        }

        var result = apptService.createAppointment(customer.id, staffUser.getId(),
                service.id, plate, dt);
        if (result == null) {
            messageLabel.setForeground(new Color(220, 80, 80));
            messageLabel.setText("Booking failed: service inactive or time slot conflict.");
            return;
        }

        messageLabel.setForeground(new Color(80, 200, 80));
        messageLabel.setText("Appointment " + result.getAppointmentId() + " created!");
        vehicleField.setText("");
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int row,
                         String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35; gc.gridwidth = 1;
        panel.add(styledLabel(label), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        panel.add(field, gc);
    }

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.LIGHT_GRAY);
        return lbl;
    }

    private static class UserItem {
        final String id, label;
        UserItem(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class ServiceItem {
        final String id, label;
        ServiceItem(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
