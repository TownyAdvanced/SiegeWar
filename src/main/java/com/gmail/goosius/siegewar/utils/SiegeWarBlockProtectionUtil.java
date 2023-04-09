package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SiegeWarBlockProtectionUtil {

    /**
     * Determine if the town location qualifies for besieged-town-trap-warfare-mitigation
     * 
     * @param locationInTown the location to check in the town
     * @param town the town
     * 
     * @return true if the location is protected
     */
    public static boolean isTownLocationProtectedByTrapWarfareMitigation(Location locationInTown, @NotNull Town town) {
        Siege siege = SiegeController.getSiege(town);
        if(siege != null && siege.getStatus().isActive()) {
            int protectionRadiusBlocks = SiegeWarSettings.getBesiegedTownTrapWarfareMitigationRadius();
            Location siegeBannerLocation = siege.getFlagLocation();
            return SiegeWarDistanceUtil.areLocationsCloseHorizontally(locationInTown, siegeBannerLocation, protectionRadiusBlocks);   //Target location is protected
        } else {
            return false;
        }
    }

    /**
     * Determine if the location qualifies for wilderness-trap-warfare-mitigation
     *
     * @param wildernessLocation the location to check. We know it is in the wilderness
     * @param nearbySiege the nearest siege.
     *                    
     * @return true if the event qualifies
     */
    public static boolean isWildernessLocationProtectedByTrapWarfareMitigation(Location wildernessLocation, @NotNull Siege nearbySiege) {
        int protectionRadiusBlocks = SiegeWarSettings.getWildernessTrapWarfareMitigationRadiusBlocks();
        int upperHeightLimit = SiegeWarSettings.getWildernessTrapWarfareMitigationUpperHeightLimit();
        int lowerHeightLimit = SiegeWarSettings.getWildernessTrapWarfareMitigationLowerHeightLimit();
        Location siegeBannerLocation = nearbySiege.getFlagLocation();

        if(wildernessLocation.getY() <= siegeBannerLocation.getY() + upperHeightLimit
                && wildernessLocation.getY() >= siegeBannerLocation.getY() + lowerHeightLimit) {
            return false;  //Not high/low enough for protection
        } else if(SiegeWarDistanceUtil.areLocationsCloseHorizontally(wildernessLocation, siegeBannerLocation, protectionRadiusBlocks)) {
            return true;   //Target location is protected
        } else {
            return false;  //Target location is not protected
        }
     }

}
