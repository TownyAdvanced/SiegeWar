package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains utility functions related to calculating and validating distances
 *
 * @author Goosius
 */
public class SiegeWarDistanceUtil {

	/**
	 * List of players registered to active siege zones
	 *
	 * - Every short tick (20 secs), players are registered/de-registered as appropriate.
	 *   - In an active SiegeZone = registered
	 *   - Not in an active SiegeZone = de-registered
	 *
	 * - Also when a player logs in they are registered/re-registered if appropriate.
	 *
	 * - This list & associated methods are good for PVP events, which occur frequently and rapidly
	 * - For infrequent events (like deaths), it is appropriate to use more precise methods.
	 * 
	 * Besides login, if a player enters or leaves a Siege-Zone, this map is not immediately updated.
	 * This can occasionally cause players to be pvp protected/unprotected in inappropriate locations
	 * But the really critical mechanism of keep-inventory will still function as expected,
	 * because it precisely calculates player-location at the moment of death (all in the TownyCombat plugin code).
	 */
	private static Map<Player, Siege> playersRegisteredToActiveSiegeZones = new HashMap<>();

	public static void registerPlayerToActiveSiegeZone(Player player, Siege siege) {
		playersRegisteredToActiveSiegeZones.put(player, siege);
	}

	public static boolean isPlayerRegisteredToActiveSiegeZone(Player player) {
		return playersRegisteredToActiveSiegeZones.containsKey(player);
	}

	public static void recalculatePlayersRegisteredToActiveSiegeZones() {
		playersRegisteredToActiveSiegeZones.clear();
		for(Player player: Bukkit.getOnlinePlayers()) {
			Siege siege = SiegeController.getActiveSiegeAtLocation(player.getLocation());
			if(siege != null)
				playersRegisteredToActiveSiegeZones.put(player, siege);
		}
	}
	
	/**
	 * Returns null if player is not in an active Siege-Zone
	 */
	@Nullable
	public static Siege getActiveSiegeZoneWherePlayerIsRegistered(Player player) {
		return playersRegisteredToActiveSiegeZones.get(player);
	}

