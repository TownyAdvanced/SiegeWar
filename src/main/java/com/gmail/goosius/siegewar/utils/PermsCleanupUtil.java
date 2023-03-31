package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.permissions.TownyPerms;

import java.util.List;

public class PermsCleanupUtil {

    public static void cleanupPerms(boolean siegeWarPluginError) {
        if(siegeWarPluginError) {
            SiegeWar.severe("SiegeWar is in safe mode. Data cleanup not attempted.");
        }
        CommentedConfiguration townyPermsFile = TownyPerms.getTownyPermsFile();
        boolean success = deleteMilitaryPermsFromSheriffRank(townyPermsFile);
        if(success) {
            townyPermsFile.save();
            SiegeWar.info("Perms Cleanup Complete");
        }
    }
    
    private static boolean deleteMilitaryPermsFromSheriffRank(CommentedConfiguration townyPermsFile) {
        boolean success = false;
        List<String> groupNodes;
        String townpoints = "siegewar.town.siege.battle.points";
        // Add nodes to the sheriff rank.
        if (TownyPerms.mapHasGroup("towns.ranks.sheriff")) {
            groupNodes = TownyPerms.getPermsOfGroup("towns.ranks.sheriff");
            if (groupNodes.contains(townpoints)) {
                groupNodes.remove(townpoints);
                success = true;
            }
            if (groupNodes.contains("towny.command.town.rank.guard")) {
                groupNodes.remove("towny.command.town.rank.guard");
                success = true;
            }
            if(success) {
                townyPermsFile.set("towns.ranks.sheriff", groupNodes);
            }
        }
        return success;
    }
}
