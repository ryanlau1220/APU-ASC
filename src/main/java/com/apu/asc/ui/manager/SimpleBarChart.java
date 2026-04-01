package com.apu.asc.ui.manager;

import com.apu.asc.ui.util.Theme;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public final class SimpleBarChart extends JPanel {

  private Map<String, Double> data = new LinkedHashMap<>();
  private Color barColor;
  private String yAxisLabel;

  public SimpleBarChart(Color barColor, String yAxisLabel) {
    this.barColor = barColor;
    this.yAxisLabel = yAxisLabel;
    setBackground(Theme.BG_ROOT);
  }

  public void setData(Map<String, Double> data) {
    this.data = data;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (data == null || data.isEmpty()) return;

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth();
    int height = getHeight();
    int padding = 40;
    int labelPadding = 20;

    double maxValue = data.values().stream().max(Double::compare).orElse(0.0);
    if (maxValue == 0) maxValue = 10;

    g2d.setColor(Theme.TEXT_MUTED);
    g2d.drawLine(padding + labelPadding, height - padding - labelPadding, padding + labelPadding, padding);
    g2d.drawLine(padding + labelPadding, height - padding - labelPadding, width - padding, height - padding - labelPadding);

    int numberYDivisions = 5;
    for (int i = 0; i <= numberYDivisions; i++) {
      int x0 = padding + labelPadding;
      int x1 = width - padding;
      int y = height - padding - labelPadding - (i * (height - padding * 2 - labelPadding) / numberYDivisions);
      
      g2d.setColor(new Color(70, 70, 70));
      if (i > 0) g2d.drawLine(x0, y, x1, y);

      g2d.setColor(Theme.TEXT_SECONDARY);
      String yLabel = String.format("%.0f", (maxValue * ((double) i / numberYDivisions)));
      FontMetrics metrics = g2d.getFontMetrics();
      int labelWidth = metrics.stringWidth(yLabel);
      g2d.drawString(yLabel, x0 - labelWidth - 5, y + (metrics.getHeight() / 2) - 3);
    }

    int numBars = data.size();
    int barWidth = (width - padding * 2 - labelPadding) / numBars - 10;
    if (barWidth > 60) barWidth = 60;

    int i = 0;
    for (Map.Entry<String, Double> entry : data.entrySet()) {
      int x = padding + labelPadding + (i * (width - padding * 2 - labelPadding) / numBars) + 5;
      int barHeight = (int) ((entry.getValue() / maxValue) * (height - padding * 2 - labelPadding));
      int y = height - padding - labelPadding - barHeight;

      g2d.setColor(barColor);
      g2d.fillRect(x, y, barWidth, barHeight);

      g2d.setColor(Theme.TEXT_SECONDARY);
      FontMetrics metrics = g2d.getFontMetrics();
      int labelWidth = metrics.stringWidth(entry.getKey());
      g2d.drawString(entry.getKey(), x + (barWidth - labelWidth) / 2, height - padding);
      i++;
    }
  }
}
