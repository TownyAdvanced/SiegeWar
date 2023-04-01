package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.permissions.TownyPerms;

import java.util.List;

public class PermsCleanupUtil {

    public static void cleanupPerms(boolean siegeWarPluginError) {
        if(siegeWarPluginError) {
            SiegeWar.severe("SiegeWar is in safe mode. Perms cleanup not attempted.");
            return;
        }
        CommentedConfiguration townyPermsFile = TownyPerms.getTownyPermsFile();
        boolean success = false;
        success = deleteMilitaryPermsFromSheriffRank(townyPermsFile, success);
        success = deleteFireCannonPermFromGuardRank(townyPermsFile, success);
        success = deleteGunnerAndEngineerRanks(townyPermsFile, success);

        if(success) {
            townyPermsFile.save();
            SiegeWar.info("Perms Cleanup Complete");
        }
    }
    
    private static boolean deleteMilitaryPermsFromSheriffRank(CommentedConfiguration townyPermsFile, boolean success) {
        List<String> groupNodes;
        // Deleted nodes from the sheriff rank.
        if (TownyPerms.mapHasGroup("towns.ranks.sheriff")) {
            groupNodes = TownyPerms.getPermsOfGroup("towns.ranks.sheriff");
            if (groupNodes.contains("siegewar.town.siege.battle.points")) {
                success = groupNodes.remove("siegewar.town.siege.battle.points");
            }
            if (groupNodes.contains("siegewar.town.siege.fire.cannon.in.siegezone")) {
                success = groupNodes.remove("siegewar.town.siege.fire.cannon.in.siegezone");
            }
            if (groupNodes.contains("towny.command.town.rank.guard")) {
                success = groupNodes.remove("towny.command.town.rank.guard");
            }
            if(success) {
                townyPermsFile.set("towns.ranks.sheriff", groupNodes);
            }
        }
        return success;
    }

    private static boolean deleteFireCannonPermFromGuardRank(CommentedConfiguration townyPermsFile, boolean success) {
        List<String> groupNodes;
        // Delete nodes from the guard rank.
        if (TownyPerms.mapHasGroup("towns.ranks.guard")) {
            groupNodes = TownyPerms.getPermsOfGroup("towns.ranks.guard");
            if (groupNodes.contains("siegewar.town.siege.fire.cannon.in.siegezone")) {
                success = groupNodes.remove("siegewar.town.siege.fire.cannon.in.siegezone");
            }
            if (success) {
                townyPermsFile.set("towns.ranks.guard", groupNodes);
            }
        }
        return success;
    }
    
    private static boolean deleteGunnerAndEngineerRanks(CommentedConfiguration townyPermsFile, boolean success) {
        if (TownyPerms.mapHasGroup("nations.ranks.gunner")) {
            townyPermsFile.set("nations.ranks.gunner", null);
            success = true;
        }
        if (TownyPerms.mapHasGroup("nations.ranks.engineer")) {
            townyPermsFile.set("nations.ranks.engineer", null);
            success = true;
        }
        if (TownyPerms.mapHasGroup("nations.ranks.general")) {
            List<String> groupNodes = TownyPerms.getPermsOfGroup("nations.ranks.general");
            if (groupNodes.contains("towny.command.nation.rank.engineer")) {
                success = groupNodes.remove("towny.command.nation.rank.engineer");
            }
            if (groupNodes.contains("towny.command.nation.rank.gunner")) {
                success = groupNodes.remove("towny.command.nation.rank.gunner");
            }
            if(success) {
                townyPermsFile.set("nations.ranks.general", groupNodes);      
            }
        }
        return success;
    }
}
