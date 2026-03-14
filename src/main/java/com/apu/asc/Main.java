package com.apu.asc;

import com.apu.asc.ui.AppShell;
import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    com.formdev.flatlaf.FlatLightLaf.setup();
    SwingUtilities.invokeLater(() -> new AppShell().setVisible(true));
  }
}
