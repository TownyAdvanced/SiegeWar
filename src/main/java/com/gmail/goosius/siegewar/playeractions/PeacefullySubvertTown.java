package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSubvertTownEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests by nations to peacefully 'subvert' towns.
 *
 * If such a request is successful, the target town gets subverted immediately a.k.a occupied.
 *
 * @author Goosius
 */
public class PeacefullySubvertTown {

	/**
	 * Process a subvert town request
	 *
	 * @param player the player attempting the subvert.
	 * @param residentsNation the nation of the player (can be null)
	 * @param targetPeacefulTown the target peaceful town. We know the town is peaceful, and the player is not a resident.
	 *
	 * @throws TownyException if subvert is not allowed
	 */
	public static void processActionRequest(Player player, Nation residentsNation, Town targetPeacefulTown) throws TownyException {
		// Throws an exception if the peaceful subversion of this town would not be allowed.
		allowSubversionOrThrow(player, residentsNation, targetPeacefulTown);

		PreSubvertTownEvent preEvent = new PreSubvertTownEvent(player, residentsNation, targetPeacefulTown);
		Bukkit.getPluginManager().callEvent(preEvent);
		if (preEvent.isCancelled()) {
			if (!preEvent.getCancellationMsg().isEmpty())
				Messaging.sendErrorMsg(player, preEvent.getCancellationMsg());
		} else {
			//Subvert town now
			subvertTown(residentsNation, targetPeacefulTown);
		}
	}
	
	/**
	 * Subvert the town
	 *
	 * @param subvertingNation the nation doing the subverting
	 * @param targetTown the target town
	 */
	private static void subvertTown(Nation subvertingNation, Town targetTown) {
		/*
		 * Messaging
		 * This section is here rather than the customary bottom of the method
		 * because we want to send the siegewar messages (town invaded, nation defeated etc.)
		 * before we send the standard towny messages (town has left nation, nation has been deleted etc.)
		 */
		Messaging.sendGlobalMessage(
			Translatable.of("msg_peaceful_town_subverted",
					targetTown.getName(),
					subvertingNation.getName()
		));
		Nation nationOfSubvertedTown = targetTown.getNationOrNull();
		if(nationOfSubvertedTown != null && nationOfSubvertedTown.getNumTowns() == 1) {
			Messaging.sendGlobalMessage(
					Translatable.of("msg_siege_war_nation_defeated", nationOfSubvertedTown.getName()));
		}

		//Occupy town (also saves data)
		TownOccupationController.setTownOccupation(targetTown, subvertingNation);
	}

	private static void allowSubversionOrThrow(Player player, Nation residentsNation, Town targetPeacefulTown) throws TownyException {
		final Translator translator =  Translator.locale(player);
		if(!SiegeWarSettings.isPeacefulTownsSubvertEnabled())
			throw new TownyException(translator.of("msg_err_action_disable"));

		if(residentsNation == null)
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_nation_member"));  //Can't subvert if nationless

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_SUBVERTPEACEFULTOWN.getNode()))
			throw new TownyException(translator.of("msg_err_cannot_subvert_not_enough_permissions"));

		if(SiegeController.hasActiveSiege(targetPeacefulTown))
			throw new TownyException(translator.of("msg_err_cannot_change_occupation_of_besieged_town"));

		if(TownOccupationController.isTownOccupiedByNation(residentsNation, targetPeacefulTown))
			throw new TownyException(translator.of("msg_err_cannot_subvert_town_already_occupied"));

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByWorld(residentsNation, targetPeacefulTown);

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByDistance(residentsNation, targetPeacefulTown);

		SiegeWarNationUtil.throwIfNationHasTooManyTowns(residentsNation);
		
		throwIfGuardianTownExistsAndSubverterDoesNotOwnIt(translator, targetPeacefulTown, residentsNation);
	}

	/**
	 * Throw if the given nation does not own the guardian town of the target peaceful town.
	 * 
	 * @param subvertingNation the nation attempting the subversion
	 */
	public static void throwIfGuardianTownExistsAndSubverterDoesNotOwnIt(Translator translator, Town targetPeacefulTown, Nation subvertingNation) throws TownyException {
		Town guardianTown = SiegeWarTownPeacefulnessUtil.calculateGuardianTown(targetPeacefulTown);
		if(guardianTown == null)
			return;  //There is no guardian town. Subversion allowed

		if(!guardianTown.hasNation())
			throw new TownyException(translator.of("msg_err_cannot_subvert_dont_own_guardian_town", guardianTown.getName()));

		if(guardianTown.getNationOrNull() != subvertingNation)
			throw new TownyException(translator.of("msg_err_cannot_subvert_dont_own_guardian_town", guardianTown.getName()));
	}

}
