package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSubvertTownEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

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
	 * @param targetTown the target town. We know the player is not a resident.
	 *
	 * @throws TownyException if subvert is not allowed
	 */
	public static void processActionRequest(Player player, Nation residentsNation, Town targetTown) throws TownyException {
		// Throws an exception if the peaceful subversion of this town would not be allowed.
		allowSubversionOrThrow(player, residentsNation, targetTown);

		PreSubvertTownEvent preEvent = new PreSubvertTownEvent(player, residentsNation, targetTown);
		Bukkit.getPluginManager().callEvent(preEvent);
		if (preEvent.isCancelled()) {
			if (!preEvent.getCancellationMsg().isEmpty())
				Messaging.sendErrorMsg(player, preEvent.getCancellationMsg());
		} else {
			//Subvert town now
			subvertTown(residentsNation, targetTown);
		}
	}
	
	/**
	 * Subvert the town
	 *
	 * @param subvertingNation the nation doing the subverting
	 * @param targetTown the target town
	 */
	private static void subvertTown(Nation subvertingNation, Town targetTown) {
		//Occupy town (also saves data)
		TownOccupationController.setTownOccupation(targetTown, subvertingNation);
		
		//Save to db
		targetTown.save();
		
		/*
		 * Messaging
		 *
		 * Note that we do not publicly mention the nation (if any) of the subverted town.
		 * Because the logic is simpler, and because subverting is generally less 'aggressive' than invasion.
		 */
		Messaging.sendGlobalMessage(
			Translatable.of("msg_peaceful_town_subverted",
					targetTown.getName(),
					subvertingNation.getName()
		));
	}

	private static void allowSubversionOrThrow(Player player, Nation residentsNation, Town targetTown) throws TownyException {
		final Translator translator =  Translator.locale(Translation.getLocale(player));
		if(!SiegeWarSettings.isPeacefulTownsSubvertEnabled())
			throw new TownyException(translator.of("msg_err_action_disable"));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_SUBVERTPEACEFULTOWN.getNode()))
			throw new TownyException(translator.of("msg_err_action_disable"));

		if(residentsNation == null)
			throw new TownyException(translator.of("msg_err_action_disable"));  //Can't subvert if nationless

		if(SiegeController.hasActiveSiege(targetTown))
			throw new TownyException(translator.of("msg_err_cannot_change_occupation_of_besieged_town"));

		if(targetTown.hasNation() && targetTown.getNationOrNull() == residentsNation)
			throw new TownyException(translator.of("msg_err_cannot_subvert_towns_in_own_nation"));

		if(TownOccupationController.isTownOccupiedByNation(residentsNation, targetTown))
			throw new TownyException(translator.of("msg_err_cannot_subvert_town_already_occupied"));

		if(SiegeWarDistanceUtil.isTownTooFarFromNationCapitalByWorld(residentsNation, targetTown))
			throw new TownyException(translator.of("msg_err_nation_homeblock_in_another_world"));

		if(SiegeWarDistanceUtil.isTownTooFarFromNationCapitalByDistance(residentsNation, targetTown))
			throw new TownyException(String.format(translator.of("msg_err_town_not_close_enough_to_nation"), targetTown.getName()));

		if(SiegeWarNationUtil.doesNationHaveTooManyTowns(residentsNation))
			throw new TownyException(String.format(translator.of("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
		
		verifyThatNationHasEnoughTownyInfluenceToSubvertTown(residentsNation, targetTown);
	}

	/**
	 * Verify if the given nation has enough Towny-Influence to subvert the given town
	 *
	 * @param nation the nation attempting subversion
	 * @param targetTown the town targeted for subversion
	 *
	 * @throws TownyException if the nation does not have enough Towny-Influence
	 */
	private static void verifyThatNationHasEnoughTownyInfluenceToSubvertTown(Nation nation, Town targetTown) throws TownyException {
		Map<Nation, Integer> townyInfluenceMap = TownPeacefulnessUtil.calculateTownyInfluenceMap(targetTown);
		if(townyInfluenceMap.size() == 0)
			//No nation has towny-influence in the local area
			throw new TownyException(Translation.of("msg_err_cannot_subvert_town_zero_influence"));

		Nation topNation = townyInfluenceMap.keySet().iterator().next();
		if(topNation != nation)
			//A different nation is top of the towny-influence map
			throw new TownyException(Translation.of("msg_err_cannot_subvert_town_insufficient_influence", topNation.getName(),
					townyInfluenceMap.get(topNation),            // Top scorer. 
					townyInfluenceMap.getOrDefault(nation, 0))); // The nation's score.
	}
}
