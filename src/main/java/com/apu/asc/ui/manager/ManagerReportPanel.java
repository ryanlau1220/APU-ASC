package com.apu.asc.ui.manager;

import com.apu.asc.service.ReportService;
import com.apu.asc.service.ReportService.TechnicianStats;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ManagerReportPanel extends JPanel {

  private final ReportService reportService = new ReportService();

  private DefaultTableModel revenueModel;
  private DefaultTableModel techModel;

  private JLabel kpiTotalRevenue;
  private JLabel kpiBestMonth;
  private JLabel kpiTopTech;
  private JLabel kpiTopRating;

  public ManagerReportPanel() {
    setLayout(new GridLayout(2, 1, 0, 12));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    add(buildRevenueSection());
    add(buildTechSection());
    loadData();
  }

  private JPanel buildRevenueSection() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBackground(Theme.BG_PANEL);

    JLabel title = new JLabel("Revenue by Month");
    title.setForeground(Theme.TEXT_PRIMARY);
    title.setFont(Theme.FONT_HEADING);

    JButton refreshBtn = new JButton("Refresh");
    refreshBtn.setFont(Theme.FONT_BODY_BOLD);
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadData());

    JPanel titleRow = new JPanel(new BorderLayout());
    titleRow.setBackground(Theme.BG_PANEL);
    titleRow.add(title, BorderLayout.WEST);
    titleRow.add(refreshBtn, BorderLayout.EAST);

    kpiTotalRevenue = kpiLabel("Total Revenue: —");
    kpiBestMonth = kpiLabel("Best Month: —");

    JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 4));
    kpiRow.setBackground(Theme.BG_PANEL);
    kpiRow.add(kpiTotalRevenue);
    kpiRow.add(kpiBestMonth);

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Theme.BG_PANEL);
    header.add(titleRow, BorderLayout.NORTH);
    header.add(kpiRow, BorderLayout.SOUTH);

    String[] cols = {"Month (yyyy-MM)", "Total Revenue (RM)"};
    revenueModel =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    JTable revenueTable = new JTable(revenueModel);
    revenueTable.setRowHeight(Theme.TABLE_ROW_HEIGHT);
    revenueTable.setFont(Theme.FONT_BODY);
    revenueTable.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
    revenueTable.setShowGrid(false);
    revenueTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
    revenueTable.setSelectionBackground(Theme.BG_SELECTION);
    revenueTable.setSelectionForeground(Theme.TEXT_PRIMARY);

    panel.add(header, BorderLayout.NORTH);
    panel.add(new JScrollPane(revenueTable), BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildTechSection() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBackground(Theme.BG_PANEL);

    JLabel title = new JLabel("Technician Performance");
    title.setForeground(Theme.TEXT_PRIMARY);
    title.setFont(Theme.FONT_HEADING);

    kpiTopTech = kpiLabel("Top Technician: —");
    kpiTopRating = kpiLabel("Highest Avg Rating: —");

    JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 4));
    kpiRow.setBackground(Theme.BG_PANEL);
    kpiRow.add(kpiTopTech);
    kpiRow.add(kpiTopRating);

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Theme.BG_PANEL);
    header.add(title, BorderLayout.NORTH);
    header.add(kpiRow, BorderLayout.SOUTH);

    String[] cols = {"Technician ID", "Name", "Jobs Completed", "Average Rating"};
    techModel =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    JTable techTable = new JTable(techModel);
    techTable.setRowHeight(Theme.TABLE_ROW_HEIGHT);
    techTable.setFont(Theme.FONT_BODY);
    techTable.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
    techTable.setShowGrid(false);
    techTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
    techTable.setSelectionBackground(Theme.BG_SELECTION);
    techTable.setSelectionForeground(Theme.TEXT_PRIMARY);

    panel.add(header, BorderLayout.NORTH);
    panel.add(new JScrollPane(techTable), BorderLayout.CENTER);
    return panel;
  }

  private JLabel kpiLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_PRIMARY);
    lbl.setFont(Theme.FONT_BODY_BOLD);
    return lbl;
  }

  private void loadData() {
    revenueModel.setRowCount(0);
    Map<String, Double> revenue = reportService.revenueByMonth();
    double total = 0;
    String bestMonth = "—";
    double bestAmount = -1;

    for (Map.Entry<String, Double> entry : revenue.entrySet()) {
      revenueModel.addRow(new Object[] {entry.getKey(), String.format("%.2f", entry.getValue())});
      total += entry.getValue();
      if (entry.getValue() > bestAmount) {
        bestAmount = entry.getValue();
        bestMonth = entry.getKey();
      }
    }
    if (!revenue.isEmpty()) {
      revenueModel.addRow(new Object[] {"TOTAL", String.format("%.2f", total)});
    }

    kpiTotalRevenue.setText("Total Revenue: RM " + String.format("%.2f", total));
    kpiBestMonth.setText("Best Month: " + bestMonth);

    techModel.setRowCount(0);
    List<TechnicianStats> stats = reportService.technicianPerformance();
    String topTech = "—";
    String topRating = "—";
    int maxJobs = -1;
    double maxRating = -1;

    for (TechnicianStats s : stats) {
      String avgRating =
          s.averageRating == 0.0 ? "N/A" : String.format("%.2f / 5", s.averageRating);
      techModel.addRow(
          new Object[] {
            s.technician.getId(), s.technician.getFullName(), s.jobsCompleted, avgRating
          });
      if (s.jobsCompleted > maxJobs) {
        maxJobs = s.jobsCompleted;
        topTech = s.technician.getFullName() + " (" + maxJobs + " jobs)";
      }
      if (s.averageRating > maxRating) {
        maxRating = s.averageRating;
        topRating = s.technician.getFullName() + " (" + String.format("%.2f", maxRating) + ")";
      }
    }

    kpiTopTech.setText("Top Technician: " + topTech);
    kpiTopRating.setText("Highest Avg Rating: " + topRating);
  }
}
