package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSiegeWarStartEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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

        Nation occupier = TownOccupationController.getTownOccupier(targetTown);

        //Call event
        PreSiegeWarStartEvent preSiegeWarStartEvent = new PreSiegeWarStartEvent(SiegeType.REVOLT, targetTown, occupier, townOfSiegeStarter, bannerBlock, townBlock);
        Bukkit.getPluginManager().callEvent(preSiegeWarStartEvent);

        //Setup attack
        if (!preSiegeWarStartEvent.isCancelled()) {
            startSiege(bannerBlock, occupier, targetTown, townOfSiegeStarter);
        } else {
            throw new TownyException(preSiegeWarStartEvent.getCancellationMsg());
        }
    }

    private static void startSiege(Block bannerBlock, Nation occupierNation, Town townOfSiegeStarter, Town targetTown) throws TownyException {
        //Create Siege
        SiegeController.newSiege(targetTown);
        Siege siege = SiegeController.getSiege(targetTown);

        //Set values in siege object
        siege.setSiegeType(SiegeType.REVOLT);
        siege.setTown(targetTown);
        siege.setAttacker(targetTown);
        siege.setDefender(occupierNation);
        siege.setStatus(SiegeStatus.IN_PROGRESS);
        siege.setTownPlundered(false);
        siege.setTownInvaded(false);
        siege.setStartTime(System.currentTimeMillis());
        siege.setScheduledEndTime(
                (System.currentTimeMillis() +
                        ((long) (SiegeWarSettings.getWarSiegeMaxHoldoutTimeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS))));
        siege.setActualEndTime(0);
        siege.setFlagLocation(bannerBlock.getLocation());
        siege.setWarChestAmount(SiegeWarMoneyUtil.getSiegeCost(targetTown));

        SiegeController.setSiege(targetTown, true);
        SiegeController.putTownInSiegeMap(targetTown, siege);

        //Set town pvp and explosions to true.
        SiegeWarTownUtil.setTownPvpFlags(targetTown, true);

        //No warchest

        //Save to DB
        SiegeController.saveSiege(siege);
        occupierNation.save();

        //Send global message;
        if (siege.getTown().hasNation()) {
            Messaging.sendGlobalMessage(String.format(
                    Translation.of("msg_revolt_siege_started_nation_town"),
                    targetTown.getName(),
                    targetTown.getNation().getName(),
                    TownOccupationController.getTownOccupier(targetTown).getName()
            ));
        } else {
            Messaging.sendGlobalMessage(String.format(
                    Translation.of("msg_revolt_siege_started_neutral_town"),
                    targetTown.getName(),
                    TownOccupationController.getTownOccupier(targetTown).getName()
            ));
        }

        //Call event
        Bukkit.getPluginManager().callEvent(new SiegeWarStartEvent(siege, townOfSiegeStarter, bannerBlock));
    }
}
