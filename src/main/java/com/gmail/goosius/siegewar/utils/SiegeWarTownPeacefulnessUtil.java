package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;

public class SiegeWarTownPeacefulnessUtil {

	public static boolean isTownPeaceful(Town town) {
		return TownMetaDataController.getPeacefulness(town);
	}
	
	public static void setTownPeacefulness(Town town, boolean bool) {
		TownMetaDataController.setPeacefulness(town, bool);
		town.save();
	}

	public static boolean getDesiredTownPeacefulness(Town town) {
		return TownMetaDataController.getDesiredPeacefulness(town);
	}

	public static void setDesiredTownPeacefulness(Town town, boolean bool) {
		TownMetaDataController.setDesiredPeacefulness(town, bool);
		town.save();
	}

	public static int getTownPeacefulnessChangeCountdownDays(Town town) {
		return TownMetaDataController.getPeacefulnessChangeCountdownDays(town);
	}

	public static void setTownPeacefulnessChangeCountdownDays(Town town, int days) {
		TownMetaDataController.setPeacefulnessChangeCountdownDays(town, days);
		town.save();
	}
	
	/**
	 * This method adjusts the peacefulness counters of all towns, where required
	 */
	public static void updateTownPeacefulnessCounters() {

		List<Town> towns = TownyAPI.getInstance().getTowns();
		ListIterator<Town> townItr = towns.listIterator();
		Town town;

		while (townItr.hasNext()) {
			town = townItr.next();
			/*
			 * Only adjust counter for this town if it really still exists.
			 * We are running in an Async thread so MUST verify all objects.
			 */
			if (TownyUniverse.getInstance().hasTown(town.getName()) 
				&& !town.isRuined()
				&& SiegeWarTownPeacefulnessUtil.isTownPeaceful(town) != TownMetaDataController.getDesiredPeacefulness(town))
				updateTownPeacefulnessCounters(town);
		}
	}

