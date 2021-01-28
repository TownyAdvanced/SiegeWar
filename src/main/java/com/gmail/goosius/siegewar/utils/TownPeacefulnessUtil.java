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
		town.save();
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
	 *
	 * Rules for peaceful towns
	 * If there are no guardian towns nearby - Town has free choice of nation
	 * If there are guardian towns nearby, and just 1 is NOT under siege - Town must choose the nation of that town
	 * If there are guardian towns nearby, and more than 1 is NOT under siege - Town must choose the nation of one of those towns
	 * If there are guardian towns nearby, but all are under siege - Town cannot have any nation
	 */
	public static void evaluatePeacefulTownNationAssignments() {
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

				//Find guardian towns
				Set<Town> guardianTowns = getValidGuardianTowns(peacefulTown);

				//If there are any guardian towns nearby, town does not have totally free nation choice
				if(guardianTowns.size() > 0) {
					List<Town> guardianTownsNotUnderSiege = new ArrayList<>();
					for (Town guardianTown : guardianTowns) {
						if (!SiegeController.hasActiveSiege(guardianTown))
							guardianTownsNotUnderSiege.add(guardianTown);
					}

					if (guardianTownsNotUnderSiege.size() == 0) {
						//If all guardian towns are under siege - Town is removed from nation
						townTransferred = ensurePeacefulTownJoinsNationOfGuardianTown(peacefulTown, null);

					} else if (guardianTownsNotUnderSiege.size() == 1) {
						//If just 1 guardian town is NOT under siege - Join its nation
						townTransferred = ensurePeacefulTownJoinsNationOfGuardianTown(peacefulTown, guardianTownsNotUnderSiege.get(0));

					} else {
						//If more than 1 guardian town is NOT under siege - Ensure town is in one of their nations
						townTransferred = ensurePeacefulTownJoinNationOfOneGuardianTown(peacefulTown, guardianTownsNotUnderSiege);
					}

					if (townTransferred)
						modifiedTowns += 1;
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
			boolean one = modifiedTowns == 1;
			Messaging.sendGlobalMessage(Translation.of("msg_war_siege_peaceful_town_total_switches", modifiedTowns, one ? "" : "s", one ? "has" : "have"));
		}
	}

	private static boolean ensurePeacefulTownJoinsNationOfGuardianTown(Town peacefulTown, Town guardianTown) throws Exception {
		Nation guardianNation = guardianTown.getNation();

		if(guardianNation == null) {
			if(!peacefulTown.hasNation()) {
				//Already nationless
				return false;

			} else {
				//Remove town from nation
				Nation previousNation = peacefulTown.getNation();
				TownyMessaging.sendPrefixedNationMessage(previousNation, Translation.of("msg_war_siege_peaceful_town_left_nation", peacefulTown.getFormattedName(), previousNation.getFormattedName()));
				peacefulTown.removeNation();
				peacefulTown.save();
				peacefulTown.getNation().save();
				return true;
			}

		} else {
			if(!peacefulTown.hasNation()) {
				//Join nation
				peacefulTown.setNation(guardianNation);
				TownyMessaging.sendPrefixedNationMessage(guardianNation, Translation.of("msg_war_siege_peaceful_town_joined_nation", peacefulTown.getFormattedName(), guardianNation.getFormattedName()));
				peacefulTown.save();
				guardianNation.save();
				return true;

			} else if (peacefulTown.getNation() == guardianNation){
				//Already in required nation
				return false;

			} else {
				//Transfer town to new nation
				Nation previousNation = peacefulTown.getNation();
				peacefulTown.removeNation();
				peacefulTown.setNation(guardianNation);
				TownyMessaging.sendPrefixedNationMessage(previousNation, Translation.of("msg_war_siege_peaceful_town_changed_nation", peacefulTown.getFormattedName(), previousNation.getFormattedName(), guardianNation.getFormattedName()));
				TownyMessaging.sendPrefixedNationMessage(guardianNation, Translation.of("msg_war_siege_peaceful_town_changed_nation", peacefulTown.getFormattedName(), previousNation.getFormattedName(), guardianNation.getFormattedName()));
				peacefulTown.save();
				guardianNation.save();
				return true;
			}
		}
	}

	//Join one of the given nations
	private static boolean ensurePeacefulTownJoinNationOfOneGuardianTown(Town peacefulTown, List<Town> eligibleGuardianTowns) throws Exception{
		for (Town guardianTown : eligibleGuardianTowns) {
			if (peacefulTown.getNation() == guardianTown.getNation())
			return false;
		}
		//Transfer town to the nation with the largest guardian town
		Town topGuardianTown = null;
		for(Town guardianTown: eligibleGuardianTowns) {
			if(topGuardianTown == null || guardianTown.getTownBlocks().size() > topGuardianTown.getTownBlocks().size()) {
				topGuardianTown = guardianTown;
			}
		}
		peacefulTown.setNation(topGuardianTown.getNation());
		peacefulTown.save();
		peacefulTown.getNation().save();
		return true;
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
			int guardianTownPlotsRequirement = SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement();
			int guardianTownMaxDistanceRequirementTownblocks = SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement();
	
			//Find valid guardian towns
			List<Town> candidateTowns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
			for(Town candidateTown: candidateTowns) {
				if(!candidateTown.isNeutral()
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
		town.save();
	}
}
