package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Translatable;

public class SiegeWarNationUtil {

	public static void throwIfNationHasTooManyTowns(Nation nation) throws TownyException {
		if (doesNationHaveTooManyTowns(nation))
			throw new TownyException(Translatable.of("msg_err_nation_over_town_limit", TownySettings.getMaxTownsPerNation()));
	}

	private static boolean doesNationHaveTooManyTowns(Nation nation) {
		return TownySettings.getMaxTownsPerNation() > 0 && nation.getNumTowns() >= TownySettings.getMaxTownsPerNation();
	}

	public static int getDemoralizationDaysLeft(Nation nation) {
		return NationMetaDataController.getDemoralizationDaysLeft(nation);
	}

	public static void setDemoralizationDays(Nation nation, int demoralizationDays) {
		NationMetaDataController.setDemoralizationDays(nation, demoralizationDays);
	}

	public static void removeDemoralizationDays(Nation nation) {
		NationMetaDataController.removeDemoralizationDays(nation);
	}

	public static int getDemoralizationAmount(Nation nation) {
		return NationMetaDataController.getDemoralizationAmount(nation);
	}

	public static void setDemoralizationAmount(Nation nation, int demoralizationAmount) {
		NationMetaDataController.setDemoralizationAmount(nation, demoralizationAmount);
	}

	public static void removeDemoralizationAmount(Nation nation) {
		NationMetaDataController.removeDemoralizationAmount(nation);
	}

	public static void updateNationDemoralizationCounters() {
		int demoralizationDaysLeft;
		for(Nation nation: TownyAPI.getInstance().getNations()) {
			demoralizationDaysLeft = getDemoralizationDaysLeft(nation);
			if(demoralizationDaysLeft > 1) {
				//Reduce the counter by 1
				demoralizationDaysLeft--;
				setDemoralizationDays(nation, demoralizationDaysLeft);
			} else {
				//End demoralization
				removeDemoralizationAmount(nation);
				removeDemoralizationDays(nation);
			}
		}
	}
}
