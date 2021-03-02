package com.gmail.goosius.siegewar.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.permissions.TownyPerms;

public class PermissionUtil {
	
	private static final List<String> nationMilitaryRanks = new ArrayList<>(Arrays.asList("private","sergeant","lieutenant","captain","major","colonel","general"));
	private static final List<String> townMilitaryRanks = new ArrayList<>(Arrays.asList("guard", "sheriff"));

	/**
	 * This method checks if the given nation rank, will allow the given permission node
	 *
	 * @param nationRank - A nation rank (e.g. soldier, helper)
	 * @param permissionNode - Permission node to check for
	 * @return true if the rank allows the permission node
	 */
	public static boolean doesNationRankAllowPermissionNode(String nationRank, SiegeWarPermissionNodes permissionNode) {
		return doesRankAllowPermissionNode(TownyPerms.getNationRank(nationRank), permissionNode);
	}

	/**
	 * This method checks if the given town rank, will allow the given permission node
	 *
	 * @param townRank - A town rank (e.g. guard, helper)
	 * @param permissionNode - Permission node to check for
	 * @return true if the rank allows the permission node
	 */
	public static boolean doesTownRankAllowPermissionNode(String townRank, SiegeWarPermissionNodes permissionNode) {
		return doesRankAllowPermissionNode(TownyPerms.getTownRank(townRank), permissionNode);
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
	
	public static boolean hasNationMilitaryRank(Resident resident) {
		return resident.isKing() || nationMilitaryRanks.stream().anyMatch(rank -> resident.getNationRanks().contains(rank));
	}

	public static boolean hasTownMilitaryRank(Resident resident) {
		return resident.isMayor() || townMilitaryRanks.stream().anyMatch(rank -> resident.getTownRanks().contains(rank));
	}
}

