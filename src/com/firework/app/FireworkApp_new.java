package com.firework.app;

public class FireworkApp_new {
    public static void main(String[] args) {
        // Load config
        ConfigLoader.loadConfig();

        // Initialize terminal (install Jansi, enter alt buffer, hide cursor)
        TerminalUtils.installAnsi();
        TerminalUtils.enterAlternateBuffer();
        TerminalUtils.hideCursor();

        try {
            // detect terminal size (or use configured values)
            TerminalUtils.detectTerminalSize(ConfigLoader.terminalRows, ConfigLoader.terminalCols);
            TerminalUtils.applyFontSize();
            // show wish
            WishDisplay.showCentered(ConfigLoader.wishMessage, ConfigLoader.wishDisplayTime);

            // clear screen then start show manager
            TerminalUtils.clearScreen();

            // start concurrent show (blocks until done)
            FireworkManager.startShow();

            // final message briefly
            TerminalUtils.moveCursor(TerminalUtils.getRows() / 2, Math.max(1, (TerminalUtils.getCols() - 20) / 2));
            System.out.println("🎆 Firework Show Completed 🎆");
            TerminalUtils.sleepMs(1500);

        } finally {
            // restore terminal state
            TerminalUtils.clearScreen();
            TerminalUtils.showCursor();
            TerminalUtils.exitAlternateBuffer();
            TerminalUtils.uninstallAnsi();
        }
    }
}
