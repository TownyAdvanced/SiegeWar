package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class StartRevoltSiege {

    public static void processStartSiegeRequest(Player player, Town town) throws TownyException {
        if (!SiegeWarSettings.getRevoltSiegesEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeType.REVOLT.getPermissionNodeToAttack().getNode()))
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if(!TownOccupationController.isTownOccupied(town))
            throw new TownyException(Translation.of("msg_err_cannot_start_revolt_siege_as_town_is_unoccupied"));

        if (System.currentTimeMillis() < TownMetaDataController.getRevoltImmunityEndTime(town))
            throw new TownyException(Translation.of("msg_err_siege_war_revolt_immunity_active"));

        startSiege();
    }

    private static void startSiege() {

    }

    private void parseSiegeWarRevoltCommand(Player player) {
        if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeRevoltEnabled()) {



            //TODO---- START SIEGE NOW
            //Do revolt now


            //Activate revolt immunity
            SiegeWarTimeUtil.activateRevoltImmunityTimer(event.getTown());
            event.getTown().setConquered(false);
            event.getTown().setConqueredDays(0);
            event.getTown().save();

            Messaging.sendGlobalMessage(
                    Translation.of("msg_siege_war_revolt",
                            event.getTown().getFormattedName(),
                            event.getTown().getMayor().getFormattedName(),
                            event.getNation().getFormattedName()));
        }
    }
}
