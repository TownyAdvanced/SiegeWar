package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.event.plot.toggle.PlotTogglePvpEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SiegeWarPlotEventListener implements Listener {
    @SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarPlotEventListener(SiegeWar instance) {
		plugin = instance;
    }
    
    /*
    * SW will stop plot pvp being toggled in siegezones
    */
    @EventHandler
	public void onPlotTogglePVP(PlotTogglePvpEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()
		    && event.getTownBlock().getWorld().isWarAllowed()
		    && SiegeWarDistanceUtil.isTownBlockInActiveSiegeZone(event.getTownBlock())) {
                event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("msg_err_cannon_toggle_plot_pvp_in_siegezone"));
				event.setCancelled(true);
		}	
    }
}
