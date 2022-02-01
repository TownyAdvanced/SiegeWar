package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests by nations to peacefully 'subvert' towns.
 * 
 * If such a request successful, the target town gets subverted immediately a.k.a occupied.
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
		if(!SiegeWarSettings.getPeacefulTownsSubvertEnabled())
			throw new TownyException(Translation.of("msg_err_action_disable"));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SUBVERTPEACEFULTOWN.getNode()))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		if(residentsNation == null)
			throw new TownyException(Translation.of("msg_err_action_disable"));  //Can't subvert if nationless

		if(TownOccupationController.isTownOccupied(targetTown) && TownOccupationController.getTownOccupier(targetTown) == residentsNation)
			throw new TownyException(Translation.of("msg_err_cannot_subvert_town_already_occupied"));

		if (TownySettings.getNationRequiresProximity() > 0) {
			Coord capitalCoord = residentsNation.getCapital().getHomeBlock().getCoord();
			Coord townCoord = targetTown.getHomeBlock().getCoord();
			if (!residentsNation.getCapital().getHomeBlock().getWorld().getName().equals(targetTown.getHomeBlock().getWorld().getName())) {
				throw new TownyException(Translation.of("msg_err_nation_homeblock_in_another_world"));
			}
			double distance;
			distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
			if (distance > TownySettings.getNationRequiresProximity()) {
				throw new TownyException(String.format(Translation.of("msg_err_town_not_close_enough_to_nation"), targetTown.getName()));
			}
		}

		if (TownySettings.getMaxTownsPerNation() > 0) {
			int effectiveNumTowns = SiegeWarNationUtil.getEffectiveNation(residentsNation).getNumTowns();
			if (effectiveNumTowns >= TownySettings.getMaxTownsPerNation()){
				throw new TownyException(String.format(Translation.of("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
			}
		}

		//Verify if the nation has enough "Towny Influence" to subvert the town
		verifyIfNationHasEnoughTownyInfluenceToSubvertTown(residentsNation, targetTown);

		//Subvert town now
		subvertTown(residentsNation, targetTown);
	}

	/**
	 * Verify if the given nation has enough Towny-Influence to subvert the given town
	 * 
	 * @throws TownyException if the nation does not have enough Towny-Influence
	 */
	private static void verifyIfNationHasEnoughTownyInfluenceToSubvertTown(Nation residentsNation, Town targetTown) throws TownyException {
		
	}
	
	/**
	 * Subvert the town
	 *
	 * @param subvertingNation the nation doing the subverting
	 * @param targetTown the target town
	 */
    private static void subvertTown(Nation subvertingNation, Town targetTown) {
		//Set town to occupied
		TownOccupationController.setTownOccupation(targetTown, subvertingNation);

		//Save to db
		subvertingNation.save();
        targetTown.save();
		
		/*
		 * Messaging
		 *
		 * Note that we do not publicly mention the nation (if any) of the subverted town.
		 * Because the logic is simpler, and because subverting is generally less 'aggressive' than invasion.
		 */
		Messaging.sendGlobalMessage(
			Translation.of("msg_peaceful_town_subverted",
					targetTown.getName(),
					subvertingNation.getName()
		));
    }
}
