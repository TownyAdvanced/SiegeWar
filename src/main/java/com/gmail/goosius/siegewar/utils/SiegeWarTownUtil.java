package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.TownBlock;

import java.util.Set;

/**
 * Util class containing methods related to town flags/permssions.
 */
public class SiegeWarTownUtil {

	/**
	 * Set PVP in all plots in a siegezone
	 *
	 * @param siege the siege
	 * @param desiredSetting the desired setting for the pvp flag
	 */
	public static void setPvpInAllPlotsInSiegeZone(Siege siege, boolean desiredSetting) {
		Set<TownBlock> nearbyPlots = SiegeWarDistanceUtil.getTownBlocksInSiegeZone(siege);
		for(TownBlock plot: nearbyPlots) {
			if (plot.getPermissions().pvp != desiredSetting) {
				plot.getPermissions().pvp = desiredSetting;
				plot.save();
			}
		}
	}

}