package com.apu.asc.ui.manager;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Service;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManagerServicePanel extends JPanel {

  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  public ManagerServicePanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {"Service ID", "Type", "Name", "Price (RM)", "Active"};
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

    JButton editPriceBtn = new JButton("Update Price");
    JButton toggleBtn = new JButton("Toggle Active");
    JButton refreshBtn = new JButton("Refresh");

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(new Color(45, 45, 45));
    top.add(editPriceBtn);
    top.add(toggleBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    editPriceBtn.addActionListener(e -> handleEditPrice());
    toggleBtn.addActionListener(e -> handleToggleActive());
    refreshBtn.addActionListener(e -> loadData());
  }

  private void loadData() {
    model.setRowCount(0);
    List<Service> services = serviceDAO.getAll();
    for (Service s : services) {
      model.addRow(
          new Object[] {
            s.getServiceId(),
            s.getType().name(),
            s.getServiceName(),
            String.format("%.2f", s.getPrice()),
            s.isActive() ? "Yes" : "No"
          });
    }
  }

  private void handleEditPrice() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a service first.");
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    Service svc = serviceDAO.findById(id);
    if (svc == null) return;

    String input =
        JOptionPane.showInputDialog(
            this,
            "New price for \"" + svc.getServiceName() + "\" (RM):",
            String.format("%.2f", svc.getPrice()));
    if (input == null || input.isBlank()) return;

    try {
      double price = Double.parseDouble(input.trim());
      if (price < 0) throw new NumberFormatException("negative");
      svc.setPrice(price);
      serviceDAO.save(svc);
      loadData();
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(
          this, "Invalid price. Enter a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void handleToggleActive() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a service first.");
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    Service svc = serviceDAO.findById(id);
    if (svc == null) return;

    svc.setActive(!svc.isActive());
    serviceDAO.save(svc);
    loadData();
  }
}
