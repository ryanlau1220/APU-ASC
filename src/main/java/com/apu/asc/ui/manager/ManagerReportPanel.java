package com.apu.asc.ui.manager;

import com.apu.asc.service.ReportService;
import com.apu.asc.service.ReportService.TechnicianStats;
import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManagerReportPanel extends JPanel {

  private final ReportService reportService = new ReportService();

  private DefaultTableModel revenueModel;
  private DefaultTableModel techModel;

  public ManagerReportPanel() {
    setLayout(new GridLayout(2, 1, 0, 12));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    add(buildRevenueSection());
    add(buildTechSection());
    loadData();
  }

  private JPanel buildRevenueSection() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBackground(new Color(45, 45, 45));

    JLabel title = new JLabel("Revenue by Month");
    title.setForeground(Color.WHITE);
    title.setFont(new Font("SansSerif", Font.BOLD, 14));

    JButton refreshBtn = new JButton("Refresh");
    refreshBtn.addActionListener(e -> loadData());

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(new Color(45, 45, 45));
    header.add(title, BorderLayout.WEST);
    header.add(refreshBtn, BorderLayout.EAST);

    String[] cols = {"Month (yyyy-MM)", "Total Revenue (RM)"};
    revenueModel =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    JTable revenueTable = new JTable(revenueModel);
    revenueTable.setRowHeight(24);
    revenueTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
    revenueTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

    panel.add(header, BorderLayout.NORTH);
    panel.add(new JScrollPane(revenueTable), BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildTechSection() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBackground(new Color(45, 45, 45));

    JLabel title = new JLabel("Technician Performance");
    title.setForeground(Color.WHITE);
    title.setFont(new Font("SansSerif", Font.BOLD, 14));

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(new Color(45, 45, 45));
    header.add(title, BorderLayout.WEST);

    String[] cols = {"Technician ID", "Name", "Jobs Completed", "Average Rating"};
    techModel =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    JTable techTable = new JTable(techModel);
    techTable.setRowHeight(24);
    techTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
    techTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

    panel.add(header, BorderLayout.NORTH);
    panel.add(new JScrollPane(techTable), BorderLayout.CENTER);
    return panel;
  }

  private void loadData() {
    revenueModel.setRowCount(0);
    Map<String, Double> revenue = reportService.revenueByMonth();
    double total = 0;
    for (Map.Entry<String, Double> entry : revenue.entrySet()) {
      revenueModel.addRow(new Object[] {entry.getKey(), String.format("%.2f", entry.getValue())});
      total += entry.getValue();
    }
    if (!revenue.isEmpty()) {
      revenueModel.addRow(new Object[] {"TOTAL", String.format("%.2f", total)});
    }

    techModel.setRowCount(0);
    List<TechnicianStats> stats = reportService.technicianPerformance();
    for (TechnicianStats s : stats) {
      String avgRating =
          s.averageRating == 0.0 ? "N/A" : String.format("%.2f / 5", s.averageRating);
      techModel.addRow(
          new Object[] {
            s.technician.getId(), s.technician.getFullName(), s.jobsCompleted, avgRating
          });
    }
  }
}
