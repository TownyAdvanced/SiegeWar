package com.gmail.goosius.siegewar.enums;

import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;

public enum SiegeSide {
	ATTACKERS("msg_attackers"), DEFENDERS("msg_defenders"), NOBODY("msg_nobody");

	SiegeSide(String langStringId) {
		this.langStringId = langStringId;
	}

	private String langStringId;

	public Translatable getFormattedName() {
		return Translatable.of(langStringId);
	}

	public static SiegeSide getPlayerSiegeSide(Siege siege, Player player) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		if (resident == null || !resident.hasTown())
			return SiegeSide.NOBODY;
		Town town = resident.getTownOrNull();

		// Look for defender
		Town besiegedTown = siege.getTown();
		if (isTownGuard(player, besiegedTown) || isNationSoldierOrAlliedSoldier(player, town, besiegedTown))
			return SiegeSide.DEFENDERS;

		// Look for attacker
		if (isNationSoldierOrAlliedSoldier(player, town, siege.getAttacker()))
			return SiegeSide.ATTACKERS;

		return SiegeSide.NOBODY;
	}

	public static boolean isDefender(Siege siege, Player player) {
		return getPlayerSiegeSide(siege, player).equals(SiegeSide.DEFENDERS);
	}

	public static boolean isAttacker(Siege siege, Player player) {
		return getPlayerSiegeSide(siege, player).equals(SiegeSide.ATTACKERS);
	}

	public static boolean isNobody(Siege siege, Player player) {
		return getPlayerSiegeSide(siege, player).equals(SiegeSide.NOBODY);
	}

	private static boolean isTownGuard(Player player, Town town) {
		return town.hasResident(player)
				&& player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS.getNode());
	}

	private static boolean isNationSoldierOrAlliedSoldier(Player player, Town residentTown, Government governmentToCheck) {
		Nation nation = residentTown.getNationOrNull();
		if (nation == null || !player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS.getNode()))
			return false;

		if (governmentToCheck instanceof Nation) {
			// The government-to-check is a nation
			return nation == governmentToCheck || nation.hasMutualAlly((Nation) governmentToCheck);
		} else if (((Town) governmentToCheck).hasNation()) {
			// The government-to-check is a nation town
			return nation.hasTown((Town) governmentToCheck)
					|| nation.hasMutualAlly(((Town) governmentToCheck).getNationOrNull());
		} else {
			// The government-to-check is a non-nation town. Nation soldiers cannot
			// contribute
			return false;
		}
	}
}
