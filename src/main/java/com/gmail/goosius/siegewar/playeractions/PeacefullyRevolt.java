package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;

import org.bukkit.entity.Player;

import java.util.Map;

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

    	final Translator translator = Translator.locale(Translation.getLocale(player));
        if (!SiegeWarSettings.isPeacefulTownsRevoltEnabled())
            throw new TownyException(translator.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_REVOLT_PEACEFULLY.getNode()))
            throw new TownyException(translator.of("msg_err_action_disable"));

        if(!TownOccupationController.isTownOccupied(targetTown))
            throw new TownyException(translator.of("msg_err_cannot_peacefully_revolt_because_unoccupied"));

		if(SiegeController.hasActiveSiege(targetTown)) {
			throw new TownyException(translator.of("msg_err_cannot_change_occupation_of_besieged_town"));
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
        	Translatable.of("msg_peaceful_town_revolted",
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
        Map<Nation, Integer> townyInfluenceMap = TownPeacefulnessUtil.calculateTownyInfluenceMap(revoltingTown);
        Nation occupier = TownOccupationController.getTownOccupier(revoltingTown);
        if(townyInfluenceMap.containsKey(occupier)) {
            int occupierInfluenceAmount = townyInfluenceMap.get(occupier);
            throw new TownyException(Translatable.of("msg_err_cannot_peacefully_revolt_because_occupier_has_influence", occupier.getName(), occupierInfluenceAmount));
        }
    }
}
