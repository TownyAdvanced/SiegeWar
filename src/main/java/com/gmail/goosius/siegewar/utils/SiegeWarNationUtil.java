package com.gmail.goosius.siegewar.utils;

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
}
