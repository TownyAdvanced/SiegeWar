package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
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
	 * @param siege the siege of the town.
	 * @throws TownyException when the invasion wont be allowed.
	 */
	public static void processInvadeTownRequest(Player player, Nation residentsNation, Town nearbyTown, Siege siege) throws TownyException {
		if(!SiegeWarSettings.getWarSiegeInvadeEnabled())
			throw new TownyException("msg_err_action_disable");

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, siege.getSiegeType().getPermissionNodeToSurrenderDefence().getNode()))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		if(residentsNation == null)
			throw new TownyException("msg_err_action_disable");  //Can't invade if nationless

		if(residentsNation != siege.getAttacker())
			throw new TownyException("msg_err_action_disable");  //Can't invade unless you are the attacker

		if(siege.getStatus().isActive())
			throw new TownyException("msg_err_cannot_invade_siege_still_in_progress");

		if(siege.getTownInvaded())
			throw new TownyException("msg_err_cannot_invade_town_already_invaded");

		if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
			throw new TownyException(Translation.of("msg_err_cannot_invade_without_victory"));

		if (siege.isTownInvaded())
			throw new TownyException(Translation.of("msg_err_town_already_invaded"));

		if (TownySettings.getNationRequiresProximity() > 0) {
			Coord capitalCoord = residentsNation.getCapital().getHomeBlock().getCoord();
			Coord townCoord = nearbyTown.getHomeBlock().getCoord();
			if (!residentsNation.getCapital().getHomeBlock().getWorld().getName().equals(nearbyTown.getHomeBlock().getWorld().getName())) {
				throw new TownyException(Translation.of("msg_err_nation_homeblock_in_another_world"));
			}
			double distance;
			distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
			if (distance > TownySettings.getNationRequiresProximity()) {
				throw new TownyException(String.format(Translation.of("msg_err_town_not_close_enough_to_nation"), nearbyTown.getName()));
			}
		}

		if (TownySettings.getMaxTownsPerNation() > 0) {
			int effectiveNumTowns = SiegeWarNationUtil.calculateEffectiveNumberOfTownsInNation(residentsNation);
			if (effectiveNumTowns >= TownySettings.getMaxTownsPerNation()){
				throw new TownyException(String.format(Translation.of("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
			}
		}

		invadeTown(residentsNation, nearbyTown, siege);
	}

	/**
	 * Invade the town
	 *
	 * @param siege the siege
	 */
    private static void invadeTown(Nation invadingNation, Town invadedTown, Siege siege) {
		Nation nationOfInvadedTown = null;

        if(invadedTown.hasNation()) {
			//Update stats of defeated nation
            nationOfInvadedTown = TownyAPI.getInstance().getTownNationOrNull(invadedTown);
			NationMetaDataController.setTotalTownsLost(nationOfInvadedTown, NationMetaDataController.getTotalTownsLost(nationOfInvadedTown) + 1);
        }

		//Set town to occupied
		TownOccupationController.setTownOccupation(invadedTown, invadingNation);
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
		if(nationOfInvadedTown == null) {
			Messaging.sendGlobalMessage(
					Translation.of("msg_neutral_town_invaded",
							invadedTown.getFormattedName(),
							invadingNation.getFormattedName()
					));
		} else {
			Messaging.sendGlobalMessage(
					Translation.of("msg_nation_town_invaded",
							invadedTown.getFormattedName(),
							nationOfInvadedTown.getFormattedName(),
							invadingNation.getFormattedName()
					));
		}

		//Activate revolt immunity
		SiegeWarTimeUtil.activateRevoltImmunityTimer(invadedTown);
    }
}
