package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.TimeMgmt;

/**
 * This class contains utility functions related siege timings
 * 
 * @author Goosius
 */
public class SiegeWarTimeUtil {

	/**
	 * Activate the siege immunity timer after a siege ends
	 * 
	 * While this timer is active, the town cannot be attacked
	 * 
	 * @param town the town
	 * @param siege the siege which was previously in progress
	 */
	public static void activateSiegeImmunityTimer(Town town, Siege siege) {
        double siegeDuration = siege.getActualEndTime() - siege.getStartTime();
        double cooldownDuration = siegeDuration * SiegeWarSettings.getWarSiegeSiegeImmunityTimeModifier();
        TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + (long)(cooldownDuration + 0.5));
    }

	/**
	 * Activate the revolt immunity timer for a town
	 *
	 * While this timer is active, the town cannot start a revolt siege.
     * When this timer hits 0, the town can start a revolt siege.
	 *
	 * Note:
	 * Siege immunity does not block revolts;
	 * only revolt immunity blocks revolts.
	 *
	 * @param town the town
	 */
	public static void activateRevoltImmunityTimer(Town town) {
		long siegeImmunityDurationMillis = TownMetaDataController.getSiegeImmunityEndTime(town) - System.currentTimeMillis();
		long revoltImmunityDurationMillis = (long)(siegeImmunityDurationMillis * SiegeWarSettings.getWarSiegeRevoltImmunityTimeModifier());
		long revoltImmunityEndTime = System.currentTimeMillis() + revoltImmunityDurationMillis;
		TownMetaDataController.setRevoltImmunityEndTime(town, revoltImmunityEndTime);
    }
}
