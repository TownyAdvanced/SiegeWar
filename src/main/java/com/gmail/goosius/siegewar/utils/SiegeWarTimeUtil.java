package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Town;

/**
 * This class contains utility functions related siege timings
 * 
 * @author Goosius
 */
public class SiegeWarTimeUtil {

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
