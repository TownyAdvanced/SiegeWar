package com.gmail.goosius.siegewar.utils;

import at.pavlov.cannons.cannon.Cannon;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This class contains utility functions related to the cannons plugin integration
 *
 * @author Goosius
 */
public class SiegeWarCannonsUtil {

	//List of all cannon sessions in the universe
	private static Map<Town, Integer> cannonSessionsRemainingShortTicks = new HashMap<>();

	//Synchronize this variable whenever you modify the above cannon sessions map
	private static final Integer CANNONS_MAP_LOCK = 1;

	/**
	 * Check if the given town has a cannon session
	 * 
	 * Thread safe
	 * 
	 * @param town the town to check
	 * @return true if the given town has a cannon session
	 */
	public static boolean doesTownHaveCannonSession(Town town) {
		return (new HashMap<>(cannonSessionsRemainingShortTicks)).containsKey(town);		
	}

	/**
	 * The method determines if a town cannon can be fired.
	 *
	 * - Step 1: If a player is firing, and has the siegewar.siege.town.start.cannon.session permission,
	 *   a cannon session starts/refreshes.
	 *
	 * - Step 2: If a cannon session is found to be active, event is allowed,
	 *   otherwise it is prevented.
	 *
	 * @param player the player interacting with the cannon
	 * @param cannon the cannon
	 * @throws TownyException if the cannon use is blocked
	 */
	public static void processPlayerCannonInteraction(Player player, Cannon cannon, String permissionErrorString) throws TownyException {
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

		//If a player is firing and has the start-cannon-session permission, start/refresh the cannon session.
		if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_START_CANNON_SESSION.getNode())) {
			Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
			if (resident != null && resident.hasTown() && resident.getTown() == townWhereCannonIsLocated) {
				synchronized (CANNONS_MAP_LOCK) {
					//Add/refresh cannon session object
					cannonSessionsRemainingShortTicks.put(townWhereCannonIsLocated, SiegeWarSettings.getMaxCannonSessionDuration());
				}
			}
		}

		//If the town has no cannon session, prevent the event.
		if (!doesTownHaveCannonSession(townWhereCannonIsLocated)) {
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
		synchronized (CANNONS_MAP_LOCK) {
			for(Map.Entry<Town, Integer> townTicks: new HashMap<>(cannonSessionsRemainingShortTicks).entrySet()) {
				if(townTicks.getValue() > 1) {
					//Decrement remaining duration
					cannonSessionsRemainingShortTicks.put(townTicks.getKey(), townTicks.getValue()-1);
				} else {
					//Remove cannon session
					cannonSessionsRemainingShortTicks.remove(townTicks.getKey());
				}
			}
		}
	}
}
