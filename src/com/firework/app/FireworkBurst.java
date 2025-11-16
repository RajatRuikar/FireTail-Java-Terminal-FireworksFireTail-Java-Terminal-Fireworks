package com.firework.app;

public class FireworkBurst implements Runnable {
    @Override
    public void run() {
        try {
            FireworkRenderer.runBurstWithTail(ConfigLoader.colorMode);
        } catch (Exception e) {
            // swallow exceptions for individual bursts to avoid crashing manager
        }
    }
}
	