	/**
	 * This method adjusts the peacefulness counter of a single town
	 */
	public static void updateTownPeacefulnessCounters(Town town) {
		String message;

		int days = TownMetaDataController.getPeacefulnessChangeCountdownDays(town); 
		if (days > 1) {
			SiegeWarTownPeacefulnessUtil.setTownPeacefulnessChangeCountdownDays(town, --days);
			return;
		}
		TownMetaDataController.setPeacefulnessChangeCountdownDays(town, 0);
		
		//Reverse the town peacefulness setting
		TownMetaDataController.setPeacefulness(town, !TownMetaDataController.getPeacefulness(town));

		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if (TownMetaDataController.getPeacefulness(town)) {
				message = Translation.of("msg_town_became_peaceful", town.getFormattedName());
			} else {
				message = Translation.of("msg_town_became_non_peaceful", town.getFormattedName());
			}
		} else {
			if (TownMetaDataController.getPeacefulness(town)) {
				message = Translation.of("msg_town_became_peaceful", town.getFormattedName());
			} else {
				message = Translation.of("msg_town_became_non_peaceful", town.getFormattedName());
			}
		}
		TownyMessaging.sendPrefixedTownMessage(town, message);
		town.save();
	}

	/**
     * Calculates the Towny-Influence map for the target town
     *
     * @param targetTown targetTown
     *
     * @return Towny-Influence map ....in the form: NationX:Amt, NationY:Amt, NationZ:Amt ....etc.
     *         The map is sorted in descending order, with the highest influence nation being first
     */
    public static Map<Nation,Integer> calculateTownyInfluenceMap(Town targetTown) {
		Map<Nation,Integer> result = new LinkedHashMap<>();
		List<Town> allTowns = TownyAPI.getInstance().getTowns();
		ListIterator<Town> allTownsItr = allTowns.listIterator();
		Town town;
		Nation nation;

		//Cycle all towns
		while (allTownsItr.hasNext()) {
			town = allTownsItr.next();

			try {
				//Skip if town is ruined
				if (town.isRuined())
					continue;

				//Skip if town is peaceful
				if(TownMetaDataController.getPeacefulness(town))
					continue;

				//Skip if town has no natural or occupying nation
				if(!town.hasNation() && !SiegeWarTownOccupationUtil.isTownOccupied(town))
					continue;

				//Skip if town is besieged
				if(SiegeController.hasActiveSiege(town))
					continue;

				//Skip if town is too far from target town
				int townyInfluenceRadiusInTownBlocks = SiegeWarSettings.getPeacefulTownsTownyInfluenceRadius() / TownySettings.getTownBlockSize();
				if(!SiegeWarDistanceUtil.areTownsClose(town, targetTown, townyInfluenceRadiusInTownBlocks))
					continue;

				//Update towny-influence map
				nation = SiegeWarTownOccupationUtil.isTownOccupied(town) ? SiegeWarTownOccupationUtil.getTownOccupier(town) : town.getNation();
				if(result.containsKey(nation)) {
					result.put(nation, result.get(nation) + town.getTownBlocks().size());
				} else {
					result.put(nation, town.getTownBlocks().size());
				}
			} catch (Exception e) {
				try {
					SiegeWar.severe("Problem evaluating towny-influence map generation for town: " + targetTown.getName());
				} catch (Exception e2) {
					SiegeWar.severe("Problem evaluating towny-influence map generation for town (could not read town name)");
				}
				e.printStackTrace();
			}
		}
		//Sort result, highest result first
		result = sortTownyInfluenceMap(result);
		//Return result
		return result;
    }

	/**
	 * Sort the influence map in descending order, so that the nation with the highest value,
	 * is first in the map.
	 *
	 * @param unsortedTownyInfluenceMap the given unsorted map
	 * @return sorted towny-influence map
	 */
	private static Map<Nation,Integer> sortTownyInfluenceMap(Map<Nation,Integer> unsortedTownyInfluenceMap) {
		// Now, getting all entries from map and
        // convert it to a list using entrySet() method
        List<Map.Entry<Nation, Integer>> list = new ArrayList<>(unsortedTownyInfluenceMap.entrySet());

        // Using collections class sort method
        // and inside which we are using
        // custom comparator to compare value of map
        Collections.sort(
            list,
            new Comparator<Map.Entry<Nation, Integer> >() {
                // Comparing two entries by value
                public int compare(
                    Map.Entry<Nation, Integer> entry1,
                    Map.Entry<Nation, Integer> entry2)
                {
                    // Subtracting the entries
                    return entry2.getValue()
                        - entry1.getValue();
                }
            });

		// Iterating over the sorted map
		// using the for each method
		Map<Nation, Integer> result = new LinkedHashMap<>();
		for (Map.Entry<Nation, Integer> mapEntry : list) {
			result.put(mapEntry.getKey(), mapEntry.getValue());
		}

		return result;
	}

	public static void toggleTownPeacefulness(Player player) {
		if (!SiegeWarSettings.getWarSiegeEnabled()) {
			player.sendMessage(Translation.of("msg_err_command_disable"));
			return;
		}

		if(!SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
			player.sendMessage(Translation.of("msg_err_command_disable"));
			return;
		}

		Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
		if(resident == null || !resident.hasTown()) {
			player.sendMessage(Translation.of("msg_err_command_disable"));
			return;
		}

		//Capital towns cannot go peaceful.
		Town town = resident.getTownOrNull();
		if (town.isCapital() && !SiegeWarSettings.capitalsAllowedTownPeacefulness()) {
			player.sendMessage(Translation.of("msg_err_capital_towns_cannot_go_peaceful"));
			return;
		}

		//Get the days required for a status change
		int daysRequiredForStatusChange;
		if(System.currentTimeMillis() < (town.getRegistered() + (TimeMgmt.ONE_DAY_IN_MILLIS * 7))) {
			daysRequiredForStatusChange = SiegeWarSettings.getWarCommonPeacefulTownsNewTownConfirmationRequirementDays();
		} else {
			daysRequiredForStatusChange = SiegeWarSettings.getWarCommonPeacefulTownsConfirmationRequirementDays();
		}

		//Check if there is a countdown in progress
		if (TownMetaDataController.getPeacefulnessChangeCountdownDays(town) == 0) {
			//No countdown in progress
			TownMetaDataController.setDesiredPeacefulness(town, !SiegeWarTownPeacefulnessUtil.isTownPeaceful(town));
			TownMetaDataController.setPeacefulnessChangeCountdownDays(town, daysRequiredForStatusChange);

			//Send message to town
			if (TownMetaDataController.getDesiredPeacefulness(town))
				TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_declared_peaceful"), daysRequiredForStatusChange));
			else
				TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_declared_non_peaceful"), daysRequiredForStatusChange));

			//Remove any military nation ranks of residents
			for(Resident peacefulTownResident: town.getResidents()) {
				for (String nationRank : new ArrayList<>(peacefulTownResident.getNationRanks())) {
					if (PermissionUtil.doesNationRankAllowPermissionNode(nationRank, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS)) {
						peacefulTownResident.removeNationRank(nationRank);
					}
				}
			}
		} else {
			//Countdown in progress. Cancel the countdown
			TownMetaDataController.setDesiredPeacefulness(town, SiegeWarTownPeacefulnessUtil.isTownPeaceful(town));
			TownMetaDataController.setPeacefulnessChangeCountdownDays(town, 0);
			//Send message to town
			TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_peacefulness_countdown_cancelled")));
		}
		//Save data
		town.save();
	}
}
