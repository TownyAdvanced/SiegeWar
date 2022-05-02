package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.ArrayList;
import java.util.Comparator;

public class SiegeWarNationUtil {

    public static final Comparator<Nation> BY_NUM_RESIDENTS = (n1, n2) -> {
        return getEffectiveNation(n2).getResidents().size() 
                     - getEffectiveNation(n1).getResidents().size();
    };

    public static final Comparator<Nation> BY_NUM_TOWNS = (n1, n2) -> {
        return getEffectiveNation(n2).getNumTowns() 
                     - getEffectiveNation(n1).getNumTowns();
    };

    public static final Comparator<Nation> BY_NUM_TOWNBLOCKS = (n1, n2) -> {
        return getEffectiveNation(n2).getNumTownblocks() 
                     - getEffectiveNation(n1).getNumTownblocks();
    };

    public static final Comparator<Nation> BY_NUM_ONLINE_PLAYERS = (n1, n2) -> {    
        return TownyAPI.getInstance().getOnlinePlayers(getEffectiveNation(n2)).size() 
               - TownyAPI.getInstance().getOnlinePlayers(getEffectiveNation(n1)).size();
    };

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

        //Count all home towns which are unoccupied or occupied by the nation;
        for(Town town: new ArrayList<>(nation.getTowns())) {
            if(!TownOccupationController.isTownOccupied(town) || TownOccupationController.getTownOccupier(town) == nation) {
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
