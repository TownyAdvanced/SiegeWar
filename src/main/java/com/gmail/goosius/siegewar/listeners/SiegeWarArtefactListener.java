package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.events.ArtefactConsumeItemEvent;
import com.gmail.goosius.siegewar.events.ArtefactDamageEntityEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

	@EventHandler (ignoreCancelled = true)
	public void on(ArtefactConsumeItemEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			for(String customEffect: event.getCustomEffects()) {
				switch (customEffect) {
					case "delayed_massive_self_explosion_on_consume":
						Towny.getPlugin().getServer().getScheduler().runTaskLater(Towny.getPlugin(), new Runnable() {
							public void run() {
								event.getConsumer().getWorld().createExplosion(event.getConsumer().getLocation(), 200, true);
							}
						}, 140);
					break;
				}
			}
		}
	}
}
