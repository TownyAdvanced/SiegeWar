package com.gmail.goosius.siegewar.listeners;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import at.pavlov.cannons.event.CannonUseEvent;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.*;
import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarCannonsListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;

	public SiegeWarCannonsListener(SiegeWar instance) {

		plugin = instance;
	}

	@EventHandler
	public void cannonFireEvent(CannonFireEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			Player player = Towny.getPlugin().getServer().getPlayer(event.getPlayer());
			if (!processPlayerCannonInteraction(player, event.getCannon())) {
				event.setCancelled(true);
				//Todo - message player
			}
		}
	}

	@EventHandler
	public void cannonUseEvent(CannonUseEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			Player player = Towny.getPlugin().getServer().getPlayer(event.getPlayer());
			if (!processPlayerCannonInteraction(player, event.getCannon())) {
				event.setCancelled(true);
				//Todo - message player
			}
		}
	}

	/**
	 * If any block of the cannon is located in the town
	 * And the town is under active siege
	 * And the cannons enabled counter is 0
	 * then the event is generally prevented
	 *
	 * However if the player has the siegewar.town.cannons.key permission,
	 * then the event is allowed
	 * and the counter is activated/refreshed (e.g. to last maybe 5 mins)
	 *
	 * @param player the player interacting with the cannon
	 * @param cannon the cannon
	 * @return true if the cannon interaction can proceed
	 */
	private boolean processPlayerCannonInteraction(Player player, Cannon cannon) {
		try {
			if (player == null)
				return false;

			Resident resident;

			Town townWhereCannonIsLocated = null;

			if(townWhereCannonIsLocated != null) {
				if (TownMetaDataController.getCannonsEnabledCounter(townWhereCannonIsLocated) > 0) {
					//Does the play have the cannons key permission ?
					if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_CANNON_KEY.getNode())) {
						resident = TownyUniverse.getInstance().getResident(player.getUniqueId());

						//Is the resident a member of the town ?
						if (resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
							//Refresh cannons enabled duration
							TownMetaDataController.setCannonsEnabledCounter(townWhereCannonIsLocated, SiegeWarSettings.getCannonsEnabledCounterValue());
						}
					}
					//Return true, allowing the event
					return true;
				} else {
					//Does the player have the cannons key permission
					if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_CANNON_KEY.getNode())) {
						resident = TownyUniverse.getInstance().getResident(player.getUniqueId());

						//Is the resident a member of the town ?
						if (resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
							/*
							 * Start counter, turning on cannons for the town
							 * Turn on explosions in the town
							 * Return true, allowing the event
							 */
							SiegeWarTownUtil.setTownExplosionFlags(townWhereCannonIsLocated, true);
							TownMetaDataController.setCannonsEnabledCounter(townWhereCannonIsLocated, SiegeWarSettings.getCannonsEnabledCounterValue());
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error intercepting player-cannon interaction event. Cannon fire prevented: " + e.getMessage());
		}
		//Return false, preventing the event
		return false;
	}

	/**
	 * If any block of the cannon is located in the town
	 * And the town is under active siege
	 * And the cannons enabled counter is 0
	 * then the event is prevented
	 * @param event
	 */
	@EventHandler
	public void cannonRedstoneEvent(CannonRedstoneEvent event) {
		try {
			Town townWhereCannonIsLocated = null;
			if(townWhereCannonIsLocated != null
				&& SiegeController.hasActiveSiege(townWhereCannonIsLocated)
				&& TownMetaDataController.getCannonsEnabledCounter(townWhereCannonIsLocated) == 0) {
					event.setCancelled(true);
			}
		} catch (Exception e) {
			System.out.println("Error processing cannon redstone event. Cannon fire prevented: " + e.getMessage());
			event.setCancelled(true);
		}
	}
}
