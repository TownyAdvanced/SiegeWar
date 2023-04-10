package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
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
            return SiegeWarDistanceUtil.areLocationsCloseHorizontally(locationInTown, siege.getFlagLocation(), SiegeWarSettings.getBesiegedTownTrapWarfareMitigationRadius());
        } else {
            return false;
        }
    }

    /**
     * Determine if the target wilderness location is protected by trap warfare mitigation
     *
     * @param wildernessLocation target location
     * @param nearestSiege the nearest siege
     */
    public static boolean isWildernessLocationProtectedByTrapWarfareMitigation(Location wildernessLocation, @NotNull Siege nearestSiege) {
        int protectionRadiusBlocks = SiegeWarSettings.getWildernessTrapWarfareMitigationRadiusBlocks();
        int upperHeightLimit = SiegeWarSettings.getWildernessTrapWarfareMitigationUpperHeightLimit();
        int lowerHeightLimit = SiegeWarSettings.getWildernessTrapWarfareMitigationLowerHeightLimit();
        Location siegeBannerLocation = nearestSiege.getFlagLocation();
        return isWildernessLocationProtectedByTrapWarfareMitigation(
                wildernessLocation,
                siegeBannerLocation,
                protectionRadiusBlocks,
                upperHeightLimit,
                lowerHeightLimit);
    }

    /**
     * Determine if the target location is protected by trap warfare mitigation
     *
     * @param targetLocation target location
     * @param siegeBannerLocation location of nearby siege banner
     * @param protectionRadiusBlocks protection radius in blocks
     * @param upperHeightLimit cannot alter above this
     * @param lowerHeightLimit cannot alter below this
     *
     * @return true if the location is protected
     */
    public static boolean isWildernessLocationProtectedByTrapWarfareMitigation(Location targetLocation, Location siegeBannerLocation, int protectionRadiusBlocks, int upperHeightLimit, int lowerHeightLimit) {
        if(targetLocation.getY() <= siegeBannerLocation.getY() + upperHeightLimit
                && targetLocation.getY() >= siegeBannerLocation.getY() + lowerHeightLimit) {
            return false;  //Not high/low enough for protection
        } else if(SiegeWarDistanceUtil.areLocationsCloseHorizontally(targetLocation, siegeBannerLocation, protectionRadiusBlocks)) {
            return true;   //Target location is protected
        } else {
            return false;  //Target location is not protected
        }
    }

}
