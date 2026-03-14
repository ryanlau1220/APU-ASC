package com.apu.asc.ui.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized UI theme constants for APU-ASC.
 *
 * <p>All colours are derived from the existing dark palette in the legacy code. No hardcoded {@code
 * new Color(...)} calls should appear outside this class.
 */
public final class Theme {

  private Theme() {}

  // ── Backgrounds ─────────────────────────────────────────────────────────────
  /** Outermost window / root background. */
  public static final Color BG_ROOT = new Color(30, 30, 30);

  /** Standard panel / card background. */
  public static final Color BG_PANEL = new Color(45, 45, 45);

  /** Sidebar background — slightly lighter than root for visual separation. */
  public static final Color BG_SIDEBAR = new Color(38, 38, 38);

  /** Header bar background. */
  public static final Color BG_HEADER = new Color(35, 35, 35);

  /** Table row hover / selection. */
  public static final Color BG_SELECTION = new Color(0, 100, 180);

  // ── Text ────────────────────────────────────────────────────────────────────
  /** Primary white text on dark backgrounds. */
  public static final Color TEXT_PRIMARY = Color.WHITE;

  /** Light-grey label text. */
  public static final Color TEXT_SECONDARY = Color.LIGHT_GRAY;

  /** Muted / hint text (e.g. placeholder hints). */
  public static final Color TEXT_MUTED = new Color(140, 140, 140);

  /** Section heading text. */
  public static final Color TEXT_HEADING = new Color(180, 180, 180);

  /** Error / danger inline text. */
  public static final Color TEXT_ERROR = new Color(220, 80, 80);

  /** Success inline text. */
  public static final Color TEXT_SUCCESS = new Color(80, 200, 80);

  /** Hyperlink-style text. */
  public static final Color TEXT_LINK = new Color(100, 160, 255);

  // ── Buttons ─────────────────────────────────────────────────────────────────
  /** Primary action (login, book, save, etc.). */
  public static final Color BTN_PRIMARY = new Color(0, 120, 215);

  /** Positive / create action (register, submit, etc.). */
  public static final Color BTN_SUCCESS = new Color(0, 160, 80);

  /** Destructive / warning action (change password, deactivate). */
  public static final Color BTN_DANGER = new Color(180, 80, 0);

  /** Sidebar nav button — selected / active state. */
  public static final Color BTN_SIDEBAR_ACTIVE = new Color(0, 100, 180);

  /** Sidebar nav button — default (unselected). */
  public static final Color BTN_SIDEBAR_DEFAULT = new Color(38, 38, 38);

  // ── Toast ───────────────────────────────────────────────────────────────────
  public static final Color TOAST_SUCCESS_BG = new Color(30, 90, 50);
  public static final Color TOAST_ERROR_BG = new Color(120, 30, 30);
  public static final Color TOAST_INFO_BG = new Color(30, 70, 130);

  // ── Fonts ───────────────────────────────────────────────────────────────────
  public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
  public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 12);
  public static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 13);
  public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 14);
  public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
  public static final Font FONT_DISPLAY = new Font("SansSerif", Font.BOLD, 22);

  // ── Sizes ───────────────────────────────────────────────────────────────────
  public static final int SIDEBAR_WIDTH = 200;
  public static final int HEADER_HEIGHT = 52;
  public static final int TABLE_ROW_HEIGHT = 28;
}
