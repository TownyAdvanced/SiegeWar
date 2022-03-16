package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyAPI;
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
				if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_NOTIFICATIONS_ALL.getNode())) {
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

	public static void warnPlayerOfSiegeDanger(Player player) {
		//Return is war is not enabled at player's location
		if(!TownyAPI.getInstance().getTownyWorld(player.getWorld()).isWarAllowed())
			return;
		
		//Send warning if player is in SiegeZone (& didn't already get the warning)
		Siege siege = SiegeController.getSiegeAtLocation(player.getLocation());
		if(siege != null 
			&& siege.getStatus().isActive()
			&& !siege.getPlayersWhoWereInTheSiegeZone().contains(player)
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
				Messaging.sendErrorMsg(player, Translatable.of("msg_siege_zone_proximity_warning_text"));
				siege.addPlayerWhoWasInTheSiegeZone(player);		
		}
		
		//Send warning if player is in besieged town (& didn't already get the warning)
		//Note: Being in the SiegeZone doesn't necessarily mean being in a besieged town
		Town town = TownyAPI.getInstance().getTown(player.getLocation());
		if(town == null)
			return;
		siege = SiegeController.getSiege(town);
		if(siege != null 
			&& siege.getStatus().isActive()
			&& !siege.getPlayersWhoWereInTheBesiegedTown().contains(player)) {
				Messaging.sendErrorMsg(player, Translatable.of("msg_besieged_town_proximity_warning_text"));
				siege.addPlayersWhoWasInTheBesiegedTown(player);		
		}
	}

	public static void warnPlayersOfSiegeDanger() {
		for(Player player: Bukkit.getOnlinePlayers()) {
			warnPlayerOfSiegeDanger(player);
		}
	}
}
