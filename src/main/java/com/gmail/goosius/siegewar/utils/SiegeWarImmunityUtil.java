package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * This class contains utility functions related to siege/revolt immunity
 * 
 * @author Goosius
 */
public class SiegeWarImmunityUtil {

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
		town.save();
    }

    /**
     * The siege ended.
     *
     * 1. Grant siege immunity to the town which was besieged.
     * 2. If the town was the defender,
     *    grant siege immunity to any nations who were the home nation of the town during the siege
     *
     * @param town the town which was besieged
     * @param siege the siege
     */
    public static void grantSiegeImmunityAfterEndedSiege(Town town, Siege siege) {
        //Grant siege immunity to town
        double siegeDurationMillis = siege.getActualEndTime() - siege.getStartTime();
        double immunityDurationMillisDouble = siegeDurationMillis * SiegeWarSettings.getWarSiegeSiegeImmunityTimeModifier();
        long immunityDurationMillis = (long)(immunityDurationMillisDouble + 0.5);
        TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + immunityDurationMillis);
        town.save();
    }

	public static void evaluateExpiredImmunities() {
		final long olderThanAnHour = System.currentTimeMillis() - 3600000;
		for (Town town : new ArrayList<>(TownyUniverse.getInstance().getTowns())) {
			long expirationTime = TownMetaDataController.getSiegeImmunityEndTime(town);
			// Expiration happened longer than an hour ago or MetaData returned 0l.
			if (expirationTime < olderThanAnHour)
				continue;
			// Expired in the last hour.
			if (expirationTime < System.currentTimeMillis())
				TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_town_immunity_expired"));
		}
	}
	
	public static boolean isTownSiegeImmune(Town town) {
		return System.currentTimeMillis() < TownMetaDataController.getSiegeImmunityEndTime(town)
		|| TownMetaDataController.getSiegeImmunityEndTime(town) == -1l;
	}
}
