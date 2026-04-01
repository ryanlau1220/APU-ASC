package com.apu.asc.ui.manager;

import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Service;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public final class ManagerServicePanel extends JPanel implements Refreshable {

  private final ServiceDAO serviceDAO = ServiceDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;

  private JButton editPriceBtn;
  private JButton toggleBtn;
  private JButton refreshBtn;

  public ManagerServicePanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.serviceId"),
      LanguageManager.t("col.type"),
      LanguageManager.t("col.name"),
      LanguageManager.t("col.priceRm"),
      LanguageManager.t("col.active")
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
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowGrid(false);
    table.setIntercellSpacing(new java.awt.Dimension(0, 0));
    table.setSelectionBackground(Theme.BG_SELECTION);
    table.setSelectionForeground(Theme.TEXT_PRIMARY);
    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    editPriceBtn = makeBtn(LanguageManager.t("btn.updatePrice"), Theme.BTN_PRIMARY);
    toggleBtn = makeBtn(LanguageManager.t("btn.toggleActive"), Theme.BTN_PRIMARY);
    refreshBtn = makeBtn(LanguageManager.t("btn.refresh"), Theme.BTN_PRIMARY);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);
    top.add(editPriceBtn);
    top.add(toggleBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    editPriceBtn.addActionListener(e -> handleEditPrice());
    toggleBtn.addActionListener(e -> handleToggleActive());
    refreshBtn.addActionListener(e -> loadData());
  }

  private JButton makeBtn(String text, java.awt.Color bg) {
    JButton btn = new JButton(text);
    btn.setBackground(bg);
    btn.setForeground(Theme.TEXT_PRIMARY);
    btn.setFocusPainted(false);
    btn.setFont(Theme.FONT_BODY_BOLD);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
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
            s.isActive() ? LanguageManager.t("label.yes") : LanguageManager.t("label.no")
          });
    }
  }

  private void handleEditPrice() {
    int row = table.getSelectedRow();
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, LanguageManager.t("msg.service.selectFirst"));
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    Service svc = serviceDAO.findById(id);
    if (svc == null) return;

    JTextField priceField = new JTextField(String.format("%.2f", svc.getPrice()), 12);
    JLabel errorLabel = new JLabel(LanguageManager.t("msg.empty"));
    errorLabel.setForeground(Theme.TEXT_ERROR);
    errorLabel.setFont(Theme.FONT_BODY);

    JDialog dialog =
        new JDialog(
            owner,
            String.format(LanguageManager.t("dialog.updatePrice.title"), svc.getServiceName()),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(300, 160);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    form.setBackground(Theme.BG_PANEL);
    JLabel lbl = new JLabel(LanguageManager.t("label.newPriceRm"));
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_BODY);
    form.add(lbl);
    form.add(priceField);

    JButton saveBtn = makeBtn(LanguageManager.t("btn.save"), Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn(LanguageManager.t("btn.cancel"), Theme.BTN_DANGER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
    btnRow.setBackground(Theme.BG_PANEL);
    btnRow.add(errorLabel);
    btnRow.add(cancelBtn);
    btnRow.add(saveBtn);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Theme.BG_PANEL);
    root.add(form, BorderLayout.CENTER);
    root.add(btnRow, BorderLayout.SOUTH);
    dialog.setContentPane(root);

    cancelBtn.addActionListener(e -> dialog.dispose());
    saveBtn.addActionListener(
        e -> {
          try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) throw new NumberFormatException("negative");
            svc.setPrice(price);
            String violations = ValidationUtil.getViolations(svc);
            if (violations != null) {
              errorLabel.setText(violations);
              return;
            }
            serviceDAO.save(svc);
            dialog.dispose();
            loadData();
            Toast.success(
                owner,
                String.format(LanguageManager.t("msg.service.priceUpdated"), svc.getServiceName()));
          } catch (NumberFormatException ex) {
            errorLabel.setText(LanguageManager.t("msg.service.priceInvalid"));
          }
        });

    dialog.setVisible(true);
  }

  private void handleToggleActive() {
    int row = table.getSelectedRow();
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, LanguageManager.t("msg.service.selectFirst"));
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    Service svc = serviceDAO.findById(id);
    if (svc == null) return;

    int confirm =
        JOptionPane.showConfirmDialog(
            owner,
            String.format(
                LanguageManager.t("msg.service.toggleConfirm"),
                svc.isActive()
                    ? LanguageManager.t("action.deactivate")
                    : LanguageManager.t("action.activate"),
                svc.getServiceName()),
            LanguageManager.t("dialog.confirm.title"),
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    svc.setActive(!svc.isActive());
    serviceDAO.save(svc);
    loadData();
    Toast.success(
        owner,
        svc.isActive()
            ? LanguageManager.t("msg.service.activated")
            : LanguageManager.t("msg.service.deactivated"));
  }

  @Override
  public void refresh() {
    editPriceBtn.setText(LanguageManager.t("btn.updatePrice"));
    toggleBtn.setText(LanguageManager.t("btn.toggleActive"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {"col.serviceId", "col.type", "col.name", "col.priceRm", "col.active"};
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }
}
