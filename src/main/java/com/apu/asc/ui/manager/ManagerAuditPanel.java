package com.apu.asc.ui.manager;

import com.apu.asc.dao.AuditLogDAO;
import com.apu.asc.model.AuditLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class ManagerAuditPanel extends JPanel {

    private final AuditLogDAO auditLogDAO = AuditLogDAO.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public ManagerAuditPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        buildUI();
        loadData();
    }

    private void buildUI() {
        String[] cols = {"Log ID", "Timestamp", "User ID", "Action Type", "Target Entity", "Description"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
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
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(sorter); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(sorter); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(sorter); }
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.LIGHT_GRAY);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(new Color(45, 45, 45));
        top.add(searchLabel);
        top.add(searchField);
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        add(statusLabel, BorderLayout.SOUTH);

        model.addTableModelListener(e -> {
            statusLabel.setText("  Total entries: " + model.getRowCount());
        });
    }

    private void filterTable(TableRowSorter<DefaultTableModel> sorter) {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadData() {
        model.setRowCount(0);
        List<AuditLog> logs = auditLogDAO.getAll();

        for (AuditLog log : logs) {
            model.addRow(new Object[]{
                log.getLogId(),
                log.getTimestamp().toString().replace("T", " "),
                log.getUserId(),
                log.getActionType(),
                log.getTargetEntityId(),
                log.getDescription()
            });
        }
    }
}
