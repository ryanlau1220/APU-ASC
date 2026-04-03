package com.apu.asc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

import com.apu.asc.ui.AppShell;

public class Main {
  public static void main(String[] args) {
    try {
      Path dataDir = Path.of("data");
      if (!Files.exists(dataDir)) {
        Files.createDirectories(dataDir);
      }
    } catch (IOException e) {
      System.err.println("Could not create data directory: " + e.getMessage());
    }

    try {
      javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
      System.err.println("Failed to initialize Nimbus Look and Feel");
    }
    SwingUtilities.invokeLater(() -> new AppShell().setVisible(true));
  }
}
