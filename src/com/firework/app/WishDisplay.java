package com.firework.app;

public class WishDisplay {
    public static void showCentered(String message, int seconds) {
        TerminalUtils.clearScreen();
        int rows = TerminalUtils.getRows();
        int cols = TerminalUtils.getCols();

        int midRow = rows / 2;
        int startCol = Math.max(1, (cols - message.length()) / 2);

        TerminalUtils.moveCursor(midRow - 1, startCol);
        System.out.println("============================");
        TerminalUtils.moveCursor(midRow, startCol);
        System.out.println(message);
        TerminalUtils.moveCursor(midRow + 1, startCol);
        System.out.println("============================");

        TerminalUtils.sleepMs(seconds * 1000L);
    }
}
