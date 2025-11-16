package com.firework.app;

import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TerminalUtils {
    private static int rows = 40;
    private static int cols = 120;

    public static void installAnsi() {
        AnsiConsole.systemInstall();
    }
    public static void uninstallAnsi() {
        AnsiConsole.systemUninstall();
    }

    public static void enterAlternateBuffer() {
        System.out.print("\033[?1049h");
        System.out.flush();
    }
    public static void exitAlternateBuffer() {
        System.out.print("\033[?1049l");
        System.out.flush();
    }

    public static void hideCursor() {
        System.out.print("\033[?25l");
        System.out.flush();
    }
    public static void showCursor() {
        System.out.print("\033[?25h");
        System.out.flush();
    }

    public static void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    public static void moveCursor(int r, int c) {
        if (r < 1) r = 1;
        if (c < 1) c = 1;
        System.out.printf("\033[%d;%dH", r, c);
    }

    public static void detectTerminalSize(int configuredRows, int configuredCols) {
        if (configuredRows > 0 && configuredCols > 0) {
            rows = configuredRows;
            cols = configuredCols;
            return;
        }

        // try stty (Unix)
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) {
                Process p = new ProcessBuilder("sh", "-c", "stty size 2>/dev/null || true").redirectErrorStream(true).start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = br.readLine();
                if (line != null && line.trim().length() > 0) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        rows = Integer.parseInt(parts[0]);
                        cols = Integer.parseInt(parts[1]);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}

        // try environment variables
        try {
            String c = System.getenv("COLUMNS");
            String r = System.getenv("LINES");
            if (c != null && r != null) {
                cols = Integer.parseInt(c);
                rows = Integer.parseInt(r);
                return;
            }
        } catch (Exception ignored) {}

        // fallback defaults
        rows = 40;
        cols = 120;
    }

    public static int getRows() { return rows; }
    public static int getCols() { return cols; }

    public static void sleepMs(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
    
    public static void applyFontSize() {
        try {
            int size = ConfigLoader.fontSize;
            String cmd = String.format("powershell -command \"&{$host.UI.RawUI.FontSize=%d}\"", size);
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) { }
    }
}
