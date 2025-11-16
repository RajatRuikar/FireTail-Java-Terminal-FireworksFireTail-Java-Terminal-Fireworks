package com.firework.app;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FireworkManager {
    private static final Random RAND = new Random();

    public static void startShow() {
        long start = System.currentTimeMillis();
        long endTime = start + ConfigLoader.totalFireTime * 1000L;
        int totalAllowed = ConfigLoader.totalFireCount;

        ExecutorService pool = Executors.newCachedThreadPool();

        int fired = 0;
        while (true) {
            long now = System.currentTimeMillis();
            if (now >= endTime) break;
            if (fired >= totalAllowed) break;

            int min = Math.max(1, ConfigLoader.fireFrom);
            int max = Math.max(min, ConfigLoader.fireTo);
            int spawn = min + RAND.nextInt(max - min + 1);

            for (int i = 0; i < spawn && fired < totalAllowed; i++) {
                pool.submit(new FireworkBurst());
                fired++;
            }

            // sleep to make this roughly per-second batches; we subdivide the second a bit
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        pool.shutdown();
        try {
            pool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }
}
