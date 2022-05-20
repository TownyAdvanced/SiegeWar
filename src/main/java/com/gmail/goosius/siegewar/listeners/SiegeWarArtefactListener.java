package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.events.ArtefactDamageEntityEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Goosius
 *
 */
public class SiegeWarArtefactListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;

	public SiegeWarArtefactListener(SiegeWar siegeWar) {
		plugin = siegeWar;
	}

	@EventHandler (ignoreCancelled = true)
	public void on(ArtefactDamageEntityEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			for(String customEffect: event.getCustomEffects()) {
				switch (customEffect) {
					case "lightning_strike_on_hit":
						event.getAttacker().getWorld().strikeLightning(event.getVictim().getLocation());
					break;
				}
			}
		}
	}
}
