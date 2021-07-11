package com.gmail.goosius.siegewar.utils;

import at.pavlov.cannons.cannon.Cannon;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
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
				TownMetaDataController.setCannonSessionRemainingShortTicks(townWhereCannonIsLocated, SiegeWarSettings.getMaxCannonSessionDuration());
			}
		}

		//If the town cannon session is inactive, prevent the event, otherwise allow the event.
		if (TownMetaDataController.getCannonSessionRemainingShortTicks(townWhereCannonIsLocated) == 0) {
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
		for(Town town: TownyUniverse.getInstance().getTowns()) {
			if(TownMetaDataController.getCannonSessionRemainingShortTicks(town) != 0) {
				//Decrement the remaining short ticks
				TownMetaDataController.setCannonSessionRemainingShortTicks(town, TownMetaDataController.getCannonSessionRemainingShortTicks(town) -1);
			}
		}
	}
}