	/**
	 * This method returns true if the given location is in an active siegezone
	 *
	 * @param location the target location
	 * @return true is location is in an active siegezone
	 */
	public static boolean isLocationInActiveSiegeZone(Location location) {
		for(Siege siege: SiegeController.getSieges()) {
			if(siege.getStatus().isActive()
				&& SiegeWarDistanceUtil.isInSiegeZone(location, siege)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method returns true if the given location is in an active SiegeCamp or
	 * SiegeAssembly (which are two names for the same feature.)
	 *
	 * @param location the target location
	 * @return true is location is in an active SiegeAssembly
	 */
	public static boolean isLocationInActiveSiegeAssembly(Location location) {
		for(SiegeCamp siegeCamp: SiegeController.getSiegeCamps()) {
			if(SiegeWarDistanceUtil.isInSiegeCampZone(location, siegeCamp)) {
				return true;
			}
		}
		return false;
	}
	public static boolean isTownBlockInActiveSiegeZone(TownBlock townBlock) {
		//Transform worldcoord to Location object
		World world = Bukkit.getWorld(townBlock.getWorld().getName());
		int townBlockSize = TownySettings.getTownBlockSize();
		int x = (townBlock.getX() * townBlockSize) + (townBlockSize /2);
		int y = 0;
		int z = (townBlock.getZ() * townBlockSize) + (townBlockSize /2);
		Location locationOfTownBlock = new Location(world, x, y, z);

		//There is a good chance the townblock will belong to the under-siege town and be in the siegezone
		//Check this quickly before going through the full list of sieges
		Siege siege = SiegeController.getSiege(townBlock.getTownOrNull());
		if(siege != null && siege.getStatus().isActive() && isInSiegeZone(locationOfTownBlock, siege)) {
			//Townblock is in an under siege town, and in the siegezone
			return true;
		} else {
			//Search the full list of sieges
			return isLocationInActiveSiegeZone(locationOfTownBlock);
		}
	}

	public static boolean isInSiegeZone(Location location, Siege siege) {
		return areLocationsCloseHorizontally(location, siege.getFlagLocation(), SiegeWarSettings.getWarSiegeZoneRadiusBlocks());
	}

	public static boolean isInSiegeZone(Entity entity, Siege siege) {
		return areLocationsCloseHorizontally(entity.getLocation(), siege.getFlagLocation(), SiegeWarSettings.getWarSiegeZoneRadiusBlocks());
	}

	public static boolean isInTimedPointZone(Location location, Siege siege) {
		return areLocationsClose(location, siege.getFlagLocation(), TownySettings.getTownBlockSize());
	}

	public static boolean areTownsClose(Town town1, Town town2, int radiusTownblocks) {
		try {
			if(town1.hasHomeBlock() && town2.hasHomeBlock()) {
				return areCoordsClose(
					town1.getHomeBlock().getWorld(),
					town1.getHomeBlock().getCoord(),
					town2.getHomeBlock().getWorld(),
					town2.getHomeBlock().getCoord(),
					radiusTownblocks
				);
			} else {
				return false;
			}
		} catch(TownyException te) {
			return false;
		}
	}

	private static boolean areCoordsClose(TownyWorld world1, Coord coord1, TownyWorld world2, Coord coord2, int radiusTownblocks) {
		if(!world1.getName().equalsIgnoreCase(world2.getName()))
			return false;

		double distanceTownblocks = Math.sqrt(Math.pow(coord1.getX() - coord2.getX(), 2) + Math.pow(coord1.getZ() - coord2.getZ(), 2));

		return distanceTownblocks < radiusTownblocks;
	}

	public static boolean isBlockCloseToTownBlock(Block block, TownBlock townBlock, int radiusTownBlocks) {
		TownyWorld townyWorldOfBlock = TownyAPI.getInstance().getTownyWorld(block.getWorld());
		if(townyWorldOfBlock != null) {
			return areCoordsClose(				
				townyWorldOfBlock,
				Coord.parseCoord(block),
				townBlock.getWorld(),
				townBlock.getCoord(),
				radiusTownBlocks);
		} else {
			return false;
		}
	}

	private static boolean areLocationsClose(Location location1, Location location2, int maxHorizontalDistance) {
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return false;

		//Check horizontal distance
		double xzDistance = MathUtil.distance(location1.getX(), location2.getX(), location1.getZ(), location2.getZ());
		if(xzDistance > maxHorizontalDistance)
			return false;

		//Check vertical distance
		if (location1.getY() == location2.getY())
			return true;
		if (location1.getY() > location2.getY() && location1.getY() - location2.getY() > SiegeWarSettings.getBannerControlVerticalDistanceUpBlocks())
			return false;
		if (location2.getY() > location1.getY() && location2.getY() - location1.getY() > SiegeWarSettings.getBannerControlVerticalDistanceDownBlocks())
			return false;

		return true;
	}

	//Check horizontal distance only
	public static boolean areLocationsCloseHorizontally(Location location1, Location location2, int radius) {
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return false;

		//Check horizontal distance
		return MathUtil.distance(location1.getX(), location2.getX(), location1.getZ(), location2.getZ()) < radius;
	}

	/**
	 * Is there a {@link SiegeCamp} too close to the given {@link Location}.
	 * @param location {@link Location} to check against.
	 */
	public static boolean campTooClose(Location location) {
		for (SiegeCamp camp : SiegeController.getSiegeCamps())
			if (isInSiegeCampZone(location, camp))
				return true;
		return false;
	}
	
	/**
	 * Is the location inside of a {@link SiegeCamp}'s TimedPointZone.
	 * @param location {@link Location}.
	 * @param camp {@link SiegeCamp} to check against.
	 */
	public static boolean isInSiegeCampZone(Location location, SiegeCamp camp) {
		return areLocationsClose(location, camp.getBannerBlock().getLocation(), TownySettings.getTownBlockSize());
	}

	/**
	 * Get distance between given location and given siege
	 * @param location1 given location
	 * @param siege given siege
	 * @return distance
	 */
	public static int getDistanceToSiege(Location location1, Siege siege) {
		Location location2 = siege.getFlagLocation();
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return Integer.MAX_VALUE;
		return (int)MathUtil.distance(location1.getX(), location2.getX(), location1.getZ(), location2.getZ());
	}

	/**
	 * Determine if the target location is protected by trap warfare mitigation
	 *
	 * @param targetLocation target location
	 */
	public static boolean isTargetLocationProtectedByTrapWarfareMitigation(Location targetLocation, Siege siege) {
        int protectionRadiusBlocks = SiegeWarSettings.getTrapWarfareMitigationRadiusBlocks();
        int upperHeightLimit = SiegeWarSettings.getTrapWarfareMitigationUpperHeightLimit();
        int lowerHeightLimit = SiegeWarSettings.getTrapWarfareMitigationLowerHeightLimit();
        Location siegeBannerLocation = siege.getFlagLocation();
        return isTargetLocationProtectedByTrapWarfareMitigation(
			targetLocation,
			siegeBannerLocation,
			protectionRadiusBlocks,
			upperHeightLimit,
			lowerHeightLimit);
	}

	/**
	 * Determine if the target location is protected by trap warfare mitigation
	 *
	 * @param targetLocation target location
	 * @param siegeBannerLocation location of nearby siege banner
	 * @param protectionRadiusBlocks protection radius in blocks
	 * @param upperHeightLimit cannot alter above this
	 * @param lowerHeightLimit cannot alter below this
	 *
	 * @return true if the location is protected
	 */
	public static boolean isTargetLocationProtectedByTrapWarfareMitigation(Location targetLocation, Location siegeBannerLocation, int protectionRadiusBlocks, int upperHeightLimit, int lowerHeightLimit) {
		if(!TownyAPI.getInstance().isWilderness(targetLocation)) {
			return false;  //In town. Protection does not apply.
		} else if(targetLocation.getY() <= siegeBannerLocation.getY() + upperHeightLimit
					&& targetLocation.getY() >= siegeBannerLocation.getY() + lowerHeightLimit) {
			return false;  //Not high/low enough for protection
		} else if(areLocationsCloseHorizontally(targetLocation, siegeBannerLocation, protectionRadiusBlocks)) {
			return true;   //Target location is protected
		} else {
			return false;  //Target location is not protected
		}
	}

	/**
	 * Return true if a town is too far from the nation capital by distance
	 *
	 * @param nation nation to check
	 * @param town town to check
	 * @return true if the town is too far by distance
	 * @throws TownyException
	 */
	public static boolean isTownTooFarFromNationCapitalByDistance(Nation nation, Town town) throws TownyException {
		return TownySettings.getNationRequiresProximity() > 0 && MathUtil.distance(nation.getCapital().getHomeBlock().getCoord(), town.getHomeBlock().getCoord()) > TownySettings.getNationRequiresProximity();
	}
	/**
	 * Return true if a town is too far from the nation capital owing to being in different worlds
	 *
	 * @param nation nation to check
	 * @param town town to check
	 * @return true if the town is too far because capital & town are in different worlds
	 * @throws TownyException
	 */

	public static boolean isTownTooFarFromNationCapitalByWorld(Nation nation, Town town) throws TownyException {
		return TownySettings.getNationRequiresProximity() > 0 && !nation.getCapital().getHomeBlock().getWorld().getName().equals(town.getHomeBlock().getWorld().getName());
	}
}
