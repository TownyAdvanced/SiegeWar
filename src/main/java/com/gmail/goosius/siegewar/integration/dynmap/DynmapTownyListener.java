package com.gmail.goosius.siegewar.integration.dynmap;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import org.dynmap.towny.events.BuildTownMarkerDescriptionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DynmapTownyListener implements Listener {
    /**
     * This method updates the town popup box on Dynmap-Towny
     *
     * 1. It looks for the %occupier% tag in the popup
     * 2. If the %occupier% tag exists, it replaces it with the occupier name (or blank if there is no occupier)
     * 
     * As of SW2.0.0, this method is depreciated, because the town's occupier will always be the same as the town's normal nation
     */
    @Deprecated
    @EventHandler
    public void on(BuildTownMarkerDescriptionEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled() && event.getDescription().contains("%occupier%")) {
            event.setDescription(event.getDescription().replace("%occupier%", TownOccupationController.isTownOccupied(event.getTown())
                    ? TownOccupationController.getTownOccupier(event.getTown()).getName()
                    : ""));
        }
    }
}
