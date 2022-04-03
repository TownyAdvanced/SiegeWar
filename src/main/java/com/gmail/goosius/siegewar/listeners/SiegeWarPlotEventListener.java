package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.event.damage.TownBlockPVPTestEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SiegeWarPlotEventListener implements Listener {
    @SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarPlotEventListener(SiegeWar instance) {
		plugin = instance;
    }

	/**
	 * When a townblock in in a SiegeZone, PVP is always on
	 *
	 * @param event the test event
	 */
	@EventHandler
	public void onTownBlockPVPTest(TownBlockPVPTestEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()
		    && event.getTownBlock().getWorld().isWarAllowed()
		    && SiegeWarSettings.isStopTownyPlotPvpProtection()
		    && !event.getTownBlock().getPermissions().pvp
		    && SiegeWarDistanceUtil.isTownBlockInActiveSiegeZone(event.getTownBlock())) {
				event.setPvp(true);
		}
	}
}
