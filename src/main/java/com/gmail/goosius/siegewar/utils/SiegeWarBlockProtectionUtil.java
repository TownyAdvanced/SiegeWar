package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;

public class SiegeWarBlockProtectionUtil {

    /**
     * Determine if the event qualifies for besieged town trap warfare mitigation
     * 
     * @param townLocation the location to check. We know it is in a town
     * @return true if the event qualifies
     */
    public static boolean isTownLocationProtectedByBesiegedTownTrapWarfareMitigation(Location townLocation) {
        Town town = TownyAPI.getInstance().getTown(townLocation);
        Siege siege = SiegeController.getSiege(town);
        if(siege != null && siege.getStatus().isActive()) {
            int protectionRadiusBlocks = SiegeWarSettings.getBesiegedTownTrapWarfareMitigationRadius();
            Location siegeBannerLocation = siege.getFlagLocation();
            return SiegeWarDistanceUtil.areLocationsCloseHorizontally(townLocation, siegeBannerLocation, protectionRadiusBlocks);   //Target location is protected
        } else {
            return false;
        }
    }

}
