package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
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
import java.util.Set;
import java.util.HashSet;

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
			} else {
				message = Translation.of("msg_town_became_non_peaceful", town.getFormattedName());
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
	 * Cycle peaceful towns
	 * - If town's has the correct occupation status, skip and go to next town
	 * - If town's has an incorrect occupation status, update occupation status.
	 *
	 * Rules for each peaceful town:
	 *
	 * 1. If there is a guardian town nearby which is:
	 * - Unsieged
	 * - Belonging to a foreign nation
	 * - The largest guardian town in the area
	 * =====> Then the peaceful town will be occupied
	 *
	 * 2. If there is such town nearby:
	 * =====> The the peaceful town will be unoccupied
	 *
	 */
	public static void evaluatePeacefulTownOccupationAssignments() {
		TownyUniverse townyUniverse = TownyUniverse.getInstance();
		List<Town> towns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
		ListIterator<Town> townItr = towns.listIterator();
		Town peacefulTown;
		int modifiedTowns = 0;
		boolean townTransferred;

		while (townItr.hasNext()) {
			peacefulTown = townItr.next();

			try {
				//Skip if town is non-peaceful
				if (!peacefulTown.isNeutral())
					continue;

				//Skip if town is ruined
				if(peacefulTown.isRuined())
					continue;

				//Find guardian towns
				Set<Town> guardianTowns = getGuardianTowns(peacefulTown);

				//If there are no guardian towns, ensure the town is not occupied
				if(guardianTowns.size() == 0) {
					townTransferred = ensureTownIsPeacefullyUnoccupied(peacefulTown);

				} else {
					//Find guardian nation
					Nation guardianNation = getGuardianNation(guardianTowns);

					//If the guardian nation is the town's home nation, continue
					//1. Do not occupy (because obviously)
					//2. Do not release (because the king must always permit release)
					if (peacefulTown.hasNation() && peacefulTown.getNation() == guardianNation)
						continue;

					//Ensure the town is occupied
					townTransferred = ensureTownIsPeacefullyOccupied(peacefulTown, guardianNation);
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

	private static Nation getGuardianNation(Set<Town> guardianTowns) throws NotRegisteredException {
		//Find the largest guardian town & thus guardian nation
		Town topGuardianTown = null;
		for(Town guardianTown: guardianTowns) {
			if(topGuardianTown == null || guardianTown.getTownBlocks().size() > topGuardianTown.getTownBlocks().size()) {
				topGuardianTown = guardianTown;
			}
		}
		return topGuardianTown.getNation();
	}

	private static boolean ensureTownIsPeacefullyUnoccupied(Town peacefulTown) throws NotRegisteredException {

		if(TownOccupationController.isTownOccupied(peacefulTown)) {
			//Get current occupier
			Nation currentOccupier = TownOccupationController.getTownOccupier(peacefulTown);
			//Remove occupation
			TownOccupationController.removeTownOccupation(peacefulTown);
			//Send messages
			if(peacefulTown.hasNation()) {
				TownyMessaging.sendPrefixedNationMessage(currentOccupier, Translation.of("msg_nation_town_peacefully_released", peacefulTown.getName(), peacefulTown.getNation().getName()));
				TownyMessaging.sendPrefixedNationMessage(peacefulTown.getNation(), Translation.of("msg_nation_town_peacefully_released", peacefulTown.getFormattedName(), peacefulTown.getNation().getName()));
			} else {
				TownyMessaging.sendPrefixedNationMessage(currentOccupier, Translation.of("msg_neutral_town_peacefully_released", peacefulTown.getName()));
				TownyMessaging.sendPrefixedTownMessage(peacefulTown, Translation.of("msg_neutral_town_peacefully_released", peacefulTown.getName()));
			}
			return true;
		} else {
			return false;
		}
	}

	private static boolean ensureTownIsPeacefullyOccupied(Town peacefulTown, Nation newOccupier) throws NotRegisteredException {
		if(TownOccupationController.isTownOccupied(peacefulTown)) {
			//Town is already occupied
			Nation currentOccupier = TownOccupationController.getTownOccupier(peacefulTown);

			//Check if town is already occupied by correct nation
			if(currentOccupier == newOccupier)
				return false;

			//Change occupation
			TownOccupationController.setTownOccupation(peacefulTown, newOccupier);
			//Send messages
			if(peacefulTown.hasNation()) {
				//Send to nation of peaceful town
				TownyMessaging.sendPrefixedNationMessage(peacefulTown.getNation(), Translation.of("msg_nation_town_peacefully_occupied", peacefulTown.getName(), peacefulTown.getNation().getName(), newOccupier.getName()));
				//Send to previous occupier
				TownyMessaging.sendPrefixedNationMessage(currentOccupier, Translation.of("msg_nation_town_peacefully_occupied", peacefulTown.getName(), peacefulTown.getNation().getName(), newOccupier.getName()));
				//Send to new occupier
				TownyMessaging.sendPrefixedNationMessage(newOccupier, Translation.of("msg_nation_town_peacefully_occupied", peacefulTown.getName(), peacefulTown.getNation().getName(), newOccupier.getName()));
			} else {
				//Send to peaceful town
				TownyMessaging.sendPrefixedTownMessage(peacefulTown, Translation.of("msg_neutral_town_peacefully_occupied", peacefulTown.getName()));
				//Send to previous occupier
				TownyMessaging.sendPrefixedNationMessage(currentOccupier, Translation.of("msg_neutral_town_peacefully_occupied", peacefulTown.getName()));
				//Send to new occupier
				TownyMessaging.sendPrefixedNationMessage(newOccupier, Translation.of("msg_neutral_town_peacefully_occupied", peacefulTown.getName()));
			}
		} else {
			//Town is not yet occupied
			//Occupy town
			TownOccupationController.setTownOccupation(peacefulTown, newOccupier);
			//Send to peaceful town
			TownyMessaging.sendPrefixedTownMessage(peacefulTown, Translation.of("msg_your_town_peacefully_occupied", newOccupier.getName()));
			//Send to new occupier
			TownyMessaging.sendPrefixedNationMessage(newOccupier, Translation.of("msg_home_town_peacefully_occupied", peacefulTown.getName()));
			//Send messages
			if(peacefulTown.hasNation()) {
				//Send to nation of peaceful town
				TownyMessaging.sendPrefixedNationMessage(peacefulTown.getNation(), Translation.of("msg_home_town_peacefully_occupied", peacefulTown.getName(), newOccupier.getName()));
			}
		}
		return true; //Town switched
	}

	public static Set<Town> getGuardianTowns(Town peacefulTown) {
		Set<Town> validGuardianTowns = new HashSet<>();
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		try {
			int guardianTownPlotsRequirement = SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement();
			int guardianTownMaxDistanceRequirementTownblocks = SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement();
	
			//Find valid guardian towns
			List<Town> candidateTowns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
			for(Town candidateTown: candidateTowns) {
				if(!candidateTown.isNeutral()
					&& candidateTown.hasNation()
					&& candidateTown.isOpen()
					&& !TownOccupationController.isTownOccupied(candidateTown)
					&& !SiegeController.hasActiveSiege(candidateTown)
					&& candidateTown.getTownBlocks().size() >= guardianTownPlotsRequirement
					&& SiegeWarDistanceUtil.areTownsClose(peacefulTown, candidateTown, guardianTownMaxDistanceRequirementTownblocks)) {
					validGuardianTowns.add(candidateTown);
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
		return validGuardianTowns;
	}
}
