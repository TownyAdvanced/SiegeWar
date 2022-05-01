package com.gmail.goosius.siegewar.integration.maptowny;

import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapTownyShortTimeListener implements Listener {

    private final Runnable renderSieges;

    public MapTownyShortTimeListener(Runnable renderSieges) {
        this.renderSieges = renderSieges;
    }

    @EventHandler
    public void onNewShortTime(NewShortTimeEvent event) {
        this.renderSieges.run();
    }
}
