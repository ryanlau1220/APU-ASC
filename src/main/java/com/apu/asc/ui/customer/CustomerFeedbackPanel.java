package com.apu.asc.ui.customer;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.service.AppointmentService;
import com.apu.asc.service.FeedbackService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class CustomerFeedbackPanel extends JPanel implements Refreshable {

  private final User user;
  private final AppointmentService apptService = new AppointmentService();
  private final FeedbackService feedbackService = new FeedbackService();
  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();

  private JComboBox<AppointmentItem> apptCombo;
  private JSpinner ratingSpinner;
  private JTextArea commentsArea;
  private JLabel messageLabel;

  public CustomerFeedbackPanel(User user) {
    this.user = user;
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    buildUI();
  }

  private void buildUI() {
    JLabel title = new JLabel("Submit Feedback");
    title.setFont(Theme.FONT_TITLE);
    title.setForeground(Theme.TEXT_PRIMARY);
    add(title, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Theme.BG_PANEL);
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(8, 4, 8, 4);
    gc.fill = GridBagConstraints.HORIZONTAL;

    apptCombo = new JComboBox<>();
    loadEligibleAppointments();

    ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
    commentsArea = new JTextArea(4, 30);
    commentsArea.setLineWrap(true);
    commentsArea.setWrapStyleWord(true);

    addRow(form, gc, 0, "Appointment:", apptCombo);
    addRow(form, gc, 1, "Rating (1-5):", ratingSpinner);

    gc.gridx = 0;
    gc.gridy = 2;
    gc.weightx = 0.3;
    form.add(styledLabel("Comments:"), gc);
    gc.gridx = 1;
    gc.weightx = 0.7;
    form.add(new JScrollPane(commentsArea), gc);

    messageLabel = new JLabel(" ");
    messageLabel.setForeground(Theme.TEXT_ERROR);
    gc.gridx = 0;
    gc.gridy = 3;
    gc.gridwidth = 2;
    form.add(messageLabel, gc);

    JButton submitBtn = new JButton("Submit Feedback");
    submitBtn.setBackground(Theme.BTN_SUCCESS);
    submitBtn.setForeground(Theme.TEXT_PRIMARY);
    submitBtn.setFocusPainted(false);
    submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gc.gridy = 4;
    form.add(submitBtn, gc);

    JButton refreshBtn = new JButton("Refresh Appointments");
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadEligibleAppointments());
    gc.gridy = 5;
    gc.gridwidth = 1;
    form.add(refreshBtn, gc);

    add(form, BorderLayout.CENTER);

    submitBtn.addActionListener(e -> handleSubmit());
  }

  private void loadEligibleAppointments() {
    apptCombo.removeAllItems();
    List<Appointment> completed =
        apptService.getByCustomer(user.getId()).stream()
            .filter(a -> a.getStatus() == ApptStatus.COMPLETED)
            .filter(a -> feedbackService.findByAppointment(a.getAppointmentId()) == null)
            .collect(Collectors.toList());

    for (Appointment a : completed) {
      Service svc = serviceDAO.findById(a.getServiceId());
      String label =
          a.getAppointmentId()
              + " \u2014 "
              + a.getVehiclePlate()
              + " \u2014 "
              + (svc != null ? svc.getServiceName() : a.getServiceId());
      apptCombo.addItem(new AppointmentItem(a.getAppointmentId(), label));
    }
  }

  private void handleSubmit() {
    if (apptCombo.getItemCount() == 0) {
      messageLabel.setText("No eligible appointments to review.");
      return;
    }

    AppointmentItem selected = (AppointmentItem) apptCombo.getSelectedItem();
    if (selected == null) return;

    int rating = (int) ratingSpinner.getValue();
    String comments = commentsArea.getText().trim();

    Result<?> result = feedbackService.submitFeedback(selected.id, rating, comments);
    if (!result.isSuccess()) {
      messageLabel.setText(result.getError());
      return;
    }

    messageLabel.setText(" ");
    commentsArea.setText("");
    loadEligibleAppointments();
    Toast.success(SwingUtilities.getWindowAncestor(this), "Feedback submitted! Thank you.");
  }

  private void addRow(
      JPanel panel, GridBagConstraints gc, int row, String label, JComponent field) {
    gc.gridx = 0;
    gc.gridy = row;
    gc.weightx = 0.3;
    gc.gridwidth = 1;
    panel.add(styledLabel(label), gc);
    gc.gridx = 1;
    gc.weightx = 0.7;
    panel.add(field, gc);
  }

  private JLabel styledLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    return lbl;
  }

  @Override
  public void refresh() {
    loadEligibleAppointments();
  }

  private static class AppointmentItem {
    final String id;
    final String label;

    AppointmentItem(String id, String label) {
      this.id = id;
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
