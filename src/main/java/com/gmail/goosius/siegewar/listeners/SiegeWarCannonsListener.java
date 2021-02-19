package com.gmail.goosius.siegewar.listeners;

import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import at.pavlov.cannons.event.CannonUseEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarCannonsUtil;
import com.palmergames.bukkit.towny.Towny;
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
public class SiegeWarCannonsListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;

	public SiegeWarCannonsListener(SiegeWar instance) {
		plugin = instance;
	}

	/**
	 * If any block of the cannon is located in the town
	 * And the town is under active siege
	 * And there is no cannon session in progress
	 * then the event is prevented
	 *
	 * However if the player has the siegewar.siege.town.start.cannon.session permission,
	 * then a cannon session starts
	 * and the event is allowed
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonFireEvent(CannonFireEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			Player player = null;
			try {
				player = Towny.getPlugin().getServer().getPlayer(event.getPlayer());
				SiegeWarCannonsUtil.processPlayerCannonInteraction(player, event.getCannon(), Translation.of("msg_err_cannon_fire_not_permitted_yet"));
			} catch (Exception e) {
				event.setCancelled(true);
				if(player != null) {
					Messaging.sendErrorMsg(player, e.getMessage());
				} else {
					System.out.println("Problem Processing fire cannon event: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * If any block of the cannon is located in the town
	 * And the town is under active siege
	 * And there is no cannon session in progress
	 * then the event is prevented
	 * @param event the event
	 */
	@EventHandler
	public void cannonRedstoneEvent(CannonRedstoneEvent event) {
		try {
			Town townWhereCannonIsLocated;
			Set<Town> cannonTowns = SiegeWarCannonsUtil.getTownsWhereCannonIsLocated(event.getCannon());
			if(cannonTowns.size() == 0) {
				return; //cannon is not in a town
			} else if (cannonTowns.size() > 1) {
				event.setCancelled(true); //too many towns
				return;
			} else {
				townWhereCannonIsLocated = (Town)cannonTowns.toArray()[0];
			}

			if(townWhereCannonIsLocated != null
					&& SiegeController.hasActiveSiege(townWhereCannonIsLocated)
					&& SiegeController.getSiege(townWhereCannonIsLocated).getCannonSessionRemainingShortTicks() == 0) {
				event.setCancelled(true);
			}
		} catch (Exception e) {
			event.setCancelled(true);
			System.out.println("Error processing cannon redstone event. Cannon fire prevented: " + e.getMessage());
		}
	}
}
