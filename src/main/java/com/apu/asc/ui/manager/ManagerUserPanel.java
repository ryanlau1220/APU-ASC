package com.apu.asc.ui.manager;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.service.UserService;
import com.apu.asc.util.Result;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManagerUserPanel extends JPanel {

  private final UserService userService = new UserService();

  private JTable table;
  private DefaultTableModel model;
  private JComboBox<String> roleFilter;

  public ManagerUserPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(new Color(45, 45, 45));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData(null);
  }

  private void buildUI() {
    String[] cols = {"ID", "Username", "Full Name", "Email", "Contact", "Role", "Status"};
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

    roleFilter =
        new JComboBox<>(new String[] {"ALL", "MANAGER", "STAFF", "TECHNICIAN", "CUSTOMER"});
    JButton filterBtn = new JButton("Filter");
    JButton addBtn = new JButton("Add User");
    JButton editBtn = new JButton("Edit Selected");
    JButton deactBtn = new JButton("Deactivate");
    JButton reactivateBtn = new JButton("Reactivate");
    JButton refreshBtn = new JButton("Refresh");

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(new Color(45, 45, 45));
    top.add(new JLabel("Role:"));
    top.add(roleFilter);
    top.add(filterBtn);
    top.add(addBtn);
    top.add(editBtn);
    top.add(deactBtn);
    top.add(reactivateBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    filterBtn.addActionListener(e -> applyFilter());
    addBtn.addActionListener(e -> openAddDialog());
    editBtn.addActionListener(e -> openEditDialog());
    deactBtn.addActionListener(e -> handleDeactivate());
    reactivateBtn.addActionListener(e -> handleReactivate());
    refreshBtn.addActionListener(e -> loadData(null));
  }

  private void applyFilter() {
    String sel = (String) roleFilter.getSelectedItem();
    if ("ALL".equals(sel)) loadData(null);
    else loadData(Role.valueOf(sel));
  }

  private void loadData(Role role) {
    model.setRowCount(0);
    List<User> users = role != null ? userService.getAllByRole(role) : userService.getAll();
    for (User u : users) {
      model.addRow(
          new Object[] {
            u.getId(),
            u.getUsername(),
            u.getFullName(),
            u.getEmail(),
            u.getContactNumber(),
            u.getRole().name(),
            u.getStatus().name()
          });
    }
  }

  private void openAddDialog() {
    String[] roles = {"CUSTOMER", "STAFF", "TECHNICIAN", "MANAGER"};
    JComboBox<String> roleCombo = new JComboBox<>(roles);
    JTextField username = new JTextField(16);
    JPasswordField pass = new JPasswordField(16);
    JTextField fullName = new JTextField(16);
    JTextField email = new JTextField(16);
    JTextField contact = new JTextField(16);

    JPanel p = new JPanel(new GridLayout(6, 2, 6, 6));
    p.add(new JLabel("Role:"));
    p.add(roleCombo);
    p.add(new JLabel("Username:"));
    p.add(username);
    p.add(new JLabel("Password:"));
    p.add(pass);
    p.add(new JLabel("Full Name:"));
    p.add(fullName);
    p.add(new JLabel("Email:"));
    p.add(email);
    p.add(new JLabel("Contact:"));
    p.add(contact);

    int res =
        JOptionPane.showConfirmDialog(
            this, p, "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (res != JOptionPane.OK_OPTION) return;

    Role role = Role.valueOf((String) roleCombo.getSelectedItem());
    Result<User> created =
        userService.createUser(
            role,
            username.getText(),
            new String(pass.getPassword()),
            fullName.getText(),
            email.getText(),
            contact.getText());

    if (!created.isSuccess()) {
      JOptionPane.showMessageDialog(this, created.getError(), "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      loadData(null);
    }
  }

  private void openEditDialog() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a user first.");
      return;
    }

    String id = (String) model.getValueAt(row, 0);
    User user = userService.findById(id);
    if (user == null) return;

    JTextField fullName = new JTextField(user.getFullName(), 16);
    JTextField email = new JTextField(user.getEmail(), 16);
    JTextField contact = new JTextField(user.getContactNumber(), 16);

    JPanel p = new JPanel(new GridLayout(3, 2, 6, 6));
    p.add(new JLabel("Full Name:"));
    p.add(fullName);
    p.add(new JLabel("Email:"));
    p.add(email);
    p.add(new JLabel("Contact:"));
    p.add(contact);

    int res =
        JOptionPane.showConfirmDialog(
            this, p, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (res != JOptionPane.OK_OPTION) return;

    userService.updateProfile(user, fullName.getText(), email.getText(), contact.getText());
    loadData(null);
  }

  private void handleDeactivate() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a user first.");
      return;
    }
    String id = (String) model.getValueAt(row, 0);
    String name = (String) model.getValueAt(row, 2);
    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Deactivate user \"" + name + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;
    userService.deactivateUser(id);
    loadData(null);
  }

  private void handleReactivate() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a user first.");
      return;
    }
    String id = (String) model.getValueAt(row, 0);
    String name = (String) model.getValueAt(row, 2);
    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Reactivate user \"" + name + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;
    userService.reactivateUser(id);
    loadData(null);
  }
}
