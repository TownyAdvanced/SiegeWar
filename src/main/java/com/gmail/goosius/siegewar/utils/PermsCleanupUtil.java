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
        success = deleteMilitaryPermsFromAssistantRanks(townyPermsFile, success);
        success = deleteFireCannonPermFromGuardRank(townyPermsFile, success);
        success = deleteGunnerAndEngineerRanks(townyPermsFile, success);

        if(success) {
            townyPermsFile.save();
            SiegeWar.info("Perms Cleanup Complete");
        }
    }
    
    /**
     * Delete military perms from sheriff rank. So that peaceful and occupied towns can still have sheriffs
     */
    private static boolean deleteMilitaryPermsFromSheriffRank(CommentedConfiguration townyPermsFile, boolean success) {
        List<String> groupNodes;
        boolean localSuccess = false;
        // Deleted nodes from the sheriff rank.
        if (TownyPerms.mapHasGroup("towns.ranks.sheriff")) {
            groupNodes = TownyPerms.getPermsOfGroup("towns.ranks.sheriff");
            if (groupNodes.contains("siegewar.town.siege.battle.points")) {
                localSuccess = groupNodes.remove("siegewar.town.siege.battle.points");
            }
            if (groupNodes.contains("siegewar.town.siege.fire.cannon.in.siegezone")) {
                localSuccess = groupNodes.remove("siegewar.town.siege.fire.cannon.in.siegezone");
            }
            if (groupNodes.contains("towny.command.town.rank.guard")) {
                localSuccess = groupNodes.remove("towny.command.town.rank.guard");
            }
            if(localSuccess) {
                townyPermsFile.set("towns.ranks.sheriff", groupNodes);
            }
        }
        return success || localSuccess;
    }
    
    /**
     * Delete military perms from assistant rank. So that peaceful and occupied towns can still have assistants
     */
    private static boolean deleteMilitaryPermsFromAssistantRanks(CommentedConfiguration townyPermsFile, boolean success) {
        List<String> groupNodes = null;
        boolean localSuccess = false;
        if (TownyPerms.mapHasGroup("towns.ranks.assistant")) {
            groupNodes = TownyPerms.getPermsOfGroup("towns.ranks.assistant");
            if (groupNodes.contains("siegewar.town.siege.*")) {
                localSuccess = groupNodes.remove("siegewar.town.siege.*");
            }
        }
        if (TownyPerms.mapHasGroup("nations.ranks.assistant")) {
            groupNodes = TownyPerms.getPermsOfGroup("nations.ranks.assistant");
            if (groupNodes.contains("siegewar.nation.siege.*")) {
                localSuccess = groupNodes.remove("siegewar.nation.siege.*");
            }
        }
        if(localSuccess) {
            townyPermsFile.set("towns.ranks.sheriff", groupNodes);
        }
        return success || localSuccess;
    }
    
    private static boolean deleteFireCannonPermFromGuardRank(CommentedConfiguration townyPermsFile, boolean success) {
        List<String> groupNodes;
        boolean localSuccess = false;
        // Delete nodes from the guard rank.
        if (TownyPerms.mapHasGroup("towns.ranks.guard")) {
            groupNodes = TownyPerms.getPermsOfGroup("towns.ranks.guard");
            if (groupNodes.contains("siegewar.town.siege.fire.cannon.in.siegezone")) {
                localSuccess = groupNodes.remove("siegewar.town.siege.fire.cannon.in.siegezone");
            }
            if (localSuccess) {
                townyPermsFile.set("towns.ranks.guard", groupNodes);
            }
        }
        return success || localSuccess;
    }
    
    private static boolean deleteGunnerAndEngineerRanks(CommentedConfiguration townyPermsFile, boolean success) {
        boolean localSuccess = false;
        if (TownyPerms.mapHasGroup("nations.ranks.gunner")) {
            townyPermsFile.set("nations.ranks.gunner", null);
            localSuccess = true;
        }
        if (TownyPerms.mapHasGroup("nations.ranks.engineer")) {
            townyPermsFile.set("nations.ranks.engineer", null);
            localSuccess = true;
        }
        if (TownyPerms.mapHasGroup("nations.ranks.general")) {
            List<String> groupNodes = TownyPerms.getPermsOfGroup("nations.ranks.general");
            if (groupNodes.contains("towny.command.nation.rank.engineer")) {
                localSuccess = groupNodes.remove("towny.command.nation.rank.engineer");
            }
            if (groupNodes.contains("towny.command.nation.rank.gunner")) {
                localSuccess = groupNodes.remove("towny.command.nation.rank.gunner");
            }
            if(localSuccess) {
                townyPermsFile.set("nations.ranks.general", groupNodes);
            }
        }
        return success || localSuccess;
    }
}
