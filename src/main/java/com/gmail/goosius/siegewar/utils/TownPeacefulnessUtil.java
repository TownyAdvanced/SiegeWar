package com.gmail.goosius.siegewar.utils;


import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeTools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class TownPeacefulnessUtil {

	/**
	 * This method adjust the peacefulness counters of all towns, where required
	 */
	public static void updateTownPeacefulnessCounters() {
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		List<Town> towns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
		ListIterator<Town> townItr = towns.listIterator();
		Town town;

		while (townItr.hasNext()) {
			town = townItr.next();
			/*
			 * Only adjust counter for this town if it really still exists.
			 * We are running in an Async thread so MUST verify all objects.
			 */
			if (townyUniverse.getDataSource().hasTown(town.getName()) 
				&& !town.isRuined()
				&& town.isNeutral() != TownMetaDataController.getDesiredPeacefulnessSetting(town))
				updateTownPeacefulnessCounters(town);
		}
	}

	/**
	 * This method adjust the peacefulness counter of a single town
	 */
	public static void updateTownPeacefulnessCounters(Town town) {
		String message;

		int days = TownMetaDataController.getPeacefulnessChangeConfirmationCounterDays(town); 
		if (days > 1) {
			TownMetaDataController.setPeacefulnessChangeDays(town, --days);
			return;
		}
		TownMetaDataController.setPeacefulnessChangeDays(town, 0);
		town.setNeutral(!town.isNeutral());

		if (town.isNeutral() && !SiegeWarSettings.getWarCommonPeacefulTownsAllowedToTogglePVP()) 
			SiegeWarTownUtil.disableTownPVP(town);	

		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if (town.isNeutral()) {
				message = Translation.of("msg_town_became_peaceful", town.getFormattedName());

				//If town is occupied, record the occupier
				if(TownOccupationController.isTownOccupied(town)) {
					TownMetaDataController.setPrePeacefulOccupierUUID(town, TownOccupationController.getTownOccupier(town).getUUID().toString());
				}
			} else {
				message = Translation.of("msg_town_became_non_peaceful", town.getFormattedName());

				/*
				 * If the town was occupied before turning peaceful,
				 * return it to the previous occupier.
				 * If the previous occupier is now the town's home nation, do not re-occupy.
				 */
				try {
					String prePeacefulOccupierUUID = TownMetaDataController.getPrePeacefulOccupierUUID(town);
					if(prePeacefulOccupierUUID != null) {
						Nation prePeacefulOccupierNation = TownyUniverse.getInstance().getNation(UUID.fromString(prePeacefulOccupierUUID));
							if (!(town.hasNation() && town.getNation() == prePeacefulOccupierNation)) {
								TownOccupationController.setTownOccupation(town, prePeacefulOccupierNation);
								TownMetaDataController.removePrePeacefulOccupierUUID(town);
								message += Translation.of("msg_town_returned_to_pre_peaceful_occupier",prePeacefulOccupierNation.getName());
						}
					}
				} catch (Throwable t) {
					SiegeWar.severe("Issue with re-assigning pre-peaceful occupier for town " + town.getName());
					t.printStackTrace();
				}
			}
		} else {
			if (town.isNeutral()) {
				message = Translation.of("msg_town_became_peaceful", town.getFormattedName());
			} else {
				message = Translation.of("msg_town_became_non_peaceful", town.getFormattedName());
			}
		}
		TownyMessaging.sendPrefixedTownMessage(town, message);
		town.save();
	}

	/**
	 * This method punishes any peaceful players who are in siege-zones
	 * (except for their own town OR any peaceful town)
	 * 
	 * A player is peaceful if they
	 * 1. Are resident in a peaceful town
	 * 2. Are resident in a declared (but not confirmed) peaceful town
	 *
	 * The punishment is a status effect (e.g. poison, nausea)
	 * The punishment is refreshed every 20 seconds, until the player leaves the siege-zone
	 */
	public static void punishPeacefulPlayersInActiveSiegeZones() {
		for(final Player player: Bukkit.getOnlinePlayers()) {
			try {
				//Don't apply to towny admins
				if(TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(player))
					continue;

				//Dont apply if player has the immunity perm
				if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_IMMUNE_TO_WAR_NAUSEA.getNode()))
					continue;

				//Don't apply to non-peaceful players
				Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
				if(resident == null || !(resident.hasTown()&& resident.getTown().isNeutral()))
					continue;

				//Don't punish if the player is in a peaceful town
				TownBlock townBlockAtPlayerLocation = TownyAPI.getInstance().getTownBlock(player.getLocation());
				if(townBlockAtPlayerLocation != null
					&& townBlockAtPlayerLocation.getTown().isNeutral())
				{
					continue;
				}

				//Don't punish if the player is in their own town
				if(resident.hasTown()
					&& townBlockAtPlayerLocation != null
					&& resident.getTown() == townBlockAtPlayerLocation.getTown())
				{
					continue;
				}

				//Punish if the player is in a siege zone
				if(SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
					TownyMessaging.sendMsg(player, Translation.of("msg_war_siege_peaceful_player_punished_for_being_in_siegezone"));
					final int effectDurationTicks = (int)(TimeTools.convertToTicks(TownySettings.getShortInterval() + 5));
					Towny.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Towny.getPlugin(), new Runnable() {
						public void run() {
							List<PotionEffect> potionEffects = new ArrayList<>();
							potionEffects.add(new PotionEffect(PotionEffectType.CONFUSION, effectDurationTicks, 4));
							potionEffects.add(new PotionEffect(PotionEffectType.POISON, effectDurationTicks, 4));
							potionEffects.add(new PotionEffect(PotionEffectType.WEAKNESS, effectDurationTicks, 4));
							potionEffects.add(new PotionEffect(PotionEffectType.SLOW, effectDurationTicks, 2));
							player.addPotionEffects(potionEffects);
							player.setHealth(1);
						}
					});
				}
			} catch (Exception e) {
				try {
					SiegeWar.severe("Problem punishing peaceful player in siege zone - " + player.getName());
				} catch (Exception e2) {
					SiegeWar.severe("Problem punishing peaceful player in siege zone (could not read player name)");
				}
				e.printStackTrace();
			}
		}
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
		List<Town> allTowns = new ArrayList<>(TownyUniverse.getInstance().getDataSource().getTowns());
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
				if(town.isNeutral())
					continue;

				//Skip is town has no nation
				if(!town.hasNation())
					continue;

				//Skip if town is besieged
				if(SiegeController.hasActiveSiege(town))
					continue;

				//Skip if town is too far from target town
				int townyInfluenceRadiusInTownBlocks = SiegeWarSettings.getPeacefulTownsTownyInfluenceRadius() / TownySettings.getTownBlockSize();
				if(!SiegeWarDistanceUtil.areTownsClose(town, targetTown, townyInfluenceRadiusInTownBlocks))
					continue;					

				//Update towny-influence map
				nation = town.getNation();
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

}
