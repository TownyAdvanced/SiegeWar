package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SiegeWarNotificationUtil {

	public static void informSiegeParticipants(Siege siege, Translatable... message) {

		try {
			//Build list of who to inform
			Nation nation;
			Set<Nation> nationsToInform = new HashSet<>();
			Set<Town> townsToInform= new HashSet<>();

			//Attackers
			if(siege.getAttacker() instanceof Nation) {
				//Attacker is a nation
				nation = (Nation)siege.getAttacker();
				nationsToInform.add(nation);
				nationsToInform.addAll(nation.getMutualAllies());
			} else if (((Town)siege.getAttacker()).hasNation()) {
				//Attacker is a nation town
				nation = ((Town)siege.getAttacker()).getNation();
				nationsToInform.add(nation);
				nationsToInform.addAll(nation.getMutualAllies());
			} else {
				//Attacker is a non-nation town
				townsToInform.add((Town)siege.getAttacker());
			}

			//Defenders
			if(siege.getDefender() instanceof Nation) {
				//Defender is a nation
				nation = (Nation)siege.getDefender();
				nationsToInform.add(nation);
				nationsToInform.addAll(nation.getMutualAllies());
			} else if (((Town)siege.getDefender()).hasNation()) {
				//Defender is a nation town
				nation = ((Town)siege.getDefender()).getNation();
				nationsToInform.add(nation);
				nationsToInform.addAll(nation.getMutualAllies());
			} else {
				//Defender is a non-nation town
				townsToInform.add((Town)siege.getDefender());
			}

			//Inform required towns and nations
			for(Nation nationToInform: nationsToInform) {
				for (Translatable line : message) 
					if (line != null)
						TownyMessaging.sendPrefixedNationMessage(nationToInform, line);
			}
			for(Town townToInform: townsToInform) {
				for (Translatable line : message)
					if (line != null)
						TownyMessaging.sendPrefixedTownMessage(townToInform, line);
			}

			//Inform battlefield observers
			for(Player player: Bukkit.getOnlinePlayers()) {
				if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_NOTIFICATIONS_ALL.getNode())
				&& !nationHasPlayer(nationsToInform, player)
				&& !townHasPlayer(townsToInform, player)) {
					for (Translatable line : message)
						if (line != null)
							Messaging.sendMsg(player, line);
				}
			}

		} catch (Exception e) {
			SiegeWar.severe("Problem informing siege participants");
			SiegeWar.severe("Message : " + message);
			e.printStackTrace();
		}
	}

	private static boolean townHasPlayer(Set<Town> towns, Player player) {
		for (Town town : towns)
			if (town.hasResident(player.getName()))
				return true;
		return false;
	}

	private static boolean nationHasPlayer(Set<Nation> nations, Player player) {
		for (Nation nation : nations)
			if (nation.hasResident(player.getName()))
				return true;
		return false;
	}


	/**
	 * Warn player who we know is in an active siege zones
	 * @param player player
	 * @param siege siege
	 */
	public static void warnPlayerOfActiveSiegeDanger(Player player, Siege siege) {
		if(!siege.getPlayersWhoWereInTheSiegeZone().contains(player)) {
			if(SiegeWarSettings.getKillPlayersWhoLogoutInSiegeZones()) {
				Messaging.sendErrorMsg(player, Translatable.of("msg_siege_zone_proximity_warning_with_logout_risk"));
			} else {
				Messaging.sendErrorMsg(player, Translatable.of("msg_siege_zone_proximity_warning"));
			}
			siege.addPlayerWhoWasInTheSiegeZone(player);
		}
	}

	public static void warnAllPlayersOfSiegeDanger() {
		for(Player player: Bukkit.getOnlinePlayers()) {
			Siege siege = SiegeWarDistanceUtil.getActiveSiegeZoneWherePlayerIsRegistered(player);
			if(siege != null)
				warnPlayerOfActiveSiegeDanger(player, siege); 
		}
	}
}
