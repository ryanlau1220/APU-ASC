package com.apu.asc;

import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

import com.apu.asc.ui.AppShell;

public class Main {
  public static void main(String[] args) {
    try {
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
      System.err.println("Failed to initialize Nimbus Look and Feel");
    }
    SwingUtilities.invokeLater(() -> new AppShell().setVisible(true));
  }
}
