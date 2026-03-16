package com.apu.asc.ui.manager;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.CounterStaff;
import com.apu.asc.model.Customer;
import com.apu.asc.model.Manager;
import com.apu.asc.model.Role;
import com.apu.asc.model.Technician;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.DataSanitizer;
import com.apu.asc.util.Result;
import com.apu.asc.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ManagerUserPanel extends JPanel implements Refreshable {

  private final UserService userService = new UserService();
  private final UserDAO userDAO = UserDAO.getInstance();

  private JTable table;
  private DefaultTableModel model;
  private TableRowSorter<DefaultTableModel> sorter;
  private JTextField searchField;
  private JComboBox<String> roleFilter;

  private JLabel searchLabel;
  private JLabel roleLabel;
  private JButton filterBtn;
  private JButton addBtn;
  private JButton editBtn;
  private JButton deactBtn;
  private JButton reactivateBtn;
  private JButton refreshBtn;
  private JButton exportBtn;
  private JButton importBtn;

  public ManagerUserPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData(null);
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.id"),
      LanguageManager.t("col.username"),
      LanguageManager.t("col.fullName"),
      LanguageManager.t("col.email"),
      LanguageManager.t("col.contact"),
      LanguageManager.t("col.role"),
      LanguageManager.t("col.status")
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

    sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);

    roleFilter =
        new JComboBox<>(new String[] {"ALL", "MANAGER", "STAFF", "TECHNICIAN", "CUSTOMER"});
    filterBtn = makeBtn(LanguageManager.t("btn.filter"), Theme.BTN_PRIMARY);
    addBtn = makeBtn(LanguageManager.t("btn.addUser"), Theme.BTN_SUCCESS);
    editBtn = makeBtn(LanguageManager.t("btn.editSelected"), Theme.BTN_PRIMARY);
    deactBtn = makeBtn(LanguageManager.t("btn.deactivate"), Theme.BTN_DANGER);
    reactivateBtn = makeBtn(LanguageManager.t("btn.reactivate"), Theme.BTN_SUCCESS);
    refreshBtn = makeBtn(LanguageManager.t("btn.refresh"), Theme.BTN_PRIMARY);
    exportBtn = makeBtn(LanguageManager.t("btn.exportCsv"), Theme.BTN_PRIMARY);
    importBtn = makeBtn(LanguageManager.t("btn.importCsv"), Theme.BTN_SUCCESS);

    searchField = new JTextField(16);
    searchLabel = new JLabel(LanguageManager.t("label.search"));
    searchLabel.setForeground(Theme.TEXT_SECONDARY);
    searchLabel.setFont(Theme.FONT_BODY);
    searchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                applySearch();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                applySearch();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                applySearch();
              }
            });

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);
    roleLabel = new JLabel(LanguageManager.t("label.role"));
    roleLabel.setForeground(Theme.TEXT_SECONDARY);
    roleLabel.setFont(Theme.FONT_BODY);
    top.add(searchLabel);
    top.add(searchField);
    top.add(roleLabel);
    top.add(roleFilter);
    top.add(filterBtn);
    top.add(addBtn);
    top.add(editBtn);
    top.add(deactBtn);
    top.add(reactivateBtn);
    top.add(refreshBtn);
    top.add(exportBtn);
    top.add(importBtn);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);

    filterBtn.addActionListener(e -> applyFilter());
    addBtn.addActionListener(e -> openAddDialog());
    editBtn.addActionListener(e -> openEditDialog());
    deactBtn.addActionListener(e -> handleDeactivate());
    reactivateBtn.addActionListener(e -> handleReactivate());
    refreshBtn.addActionListener(e -> loadData(null));
    exportBtn.addActionListener(e -> handleExportCsv());
    importBtn.addActionListener(e -> handleImportCsv());
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

  private void applyFilter() {
    String sel = (String) roleFilter.getSelectedItem();
    if ("ALL".equals(sel)) loadData(null);
    else loadData(Role.valueOf(sel));
  }

  private void applySearch() {
    String text = searchField.getText().trim();
    if (text.isEmpty()) {
      sorter.setRowFilter(null);
    } else {
      sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }
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
    JLabel errorLabel = new JLabel(LanguageManager.t("msg.empty"));
    errorLabel.setForeground(Theme.TEXT_ERROR);
    errorLabel.setFont(Theme.FONT_BODY);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(
            owner,
            LanguageManager.t("dialog.user.add.title"),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(360, 320);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel form = new JPanel(new GridLayout(6, 2, 6, 6));
    form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
    form.setBackground(Theme.BG_PANEL);
    addFormRow(form, LanguageManager.t("label.role"), roleCombo);
    addFormRow(form, LanguageManager.t("label.username"), username);
    addFormRow(form, LanguageManager.t("label.password"), pass);
    addFormRow(form, LanguageManager.t("label.fullName"), fullName);
    addFormRow(form, LanguageManager.t("label.email"), email);
    addFormRow(form, LanguageManager.t("label.contact"), contact);

    JButton saveBtn = makeBtn(LanguageManager.t("btn.save"), Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn(LanguageManager.t("btn.cancel"), Theme.BTN_DANGER);

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
            errorLabel.setText(created.getError());
          } else {
            dialog.dispose();
            loadData(null);
            Toast.success(owner, LanguageManager.t("msg.user.created"));
          }
        });

    dialog.setVisible(true);
  }

  private void openEditDialog() {
    int row = table.getSelectedRow();
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, LanguageManager.t("msg.user.selectFirst"));
      return;
    }

    int modelRow = table.convertRowIndexToModel(row);
    String id = (String) model.getValueAt(modelRow, 0);
    User user = userService.findById(id);
    if (user == null) return;

    JTextField fullName = new JTextField(user.getFullName(), 16);
    JTextField email = new JTextField(user.getEmail(), 16);
    JTextField contact = new JTextField(user.getContactNumber(), 16);
    JLabel errorLabel = new JLabel(LanguageManager.t("msg.empty"));
    errorLabel.setForeground(Theme.TEXT_ERROR);
    errorLabel.setFont(Theme.FONT_BODY);

    JDialog dialog =
        new JDialog(
            owner,
            LanguageManager.t("dialog.user.edit.title"),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(340, 220);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
    form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
    form.setBackground(Theme.BG_PANEL);
    addFormRow(form, LanguageManager.t("label.fullName"), fullName);
    addFormRow(form, LanguageManager.t("label.email"), email);
    addFormRow(form, LanguageManager.t("label.contact"), contact);

    JButton saveBtn = makeBtn(LanguageManager.t("btn.save"), Theme.BTN_SUCCESS);
    JButton cancelBtn = makeBtn(LanguageManager.t("btn.cancel"), Theme.BTN_DANGER);

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
            Toast.success(owner, LanguageManager.t("msg.user.updated"));
          }
        });

    dialog.setVisible(true);
  }

  private void handleDeactivate() {
    int row = table.getSelectedRow();
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, LanguageManager.t("msg.user.selectFirst"));
      return;
    }
    String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
    String name = (String) model.getValueAt(table.convertRowIndexToModel(row), 2);
    int confirm =
        JOptionPane.showConfirmDialog(
            owner,
            String.format(LanguageManager.t("msg.user.deactivateConfirm"), name),
            LanguageManager.t("dialog.confirm.title"),
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;
    userService.deactivateUser(id);
    loadData(null);
    Toast.success(owner, LanguageManager.t("msg.user.deactivated"));
  }

  private void handleReactivate() {
    int row = table.getSelectedRow();
    Window owner = SwingUtilities.getWindowAncestor(this);
    if (row < 0) {
      Toast.error(owner, LanguageManager.t("msg.user.selectFirst"));
      return;
    }
    String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
    String name = (String) model.getValueAt(table.convertRowIndexToModel(row), 2);
    int confirm =
        JOptionPane.showConfirmDialog(
            owner,
            String.format(LanguageManager.t("msg.user.reactivateConfirm"), name),
            LanguageManager.t("dialog.confirm.title"),
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;
    userService.reactivateUser(id);
    loadData(null);
    Toast.success(owner, LanguageManager.t("msg.user.reactivated"));
  }

  @Override
  public void refresh() {
    searchLabel.setText(LanguageManager.t("label.search"));
    roleLabel.setText(LanguageManager.t("label.role"));
    filterBtn.setText(LanguageManager.t("btn.filter"));
    addBtn.setText(LanguageManager.t("btn.addUser"));
    editBtn.setText(LanguageManager.t("btn.editSelected"));
    deactBtn.setText(LanguageManager.t("btn.deactivate"));
    reactivateBtn.setText(LanguageManager.t("btn.reactivate"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));
    exportBtn.setText(LanguageManager.t("btn.exportCsv"));
    importBtn.setText(LanguageManager.t("btn.importCsv"));

    String[] colKeys = {
      "col.id", "col.username", "col.fullName", "col.email", "col.contact", "col.role", "col.status"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData(null);
  }

  private void addFormRow(JPanel panel, String labelText, javax.swing.JComponent field) {
    JLabel lbl = new JLabel(labelText);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_BODY);
    panel.add(lbl);
    panel.add(field);
  }

  private void handleExportCsv() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    JFileChooser fc = new JFileChooser();
    fc.setSelectedFile(new File("users.csv"));
    fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    if (fc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;
    try (BufferedWriter w =
        Files.newBufferedWriter(fc.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
      w.write("ID,Username,PasswordHash,Role,FullName,Email,Contact,Status");
      w.newLine();
      List<User> users = userService.getAll();
      for (User user : users) {
        w.write(
            String.join(
                ",",
                toCsvField(user.getId()),
                toCsvField(user.getUsername()),
                toCsvField(user.getPasswordHash()),
                toCsvField(user.getRole().name()),
                toCsvField(user.getFullName()),
                toCsvField(user.getEmail()),
                toCsvField(user.getContactNumber()),
                toCsvField(user.getStatus().name())));
        w.newLine();
      }
      Toast.success(owner, String.format(LanguageManager.t("msg.csv.exported"), users.size()));
    } catch (IOException ex) {
      Toast.error(owner, String.format(LanguageManager.t("msg.csv.exportFailed"), ex.getMessage()));
    }
  }

  private void handleImportCsv() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    if (fc.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) return;
    List<String[]> rows = new ArrayList<>();
    try (BufferedReader r =
        Files.newBufferedReader(fc.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
      String line;
      boolean first = true;
      while ((line = r.readLine()) != null) {
        if (line.isBlank()) continue;
        if (first) {
          first = false;
          continue;
        }
        List<String> fields = parseCsvLine(line);
        if (fields == null || fields.size() != 8) {
          Toast.error(owner, String.format(LanguageManager.t("msg.csv.invalidRow"), line));
          return;
        }
        rows.add(fields.toArray(String[]::new));
      }
    } catch (IOException ex) {
      Toast.error(owner, String.format(LanguageManager.t("msg.csv.importFailed"), ex.getMessage()));
      return;
    }
    List<User> toImport = new ArrayList<>();
    Set<String> ids = new HashSet<>();
    Set<String> usernames = new HashSet<>();
    Set<String> emails = new HashSet<>();
    for (User existing : userDAO.getAll()) {
      emails.add(existing.getEmail().toLowerCase());
    }
    int rowIndex = 0;
    for (String[] p : rows) {
      rowIndex++;
      String id = DataSanitizer.clean(p[0]).trim();
      String username = DataSanitizer.clean(p[1]).trim();
      String passwordHash = p[2].trim();
      String roleRaw = p[3].trim().toUpperCase();
      String fullName = DataSanitizer.clean(p[4]).trim();
      String email = DataSanitizer.clean(p[5]).trim();
      String contact = DataSanitizer.clean(p[6]).trim();
      String statusRaw = p[7].trim().toUpperCase();

      if (id.isBlank() || username.isBlank() || passwordHash.isBlank()) {
        Toast.error(owner, String.format(LanguageManager.t("msg.csv.rowRequired"), rowIndex));
        return;
      }

      Role role;
      UserStatus status;
      try {
        role = Role.valueOf(roleRaw);
      } catch (IllegalArgumentException ex) {
        Toast.error(
            owner, String.format(LanguageManager.t("msg.csv.rowInvalidRole"), rowIndex, roleRaw));
        return;
      }
      try {
        status = UserStatus.valueOf(statusRaw);
      } catch (IllegalArgumentException ex) {
        Toast.error(
            owner,
            String.format(LanguageManager.t("msg.csv.rowInvalidStatus"), rowIndex, statusRaw));
        return;
      }

      if (userDAO.findById(id) != null || ids.contains(id)) {
        Toast.error(
            owner, String.format(LanguageManager.t("msg.csv.rowDuplicateId"), rowIndex, id));
        return;
      }
      if (userDAO.findByUsername(username) != null || usernames.contains(username)) {
        Toast.error(
            owner,
            String.format(LanguageManager.t("msg.csv.rowDuplicateUsername"), rowIndex, username));
        return;
      }

      String emailKey = email.toLowerCase();
      if (emails.contains(emailKey)) {
        Toast.error(
            owner, String.format(LanguageManager.t("msg.csv.rowDuplicateEmail"), rowIndex, email));
        return;
      }

      User user = buildUser(role, id, username, passwordHash, status, fullName, email, contact);
      String violations = ValidationUtil.getViolations(user);
      if (violations != null) {
        Toast.error(
            owner, String.format(LanguageManager.t("msg.csv.rowViolations"), rowIndex, violations));
        return;
      }

      toImport.add(user);
      ids.add(id);
      usernames.add(username);
      emails.add(emailKey);
    }

    for (User user : toImport) {
      userDAO.save(user);
    }
    loadData(null);
    Toast.success(owner, String.format(LanguageManager.t("msg.csv.imported"), toImport.size()));
  }

  private User buildUser(
      Role role,
      String id,
      String username,
      String passwordHash,
      UserStatus status,
      String fullName,
      String email,
      String contactNumber) {
    return switch (role) {
      case CUSTOMER ->
          new Customer(id, username, passwordHash, status, fullName, email, contactNumber);
      case STAFF ->
          new CounterStaff(id, username, passwordHash, status, fullName, email, contactNumber);
      case TECHNICIAN ->
          new Technician(id, username, passwordHash, status, fullName, email, contactNumber);
      case MANAGER ->
          new Manager(id, username, passwordHash, status, fullName, email, contactNumber);
    };
  }

  private String toCsvField(String value) {
    String safe = value == null ? "" : value;
    return "\"" + safe.replace("\"", "\"\"") + "\"";
  }

  private List<String> parseCsvLine(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (inQuotes) {
        if (c == '"') {
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            current.append('"');
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          current.append(c);
        }
      } else {
        if (c == '"') {
          inQuotes = true;
        } else if (c == ',') {
          fields.add(current.toString());
          current.setLength(0);
        } else {
          current.append(c);
        }
      }
    }
    if (inQuotes) return null;
    fields.add(current.toString());
    return fields;
  }
}
