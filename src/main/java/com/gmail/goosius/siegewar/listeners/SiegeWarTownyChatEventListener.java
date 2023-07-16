package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarNotificationUtil;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SiegeWarTownyChatEventListener  implements Listener {

    /**
     * If toxicity reduction is enabled, the following effects apply:
     * 1. No local chat in Siege Zones
     * 2. No general chat if a BattleSession is in progress (and for 10 mins after)
     *
     * @param event the AsyncChatHook event from TownyChat
     */
    @EventHandler(ignoreCancelled = true)
    public void on(AsyncChatHookEvent event) {
        if(!SiegeWarSettings.getWarSiegeEnabled()
                || !SiegeWarSettings.isToxicityReductionEnabled()
                || !BattleSession.getBattleSession().isChatDisabled())
            return;

        String channelName = event.getChannel().getName();
        if(channelName.equalsIgnoreCase("local") || channelName.equalsIgnoreCase("general")) {
            event.setCancelled(true);
            SiegeWarNotificationUtil.notifyPlayerOfBattleSessionChatRestriction(event.getPlayer(), channelName);
        }
    }

}
