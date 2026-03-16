package com.apu.asc.ui.manager;

import com.apu.asc.dao.AuditLogDAO;
import com.apu.asc.model.AuditLog;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ManagerAuditPanel extends JPanel implements Refreshable {

  private final AuditLogDAO auditLogDAO = AuditLogDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;
  private JTextField searchField;

  private JLabel searchLabel;
  private JButton refreshBtn;

  public ManagerAuditPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.logId"),
      LanguageManager.t("col.timestamp"),
      LanguageManager.t("col.userId"),
      LanguageManager.t("col.actionType"),
      LanguageManager.t("col.targetEntity"),
      LanguageManager.t("col.description")
    };
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
    table.setShowGrid(false);
    table.setIntercellSpacing(new java.awt.Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.getColumnModel().getColumn(0).setPreferredWidth(100);
    table.getColumnModel().getColumn(1).setPreferredWidth(150);
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(3).setPreferredWidth(130);
    table.getColumnModel().getColumn(4).setPreferredWidth(120);
    table.getColumnModel().getColumn(5).setPreferredWidth(300);

    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);

    searchField = new JTextField(20);
    searchField
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              @Override
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable(sorter);
              }

              @Override
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable(sorter);
              }

              @Override
              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable(sorter);
              }
            });

    refreshBtn = new JButton(LanguageManager.t("btn.refresh"));
    refreshBtn.setFont(Theme.FONT_BODY_BOLD);
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadData());

    searchLabel = new JLabel(LanguageManager.t("label.search"));
    searchLabel.setForeground(Theme.TEXT_SECONDARY);
    searchLabel.setFont(Theme.FONT_BODY);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);
    top.add(searchLabel);
    top.add(searchField);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    JLabel statusLabel = new JLabel(" ");
    statusLabel.setForeground(Theme.TEXT_MUTED);
    statusLabel.setFont(Theme.FONT_BODY);
    add(statusLabel, BorderLayout.SOUTH);

    model.addTableModelListener(
        e ->
            statusLabel.setText(
                String.format(LanguageManager.t("label.totalEntries"), model.getRowCount())));
  }

  private void filterTable(TableRowSorter<DefaultTableModel> sorter) {
    String text = searchField.getText().trim();
    if (text.isEmpty()) {
      sorter.setRowFilter(null);
    } else {
      sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }
  }

  private void loadData() {
    model.setRowCount(0);
    List<AuditLog> logs = auditLogDAO.getAll();

    for (AuditLog log : logs) {
      model.addRow(
          new Object[] {
            log.getLogId(),
            log.getTimestamp().toString().replace("T", " "),
            log.getUserId(),
            log.getActionType(),
            log.getTargetEntityId(),
            log.getDescription()
          });
    }
  }

  @Override
  public void refresh() {
    searchLabel.setText(LanguageManager.t("label.search"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.logId",
      "col.timestamp",
      "col.userId",
      "col.actionType",
      "col.targetEntity",
      "col.description"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
