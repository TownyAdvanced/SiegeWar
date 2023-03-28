package com.gmail.goosius.siegewar.utils;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;

public class SiegeWarNationUtil {

    public static boolean doesNationHaveTooManyTowns(Nation nation) {
        return TownySettings.getMaxTownsPerNation() > 0 && nation.getNumTowns() >= TownySettings.getMaxTownsPerNation();
    }

}
