package com.apu.asc.ui.staff;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.service.UserService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class StaffCustomerPanel extends JPanel {

  private final UserService userService = new UserService();

  private JTable table;
  private DefaultTableModel model;

  private JTextField searchField;

  public StaffCustomerPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(new Color(45, 45, 45));
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
    table.setRowHeight(24);
    table.setFont(new Font("SansSerif", Font.PLAIN, 12));
    table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JScrollPane scroll = new JScrollPane(table);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(new Color(45, 45, 45));

    searchField = new JTextField(20);
    JButton searchBtn = new JButton("Search");
    JButton addBtn = new JButton("Add Customer");
    JButton editBtn = new JButton("Edit Selected");
    JButton deactBtn = new JButton("Deactivate");
    JButton refreshBtn = new JButton("Refresh");

    top.add(new JLabel("Search:"));
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

    JPanel p = new JPanel(new GridLayout(5, 2, 6, 6));
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
            this, p, "Add Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (res != JOptionPane.OK_OPTION) return;

    User created =
        userService.createUser(
            Role.CUSTOMER,
            username.getText(),
            new String(pass.getPassword()),
            fullName.getText(),
            email.getText(),
            contact.getText());

    if (created == null) {
      JOptionPane.showMessageDialog(
          this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      loadData(null);
    }
  }

  private void openEditDialog() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a customer first.");
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
            this, p, "Edit Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (res != JOptionPane.OK_OPTION) return;

    userService.updateProfile(user, fullName.getText(), email.getText(), contact.getText());
    loadData(null);
  }

  private void handleDeactivate() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Select a customer first.");
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
  }
}
