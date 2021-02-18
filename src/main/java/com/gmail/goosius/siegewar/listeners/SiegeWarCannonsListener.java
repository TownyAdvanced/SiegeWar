package com.gmail.goosius.siegewar.listeners;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import at.pavlov.cannons.event.CannonUseEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.*;
import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			Player player = null;
			try {
				player = Towny.getPlugin().getServer().getPlayer(event.getPlayer());
				processPlayerCannonInteraction(player, event.getCannon(), Translation.of("msg_err_cannon_fire_not_permitted_yet"));
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

	@EventHandler
	public void cannonUseEvent(CannonUseEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			Player player = null;
			try {
				player = Towny.getPlugin().getServer().getPlayer(event.getPlayer());
				processPlayerCannonInteraction(player, event.getCannon(), Translation.of("msg_err_cannon_use_not_permitted_yet"));
			} catch (Exception e) {
				event.setCancelled(true);
				if(player != null) {
					Messaging.sendErrorMsg(player, e.getMessage());
				} else {
					System.out.println("Problem Processing use cannon event: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * If any block of the cannon is located in the town
	 * And the town is under active siege
	 * And the cannons enabled counter is 0
	 * then the event is prevented
	 * @param event the event
	 */
	@EventHandler
	public void cannonRedstoneEvent(CannonRedstoneEvent event) {
		try {
			Town townWhereCannonIsLocated;
			Set<Town> cannonTowns = getTownsWhereCannonIsLocated(event.getCannon());
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
					&& SiegeController.getSiege(townWhereCannonIsLocated).getCannonsEnabledCounter() == 0) {
				event.setCancelled(true);
			}
		} catch (Exception e) {
			System.out.println("Error processing cannon redstone event. Cannon fire prevented: " + e.getMessage());
			event.setCancelled(true);
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
	 * @throws TownyException if the cannon use is not blocked
	 */
	private void processPlayerCannonInteraction(Player player, Cannon cannon, String permissionErrorString) throws TownyException {
		if (player == null)
			return;

		//Find the town where the cannon is located
		Town townWhereCannonIsLocated;
		Set<Town> cannonTowns = getTownsWhereCannonIsLocated(cannon);
		if(cannonTowns.size() == 0) {
			return; //Cannon not in a town
		} else if (cannonTowns.size() > 1) {
			throw new TownyException(Translation.of("msg_err_cannon_in_two_towns"));
		} else {
			townWhereCannonIsLocated = (Town)cannonTowns.toArray()[0];
		}

		//Find the siege
		Siege siege;
		if(SiegeController.hasActiveSiege(townWhereCannonIsLocated)) {
			siege = SiegeController.getSiege(townWhereCannonIsLocated);
		} else {
			return;
		}

		Resident resident;
		if (siege.getCannonsEnabledCounter() > 0) {
			/*
			 * Cannons are enabled.
			 * Allow the event
			 * Also If the resident is a member of the town, refresh cannons-enabled duration
			 */
			if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_CANNON_KEY.getNode())) {
				resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
				if (resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
					siege.setCannonsEnabledCounter(SiegeWarSettings.getCannonsEnabledCounterValue());
				}
			}
			return;
		} else {
			/*
			 * Cannons are disabled
			 *
			 * If the resident is a ranked member of the town:
			 * - Start counter, turning on cannons for the town
			 * - Turn on explosions in the town
			 * - Return true, allowing the event
			 *
			 * If the resident is not a ranked member of the town, do not allow the firing
			 */
			if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_CANNON_KEY.getNode())) {
				resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
				if (resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
					SiegeWarTownUtil.setTownExplosionFlags(townWhereCannonIsLocated, true);
					siege.setCannonsEnabledCounter(SiegeWarSettings.getCannonsEnabledCounterValue());
					return;
				}
			}
			throw new TownyException(permissionErrorString);
		}
	}

	private Set<Town> getTownsWhereCannonIsLocated(Cannon cannon) {
		Set<Town> townsWhereCannonIsLocated = new HashSet<>();
		List<Location> locationOfCannonBlocks = cannon.getCannonDesign().getAllCannonBlocks(cannon);
		Town possibleTown;
		for (Location locationOfCannonBlock : locationOfCannonBlocks) {
			possibleTown = TownyAPI.getInstance().getTown(locationOfCannonBlock);
			if (possibleTown != null) {
				townsWhereCannonIsLocated.add(possibleTown);
			}
		}
		return townsWhereCannonIsLocated;
	}
}
