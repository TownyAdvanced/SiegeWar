package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.ArrayList;
import java.util.Map;

public class SiegeWarNationUtil {

    /**
     * Calculate the 'effective' nation level
     *
     * This is calculated as follows:
     * 1. Count all the residents from home towns which are unoccupied.
     * 2. Plus all the residents from foreign towns which are unoccupied.
     *
     * @return effective nation level
     */
    public static  Map<TownySettings.NationLevel, Object> calculateEffectiveNationLevel(Nation nation) {
        int effectNumberOfResidents = 0;

        //Count all home towns which are unoccupied;
        for(Town town: new ArrayList<>(nation.getTowns())) {
            if(!TownOccupationController.isTownOccupied(town)) {
                effectNumberOfResidents += town.getNumResidents();
            }
        }

        //Count all foreign towns which the nation has occupied
        for(Town town: TownOccupationController.getTownsOccupiedByNation(nation)) {
            effectNumberOfResidents += town.getNumResidents();
        }

        //Return effective nation level
        return TownySettings.getNationLevel(effectNumberOfResidents);
    }

    public static int calculateEffectiveNumberOfTownsInNation(Nation nation) {
        int effectiveNumber = 0;
        effectiveNumber += nation.getNumTowns();
        effectiveNumber += TownOccupationController.getTownsOccupiedByNation(nation).size();
        return effectiveNumber;
    }
}
