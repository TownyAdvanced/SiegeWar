package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;

/**
 * This class is responsible for processing requests to invade towns
 *
 * @author Goosius
 */
public class InvadeTown {

	/**
	 * Process an invade town request
	 *
	 * This method does some final checks and if they pass, the invasion is executed.
	 *
	 * @param nationOfInvadingResident the nation who the invading resident belongs to.
	 * @param townToBeInvaded the town to be invaded
	 * @param siege the siege of the town.
	 * @throws TownyException when the invasion wont be allowed.
	 */
    public static void processInvadeTownRequest(Nation nationOfInvadingResident,
                                                Town townToBeInvaded,
                                                Siege siege) throws TownyException {

    	Nation attackerWinner = siege.getAttackingNation();
		
		if (nationOfInvadingResident != attackerWinner)
			throw new TownyException(Translation.of("msg_err_siege_war_cannot_invade_without_victory"));

        if (siege.isTownInvaded())
            throw new TownyException(String.format(Translation.of("msg_err_siege_war_town_already_invaded"), townToBeInvaded.getName()));

		if(townToBeInvaded.hasNation() && townToBeInvaded.getNation() == attackerWinner)
			throw new TownyException(String.format(Translation.of("msg_err_siege_war_town_already_belongs_to_your_nation"), townToBeInvaded.getName()));

		if (TownySettings.getNationRequiresProximity() > 0) {
			Coord capitalCoord = attackerWinner.getCapital().getHomeBlock().getCoord();
			Coord townCoord = townToBeInvaded.getHomeBlock().getCoord();
			if (!attackerWinner.getCapital().getHomeBlock().getWorld().getName().equals(townToBeInvaded.getHomeBlock().getWorld().getName())) {
				throw new TownyException(Translation.of("msg_err_nation_homeblock_in_another_world"));
			}
			double distance;
			distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
			if (distance > TownySettings.getNationRequiresProximity()) {
				throw new TownyException(String.format(Translation.of("msg_err_town_not_close_enough_to_nation"), townToBeInvaded.getName()));
			}
		}

		if (TownySettings.getMaxTownsPerNation() > 0) {
			if (attackerWinner.getTowns().size() >= TownySettings.getMaxTownsPerNation()){
				throw new TownyException(String.format(Translation.of("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
			}
		}

		captureTown(siege, attackerWinner, townToBeInvaded);

    }

    private static void captureTown(Siege siege, Nation attackingNation, Town defendingTown) {
		Nation nationOfDefendingTown = null;
		boolean nationTown = false;
		boolean nationDefeated = false;
		
        if(defendingTown.hasNation()) {
			nationTown = true;
			
            try {
                nationOfDefendingTown = defendingTown.getNation();
            } catch (NotRegisteredException x) {}

            // This will delete the Nation when it loses its last town, mark them defeated.
            if(nationOfDefendingTown.getTowns().size() == 1) {
				nationDefeated = true;
				NationMetaDataController.incrementDefeatedNations(attackingNation);   
			} else
				NationMetaDataController.setTotalTownsLost(nationOfDefendingTown, NationMetaDataController.getTotalTownsLost(nationOfDefendingTown) + 1);
            
			//Remove town from nation (and nation itself if empty)
            defendingTown.removeNation();
        }
        
        // Add town to nation.
        try {
			defendingTown.setNation(attackingNation);
		} catch (AlreadyRegisteredException ignored) {}
        
        //Set flags to indicate success
		siege.setTownInvaded(true);
		defendingTown.setConquered(true);
		
		SiegeWarTownUtil.disableNationPerms(defendingTown);

		NationMetaDataController.setTotalTownsGained(attackingNation, NationMetaDataController.getTotalTownsGained(attackingNation) + 1);

		//Save to db
        SiegeController.saveSiege(siege);
		defendingTown.save();
		attackingNation.save();
		if(nationTown && !nationDefeated) {
			nationOfDefendingTown.save();
		}
		
		//Messaging
		if(nationTown) {
			Messaging.sendGlobalMessage(
				Translation.of("msg_siege_war_nation_town_captured",
				defendingTown.getFormattedName(),
				nationOfDefendingTown.getFormattedName(),
				attackingNation.getFormattedName()
			));
		} else {
			Messaging.sendGlobalMessage(
				Translation.of("msg_siege_war_neutral_town_captured",
				defendingTown.getFormattedName(),
				attackingNation.getFormattedName()
			));
		}
		if(nationDefeated) {
			Messaging.sendGlobalMessage(
				Translation.of("msg_siege_war_nation_defeated",
				nationOfDefendingTown.getFormattedName()
			));
		}
    }
}
