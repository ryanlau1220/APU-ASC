package com.apu.asc.ui.manager;

import com.apu.asc.service.ReportService;
import com.apu.asc.service.ReportService.TechnicianStats;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class ManagerReportPanel extends JPanel implements Refreshable {

  private final ReportService reportService = new ReportService();

  private JLabel kpiTotalRevenue;
  private JLabel kpiBestMonth;
  private JLabel kpiTopTech;
  private JLabel kpiTopRating;

  private JLabel revenueTitleLabel;
  private JLabel techTitleLabel;
  private JButton refreshBtn;

  private SimpleBarChart revenueChart;
  private SimpleBarChart techChart;

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

    revenueTitleLabel = new JLabel(LanguageManager.t("title.revenueByMonth"));
    revenueTitleLabel.setForeground(Theme.TEXT_PRIMARY);
    revenueTitleLabel.setFont(Theme.FONT_HEADING);

    refreshBtn = new JButton(LanguageManager.t("btn.refresh"));
    refreshBtn.setFont(Theme.FONT_BODY_BOLD);
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadData());

    JPanel titleRow = new JPanel(new BorderLayout());
    titleRow.setBackground(Theme.BG_PANEL);
    titleRow.add(revenueTitleLabel, BorderLayout.WEST);
    titleRow.add(refreshBtn, BorderLayout.EAST);

    kpiTotalRevenue = kpiLabel(LanguageManager.t("kpi.totalRevenue") + " —");
    kpiBestMonth = kpiLabel(LanguageManager.t("kpi.bestMonth") + " —");

    JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 4));
    kpiRow.setBackground(Theme.BG_PANEL);
    kpiRow.add(kpiTotalRevenue);
    kpiRow.add(kpiBestMonth);

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Theme.BG_PANEL);
    header.add(titleRow, BorderLayout.NORTH);
    header.add(kpiRow, BorderLayout.SOUTH);

    revenueChart = new SimpleBarChart(Theme.BTN_PRIMARY, "Revenue (RM)");

    panel.add(header, BorderLayout.NORTH);
    panel.add(revenueChart, BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildTechSection() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBackground(Theme.BG_PANEL);

    techTitleLabel = new JLabel(LanguageManager.t("title.techPerformance"));
    techTitleLabel.setForeground(Theme.TEXT_PRIMARY);
    techTitleLabel.setFont(Theme.FONT_HEADING);

    kpiTopTech = kpiLabel(LanguageManager.t("kpi.topTechnician") + " —");
    kpiTopRating = kpiLabel(LanguageManager.t("kpi.highestAvgRating") + " —");

    JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 4));
    kpiRow.setBackground(Theme.BG_PANEL);
    kpiRow.add(kpiTopTech);
    kpiRow.add(kpiTopRating);

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Theme.BG_PANEL);
    header.add(techTitleLabel, BorderLayout.NORTH);
    header.add(kpiRow, BorderLayout.SOUTH);

    techChart = new SimpleBarChart(Theme.BTN_SUCCESS, "Jobs Completed");

    panel.add(header, BorderLayout.NORTH);
    panel.add(techChart, BorderLayout.CENTER);
    return panel;
  }

  private JLabel kpiLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(Theme.TEXT_PRIMARY);
    lbl.setFont(Theme.FONT_BODY_BOLD);
    return lbl;
  }

  private void loadData() {
    Map<String, Double> revenue = reportService.revenueByMonth();
    double total = 0;
    String bestMonth = "—";
    double bestAmount = -1;

    for (Map.Entry<String, Double> entry : revenue.entrySet()) {
      total += entry.getValue();
      if (entry.getValue() > bestAmount) {
        bestAmount = entry.getValue();
        bestMonth = entry.getKey();
      }
    }
    revenueChart.setData(revenue);

    kpiTotalRevenue.setText(
        LanguageManager.t("kpi.totalRevenue") + " RM " + String.format("%.2f", total));
    kpiBestMonth.setText(LanguageManager.t("kpi.bestMonth") + " " + bestMonth);

    List<TechnicianStats> stats = reportService.technicianPerformance();
    String topTech = "—";
    String topRating = "—";
    int maxJobs = -1;
    double maxRating = -1;
    
    Map<String, Double> techData = new LinkedHashMap<>();

    for (TechnicianStats s : stats) {
      techData.put(s.technician.getFullName(), (double) s.jobsCompleted);
      if (s.jobsCompleted > maxJobs) {
        maxJobs = s.jobsCompleted;
        topTech = s.technician.getFullName() + " (" + maxJobs + " jobs)";
      }
      if (s.averageRating > maxRating) {
        maxRating = s.averageRating;
        topRating = s.technician.getFullName() + " (" + String.format("%.2f", maxRating) + ")";
      }
    }
    techChart.setData(techData);

    kpiTopTech.setText(LanguageManager.t("kpi.topTechnician") + " " + topTech);
    kpiTopRating.setText(LanguageManager.t("kpi.highestAvgRating") + " " + topRating);
  }

  @Override
  public void refresh() {
    revenueTitleLabel.setText(LanguageManager.t("title.revenueByMonth"));
    techTitleLabel.setText(LanguageManager.t("title.techPerformance"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));
    loadData();
  }
}
