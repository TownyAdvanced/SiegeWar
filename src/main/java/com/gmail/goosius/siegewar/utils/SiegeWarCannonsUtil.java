package com.gmail.goosius.siegewar.utils;

import at.pavlov.cannons.cannon.Cannon;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains utility functions related to the cannons plugin integration
 *
 * @author Goosius
 */
public class SiegeWarCannonsUtil {

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
	 * @param player the player interacting with the cannon
	 * @param cannon the cannon
	 * @throws TownyException if the cannon use is not blocked
	 */
	public static void processPlayerCannonInteraction(Player player, Cannon cannon, String permissionErrorString) throws TownyException {
		if (player == null)
			return;

		//Find the town where the cannon is located
		Town townWhereCannonIsLocated = null;
		Set<Town> cannonTowns = getTownsWhereCannonIsLocated(cannon);
		if(cannonTowns.size() == 0) {
			return; //Cannon not in a town
		} else if (cannonTowns.size() > 1) {
			throw new TownyException(Translation.of("msg_err_cannon_in_two_towns"));
		} else {
			for(Town town: cannonTowns)
				townWhereCannonIsLocated = town;
		}

		//Find the siege
		Siege siege;
		if(SiegeController.hasActiveSiege(townWhereCannonIsLocated)) {
			siege = SiegeController.getSiege(townWhereCannonIsLocated);
		} else {
			return;
		}

		Resident resident;
		if (siege.getCannonSessionRemainingShortTicks() > 0) {
			/*
			 * Cannons are enabled.
			 * Allow the event
			 * Also If the resident is a member of the town, refresh cannons-enabled duration
			 */
			if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_START_CANNON_SESSION.getNode())) {
				resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
				if (resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
					siege.setCannonSessionRemainingShortTicks(SiegeWarSettings.getMaxCannonSessionDuration());
				}
			}
			return;
		} else {
			/*
			 * Cannons are disabled
			 *
			 * If the resident is a ranked member of the town:
			 * - Start cannon session, turning on cannons for the town
			 * - Turn on explosions in the town
			 * - Return true, allowing the event
			 *
			 * If the resident is not a ranked member of the town, do not allow the firing
			 */
			if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_START_CANNON_SESSION.getNode())) {
				resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
				if (resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
					SiegeWarTownUtil.setTownExplosionFlags(townWhereCannonIsLocated, true);
					siege.setCannonSessionRemainingShortTicks(SiegeWarSettings.getMaxCannonSessionDuration());
					return;
				}
			}
			throw new TownyException(permissionErrorString);
		}
	}

	public static Set<Town> getTownsWhereCannonIsLocated(Cannon cannon) {
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

	public static void evaluateCannonSessions() {
		List<Siege> sieges = SiegeController.getSieges();
		for(Siege siege: sieges) {
			if(siege.getStatus().isActive() && siege.getCannonSessionRemainingShortTicks() > 0) {
				siege.decrementCannonSessionRemainingShortTicks();
				if(siege.getCannonSessionRemainingShortTicks() < 1) {
					//Turn explosions off in the town
					SiegeWarTownUtil.setTownExplosionFlags(siege.getDefendingTown(), false);
				}
			}
		}
	}
}
