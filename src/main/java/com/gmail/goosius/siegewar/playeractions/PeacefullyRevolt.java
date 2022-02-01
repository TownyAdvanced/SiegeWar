package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests by towns to peacefully revolt.
 * 
 * If such a request is successful, the relevant towns gets immediately freed from occupation.
 * 
 * @author Goosius
 */

public class PeacefullyRevolt {

    /**
     * Process a request by a town member to peacefully revolt.
     *
     * @param player               the player
     * @param targetTown           the revolting peaceful town, where we already know the player is a resident
     * @throws TownyException if the revolt request fails.
     */
    public static void processActionRequest(Player player,
                                            Town targetTown) throws TownyException {

        if (!SiegeWarSettings.isPeacefulTownsRevoltEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_REVOLT_PEACEFULLY.getNode()))
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if(!TownOccupationController.isTownOccupied(targetTown))
            throw new TownyException(Translation.of("msg_err_cannot_peacefully_revolt_because_unoccupied"));

		if(SiegeController.hasActiveSiege(targetTown)) {
			throw new TownyException(Translation.of("msg_err_cannot_change_occupation_of_besieged_town"));
		}

		verifyThatOccupierHasZeroTownyInfluence(targetTown);

        peacefullyRevolt(targetTown);
    }
    
    private static void peacefullyRevolt(Town revoltingTown) {
        //Remove occupation
        Nation occupier = TownOccupationController.getTownOccupier(revoltingTown);
        TownOccupationController.removeTownOccupation(revoltingTown);
        
        //Save to db
        revoltingTown.save();
		
        //Messaging
		Messaging.sendGlobalMessage(
			Translation.of("msg_peaceful_town_revolted",
					revoltingTown.getName(),
					occupier.getName()
		));
    }

    /**
	 * Verify that the occupier has zero Towny-Influence.
	 * 
	 * @throws TownyException if the occupier has more than zero Towny-Influence.
	 */
    private static void verifyThatOccupierHasZeroTownyInfluence(Town revoltingTown) throws TownyException {
        Nation occupier = TownOccupationController.getTownOccupier(revoltingTown);        
        if(TownOccupationController.calculateTownyInfluenceAmount(revoltingTown, occupier) > 0) {
            throw new TownyException(Translation.of("msg_err_cannot_peacefully_revolt_because_occupier_has_influence", occupier.getName()));
        }
    }
}
