package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SiegeWarTownyDynmapListener implements Listener {

    @SuppressWarnings("unused")
    private final SiegeWar plugin;

    public SiegeWarTownyDynmapListener(SiegeWar instance) {
        plugin = instance;
    }

    /**
     * This method updates the town popup box on Dynmap-Towny
     *
     * 1. It looks for the %occupier% tag in the popup
     * 2. If the %occupier% tag exists, it replaces it with the occupier name (blank for no occupier)
     */

    /*
//    TODO - ENABLE ME WHEN THE REQUIRED TOWNY CODE GETS ON MAVEN
    @EventHandler
    public void on(BuildTownMarkerDescriptionEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled()) {
            if(event.getDescription().contains("%occupier%")) {
                String finalDescription;
                if(TownOccupationController.isTownOccupied(event.getTown()) {
                    finalDescription = event.getDescription().replace("%occupier%", TownOccupationController.getTownOccupier(town).getName());
                } else {
                    finalDescription = event.getDescription().replace("%occupier%", "");
                }
                event.setDescription(finalDescription);
            }
        }
    }
    */
     
}
