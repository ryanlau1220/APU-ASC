package com.apu.asc.ui.staff;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.service.UserService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import com.apu.asc.util.Result;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;
import java.util.regex.Pattern;
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
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class StaffCustomerPanel extends JPanel implements Refreshable {

  private final UserService userService = new UserService();

  private JTable table;
  private DefaultTableModel model;
  private TableRowSorter<DefaultTableModel> sorter;
  private JTextField searchField;

  private JLabel searchLabel;
  private JButton addBtn;
  private JButton editBtn;
  private JButton deactBtn;
  private JButton refreshBtn;

  public StaffCustomerPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    buildUI();
    loadData();
  }

  private void buildUI() {
    String[] cols = {
      LanguageManager.t("col.id"),
      LanguageManager.t("col.username"),
      LanguageManager.t("col.fullName"),
      LanguageManager.t("col.email"),
      LanguageManager.t("col.contact"),
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

    JScrollPane scroll = new JScrollPane(table);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.setBackground(Theme.BG_PANEL);

    searchField = new JTextField(20);
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

    addBtn = makeBtn(LanguageManager.t("btn.addCustomer"), Theme.BTN_SUCCESS);
    editBtn = makeBtn(LanguageManager.t("btn.editSelected"), Theme.BTN_PRIMARY);
    deactBtn = makeBtn(LanguageManager.t("btn.deactivate"), Theme.BTN_DANGER);
    refreshBtn = makeBtn(LanguageManager.t("btn.refresh"), Theme.BTN_PRIMARY);

    searchLabel = new JLabel(LanguageManager.t("label.search"));
    searchLabel.setForeground(Theme.TEXT_SECONDARY);
    searchLabel.setFont(Theme.FONT_BODY);

    top.add(searchLabel);
    top.add(searchField);
    top.add(addBtn);
    top.add(editBtn);
    top.add(deactBtn);
    top.add(refreshBtn);

    add(top, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);

    refreshBtn.addActionListener(e -> loadData());
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

  private void applySearch() {
    String text = searchField.getText().trim();
    if (text.isEmpty()) {
      sorter.setRowFilter(null);
    } else {
      sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }
  }

  private void loadData() {
    model.setRowCount(0);
    List<User> customers = userService.getAllByRole(Role.CUSTOMER);
    for (User u : customers) {
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
    JLabel errorLabel = new JLabel(LanguageManager.t("msg.empty"));
    errorLabel.setForeground(Theme.TEXT_ERROR);
    errorLabel.setFont(Theme.FONT_BODY);

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(
            owner,
            LanguageManager.t("dialog.customer.add.title"),
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(360, 300);
    dialog.setLocationRelativeTo(owner);
    dialog.setResizable(false);

    JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
    form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
    form.setBackground(Theme.BG_PANEL);
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
          Result<User> created =
              userService.createUser(
                  Role.CUSTOMER,
                  username.getText(),
                  new String(pass.getPassword()),
                  fullName.getText(),
                  email.getText(),
                  contact.getText());
          if (!created.isSuccess()) {
            errorLabel.setText(LanguageManager.resolveError(created.getError()));
          } else {
            dialog.dispose();
            loadData();
            Toast.success(owner, LanguageManager.t("msg.customer.added"));
          }
        });

    dialog.setVisible(true);
  }

  private void openEditDialog() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.customer.selectFirst"));
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

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(
            owner,
            LanguageManager.t("dialog.customer.edit.title"),
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
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
            errorLabel.setText(LanguageManager.resolveError(result.getError()));
          } else {
            dialog.dispose();
            loadData();
            Toast.success(owner, LanguageManager.t("msg.customer.updated"));
          }
        });

    dialog.setVisible(true);
  }

  private void handleDeactivate() {
    int row = table.getSelectedRow();
    if (row < 0) {
      Toast.error(
          SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.customer.selectFirst"));
      return;
    }

    int modelRow = table.convertRowIndexToModel(row);
    String id = (String) model.getValueAt(modelRow, 0);
    String name = (String) model.getValueAt(modelRow, 2);

    int confirm =
        JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            String.format(LanguageManager.t("msg.customer.deactivateConfirm"), name),
            LanguageManager.t("dialog.confirm.title"),
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    userService.deactivateUser(id);
    loadData();
    Toast.success(
        SwingUtilities.getWindowAncestor(this), LanguageManager.t("msg.customer.deactivated"));
  }

  @Override
  public void refresh() {
    searchLabel.setText(LanguageManager.t("label.search"));
    addBtn.setText(LanguageManager.t("btn.addCustomer"));
    editBtn.setText(LanguageManager.t("btn.editSelected"));
    deactBtn.setText(LanguageManager.t("btn.deactivate"));
    refreshBtn.setText(LanguageManager.t("btn.refresh"));

    String[] colKeys = {
      "col.id", "col.username", "col.fullName", "col.email", "col.contact", "col.status"
    };
    for (int i = 0; i < colKeys.length; i++) {
      table.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.t(colKeys[i]));
    }
    table.getTableHeader().repaint();

    loadData();
  }

  private void addFormRow(JPanel panel, String labelText, javax.swing.JComponent field) {
    JLabel lbl = new JLabel(labelText);
    lbl.setForeground(Theme.TEXT_SECONDARY);
    lbl.setFont(Theme.FONT_BODY);
    panel.add(lbl);
    panel.add(field);
  }
}
