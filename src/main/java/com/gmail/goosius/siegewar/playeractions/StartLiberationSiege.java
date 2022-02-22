package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to start liberation sieges
 *
 * @author Goosius
 */
public class StartLiberationSiege {

    /**
     * Process a start liberation siege request.
     * <p>
     * At this point we know that
     * - the resident has a nation
     * - the town is occupied,
     * - the player's nation is not the occupier.
     * <p>
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

        if (!SiegeWarSettings.getLiberationSiegesEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToStartSiege(SiegeType.LIBERATION)))
            throw new TownyException(Translation.of("msg_err_action_disable"));

        Nation occupierNationOfTown = TownOccupationController.getTownOccupier(targetTown);
        if (!nationOfSiegeStarter.hasEnemy(occupierNationOfTown))
            throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_occupied_town_non_enemy_nation"));

        Nation naturalNationOfTown = targetTown.getNationOrNull();
        if(!naturalNationOfTown.hasMutualAlly(nationOfSiegeStarter))
            throw new TownyException(Translation.of("msg_err_siege_war_cannot_start_liberation_siege_at_unallied_town"));

        if (TownySettings.getNationRequiresProximity() > 0) {
            Coord capitalCoord = nationOfSiegeStarter.getCapital().getHomeBlock().getCoord();
            Coord townCoord = targetTown.getHomeBlock().getCoord();
            if (!nationOfSiegeStarter.getCapital().getHomeBlock().getWorld().getName().equals(targetTown.getHomeBlock().getWorld().getName())) {
                throw new TownyException(Translation.of("msg_err_nation_homeblock_in_another_world"));
            }
            double distance;
            distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
            if (distance > TownySettings.getNationRequiresProximity()) {
                throw new TownyException(Translation.of("msg_err_siege_war_town_not_close_enough_to_nation"));
            }
        }

		SiegeCamp camp = new SiegeCamp(player, bannerBlock, SiegeType.LIBERATION, targetTown, nationOfSiegeStarter, occupierNationOfTown, townOfSiegeStarter, townBlock);

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
