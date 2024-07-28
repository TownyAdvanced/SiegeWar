package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreInvadeEvent;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to invade towns
 *
 * @author Goosius
 */
public class InvadeTown {


	/**
	 * Process an invade town request
	 *
	 * @param siege the siege of the town.
	 * @throws TownyException when the invasion won't be allowed.
	 */
	public static void processInvadeTownRequest(Player player, Nation residentsNation, Town targetTown, Siege siege) throws TownyException {
		
		// Throw an exception if this is not an allowable invasion.
		allowInvasionOrThrow(player, residentsNation, targetTown, siege);

		PreInvadeEvent preEvent = new PreInvadeEvent(player, residentsNation, targetTown, siege);
		Bukkit.getPluginManager().callEvent(preEvent);
		if (preEvent.isCancelled()) {
			if (!preEvent.getCancellationMsg().isEmpty())
				Messaging.sendErrorMsg(player, preEvent.getCancellationMsg());
		} else {
			invadeTown(residentsNation, targetTown, siege);	
		}
	}

	/**
	 * Invade the town
	 *
	 * @param siege the siege
	 */
    public static void invadeTown(Nation invadingNation, Town targetTown, Siege siege)  {
		Nation nationOfInvadedTown = targetTown.getNationOrNull();

		/*
		 * Messaging
		 * This section is here rather than the customary bottom of the method
		 * because we want to send the siegewar messages (town invaded, nation defeated etc.)
		 * before we send the standard towny messages (town has left nation, nation has been deleted etc.)
		 */
		if(nationOfInvadedTown == null) {
			Messaging.sendGlobalMessage(
					Translatable.of("msg_neutral_town_invaded",
							targetTown.getName(),
							invadingNation.getName()
					));
		} else {
			Messaging.sendGlobalMessage(
					Translatable.of("msg_nation_town_invaded",
							targetTown.getName(),
							nationOfInvadedTown.getName(),
							invadingNation.getName()
					));
		}
		if(nationOfInvadedTown != null && nationOfInvadedTown.getNumTowns() == 1) {
			Messaging.sendGlobalMessage(
					Translatable.of("msg_siege_war_nation_defeated",nationOfInvadedTown.getName()));
		}

		//Update nation stats
		NationMetaDataController.setTotalTownsGained(invadingNation, NationMetaDataController.getTotalTownsGained(invadingNation) + 1);
		invadingNation.save();
		if(nationOfInvadedTown != null) {
			NationMetaDataController.setTotalTownsLost(nationOfInvadedTown, NationMetaDataController.getTotalTownsLost(nationOfInvadedTown) + 1);
			nationOfInvadedTown.save();
		}

		//Update siege flags & save siege data
		siege.setTownInvaded(true);
		SiegeController.saveSiege(siege);

		//Occupy town
		TownOccupationController.setTownOccupation(targetTown, invadingNation);
	}

	private static void allowInvasionOrThrow(Player player, Nation residentsNation, Town targetTown, Siege siege) throws TownyException {
		final Translator translator = Translator.locale(player);
		if(!SiegeWarSettings.getWarSiegeInvadeEnabled())
			throw new TownyException(translator.of("msg_err_invading_disabled_in_the_config"));

		if(residentsNation == null)
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_nation_member"));  //Can't invade if nationless

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_INVADE.getNode()))
			throw new TownyException(translator.of("msg_err_cannot_invade_not_enough_permissions"));

		if(siege.getStatus().isActive())
			throw new TownyException(translator.of("msg_err_cannot_invade_siege_still_in_progress"));

		if(TownOccupationController.isTownOccupiedByNation(residentsNation, targetTown))
			throw new TownyException(translator.of("msg_err_cannot_invade_town_already_occupied"));

		if(residentsNation != siege.getAttacker())
			throw new TownyException(translator.of("msg_err_your_nation_is_not_the_attacking_nation_cannot_invade"));

		if (!siege.getStatus().allowsInvading())
			throw new TownyException(translator.of("msg_err_cannot_invade_without_victory"));

		if (siege.isTownInvaded())
			throw new TownyException(translator.of("msg_err_town_already_invaded"));

		//prevent capitals from being invaded.
		if(siege.getTown().hasNation()){
			if(siege.getTown().getNation().getCapital() == siege.getTown()){
				throw new TownyException(translator.of("msg_err_capital_town"));
			}
		}

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByWorld(residentsNation, targetTown);

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByDistance(residentsNation, targetTown);

		SiegeWarNationUtil.throwIfNationHasTooManyTowns(residentsNation);
	}
}
