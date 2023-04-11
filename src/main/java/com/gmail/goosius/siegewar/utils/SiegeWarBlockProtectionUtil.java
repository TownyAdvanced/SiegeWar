package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SiegeWarBlockProtectionUtil {
	private static int WILDERNESS_PROTECTION_RADIUS;
	private static int WILDERNESS_UPPER_HEIGHT;
	private static int WILDERNESS_LOWER_HEIGHT;

	public SiegeWarBlockProtectionUtil() {
		WILDERNESS_PROTECTION_RADIUS = SiegeWarSettings.getWildernessTrapWarfareMitigationRadiusBlocks();
		WILDERNESS_UPPER_HEIGHT = SiegeWarSettings.getWildernessTrapWarfareMitigationUpperHeightLimit();
		WILDERNESS_LOWER_HEIGHT = SiegeWarSettings.getWildernessTrapWarfareMitigationLowerHeightLimit();
	}

    /**
     * Determine if the town location qualifies for besieged-town-trap-warfare-mitigation
     * 
     * @param locationInTown the location to check in the town
     * @param town the town
     * 
     * @return true if the location is protected
     */
    public static boolean isTownLocationProtectedByTrapWarfareMitigation(Location locationInTown, @NotNull Town town) {
        if (!SiegeWarSettings.isBesiegedTownTownTrapWarfareMitigationEnabled())
            return false;

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
        if (!SiegeWarSettings.isWildernessTrapWarfareMitigationEnabled())
            return false;

        return isWildernessLocationProtectedByTrapWarfareMitigation(wildernessLocation, nearestSiege.getFlagLocation());
    }

    /**
     * Determine if the target location is protected by trap warfare mitigation
     *
     * @param targetLocation target location
     * @param siegeBannerLocation location of nearby siege banner
     *
     * @return true if the location is protected
     */
    public static boolean isWildernessLocationProtectedByTrapWarfareMitigation(Location targetLocation, Location siegeBannerLocation) {
        if(targetLocation.getY() <= siegeBannerLocation.getY() + WILDERNESS_UPPER_HEIGHT
                && targetLocation.getY() >= siegeBannerLocation.getY() + WILDERNESS_LOWER_HEIGHT) {
            return false;  //Not high/low enough for protection
        } else if(SiegeWarDistanceUtil.areLocationsCloseHorizontally(targetLocation, siegeBannerLocation, WILDERNESS_PROTECTION_RADIUS)) {
            return true;   //Target location is protected
        } else {
            return false;  //Target location is not protected
        }
    }

}
