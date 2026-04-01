package com.apu.asc;

import com.apu.asc.ui.AppShell;
import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    try {
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (Exception ex) {
      System.err.println("Failed to initialize Nimbus Look and Feel");
    }
    SwingUtilities.invokeLater(() -> new AppShell().setVisible(true));
  }
}
