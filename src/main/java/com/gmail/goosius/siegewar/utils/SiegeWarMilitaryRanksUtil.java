package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import java.util.ArrayList;

public class SiegeWarMilitaryRanksUtil {
    
    public static void removeMilitaryRanksFromTownResidents(Town town) {
        for(Resident townResident: town.getResidents()) {
            //Remove town ranks
            for (String townRank : new ArrayList<>(townResident.getTownRanks())) {
                if (PermissionUtil.doesTownRankAllowPermissionNode(townRank, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS)) {
                    townResident.removeTownRank(townRank);
                }
            }
            //Remove nation ranks
            for (String nationRank : new ArrayList<>(townResident.getNationRanks())) {
                if (PermissionUtil.doesNationRankAllowPermissionNode(nationRank, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS)) {
                    townResident.removeNationRank(nationRank);
                }
            }
        }
    }
}
