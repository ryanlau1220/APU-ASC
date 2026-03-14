package com.apu.asc.ui.staff;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class StaffCustomerPanel extends JPanel {

  private final UserService userService = new UserService();

  private JTable table;
  private DefaultTableModel model;
  private JTextField searchField;

  public StaffCustomerPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData(null);
  }

  private void buildUI() {
    String[] cols = {"ID", "Username", "Full Name", "Email", "Contact", "Status"};
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

    JScrollPane scroll = new JScrollPane(table);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);

    searchField = new JTextField(20);
    JButton searchBtn = makeBtn("Search", Theme.BTN_PRIMARY);
    JButton addBtn = makeBtn("Add Customer", Theme.BTN_SUCCESS);
    JButton editBtn = makeBtn("Edit Selected", Theme.BTN_PRIMARY);
    JButton deactBtn = makeBtn("Deactivate", Theme.BTN_DANGER);
    JButton refreshBtn = makeBtn("Refresh", Theme.BTN_PRIMARY);

    JLabel searchLabel = new JLabel("Search:");
    searchLabel.setForeground(Theme.TEXT_SECONDARY);
    searchLabel.setFont(Theme.FONT_BODY);

    top.add(searchLabel);
    top.add(searchField);
    top.add(searchBtn);
    top.add(addBtn);
    top.add(editBtn);
    top.add(deactBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);

    searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));
    refreshBtn.addActionListener(e -> loadData(null));
    addBtn.addActionListener(e -> openAddDialog());
    editBtn.addActionListener(e -> openEditDialog());
    deactBtn.addActionListener(e -> handleDeactivate());
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

  private void loadData(String filter) {
    model.setRowCount(0);
    List<User> customers = userService.getAllByRole(Role.CUSTOMER);
    for (User u : customers) {
      if (filter != null && !filter.isEmpty()) {
        String fl = filter.toLowerCase();
        if (!u.getFullName().toLowerCase().contains(fl)
            && !u.getUsername().toLowerCase().contains(fl)
            && !u.getEmail().toLowerCase().contains(fl)) continue;
      }
      model.addRow(
          new Object[] {
            u.getId(), u.getUsername(), u.getFullName(),
            u.getEmail(), u.getContactNumber(), u.getStatus().name()
          });
    }
  }

  private void openAddDialog() {
    JTextField username = new JTextField(16);
    JPasswordField pass = new JPasswordField(16);
    JTextField fullName = new JTextField(16);
    JTextField email = new JTextField(16);
    JTextField contact = new JTextField(16);
    JLabel errorLabel = new JLabel(" ");
    errorLabel.setForeground(Theme.TEXT_ERROR);
    errorLabel.setFont(Theme.FONT_BODY);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(owner, "Add Customer", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(360, 300);
    dialog.setLocationRelativeTo(this);
    dialog.setResizable(false);

    JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
    form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
    form.setBackground(Theme.BG_PANEL);
    addFormRow(form, "Username:", username);
    addFormRow(form, "Password:", pass);
    addFormRow(form, "Full Name:", fullName);
    addFormRow(form, "Email:", email);
    addFormRow(form, "Contact:", contact);

    JButton saveBtn = makeBtn("Save", Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn("Cancel", Theme.BTN_DANGER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    btnRow.setBackground(Theme.BG_PANEL);
    btnRow.add(cancelBtn);
    btnRow.add(saveBtn);

    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
    bottom.setBackground(Theme.BG_PANEL);
    bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
    bottom.add(errorLabel);
    bottom.add(Box.createVerticalStrut(4));
    bottom.add(btnRow);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Theme.BG_PANEL);
    root.add(form, BorderLayout.CENTER);
    root.add(bottom, BorderLayout.SOUTH);
    dialog.setContentPane(root);

    cancelBtn.addActionListener(e -> dialog.dispose());
    saveBtn.addActionListener(
        e -> {
          Result<User> created =
              userService.createUser(
                  Role.CUSTOMER,
                  username.getText(),
                  new String(pass.getPassword()),
                  fullName.getText(),
                  email.getText(),
                  contact.getText());
          if (!created.isSuccess()) {
            errorLabel.setText(created.getError());
          } else {
            dialog.dispose();
            loadData(null);
            Toast.success(owner, "Customer added successfully.");
          }
        });

    dialog.setVisible(true);
  }

  private void openEditDialog() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(SwingUtilities.getWindowAncestor(this), "Select a customer first.");
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    User user = userService.findById(id);
    if (user == null) return;

    JTextField fullName = new JTextField(user.getFullName(), 16);
    JTextField email = new JTextField(user.getEmail(), 16);
    JTextField contact = new JTextField(user.getContactNumber(), 16);
    JLabel errorLabel = new JLabel(" ");
    errorLabel.setForeground(Theme.TEXT_ERROR);
    errorLabel.setFont(Theme.FONT_BODY);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(owner, "Edit Customer", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(340, 220);
    dialog.setLocationRelativeTo(this);
    dialog.setResizable(false);

    JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
    form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
    form.setBackground(Theme.BG_PANEL);
    addFormRow(form, "Full Name:", fullName);
    addFormRow(form, "Email:", email);
    addFormRow(form, "Contact:", contact);

    JButton saveBtn = makeBtn("Save", Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn("Cancel", Theme.BTN_DANGER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    btnRow.setBackground(Theme.BG_PANEL);
    btnRow.add(cancelBtn);
    btnRow.add(saveBtn);

    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
    bottom.setBackground(Theme.BG_PANEL);
    bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
    bottom.add(errorLabel);
    bottom.add(Box.createVerticalStrut(4));
    bottom.add(btnRow);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Theme.BG_PANEL);
    root.add(form, BorderLayout.CENTER);
    root.add(bottom, BorderLayout.SOUTH);
    dialog.setContentPane(root);

    cancelBtn.addActionListener(e -> dialog.dispose());
    saveBtn.addActionListener(
        e -> {
          Result<User> result =
              userService.updateProfile(
                  user, fullName.getText(), email.getText(), contact.getText());
          if (!result.isSuccess()) {
            errorLabel.setText(result.getError());
          } else {
            dialog.dispose();
            loadData(null);
            Toast.success(owner, "Customer updated successfully.");
          }
        });

    dialog.setVisible(true);
  }

  private void handleDeactivate() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(SwingUtilities.getWindowAncestor(this), "Select a customer first.");
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    String name = (String) model.getValueAt(row, 2);

    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Deactivate customer \"" + name + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    userService.deactivateUser(id);
    loadData(null);
    Toast.success(SwingUtilities.getWindowAncestor(this), "Customer deactivated.");
  }

  private void addFormRow(JPanel panel, String labelText, javax.swing.JComponent field) {
    JLabel lbl = new JLabel(labelText);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_BODY);
    panel.add(lbl);
    panel.add(field);
  }
}
