package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.palmergames.bukkit.towny.permissions.TownyPerms;

import java.util.List;

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
		String permissionNodeWildCardString = permissionNodeString.replaceFirst("[\\w]*$", "*");
		return (nodesAllowedByRank.contains(permissionNodeString) 
			|| nodesAllowedByRank.contains(permissionNodeWildCardString));

	}
}

