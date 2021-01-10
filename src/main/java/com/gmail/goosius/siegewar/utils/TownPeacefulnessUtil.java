package com.gmail.goosius.siegewar.utils;


import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
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
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyPermission;
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
			disableTownPVP(town);	

		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if (town.isNeutral()) {
				message = Translation.of("msg_war_siege_town_became_peaceful", town.getFormattedName());
			} else {
				message = Translation.of("msg_war_siege_town_became_non_peaceful", town.getFormattedName());
			}
		} else {
			if (town.isNeutral()) {
				message = Translation.of("msg_war_common_town_became_peaceful", town.getFormattedName());
			} else {
				message = Translation.of("msg_war_common_town_became_non_peaceful", town.getFormattedName());
			}
		}
		TownyMessaging.sendPrefixedTownMessage(town, message);
		TownyUniverse.getInstance().getDataSource().saveTown(town);
	}

	/**
	 * This method punishes any peaceful players who are in siege-zones
	 * (except for their own town OR any peaceful town)
	 * 
	 * A player is peaceful if they
	 * 1. Are resident in a peaceful town
	 * 2. Are resident in a declared (but not confirmed) peaceful town
	 * 3. Were recently resident in a peaceful town
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
	 * This method is a cleanup of peaceful town assignments
	 * 
	 * Cycle peaceful towns
	 * - If town's nation is valid, skip and go to next town
	 * - If town's nation is invalid, reassign nation status
	 */
	public static void evaluatePeacefulTownNationAssignments() {
		TownyUniverse townyUniverse = TownyUniverse.getInstance();
		List<Town> towns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
		ListIterator<Town> townItr = towns.listIterator();
		Town peacefulTown = null;
		int modifiedTowns = 0;

		CYCLE_ALL_TOWNS:
		while (townItr.hasNext()) {
			peacefulTown = townItr.next();

			try {
				//Skip if town is non-peaceful
				if (!peacefulTown.isNeutral())
					continue;

				//Find guardian towns
				Set<Town> guardianTowns = getValidGuardianTowns(peacefulTown);

				//If the nation assignment ok?
				if (peacefulTown.hasNation()) {
					for (Town guardianTown : guardianTowns) {
						if (peacefulTown.getNation() == guardianTown.getNation())
							continue CYCLE_ALL_TOWNS; //No change needed
					}
				} else {
					if (guardianTowns.size() == 0)
						continue; //No change needed
				}

				//Nation status change needed
				modifiedTowns += 1;

				if (guardianTowns.size() == 0) {
					//Guardian town list was empty. Peaceful town leaves nation
					Nation previousNation = peacefulTown.getNation();
					TownyMessaging.sendPrefixedNationMessage(previousNation, Translation.of("msg_war_siege_peaceful_town_left_nation", peacefulTown.getFormattedName(), previousNation.getFormattedName()));
					peacefulTown.removeNation();
				} else {
					//Find guardian nation (the one with the largest guardian town)
					Town topGuardianTown = null;
					for(Town guardianTown: guardianTowns) {
						if(topGuardianTown == null || guardianTown.getTownBlocks().size() > topGuardianTown.getTownBlocks().size()) {
							topGuardianTown = guardianTown;
						}
					}
					Nation guardianNation = topGuardianTown.getNation();

					//Change town's nation
					if (peacefulTown.hasNation()) {
						//Peaceful town moves from one nation to another
						Nation previousNation = peacefulTown.getNation();
						peacefulTown.removeNation();
						peacefulTown.setNation(guardianNation);
						TownyMessaging.sendPrefixedNationMessage(previousNation, Translation.of("msg_war_siege_peaceful_town_changed_nation", peacefulTown.getFormattedName(), previousNation.getFormattedName(), guardianNation.getFormattedName()));
						TownyMessaging.sendPrefixedNationMessage(guardianNation, Translation.of("msg_war_siege_peaceful_town_changed_nation", peacefulTown.getFormattedName(), previousNation.getFormattedName(), guardianNation.getFormattedName()));
					} else {
						//Peaceful town joins nation
						peacefulTown.setNation(guardianNation);
						TownyMessaging.sendPrefixedNationMessage(guardianNation, Translation.of("msg_war_siege_peaceful_town_joined_nation", peacefulTown.getFormattedName(), guardianNation.getFormattedName()));
					}
					// .setNation() does not save the Town internally.
					TownyUniverse.getInstance().getDataSource().saveTown(peacefulTown);
				}
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
			Boolean one = modifiedTowns == 1;
			Messaging.sendGlobalMessage(Translation.of("msg_war_siege_peaceful_town_total_switches", modifiedTowns, one ? "" : "s", one ? "has" : "have"));
		}
	}

	public static Set<Nation> getValidGuardianNations(Town peacefulTown) {
		Set<Town> validGuardianTowns = getValidGuardianTowns(peacefulTown);
		Set<Nation> validGuardianNations = new HashSet<>();
		for(Town validGuardianTown: validGuardianTowns) {
			try {
				validGuardianNations.add(validGuardianTown.getNation());
			} catch (NotRegisteredException e) {}
		}
		return validGuardianNations;
	}
	
	public static Set<Town> getValidGuardianTowns(Town peacefulTown) {
		Set<Town> validGuardianTowns = new HashSet<>();
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		try {
			int guardianTownPlotsRequirement = SiegeWarSettings.getWarSiegePeacefulTownsGuardianTownPlotsRequirement();
			int guardianTownMaxDistanceRequirementTownblocks = SiegeWarSettings.getWarSiegePeacefulTownsGuardianTownMinDistanceRequirement();
	
			//Find valid guardian towns
			List<Town> candidateTowns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
			for(Town candidateTown: candidateTowns) {
				if(!candidateTown.isNeutral()
					&& !SiegeController.hasSiege(candidateTown)
					&& candidateTown.hasNation()
					&& candidateTown.getNation().isOpen()
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

	public static void disableTownPVP(Town town) {
		if (town.isPVP())
				town.setPVP(false);

		for (TownBlock plot : town.getTownBlocks()) {
			if (plot.hasPlotObjectGroup()) {
				TownyPermission groupPermissions = plot.getPlotObjectGroup().getPermissions();
				if (groupPermissions.pvp) {
					groupPermissions.pvp = false;
					plot.getPlotObjectGroup().setPermissions(groupPermissions);
				}
			} 	
			if (plot.getPermissions().pvp) {
				if (plot.getType() == TownBlockType.ARENA)
					plot.setType(TownBlockType.RESIDENTIAL);
			
				plot.getPermissions().pvp = false;
				plot.setChanged(true);
			}
		}
		TownyUniverse.getInstance().getDataSource().saveTown(town);
	}
}
