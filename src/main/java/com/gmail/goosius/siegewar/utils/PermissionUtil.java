package com.gmail.goosius.siegewar.utils;

import java.util.List;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class PermissionUtil {

	/**
	 * This method checks if the given nation rank, will allow the given permission node
	 *
	 * @param nationRank - A nation rank (e.g. soldier, helper)
	 * @param permissionNode - Permission node to check for
	 * @return true if the rank allows the permission node
	 */
	public static boolean doesNationRankAllowPermissionNode(String nationRank, SiegeWarPermissionNodes permissionNode) {
		return doesRankAllowPermissionNode(TownyPerms.getNationRankPermissions(nationRank), permissionNode);
	}

	/**
	 * This method checks if the given town rank, will allow the given permission node
	 *
	 * @param townRank - A town rank (e.g. guard, helper)
	 * @param permissionNode - Permission node to check for
	 * @return true if the rank allows the permission node
	 */
	public static boolean doesTownRankAllowPermissionNode(String townRank, SiegeWarPermissionNodes permissionNode) {
		return doesRankAllowPermissionNode(TownyPerms.getTownRankPermissions(townRank), permissionNode);
	}
	
	/**
	 * This method checks if a list of permission nodes contains a node.
	 *
	 * @param nodesAllowedByRank The List of nodes.
	 * @param node The SiegeWarPermissionNode to check for.
	 * @return true if the node is in the List.
	 */
	private static boolean doesRankAllowPermissionNode(List<String> nodesAllowedByRank, SiegeWarPermissionNodes node) {
		String permissionNodeString = node.getNode();

		// Quickly succeed.
		if (nodesAllowedByRank.contains(permissionNodeString))
			return true;

		// Test replacing the last word in the permission node string with *, ie
		// siegewar.nation.SOMETHING will check if the player has siegewar.nation.*
		String permissionNodeWildCardString = permissionNodeString.replaceFirst("[\\w]*$", "*");
		if (nodesAllowedByRank.contains(permissionNodeWildCardString))
			return true;

		// Query bukkit to see if the node is a child node of a Permission given by the rank.
		for (String nodeAllowedByRank : nodesAllowedByRank) {
			Permission permissionNode = Bukkit.getPluginManager().getPermission(nodeAllowedByRank);
			if (permissionNode != null
					&& permissionNode.getChildren().containsKey(permissionNodeString)
					&& permissionNode.getChildren().get(permissionNodeString).booleanValue())
				return true;
		}

		return false;
	}
}

