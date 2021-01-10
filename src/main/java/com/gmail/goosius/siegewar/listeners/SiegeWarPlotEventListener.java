package com.gmail.goosius.siegewar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.event.PlotPreChangeTypeEvent;
import com.palmergames.bukkit.towny.event.plot.toggle.PlotTogglePvpEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlockType;

public class SiegeWarPlotEventListener implements Listener {
    @SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarPlotEventListener(SiegeWar instance) {
		plugin = instance;
    }
    
    /*
    * SW will stop plot pvp being toggled in besieged or peaceful towns.
    */
    @EventHandler
	public void onPlotTogglePVP(PlotTogglePvpEvent event) { //TODO: Change Messaging.sendErrorMsg() to event.setCancelledMsg() later on.
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if (SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns() && SiegeController.hasActiveSiege(event.getTown()))  {
				Messaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_err_siege_besieged_town_cannot_toggle_pvp"));
				event.setCancelled(true);
			}
            if (SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
                    && !SiegeWarSettings.getWarCommonPeacefulTownsAllowedToTogglePVP()
                    && event.getTown().isNeutral()) {
				Messaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_err_peaceful_town_pvp_forced_off"));
				event.setCancelled(true);
			}
		}	
    }

    /*
    * SW will stop peaceful towns from setting a plot to arena.
    */    
    @EventHandler
    public void onPlotSetType(PlotPreChangeTypeEvent event) {
        try {
            if (SiegeWarSettings.getWarSiegeEnabled()
                    && SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
                    && !SiegeWarSettings.getWarCommonPeacefulTownsAllowedToTogglePVP()
                    && event.getTownBlock().getTown().isNeutral()
                    && event.getNewType() == TownBlockType.ARENA) {
                event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_peaceful_town_pvp_forced_off"));
                event.setCancelled(true);
            }
        } catch (NotRegisteredException ignore) {

        }
    }
}
