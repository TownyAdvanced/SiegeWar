package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
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
     * @param targetTown           the target peaceful town, where we already know the player is a resident
     * @param bannerBlock          the banner block
     * @throws TownyException if the revolt request fails.
     */
    public static void processActionRequest(Player player,
                                            Town targetTown,
                                            Block bannerBlock) throws TownyException {

        if (!SiegeWarSettings.isPeacefulTownsRevoltEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_REVOLT_PEACEFULLY.getNode()))
            throw new TownyException(Translation.of("msg_err_action_disable"));

//        if(!TownOccupationController.isTownOccupied(targetTown))
  //          throw new TownyException(Translation.of("msg_err_cannot_start_revolt_siege_as_town_is_unoccupied"));

    //    long immunity = TownMetaDataController.getRevoltImmunityEndTime(targetTown);
      //  if (immunity == -1L)
        //	throw new TownyException(Translation.of("msg_err_siege_war_revolt_immunity_permanent"));
        //if (System.currentTimeMillis() < immunity)
         //   throw new TownyException(Translation.of("msg_err_siege_war_revolt_immunity_active"));

        Nation occupierNation = TownOccupationController.getTownOccupier(targetTown);

		//SiegeCamp camp = new SiegeCamp(player, bannerBlock, SiegeType.REVOLT, targetTown, targetTown, occupierNation, townOfSiegeStarter, townBlock);

		//PreSiegeCampEvent event = new PreSiegeCampEvent(camp);
		//Bukkit.getPluginManager().callEvent(event);
		//if (event.isCancelled())
		//	throw new TownyException(event.getCancellationMsg());

		//if (SiegeWarSettings.areSiegeCampsEnabled())
			// Launch a SiegeCamp, a (by default) 10 minute minigame. If successful the Siege will be initiated in ernest. 
//			SiegeController.beginSiegeCamp(camp);
//		else 
			// SiegeCamps are disabled, just do the Siege.
//			camp.startSiege();
    }
}
