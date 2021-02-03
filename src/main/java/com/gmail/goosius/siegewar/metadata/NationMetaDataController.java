package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;

public class NationMetaDataController {
    @SuppressWarnings("unused")
    private SiegeWar plugin;

    private static IntegerDataField lifetimeWins = new IntegerDataField("siegewar_lifetimewins", 0);
    private static IntegerDataField lifetimeLosses = new IntegerDataField("siegewar_lifetimelosses", 0);
    private static IntegerDataField nationsDefeated = new IntegerDataField("siegewar_nationsdefeated", 0);

    public NationMetaDataController(SiegeWar plugin) {
        this.plugin = plugin;
    }

    public static int getLifetimeWins(Nation nation) {
        IntegerDataField idf = (IntegerDataField) lifetimeWins.clone();
        if (nation.hasMeta(idf.getKey()))
            return MetaDataUtil.getInt(nation, idf);
        else
            return 0;
    }

    public static int getLifetimeLosses(Nation nation) {
        IntegerDataField idf = (IntegerDataField) lifetimeLosses.clone();
        if (nation.hasMeta(idf.getKey()))
            return MetaDataUtil.getInt(nation, idf);
        else
            return 0;
    }

    public static int getNationsDefeated(Nation nation) {
        IntegerDataField idf = (IntegerDataField) nationsDefeated.clone();
        if (nation.hasMeta(idf.getKey()))
            return MetaDataUtil.getInt(nation, idf);
        else
            return 0;
    }

    public static void setLifetimeWins(Nation nation, int num) {
        IntegerDataField idf = (IntegerDataField) lifetimeWins.clone();
        if (nation.hasMeta(idf.getKey()))
            if (num == 0)
                nation.removeMetaData(idf);
            else
                MetaDataUtil.setInt(nation, idf, num);
        else if (num != 0)
            nation.addMetaData(new IntegerDataField("siegewar_lifetimewins", num));
    }

    public static void setLifetimeLosses(Nation nation, int num) {
        IntegerDataField idf = (IntegerDataField) lifetimeLosses.clone();
        if (nation.hasMeta(idf.getKey()))
            if (num == 0)
                nation.removeMetaData(idf);
            else
                MetaDataUtil.setInt(nation, idf, num);
        else if (num != 0)
            nation.addMetaData(new IntegerDataField("siegewar_lifetimelosses", num));
    }

    public static void setNationsDefeated(Nation nation, int num) {
        IntegerDataField idf = (IntegerDataField) nationsDefeated.clone();
        if (nation.hasMeta(idf.getKey()))
            if (num == 0)
                nation.removeMetaData(idf);
            else
                MetaDataUtil.setInt(nation, idf, num);
        else if (num != 0)
            nation.addMetaData(new IntegerDataField("siegewar_nationsdefeated", num));
    }

    /**
     * Adds a win or a loss to a nation
     * 
     * @param nation The nation to update.
     * @param siegeWon Whether the nation won or lost.
     */
    public static void addWinOrLoss(Nation nation, boolean siegeWon) {
        if (siegeWon)
            setLifetimeWins(nation, getLifetimeWins(nation) + 1);
        else
            setLifetimeLosses(nation, getLifetimeLosses(nation) + 1);
    }

    public static void incrementDefeatedNations(Nation nation) {
        setNationsDefeated(nation, getNationsDefeated(nation) + 1);
        }
}
