package com.apu.asc;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("APU Automotive Service Centre starting...");
        });
    }
}
