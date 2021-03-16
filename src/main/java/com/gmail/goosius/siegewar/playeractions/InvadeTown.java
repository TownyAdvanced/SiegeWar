package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
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
	 * This method does some final checks and if they pass, the invasion is executed.
	 *
	 * @param siege the siege of the town.
	 * @throws TownyException when the invasion wont be allowed.
	 */
    public static void processInvadeTownRequest(Player player, Siege siege) throws TownyException {

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, siege.getSiegeType().getPermissionNodeToSurrenderDefence().getNode()))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		invadeTown(siege);
    }

	/**
	 * Invade the town
	 *
	 * @param siege the siege
	 */
    private static void invadeTown(Siege siege) {
    	Nation invadingNation = siege.getNation();
    	Town invadedTown = siege.getTown();
		Nation nationOfInvadedTown = null;

        if(invadedTown.hasNation()) {
			//Update stats of defeated nation
            nationOfInvadedTown = TownyAPI.getInstance().getTownNationOrNull(invadedTown);
			NationMetaDataController.setTotalTownsLost(nationOfInvadedTown, NationMetaDataController.getTotalTownsLost(nationOfInvadedTown) + 1);
        }

		//Set town to occupied
		TownOccupationController.setTownOccupier(invadedTown, invadingNation);
        //Update siege flags
		siege.setTownInvaded(true);
		//Update stats of victorious nation
		NationMetaDataController.setTotalTownsGained(invadingNation, NationMetaDataController.getTotalTownsGained(invadingNation) + 1);

		//Save to db
        SiegeController.saveSiege(siege);
		invadedTown.save();
		invadingNation.save();
		if(nationOfInvadedTown != null) {
			nationOfInvadedTown.save();
		}
		
		//Messaging
		if(nationOfInvadedTown != null) {
			Messaging.sendGlobalMessage(
				Translation.of("msg_nation_town_invaded",
				invadedTown.getFormattedName(),
				nationOfInvadedTown.getFormattedName(),
				invadingNation.getFormattedName()
			));
		} else {
			Messaging.sendGlobalMessage(
				Translation.of("msg_neutral_town_invaded",
				invadedTown.getFormattedName(),
				invadingNation.getFormattedName()
			));
		}
    }
}
