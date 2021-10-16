package com.gmail.goosius.siegewar.integration.cannons;

import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

/**
 * 
 * @author Goosius
 *
 */
public class CannonsListener implements Listener {

	private final SiegeWar plugin;
	private final CannonsIntegration integration;

	public CannonsListener(SiegeWar instance, CannonsIntegration integration) {
		plugin = instance;
		this.integration = integration;
	}

	/**
	 * Process a Cannon Fire Event, where a player fires a cannon
	 *
	 * - If the cannon is in the wilderness, it can be fired
	 *
	 * - If a player is firing from their town, 
	 *   and has the siegewar.siege.town.start.cannon.session permission,
	 *   then a cannon session starts/refreshes, and the event is allowed
	 *
	 * - If neither of the above applies, then we look for a cannon session.
	 *   If active, the event is allowed, otherwise it is prevented.
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonFireEvent(CannonFireEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			Player player = null;
			try {
				player = plugin.getServer().getPlayer(event.getPlayer());
				integration.processPlayerCannonInteraction(player, event.getCannon(), Translation.of("msg_err_cannot_fire_no_cannon_session"));
			} catch (TownyException te) {
				event.setCancelled(true);
				if (player != null) {
					Messaging.sendErrorMsg(player, te.getMessage());
				} else {
					SiegeWar.severe("Problem processing fire cannon event: " + te.getMessage());
				}
			} catch (Exception e) {
				event.setCancelled(true);
				SiegeWar.severe("Problem processing fire cannon event: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Process a Redstone Cannon Fire Event
	 *
	 * - If the cannon is in the wilderness, it can be fired
	 *
	 * - Otherwise, we look for a cannon session.
	 *   If active, the event is allowed, otherwise it is prevented.
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonRedstoneEvent(CannonRedstoneEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			try {
				Town townWhereCannonIsLocated;
				Set<Town> cannonTowns = integration.getTownsWhereCannonIsLocated(event.getCannon());
				if (cannonTowns.size() == 0) {
					return; //cannon is not in a town
				} else if (cannonTowns.size() > 1) {
					event.setCancelled(true); //too many towns
					return;
				} else {
					townWhereCannonIsLocated = (Town)cannonTowns.toArray()[0];
				}
				if (!CannonsIntegration.doesTownHaveCannonSession(townWhereCannonIsLocated)) {
					event.setCancelled(true);  //No cannon session found. Cancel event
				}
			} catch (Exception e) {
				event.setCancelled(true);
				SiegeWar.severe("Error processing cannon redstone event. Cannon fire prevented: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
