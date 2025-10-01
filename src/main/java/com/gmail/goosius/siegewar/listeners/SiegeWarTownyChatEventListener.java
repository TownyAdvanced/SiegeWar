package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarNotificationUtil;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SiegeWarTownyChatEventListener  implements Listener {

    /**
     * Listener to towny chat events
     * 
     * @param event the AsyncChatHook event from TownyChat
     */
    @EventHandler(ignoreCancelled = true)
    public void on(AsyncChatHookEvent event) {
        if(SiegeWarSettings.getWarSiegeEnabled())
        {
            /*
             * If toxicity reduction is enabled, the following effects apply:
             * 1. No local chat in Siege Zones
             * 2. No general chat if a BattleSession is in progress (and for 10 mins after)
             */
            if (SiegeWarSettings.isToxicityReductionEnabled() && BattleSession.getBattleSession().isChatDisabled())
            {
                String channelName = event.getChannel().getName();
                if (channelName.equalsIgnoreCase("local") || channelName.equalsIgnoreCase("general"))
                {
                    event.setCancelled(true);
                    SiegeWarNotificationUtil.notifyPlayerOfBattleSessionChatRestriction(event.getPlayer(), channelName);
                    return;
                }
            }

            /*
             * Adjust chat for town occupation 
             */
            if (event.getChannel().getType() == channelTypes.NATION || event.getChannel().getType() == channelTypes.ALLIANCE)
            {
                Resident senderResident = TownyAPI.getInstance().getResident(event.getPlayer());
                Town senderTown = senderResident.getTownOrNull();
                if(senderTown != null)
                {
                    if(TownOccupationController.isTownOccupied(senderTown))
                    {
                        //Player is in an occupied town. Direct chat to the home-nation / home-allies
                        Nation homeNation = TownOccupationController.getHomeNationOrNull(senderTown);
                        if(homeNation != null)
                        {
                            List<Player> updatedRecipientsList = event.getChannel().getType() == channelTypes.NATION ? TownyAPI.getInstance().getOnlinePlayersInNation(homeNation) : TownyAPI.getInstance().getOnlinePlayersAlliance(homeNation);
                            event.setRecipients(Set.copyOf(updatedRecipientsList));
                        }
                    }
                    else
                    {
                        //Player is not in an occupied town. Ensure chat does not go to occupied towns
                        Set<Player> updatedRecipientPlayersList = new HashSet<>();
                        Resident recipientResident;
                        for (Player recipientPlayer : event.getRecipients()) {
                            recipientResident = TownyAPI.getInstance().getResident(recipientPlayer);
                            if (!TownOccupationController.isTownOccupied(recipientResident.getTownOrNull())) {
                                updatedRecipientPlayersList.add(recipientPlayer);
                            }
                        }
                        event.setRecipients(updatedRecipientPlayersList);
                    }   
                }
            }
        }
    }

}
