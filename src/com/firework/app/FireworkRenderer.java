package com.firework.app;

import java.util.*;

/**
 * Responsible for a single burst animation.
 * Includes optional tail (rocket) and circular particle expansion with inner decomposition.
 */
public class FireworkRenderer {

    private static final Random RAND = new Random();
    private static final String[] ANSI_COLORS = {
            "\u001B[31m", "\u001B[33m", "\u001B[32m",
            "\u001B[36m", "\u001B[34m", "\u001B[35m", "\u001B[97m"
    };
    private static final String ANSI_RESET = "\u001B[0m";
    private static final char[] FADE_POOL = new char[] {'*', '+', 'o', '.', '`'};

    // synchronized output to avoid mixed prints from concurrent bursts
    private static final Object OUT_LOCK = new Object();

    public static void runBurstWithTail(boolean colorMode) {
        int rows = TerminalUtils.getRows();
        int cols = TerminalUtils.getCols();

        // choose random center within margins
        int maxRcfg = Math.max(3, Math.min(ConfigLoader.maxRadius, Math.min(rows, cols) / 4));
        int margin = maxRcfg + 3;
        int cx = RAND.nextInt(Math.max(1, rows - margin * 2)) + margin;
        int cy = RAND.nextInt(Math.max(1, cols - margin * 2)) + margin;

        // perform tail (if enabled)
        if (ConfigLoader.tailEnabled) {
            performTail(cx, cy, colorMode);
        }

        // explode (circular particle expansion with decomposition)
        spawnCircularBurst(cx, cy, maxRcfg, colorMode);
    }

    private static void performTail(int cx, int cy, boolean colorMode) {
        int startR = TerminalUtils.getRows(); // bottom
        int startC = cy;
        int steps = Math.max(4, Math.min(ConfigLoader.tailLength, startR - cx));
        // compute linear steps from bottom to center
        double dr = (cx - startR) / (double) steps;
        double dc = 0.0; // same column (vertical launch)
        String color = colorMode ? ANSI_COLORS[RAND.nextInt(ANSI_COLORS.length)] : "";

        // simple tail positions list to clear later
        List<long[]> tailCells = new ArrayList<>();
        for (int s = 0; s < steps; s++) {
            int r = (int) Math.round(startR + dr * s);
            int c = startC + (int) Math.round(dc * s);
            // draw a short trail of characters behind rocket
            synchronized (OUT_LOCK) {
                TerminalUtils.moveCursor(r, c);
                if (colorMode) System.out.print(color + "|" + ANSI_RESET);
                else System.out.print("|");
                System.out.flush();
            }
            tailCells.add(new long[]{r, c});
            TerminalUtils.sleepMs(ConfigLoader.tailSpeed);
            // clear previous tail cell sometimes to avoid long line (leave a short trail)
            if (tailCells.size() > 2) {
                long[] cell = tailCells.remove(0);
                synchronized (OUT_LOCK) {
                    TerminalUtils.moveCursor((int)cell[0], (int)cell[1]);
                    System.out.print(' ');
                    System.out.flush();
                }
            }
        }
        // clear remaining tail
        for (long[] cell : tailCells) {
            synchronized (OUT_LOCK) {
                TerminalUtils.moveCursor((int)cell[0], (int)cell[1]);
                System.out.print(' ');
                System.out.flush();
            }
        }
    }

