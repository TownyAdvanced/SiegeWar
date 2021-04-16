package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

/**
 * This class contains utility functions related siege timings
 * 
 * @author Goosius
 */
public class SiegeWarTimeUtil {

	/**
	 * Activate the siege immunity timers after a siege ends
	 * - The besieged town always gets it
	 * - Home nation towns might also get it (if all-nation-sieges are enabled)
	 *
	 * If a town siege immunity timer is active, the town cannot be attacked.
	 * Note however that siege immunity does not prevent the mayor starting a revolt siege.
	 *
	 * @param town the town under siege
	 * @param siege the siege which was previously active
	 */
	public static void activateSiegeImmunityTimers(Town town, Siege siege) {
		//Set siege immunity for town
        double siegeDuration = siege.getActualEndTime() - siege.getStartTime();
        double immunityDurationMillisDouble = siegeDuration * SiegeWarSettings.getWarSiegeSiegeImmunityTimeModifier();
        long immunityDurationMillis = (long)(immunityDurationMillisDouble + 0.5);
        TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + immunityDurationMillis);

		//Set siege immunity for nation home towns
		if(SiegeWarSettings.isPostWarNationImmunityEnabled() && town.hasNation()) {
			double homeTownSiegeImmunityDurationDouble = (double)immunityDurationMillis * SiegeWarSettings.getAllNationSiegesSiegeImmunityModifier();
			long homeTownSiegeImmunityDurationLong = (long)(homeTownSiegeImmunityDurationDouble + 0.5);
			long homeTownSiegeImmunityEndTime = System.currentTimeMillis() + homeTownSiegeImmunityDurationLong;

			for(Town nationTown: TownyAPI.getInstance().getTownNationOrNull(town).getTowns()) {
				if(TownMetaDataController.getSiegeImmunityEndTime(nationTown) < homeTownSiegeImmunityEndTime) {
					TownMetaDataController.setSiegeImmunityEndTime(nationTown, homeTownSiegeImmunityEndTime);
				}
			}
		}
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
