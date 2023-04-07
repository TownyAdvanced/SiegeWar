package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translator;

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

		allowSiegeOrThrow(player, targetTown);

		// Start a SiegeCamp that will kick off the Siege (or if SiegeAssemblies are disabled, start the Siege immediately.)
		SiegeController.startSiegeCampProcess(player, bannerBlock, SiegeType.REVOLT, targetTown, targetTown.getNationOrNull(), targetTown, townOfSiegeStarter, townBlock);
	}

	private static void allowSiegeOrThrow(Player player, Town targetTown) throws TownyException {
		final Translator translator = Translator.locale(player);
        if (!SiegeWarSettings.getRevoltSiegesEnabled()
        || !TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_STARTREVOLTSIEGE.getNode()))
            throw new TownyException(translator.of("msg_err_action_disable"));

        if(SiegeWarTownPeacefulnessUtil.isTownPeaceful(targetTown))
            throw new TownyException(translator.of("msg_err_peaceful_towns_cannot_revolt"));

        if(!TownOccupationController.isTownOccupied(targetTown))
            throw new TownyException(translator.of("msg_err_cannot_start_revolt_siege_as_town_is_unoccupied"));

        long immunity = TownMetaDataController.getRevoltImmunityEndTime(targetTown);
        if (immunity == -1L)
        	throw new TownyException(translator.of("msg_err_siege_war_revolt_immunity_permanent"));
        if (System.currentTimeMillis() < immunity)
            throw new TownyException(translator.of("msg_err_siege_war_revolt_immunity_active"));
	}
}
