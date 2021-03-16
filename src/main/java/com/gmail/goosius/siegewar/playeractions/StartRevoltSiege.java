package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import org.bukkit.entity.Player;

public class StartRevoltSiege {


    private void parseSiegeWarRevoltCommand(Player player) {
        if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeRevoltEnabled()) {


            //Peaceful towns cannot revolt (at all)
            if (SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
                    && town.isNeutral()
                    && !TownPeacefulnessUtil.canPeacefulTownLeaveNation(town)) {

                event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_war_siege_peaceful_town_cannot_revolt_zero_or_one_unsieged_guardian_towns_nearby",
                        SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement(),
                        SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement()));
                event.setCancelled(true);
                return;
            }


            //A town cannot revolt unless its revolt immunity timer is finished
            if (SiegeWarSettings.getWarSiegeTownLeaveDisabled()) {

                if (!SiegeWarSettings.getWarSiegeRevoltEnabled()) {
                    event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_war_town_voluntary_leave_impossible"));
                    event.setCancelled(true);
                }
                if (System.currentTimeMillis() < TownMetaDataController.getRevoltImmunityEndTime(town)) {
                    event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_war_revolt_immunity_active"));
                    event.setCancelled(true);
                } else {
                    // Towny will cancel the leaving on lowest priority if the town is conquered.
                    // We want to un-cancel it.
                    if (event.isCancelled())
                        event.setCancelled(false);
                }
            }

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
