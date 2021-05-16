package com.gmail.goosius.siegewar.utils;


import com.gmail.goosius.siegewar.Messaging;
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
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.util.TimeTools;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

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
					System.out.println("Issue with re-assigning pre-peaceful occupier for town " + town.getName());
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
		for(final Player player: BukkitTools.getOnlinePlayers()) {
			try {
				//Don't apply to towny admins
				if(TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(player))
					continue;

				//Dont apply if player has the immunity perm
				if (TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_IMMUNE_TO_WAR_NAUSEA.getNode()))
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
					System.err.println("Problem punishing peaceful player in siege zone - " + player.getName());
				} catch (Exception e2) {
					System.err.println("Problem punishing peaceful player in siege zone (could not read player name)");
				}
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method is a cleanup of peaceful town occupation statuses
	 *
	 * How it works:
	 *
	 * 1. For each peaceful town, every nearby guardian town exerts a "power-influence" on the peaceful town.
	 *    - "Nearby" is defined as 75 townblocks (configurable)
	 *    - "Guardian Town" is defined as a non-peaceful nation town of size 30 townblocks (configurable) or more.
	 *    - An occupied guardian town exerts influence for its occupier, not for its home nation.
	 *    - The strength of the influence equals the num-townblocks of the guardian town.
	 *
	 * 2. If the peaceful town has a nation,
	 *    the influences of the home nation & foreign enemy nations are greatly amplified,
	 *    such that they will always be stronger than the influences of foreign non-enemy nations.
	 *
	 * 3. Possible Outcomes:
	 *    A. If there are zero influences on the peaceful town, it will become unoccupied.
	 *    B. If the strongest influence belongs to the peaceful town's home nation, the town will become unoccupied.
	 *    C. If the strongest influence belongs to a foreign nation, the town will get peacefully occupied by that nation.
	 */
	public static void evaluatePeacefulTownOccupationAssignments() {
		TownyUniverse townyUniverse = TownyUniverse.getInstance();
		List<Town> towns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
		ListIterator<Town> townItr = towns.listIterator();
		Town peacefulTown;
		int modifiedTowns = 0;
		boolean townTransferred;
		Nation nationWithStrongestInfluence;

		//Cycle peaceful towns
		while (townItr.hasNext()) {
			peacefulTown = townItr.next();

			try {
				//Skip if town is non-peaceful
				if (!peacefulTown.isNeutral())
					continue;

				//Skip if town is ruined
				if (peacefulTown.isRuined())
					continue;

				//Find nearby nations with influence on the town (result is Map<nation, strength-of-influence>)
				Map<Nation, Long> nationsWithInfluence = findNearbyNationsWithInfluence(peacefulTown);

				//Amplify influence of home & enemy nations
				nationsWithInfluence = amplifyInfluenceOfHomeAndEnemyNations(nationsWithInfluence, peacefulTown);

				if (nationsWithInfluence.size() == 0) {
					//The town is not affected by influence.
					//Ensure peaceful town is unoccupied.
					townTransferred = ensureTownIsPeacefullyUnoccupied(peacefulTown);
				} else {
					//The town is affected by influence
					//Get the nation with the strongest influence
					nationWithStrongestInfluence = calculateNationWithStrongestInfluence(nationsWithInfluence);

					//Set town occupation status
					if (peacefulTown.hasNation() && peacefulTown.getNation() == nationWithStrongestInfluence) {
						//Strongest nation is the town's home nation. Ensure town is unoccupied.
						townTransferred = ensureTownIsPeacefullyUnoccupied(peacefulTown);
					} else {
						//Strongest nation is not the town's home nation. Ensure town is occupied by that nation.
						townTransferred = ensureTownIsPeacefullyOccupied(peacefulTown, nationWithStrongestInfluence);
					}
				}

				if (townTransferred)
					modifiedTowns += 1;

			} catch (Exception e) {
				try {
					System.err.println("Problem evaluating peaceful town nation assignment for - " + peacefulTown.getName());
				} catch (Exception e2) {
					System.err.println("Problem evaluating peaceful town nation assignment (could not read town name)");
				}
				e.printStackTrace();
			}
		}
		//Send a global message with how many towns were modified.
		if (modifiedTowns > 0) {
			boolean one = modifiedTowns == 1;
			Messaging.sendGlobalMessage(Translation.of("msg_peaceful_town_total_switches", modifiedTowns, one ? "" : "s", one ? "has" : "have"));
		}
	}

	/*
	 * Amplify the influence of home & enemy nations
	 */
	private static Map<Nation, Long> amplifyInfluenceOfHomeAndEnemyNations(Map<Nation, Long> nationInfluenceMap, Town peacefulTown) {
		Map<Nation, Long> result = new HashMap<>(nationInfluenceMap);
		long amplifiedValue;
		Nation nationOfPeacefulTown;
		if (peacefulTown.hasNation()) {
			nationOfPeacefulTown = TownyAPI.getInstance().getTownNationOrNull(peacefulTown);
			for (Map.Entry<Nation, Long> mapEntry : nationInfluenceMap.entrySet()) {
				if (mapEntry.getKey() == nationOfPeacefulTown
						|| mapEntry.getKey().hasEnemy(nationOfPeacefulTown)) {
					amplifiedValue = mapEntry.getValue() * 1000000;
					result.put(mapEntry.getKey(), amplifiedValue);
				}
			}
		}
		return result;
	}

	private static Nation calculateNationWithStrongestInfluence(Map<Nation, Long> guardianNations) {
		Map.Entry<Nation, Long> winningEntry = null;
		for(Map.Entry<Nation,Long> mapEntry: guardianNations.entrySet()) {
			if(winningEntry == null) {
				winningEntry = mapEntry;
			} else {
				if(mapEntry.getValue() > winningEntry.getValue()) {
					winningEntry = mapEntry;
				}
			}
		}
		return winningEntry.getKey();
	}

	private static boolean ensureTownIsPeacefullyUnoccupied(Town peacefulTown) {
		if(!TownOccupationController.isTownOccupied(peacefulTown)) {
			return false;
		}

		//Get current occupier
		Nation currentOccupier = TownOccupationController.getTownOccupier(peacefulTown);
		//Remove occupation
		TownOccupationController.removeTownOccupation(peacefulTown);

		//Send messages
		//Send to peaceful town
		TownyMessaging.sendPrefixedTownMessage(peacefulTown, Translation.of("msg_your_town_peacefully_released", currentOccupier.getName()));
		//Send to occupier
		TownyMessaging.sendPrefixedNationMessage(currentOccupier, Translation.of("msg_foreign_town_peacefully_released", peacefulTown.getName()));
		//Send to nation of peaceful town
		if(peacefulTown.hasNation())
			TownyMessaging.sendPrefixedNationMessage(TownyAPI.getInstance().getTownNationOrNull(peacefulTown), Translation.of("msg_home_town_peacefully_released", peacefulTown.getName(), currentOccupier.getName()));

		return true;
	}

	private static boolean ensureTownIsPeacefullyOccupied(Town peacefulTown, Nation newOccupier) {
		if(TownOccupationController.isTownOccupied(peacefulTown)) {
			//Town is already occupied
			Nation currentOccupier = TownOccupationController.getTownOccupier(peacefulTown);

			//Check if town is already occupied by correct nation
			if (currentOccupier == newOccupier)
				return false;

			//Remove current occupation
			ensureTownIsPeacefullyUnoccupied(peacefulTown);
		}

		//Set new  occupation
		TownOccupationController.setTownOccupation(peacefulTown, newOccupier);

		//Send messages
		//Send to peaceful town
		TownyMessaging.sendPrefixedTownMessage(peacefulTown, Translation.of("msg_your_town_peacefully_occupied", newOccupier.getName()));
		//Send to new occupier
		TownyMessaging.sendPrefixedNationMessage(newOccupier, Translation.of("msg_foreign_town_peacefully_occupied", peacefulTown.getName()));
		//Send to nation of peaceful town
		if(peacefulTown.hasNation())
			TownyMessaging.sendPrefixedNationMessage(TownyAPI.getInstance().getTownNationOrNull(peacefulTown), Translation.of("msg_home_town_peacefully_occupied", peacefulTown.getName(), newOccupier.getName()));

		return true; //Town switched
	}

	public static Map<Nation, Long> findNearbyNationsWithInfluence(Town peacefulTown) {
		Map<Nation, Long> guardianNations = new HashMap<>();
		Nation guardianNation;
		int numTownBlocks;
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		try {
			int guardianTownMinPlotsRequirement = SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement();
			int guardianTownMaxDistanceRequirementTownblocks = SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement();
	
			//Identify Guardian nations
			List<Town> candidateTowns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
			for(Town candidateTown: candidateTowns) {
				if(!candidateTown.isNeutral()
					&& (candidateTown.hasNation() || TownOccupationController.isTownOccupied(candidateTown))
					&& candidateTown.getTownBlocks().size() >= guardianTownMinPlotsRequirement
					&& SiegeWarDistanceUtil.areTownsClose(peacefulTown, candidateTown, guardianTownMaxDistanceRequirementTownblocks)) {

						guardianNation = TownOccupationController.isTownOccupied(candidateTown) ? TownOccupationController.getTownOccupier(candidateTown) : candidateTown.getNation();
						numTownBlocks = candidateTown.getTownBlocks().size();

						if(guardianNations.containsKey(guardianNation)) {
							guardianNations.put(guardianNation, guardianNations.get(guardianNation) + numTownBlocks);
						} else {
							guardianNations.put(guardianNation, (long)numTownBlocks);
						}
				}
			}
		} catch (Exception e) {
			try {
				System.err.println("Problem getting valid guardian towns for - " + peacefulTown.getName());
			} catch (Exception e2) {
				System.err.println("Problem getting valid guardian towns (could not read peaceful town name)");
			}
			e.printStackTrace();
		}

		//Return result
		return guardianNations;
	}
}
