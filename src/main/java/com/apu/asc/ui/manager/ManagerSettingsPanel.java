package com.apu.asc.ui.manager;

import com.apu.asc.service.BackupService;
import com.apu.asc.ui.Refreshable;
import com.apu.asc.ui.util.LanguageManager;
import com.apu.asc.ui.util.Theme;
import com.apu.asc.ui.util.Toast;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ManagerSettingsPanel extends JPanel implements Refreshable {

  private final BackupService backupService = new BackupService();

  private JLabel titleLabel;
  private JButton backupBtn;
  private JButton restoreBtn;

  public ManagerSettingsPanel() {
    setLayout(new BorderLayout(8, 8));
    setBackground(Theme.BG_PANEL);
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    buildUI();
  }

  private void buildUI() {
    titleLabel = new JLabel(LanguageManager.t("title.systemSettings"));
    titleLabel.setFont(Theme.FONT_TITLE);
    titleLabel.setForeground(Theme.TEXT_PRIMARY);
    add(titleLabel, BorderLayout.NORTH);

    backupBtn = makeBtn(LanguageManager.t("btn.backupSystem"), Theme.BTN_PRIMARY);
    restoreBtn = makeBtn(LanguageManager.t("btn.restoreBackup"), Theme.BTN_DANGER);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    btnPanel.setBackground(Theme.BG_PANEL);
    btnPanel.add(backupBtn);
    btnPanel.add(restoreBtn);
    add(btnPanel, BorderLayout.CENTER);

    backupBtn.addActionListener(e -> handleBackup());
    restoreBtn.addActionListener(e -> handleRestore());
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

  private void handleBackup() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    try {
      Path zip = backupService.backup();
      Toast.success(owner, String.format(LanguageManager.t("msg.backup.saved"), zip.getFileName()));
    } catch (IOException ex) {
      Toast.error(owner, String.format(LanguageManager.t("msg.backup.failed"), ex.getMessage()));
    }
  }

  private void handleRestore() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    int confirm =
        JOptionPane.showConfirmDialog(
            owner,
            LanguageManager.t("msg.restore.confirm"),
            LanguageManager.t("dialog.restore.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (confirm != JOptionPane.YES_OPTION) return;
    JFileChooser fc = new JFileChooser(new File("backups"));
    fc.setFileFilter(new FileNameExtensionFilter("ZIP Archives", "zip"));
    if (fc.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) return;
    try {
      backupService.restore(fc.getSelectedFile().toPath());
      Toast.success(owner, LanguageManager.t("msg.restore.complete"));
    } catch (IOException ex) {
      Toast.error(owner, String.format(LanguageManager.t("msg.restore.failed"), ex.getMessage()));
    }
  }

  @Override
  public void refresh() {
    titleLabel.setText(LanguageManager.t("title.systemSettings"));
    backupBtn.setText(LanguageManager.t("btn.backupSystem"));
    restoreBtn.setText(LanguageManager.t("btn.restoreBackup"));
  }
}
