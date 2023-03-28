package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreInvadeEvent;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
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
		Nation nationOfInvadedTown = targetTown.hasNation() ? targetTown.getNationOrNull() : null;

		//Update nation stats
		NationMetaDataController.setTotalTownsGained(invadingNation, NationMetaDataController.getTotalTownsGained(invadingNation) + 1);
		if(nationOfInvadedTown != null) {
            NationMetaDataController.setTotalTownsLost(nationOfInvadedTown, NationMetaDataController.getTotalTownsLost(nationOfInvadedTown) + 1);
        }

		//Occupy town (This also saves town & nation data)
		TownOccupationController.setTownOccupation(targetTown, invadingNation);

		//Update siege flags & save siege data
		siege.setTownInvaded(true);
		SiegeController.saveSiege(siege);

		//Messaging
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
    }

	private static void allowInvasionOrThrow(Player player, Nation residentsNation, Town targetTown, Siege siege) throws TownyException {
		final Translator translator = Translator.locale(Translation.getLocale(player));
		if(!SiegeWarSettings.getWarSiegeInvadeEnabled())
			throw new TownyException(translator.of("msg_err_action_disable"));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_INVADE.getNode()))
			throw new TownyException(translator.of("msg_err_action_disable"));

		if(residentsNation == null)
			throw new TownyException(translator.of("msg_err_action_disable"));  //Can't invade if nationless

		if(siege.getStatus().isActive())
			throw new TownyException(translator.of("msg_err_cannot_invade_siege_still_in_progress"));

		if(TownOccupationController.isTownOccupiedByNation(residentsNation, targetTown))
			throw new TownyException(translator.of("msg_err_cannot_invade_town_already_occupied"));

		if(residentsNation != siege.getAttacker())
			throw new TownyException(translator.of("msg_err_action_disable"));  //Can't invade unless you are the attacker

		if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
			throw new TownyException(translator.of("msg_err_cannot_invade_without_victory"));

		if (siege.isTownInvaded())
			throw new TownyException(translator.of("msg_err_town_already_invaded"));

		if (SiegeWarDistanceUtil.isTownTooFarFromNationCapitalByWorld(residentsNation, targetTown))
			throw new TownyException(translator.of("msg_err_nation_homeblock_in_another_world"));

		if (SiegeWarDistanceUtil.isTownTooFarFromNationCapitalByDistance(residentsNation, targetTown))
			throw new TownyException(String.format(translator.of("msg_err_town_not_close_enough_to_nation"), targetTown.getName()));

		if (SiegeWarNationUtil.doesNationHaveTooManyTowns(residentsNation))
			throw new TownyException(String.format(translator.of("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
	}



}
