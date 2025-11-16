package com.firework.app;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public static String wishMessage = "Happy Diwali";
    public static int wishDisplayTime = 4;

    public static int fireFrom = 5;
    public static int fireTo = 9;
    public static int totalFireCount = 200;
    public static int totalFireTime = 60;

    public static boolean colorMode = true;
    public static int maxRadius = 10;
    public static int frameDelay = 60;
    public static int particles = 48;

    // Tail
    public static boolean tailEnabled = true;
    public static int tailSpeed = 40;
    public static int tailLength = 12;

    // Terminal override
    public static int terminalRows = 0;
    public static int terminalCols = 0;
    
    public static int fontSize;

    public static void loadConfig() {
        Properties p = new Properties();
        // try resources/config.properties relative to working dir
        try (InputStream is = new FileInputStream("resources/config.properties")) {
            p.load(is);
        } catch (Exception e) {
            // fallback try classpath resource
            try (InputStream is2 = ConfigLoader.class.getResourceAsStream("/config.properties")) {
                if (is2 != null) p.load(is2);
            } catch (Exception ignored) {}
        }

        try {
            wishMessage = p.getProperty("wish.message", wishMessage);
            wishDisplayTime = Integer.parseInt(p.getProperty("wish.display.time", String.valueOf(wishDisplayTime)));

            fireFrom = Integer.parseInt(p.getProperty("firework.from", String.valueOf(fireFrom)));
            fireTo = Integer.parseInt(p.getProperty("firework.to", String.valueOf(fireTo)));
            totalFireCount = Integer.parseInt(p.getProperty("firework.total.count", String.valueOf(totalFireCount)));
            totalFireTime = Integer.parseInt(p.getProperty("firework.total.time", String.valueOf(totalFireTime)));

            colorMode = Boolean.parseBoolean(p.getProperty("firework.color.mode", String.valueOf(colorMode)));
            maxRadius = Integer.parseInt(p.getProperty("firework.max.radius", String.valueOf(maxRadius)));
            frameDelay = Integer.parseInt(p.getProperty("firework.frame.delay", String.valueOf(frameDelay)));
            particles = Integer.parseInt(p.getProperty("firework.particles", String.valueOf(particles)));

            tailEnabled = Boolean.parseBoolean(p.getProperty("firework.tail.enabled", String.valueOf(tailEnabled)));
            tailSpeed = Integer.parseInt(p.getProperty("firework.tail.speed", String.valueOf(tailSpeed)));
            tailLength = Integer.parseInt(p.getProperty("firework.tail.length", String.valueOf(tailLength)));

            terminalRows = Integer.parseInt(p.getProperty("terminal.rows", String.valueOf(terminalRows)));
            terminalCols = Integer.parseInt(p.getProperty("terminal.cols", String.valueOf(terminalCols)));
            fontSize = Integer.parseInt(p.getProperty("terminal.font.size", "18"));
        } catch (Exception ex) {
            System.err.println("Warning: invalid config value; using defaults. " + ex.getMessage());
        }
    }
}
