package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSiegeWarStartEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to start suppression sieges
 *
 * @author Goosius
 */
public class StartSuppressionSiege {

    /**
     * Process a start suppression siege request.
     *
     * At this point we know that the resident has a nation,
     * and the nation is occupying the town.
     *
     * This method does some final checks and if they pass, the siege is initiated.
     *
     * @param player the player
     * @param townOfSiegeStarter town
     * @param nationOfSiegeStarter nation which is attacking.
     * @param townBlock the townblock where the attack is taking place.
     * @param targetTown the town about to be attacked
     * @param bannerBlock the banner block
     *
     * @throws TownyException when attack cannot be made.
     *
     */
    public static void processStartSiegeRequest(Player player,
                                                Town townOfSiegeStarter,
                                                Nation nationOfSiegeStarter,
                                                TownBlock townBlock,
                                                Town targetTown,
                                                Block bannerBlock) throws TownyException {

        if (!SiegeWarSettings.getSuppressionSiegesEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToStartSiege(SiegeType.SUPPRESSION)))
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (targetTown.hasNation()) {
            Nation nationOfDefendingTown = targetTown.getNation();

            if (nationOfSiegeStarter == nationOfDefendingTown)
                throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_town_in_own_nation"));

            if (!nationOfSiegeStarter.hasEnemy(nationOfDefendingTown))
                throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_non_enemy_nation"));
        }

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

        //Call event
        PreSiegeWarStartEvent preSiegeWarStartEvent = new PreSiegeWarStartEvent(SiegeType.SUPPRESSION, targetTown, nationOfSiegeStarter, townOfSiegeStarter, bannerBlock, townBlock);
        Bukkit.getPluginManager().callEvent(preSiegeWarStartEvent);

        //Setup attack
        if (!preSiegeWarStartEvent.isCancelled()){
            SiegeController.startSiege(
                    bannerBlock,
                    SiegeType.SUPPRESSION,
                    targetTown,
                    nationOfSiegeStarter,
                    targetTown,
                    townOfSiegeStarter,
                    true);
        } else {
            throw new TownyException(preSiegeWarStartEvent.getCancellationMsg());
        }
    }
}
