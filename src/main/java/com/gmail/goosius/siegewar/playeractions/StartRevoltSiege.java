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
 * This class is responsible for processing requests to start revolt sieges
 *
 * @author Goosius
 */
public class StartRevoltSiege {

    /**
     * Process a start revolt siege request.
     *
     * At this point we know that
     * - the player has a town
     * - the target town is the resident's town,
     *
     * This method does some final checks and if they pass, the siege is initiated.
     *
     * @param player               the player
     * @param townOfSiegeStarter   town
     * @param nationOfSiegeStarter nation which is attacking.
     * @param townBlock            the townblock where the attack is taking place.
     * @param targetTown           the town about to be attacked
     * @param bannerBlock          the banner block
     * @throws TownyException when attack cannot be made.
     */
    public static void processStartSiegeRequest(Player player,
                                                Town townOfSiegeStarter,
                                                Nation nationOfSiegeStarter,
                                                TownBlock townBlock,
                                                Town targetTown,
                                                Block bannerBlock) throws TownyException {

        if (!SiegeWarSettings.getRevoltSiegesEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToStartSiege(SiegeType.REVOLT)))
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if(!TownOccupationController.isTownOccupied(targetTown))
            throw new TownyException(Translation.of("msg_err_cannot_start_revolt_siege_as_town_is_unoccupied"));

        if (System.currentTimeMillis() < TownMetaDataController.getRevoltImmunityEndTime(targetTown))
            throw new TownyException(Translation.of("msg_err_siege_war_revolt_immunity_active"));

        Nation occupierNation = TownOccupationController.getTownOccupier(targetTown);

		SiegeCamp camp = new SiegeCamp(player, bannerBlock, SiegeType.REVOLT, targetTown, targetTown, occupierNation, townOfSiegeStarter, townBlock);

		PreSiegeCampEvent event = new PreSiegeCampEvent(camp);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			throw new TownyException(event.getCancellationMsg());

		if (SiegeWarSettings.areSiegeCampsEnabled())
			// Launch a SiegeCamp, a (by default) 10 minute minigame. If successful the Siege will be initiated in ernest. 
			SiegeController.beginSiegeCamp(camp);
		else 
			// SiegeCamps are disabled, just do the Siege.
			camp.startSiege();
    }
}
