package com.gmail.goosius.siegewar.integration.dynmap;

import org.bukkit.scheduler.BukkitRunnable;

public class DynmapTask extends BukkitRunnable {
    private final DynmapIntegration dynmapIntegration;

    DynmapTask(DynmapIntegration dynmapIntegration) {
        this.dynmapIntegration = dynmapIntegration;
    }

    public void run() {
        dynmapIntegration.displaySieges();
    }
}
