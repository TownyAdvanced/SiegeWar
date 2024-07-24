package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.util.TimeMgmt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;

public class SiegeWarTownPeacefulnessUtil {

	public static boolean isTownPeaceful(Town town) {
		return SiegeWarSettings.getWarCommonPeacefulTownsEnabled() && TownMetaDataController.getPeacefulness(town);
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
		Translatable message;

		int days = TownMetaDataController.getPeacefulnessChangeCountdownDays(town); 
		if (days > 1) {
			SiegeWarTownPeacefulnessUtil.setTownPeacefulnessChangeCountdownDays(town, --days);
			return;
		}
		TownMetaDataController.setPeacefulnessChangeCountdownDays(town, 0);
		
		// The Town would become a peaceful capital city, which is not allowed.
		if (!SiegeWarSettings.capitalsAllowedTownPeacefulness() && town.isCapital() && !TownMetaDataController.getPeacefulness(town)) {
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_err_your_town_cannot_be_peaceful_while_a_capital_city"));
			return;
		}

		//Reverse the town peacefulness setting
		TownMetaDataController.setPeacefulness(town, !TownMetaDataController.getPeacefulness(town));
		
		if (TownMetaDataController.getPeacefulness(town)) {
			//Remove military ranks
			SiegeWarMilitaryRanksUtil.removeMilitaryRanksFromTownResidents(town);
			message = Translatable.of("msg_town_became_peaceful", town.getName());
		} else {
			message = Translatable.of("msg_town_became_non_peaceful", town.getName());
		}

		TownyMessaging.sendPrefixedTownMessage(town, message);
		town.save();
	}

	public static void toggleTownPeacefulness(Player player) {
		Translator translator = Translator.locale(player);
		if (!SiegeWarSettings.getWarSiegeEnabled()) {
			player.sendMessage(translator.of("msg_err_command_disable"));
			return;
		}

		if(!SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
			player.sendMessage(translator.of("msg_err_command_disable"));
			return;
		}

		Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
		if(resident == null || !resident.hasTown()) {
			player.sendMessage(translator.of("msg_err_command_disable"));
			return;
		}

		//Capital towns cannot go peaceful.
		Town town = resident.getTownOrNull();
		if (town.isCapital() && !SiegeWarSettings.capitalsAllowedTownPeacefulness()) {
			player.sendMessage(translator.of("msg_err_capital_towns_cannot_go_peaceful"));
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
				TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_war_common_town_declared_peaceful", daysRequiredForStatusChange));
			else
				TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_war_common_town_declared_non_peaceful", daysRequiredForStatusChange));
		} else {
			//Countdown in progress. Cancel the countdown
			TownMetaDataController.setDesiredPeacefulness(town, SiegeWarTownPeacefulnessUtil.isTownPeaceful(town));
			TownMetaDataController.setPeacefulnessChangeCountdownDays(town, 0);
			//Send message to town
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_war_common_town_peacefulness_countdown_cancelled"));
		}
		//Save data
		town.save();
	}

	/**
	 * Calculate the guardian town of a given peaceful town
	 * 
	 * @param peacefulTown given peaceful town
	 * @return guardian town
	 */
	public static @Nullable Town calculateGuardianTown(Town peacefulTown) {
		if(!peacefulTown.hasHomeBlock())  //The peaceful town can't have a guardian town if it has no homeblock
			return null;
		Town guardianTown = null;
		int winningNumTownBlocks = 0;
		int searchRadiusInTownBlocks = SiegeWarSettings.getPeacefulTownsGuardianTownSearchRadius() / TownySettings.getTownBlockSize();
		for(Town candidateGuardianTown: TownyAPI.getInstance().getTowns()) {
			//Search all towns to find the guardian town
			if (!candidateGuardianTown.isRuined()
					&& candidateGuardianTown.hasHomeBlock()
					&& !SiegeWarTownPeacefulnessUtil.isTownPeaceful(candidateGuardianTown)
					&& !SiegeController.hasActiveSiege(candidateGuardianTown)
					&& SiegeWarDistanceUtil.areTownsClose(peacefulTown, candidateGuardianTown, searchRadiusInTownBlocks)
					&& candidateGuardianTown.getNumTownBlocks() > winningNumTownBlocks) {
				//New winning candidate found 
				guardianTown = candidateGuardianTown;
				winningNumTownBlocks = candidateGuardianTown.getNumTownBlocks();
			}
		}
		return guardianTown;
	}

	/**
	 * Release peaceful towns on a guardian town homeblock move
	 *
	 * @param townMovingHomeBlock the town about to move its homeblock
	 * @return the number of peaceful towns released from occupation
	 */
	public static int releasePeacefulTownsOnGuardianTownHomeBlockMove(Town townMovingHomeBlock) {
		if(!townMovingHomeBlock.hasHomeBlock())
			return 0;
		if(!townMovingHomeBlock.hasNation())
			return 0;
		Nation nationOfTownMovingHomeBlock = townMovingHomeBlock.getNationOrNull();
		int numPeacefulTownsReleased = 0;
		List<Town> peacefulTownsInOldRadius = SiegeWarDistanceUtil.getNearbyTownsPeacefulTowns(townMovingHomeBlock.getHomeBlockOrNull(), SiegeWarSettings.getPeacefulTownsGuardianTownSearchRadius());
		for(Town peacefulTown: peacefulTownsInOldRadius) {
			if (calculateGuardianTown(peacefulTown) == townMovingHomeBlock
					&& TownOccupationController.isTownOccupied(peacefulTown)
					&& peacefulTown.getNationOrNull() == nationOfTownMovingHomeBlock) {
				TownOccupationController.removeTownOccupation(peacefulTown);
				TownyMessaging.sendPrefixedNationMessage(nationOfTownMovingHomeBlock, Translation.of("msg_nation_town_left", peacefulTown.getName()));
				numPeacefulTownsReleased++;
			}
		}
		return numPeacefulTownsReleased;
	}

}
