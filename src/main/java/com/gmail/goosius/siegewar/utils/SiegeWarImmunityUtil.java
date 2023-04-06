package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.util.TimeMgmt;

import java.util.ArrayList;

/**
 * This class contains utility functions related to siege/revolt immunity
 * 
 * @author Goosius
 */
public class SiegeWarImmunityUtil {

	/**
	 * Grant revolt immunity to the town which was besieged.
	 *
	 * @param town the town which was besieged
	 */
	public static void grantRevoltImmunityAfterEndedSiege(Town town) {
		long revoltImmunityDurationMillis = (long)(SiegeWarSettings.getRevoltImmunityPostSiegeHours() * 3600000) ;
		TownMetaDataController.setRevoltImmunityEndTime(town, System.currentTimeMillis() + revoltImmunityDurationMillis);
		town.save();
	}

    /**
     * Grant siege immunity to the town which was besieged.
     *
     * @param town the town which was besieged
     */
    public static void grantSiegeImmunityAfterEndedSiege(Town town) {
        long siegeImmunityDurationMillis = (long)(SiegeWarSettings.getSiegeImmunityPostSiegeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS) ;
        TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + siegeImmunityDurationMillis);
        town.save();
    }

	public static void evaluateExpiredImmunities() {
		final long olderThanAnHour = (long)(System.currentTimeMillis() - TimeMgmt.ONE_HOUR_IN_MILLIS);
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
