package com.apu.asc.ui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Toast {

  public enum Type {
    SUCCESS,
    ERROR,
    INFO
  }

  private Toast() {}

  public static void show(Window owner, String message, Type type) {
    SwingUtilities.invokeLater(() -> display(owner, message, type));
  }

  public static void success(Window owner, String message) {
    show(owner, message, Type.SUCCESS);
  }

  public static void error(Window owner, String message) {
    show(owner, message, Type.ERROR);
  }

  public static void info(Window owner, String message) {
    show(owner, message, Type.INFO);
  }

  private static void display(Window owner, String message, Type type) {
    Frame frame = findFrame(owner);
    JWindow toast = new JWindow(frame);
    toast.setAlwaysOnTop(true);

    Color bg =
        type == Type.SUCCESS
            ? Theme.TOAST_SUCCESS_BG
            : type == Type.ERROR ? Theme.TOAST_ERROR_BG : Theme.TOAST_INFO_BG;

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(bg);
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.brighter(), 1),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)));

    JLabel label = new JLabel(message);
    label.setFont(Theme.FONT_BODY_BOLD);
    label.setForeground(Color.WHITE);
    panel.add(label, BorderLayout.CENTER);

    toast.setContentPane(panel);
    toast.pack();
    toast.setPreferredSize(new Dimension(Math.max(toast.getWidth(), 260), toast.getHeight()));
    toast.pack();

    positionBottomRight(toast, frame);

    boolean supportsTranslucency = supportsTranslucentWindows();

    if (supportsTranslucency) {
      try {
        toast.setOpacity(0f);
      } catch (UnsupportedOperationException ex) {
        supportsTranslucency = false;
      }
    }

    toast.setVisible(true);

    final int displayMs = 2800;
    if (supportsTranslucency) {
      int[] step = {0};
      final int fadeInSteps = 10;
      final int fadeOutSteps = 10;

      Timer fadeIn = new Timer(20, null);
      fadeIn.addActionListener(
          e -> {
            step[0]++;
            toast.setOpacity(Math.min(1f, step[0] / (float) fadeInSteps));
            if (step[0] >= fadeInSteps) {
              fadeIn.stop();
              Timer hold = new Timer(displayMs, ev -> startFadeOut(toast, fadeOutSteps));
              hold.setRepeats(false);
              hold.start();
            }
          });
      fadeIn.start();
    } else {
      Timer hold = new Timer(displayMs, e -> toast.dispose());
      hold.setRepeats(false);
      hold.start();
    }

    toast.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            toast.dispose();
          }
        });
    toast.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  private static void startFadeOut(JWindow toast, int steps) {
    int[] step = {0};
    Timer fadeOut = new Timer(20, null);
    fadeOut.addActionListener(
        e -> {
          step[0]++;
          float opacity = Math.max(0f, 1f - step[0] / (float) steps);
          toast.setOpacity(opacity);
          if (step[0] >= steps) {
            fadeOut.stop();
            toast.dispose();
          }
        });
    fadeOut.start();
  }

  private static void positionBottomRight(JWindow toast, Frame frame) {
    if (frame == null) {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      toast.setLocation(
          screen.width - toast.getWidth() - 24, screen.height - toast.getHeight() - 56);
      return;
    }
    int fx = frame.getX();
    int fy = frame.getY();
    int fw = frame.getWidth();
    int fh = frame.getHeight();
    toast.setLocation(fx + (fw - toast.getWidth()) / 2, fy + fh - toast.getHeight() - 56);
  }

  private static Frame findFrame(Window owner) {
    if (owner instanceof Frame frame) return frame;
    if (owner != null) {
      Window w = owner;
      while (w != null) {
        if (w instanceof Frame frame) return frame;
        w = w.getOwner();
      }
    }
    Frame[] frames = Frame.getFrames();
    if (frames.length > 0) return frames[0];
    return null;
  }

  private static boolean supportsTranslucentWindows() {
    GraphicsDevice device =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    return device.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
  }
}
