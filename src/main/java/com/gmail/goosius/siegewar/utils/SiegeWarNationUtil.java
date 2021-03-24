package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.ArrayList;

public class SiegeWarNationUtil {

    /**
     * Calculate the 'effective' nation,
     *
     * The following towns are included in the effective nation:
     * 1. All home towns which are unoccupied.
     * 2. All foreign towns which are occupied by the nation.
     *
     * @return effective nation, containing all required towns
     */
    public static Nation getEffectiveNation(Nation nation) {
        //Create an 'effective nation' object
        Nation effectiveNation = new Nation("effectiveNationDummyName");

        //Count all home towns which are unoccupied;
        for(Town town: new ArrayList<>(nation.getTowns())) {
            if(!TownOccupationController.isTownOccupied(town)) {
                effectiveNation.addTown(town);
            }
        }

        //Count all foreign towns which the nation has occupied
        for(Town town: TownOccupationController.getOccupiedForeignTowns(nation)) {
            effectiveNation.addTown(town);
        }

        return effectiveNation;
    }
}