    private static void spawnCircularBurst(int cx, int cy, int maxR, boolean colorMode) {
        int particles = Math.max(24, ConfigLoader.particles);
        double[] angles = new double[particles];
        for (int i = 0; i < particles; i++) angles[i] = (2.0 * Math.PI / particles) * i;

        double[] radius = new double[particles];
        double[] speed = new double[particles];
        int[] life = new int[particles];
        char[] baseChar = new char[particles];

        for (int i = 0; i < particles; i++) {
            radius[i] = 0.0;
            speed[i] = 0.5 + RAND.nextDouble() * 0.9;
            life[i] = (int) (maxR * (0.9 + RAND.nextDouble() * 0.9));
            baseChar[i] = FADE_POOL[RAND.nextInt(FADE_POOL.length)];
        }

        Set<Long> prevPositions = new HashSet<>();

        int frames = Math.max(maxR + 6, (int)(maxR * 1.8) + 6);
        for (int frame = 0; frame < frames; frame++) {
            Set<Long> newPositions = new HashSet<>();
            double innerDecayCutoff = (0.35 + frame / (double) frames * 0.9) * maxR;

            for (int i = 0; i < particles; i++) {
                radius[i] += speed[i];

                double rVal = radius[i];
                int x = cx + (int) Math.round(rVal * Math.sin(angles[i]));
                int y = cy + (int) Math.round(rVal * Math.cos(angles[i]) * 0.5);

                if (x < 1 || x > TerminalUtils.getRows() || y < 1 || y > TerminalUtils.getCols()) continue;

                char drawChar = baseChar[i];

                double lifeProgress = rVal / Math.max(1.0, life[i]);
                if (lifeProgress > 1.0 || rVal > maxR * 1.2) continue;

                if (rVal < innerDecayCutoff) {
                    double p = RAND.nextDouble();
                    if (p < 0.30) drawChar = '.';
                    else if (p < 0.65) drawChar = '+';
                    else if (p < 0.90) drawChar = ':';
                    else drawChar = baseChar[i];
                } else {
                    double p = RAND.nextDouble();
                    if (p < 0.65) drawChar = '*';
                    else if (p < 0.90) drawChar = '+';
                    else drawChar = 'o';
                }

                if (RAND.nextDouble() < 0.06) drawChar = FADE_POOL[RAND.nextInt(FADE_POOL.length)];

                synchronized (OUT_LOCK) {
                    TerminalUtils.moveCursor(x, y);
                    if (colorMode) {
                        String color = ANSI_COLORS[RAND.nextInt(ANSI_COLORS.length)];
                        System.out.print(color + drawChar + ANSI_RESET);
                    } else {
                        System.out.print(drawChar);
                    }
                }

                newPositions.add(encodePos(x, y));
            }

            synchronized (OUT_LOCK) { System.out.flush(); }
            TerminalUtils.sleepMs(ConfigLoader.frameDelay);

            // Clear old positions that are not present in newPositions
            Set<Long> toClear = new HashSet<>(prevPositions);
            toClear.removeAll(newPositions);
            if (!toClear.isEmpty()) {
                clearPositions(toClear);
            }
            prevPositions = newPositions;

            if (prevPositions.isEmpty()) break;
        }

        // final sparkle
        finalSparkle(cx, cy, Math.max(1, maxR / 3), colorMode);

        if (!prevPositions.isEmpty()) clearPositions(prevPositions);
    }

    private static void finalSparkle(int cx, int cy, int rRad, boolean colorMode) {
        Set<Long> drawn = new HashSet<>();
        String color = colorMode ? ANSI_COLORS[RAND.nextInt(ANSI_COLORS.length)] : "";
        for (int rr = cx - rRad; rr <= cx + rRad; rr++) {
            for (int cc = cy - rRad * 2; cc <= cy + rRad * 2; cc++) {
                if (rr < 1 || rr > TerminalUtils.getRows() || cc < 1 || cc > TerminalUtils.getCols()) continue;
                double dy = rr - cx;
                double dx = (cc - cy) * 0.5;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist <= rRad && RAND.nextDouble() < 0.35) {
                    synchronized (OUT_LOCK) {
                        TerminalUtils.moveCursor(rr, cc);
                        if (colorMode) System.out.print(color + '*' + ANSI_RESET);
                        else System.out.print('*');
                    }
                    drawn.add(encodePos(rr, cc));
                }
            }
        }
        synchronized (OUT_LOCK) { System.out.flush(); }
        TerminalUtils.sleepMs(Math.max(30, ConfigLoader.frameDelay / 2));
        if (!drawn.isEmpty()) clearPositions(drawn);
    }

    private static void clearPositions(Set<Long> positions) {
        synchronized (OUT_LOCK) {
            for (Long e : positions) {
                int r = (int) (e >> 32);
                int c = (int) (e & 0xffffffffL);
                if (r < 1 || r > TerminalUtils.getRows() || c < 1 || c > TerminalUtils.getCols()) continue;
                TerminalUtils.moveCursor(r, c);
                System.out.print(' ');
            }
            System.out.flush();
        }
    }

    private static long encodePos(int r, int c) {
        return (((long) r) << 32) | (c & 0xffffffffL);
    }
}
