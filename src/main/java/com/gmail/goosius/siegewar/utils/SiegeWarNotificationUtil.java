package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import java.util.HashSet;
import java.util.Set;

public class SiegeWarNotificationUtil {

	public static void informSiegeParticipants(Siege siege, String message) {

		try {
			//Build list of who to inform
			Set<Nation> nationsToInform = new HashSet<>();
			Set<Town> townsToInform= new HashSet<>();

			//attackers
			nationsToInform.add(siege.getAttackingNation());
			nationsToInform.addAll(siege.getAttackingNation().getMutualAllies());

			//defenders
			if (siege.getDefendingTown().hasNation()) {
				nationsToInform.add(siege.getDefendingTown().getNation());
				nationsToInform.addAll(siege.getDefendingTown().getNation().getMutualAllies());
			} else {
				townsToInform.add(siege.getDefendingTown());
			}

			//Inform required towns and nations
			for(Nation nation: nationsToInform) {
				TownyMessaging.sendPrefixedNationMessage(nation, message);
			}
			for(Town town: townsToInform) {
				TownyMessaging.sendPrefixedTownMessage(town, message);
			}

		} catch (Exception e) {
			System.out.println("Problem informing siege participants");
			System.out.println("Message : " + message);
			e.printStackTrace();
		}
	}
}
