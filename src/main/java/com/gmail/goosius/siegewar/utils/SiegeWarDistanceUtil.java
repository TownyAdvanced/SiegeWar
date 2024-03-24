package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.util.MathUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class contains utility functions related to calculating and validating distances
 *
 * @author Goosius
 */
public class SiegeWarDistanceUtil {

	/**
	 * This is a cached record of players in active siege zones
	 *
	 * We cache these records to save processor power
	 *   - Example:
	 *     - For frequent events like PVP events, 
	 *     - we can avoid having to frequently calculate the distance to siege banners.
	 * 
	 * Every short tick (20 secs), players are registered/de-registered as appropriate.
	 *   - In an active SiegeZone = registered
	 *   - Not in an active SiegeZone = de-registered
	 *
	 * When a player logs in they are registered/re-registered if appropriate.
	 *
	 * if a player enters or leaves a Siege-Zone, this map is not immediately updated.
	 * This can very occasionally cause players to be pvp protected/unprotected in inappropriate locations
	 * 
	 * Note that for player deaths, this cache is NOT used, and a full distance check is done.
	 */
	private static Map<Player, Siege> playersRegisteredToActiveSiegeZones = new HashMap<>();

	public static void registerPlayerToActiveSiegeZone(Player player, Siege siege) {
		playersRegisteredToActiveSiegeZones.put(player, siege);
	}

	public static boolean isPlayerRegisteredToActiveSiegeZone(Player player) {
		return playersRegisteredToActiveSiegeZones.containsKey(player);
	}

	public static Siege getActiveSiegeZonePlayerIsRegisteredTo(Player player) {
		return playersRegisteredToActiveSiegeZones.get(player);
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
	 * This method returns true if the given location is in an active siegezone
	 *
	 * @param location the target location
	 * @return true if location is in an active siegezone
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


	public static boolean areTownBlocksClose(@NotNull TownBlock townBlock1, @NotNull TownBlock townBlock2, int radiusTownblocks) {
		return areCoordsClose(
			townBlock1.getWorld(),
			townBlock1.getCoord(),
			townBlock2.getWorld(), 
			townBlock2.getCoord(),
			radiusTownblocks);
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

	private static boolean areLocationsClose(Location location1, Location location2, int maxAllowedDistance) {
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return false;

		//Check horizontal distance
		double xzDistance = MathUtil.distance(location1.getX(), location2.getX(), location1.getZ(), location2.getZ());
		if(xzDistance > maxAllowedDistance)
			return false;

		//Check vertical distance
		
		double yDistance = Math.sqrt(MathUtil.sqr(location1.getY()) - MathUtil.sqr(location2.getY()));
		if(yDistance > maxAllowedDistance)
			return false;

		return true;
	}

	//Check horizontal distance only
	public static boolean areLocationsCloseHorizontally(Location location1, Location location2, int radius) {
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return false;

		//Check horizontal distance
		return MathUtil.distance(location1.getX(), location2.getX(), location1.getZ(), location2.getZ()) <= radius;
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
	 * Throws a TownyException with Translatable error message if a town is too far
	 * from a nation's capital.
	 * 
	 * @param nation Nation
	 * @param town   Town
	 * @throws TownyException thrown if the town is too far away.
	 */
	public static void throwIfTownIsTooFarFromNationCapitalByDistance(Nation nation, Town town) throws TownyException {
		if (isTownTooFarFromNationCapitalByDistance(nation, town))
			throw new TownyException(Translatable.of("msg_err_town_not_close_enough_to_nation", town.getName()));
	}
	
	/**
	 * Return true if a town is too far from the nation capital by distance
	 *
	 * @param nation nation to check
	 * @param town town to check
	 * @return true if the town is too far by distance
	 * @throws TownyException thrown if the town or nation capital has no Homeblock set.
	 */
	private static boolean isTownTooFarFromNationCapitalByDistance(Nation nation, Town town) throws TownyException {
		return TownySettings.getNationProximityToCapital() > 0 && MathUtil.distance(nation.getCapital().getHomeBlock().getCoord(), town.getHomeBlock().getCoord()) > TownySettings.getNationProximityToCapital();
	}

	/**
	 * Throws a TownyException with Translatable error message if a town is too far
	 * from a nation's capital because they are in separate worlds.
	 * 
	 * @param nation Nation
	 * @param town Town 
	 * @throws TownyException thrown if the town is too far away.
	 */
	public static void throwIfTownIsTooFarFromNationCapitalByWorld(Nation nation, Town town) throws TownyException {
		if (isTownTooFarFromNationCapitalByWorld(nation, town))
			throw new TownyException(Translatable.of("msg_err_nation_homeblock_in_another_world"));
	}
	/**
	 * Return true if a town is too far from the nation capital owing to being in different worlds
	 *
	 * @param nation nation to check
	 * @param town town to check
	 * @return true if the town is too far because capital & town are in different worlds
	 * @throws TownyException thrown if the town or nation capital has no Homeblock set.
	 */
	private static boolean isTownTooFarFromNationCapitalByWorld(Nation nation, Town town) throws TownyException {
		return TownySettings.getNationProximityToCapital() > 0 && !nation.getCapital().getHomeBlock().getWorld().getName().equals(town.getHomeBlock().getWorld().getName());
	}

	public static boolean isInANonBesiegedTown(Location location) {
		Town town = TownyAPI.getInstance().getTown(location);
		if(town != null && !SiegeController.hasActiveSiege(town)) {
			return true;
		} else {
			return false;
		}
	}

	public static int getDistanceInTownBlocks(Town townA, Town townB) {
		Coord coord1 = townA.getHomeBlockOrNull().getCoord();
		Coord coord2= townB.getHomeBlockOrNull().getCoord();
		return (int)(Math.sqrt(Math.pow(coord1.getX() - coord2.getX(), 2) + Math.pow(coord1.getZ() - coord2.getZ(), 2)));
	}

	public static List<Town> getNearbyTownsPeacefulTowns(@NotNull TownBlock townBlock, int radius) {
		List<Town> result = new ArrayList<>();
		int radiusInTownBlocks = radius / TownySettings.getTownBlockSize();
		for(Town town: TownyAPI.getInstance().getTowns()) {
			if(SiegeWarTownPeacefulnessUtil.isTownPeaceful(town)
					&& town.hasHomeBlock() 
					&& areTownBlocksClose(town.getHomeBlockOrNull(), townBlock, radiusInTownBlocks)) {
				result.add(town);
			}
		}
		return result;
	}

	public static TownBlock findFirstValidTownBlockAdjacentToMinecraftBlock(Block block) {
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		Block adjacentBlock;
		for(int i = 0; i < 8; i ++) {
			adjacentBlock = block.getRelative(x[i], 0, z[i]);
			if(!TownyAPI.getInstance().isWilderness(adjacentBlock)) {
				//Adjacent townblock found
				WorldCoord adjacentWorldCoord = WorldCoord.parseWorldCoord(adjacentBlock);
				TownBlock adjacentTownBlock = TownyAPI.getInstance().getTownBlock(adjacentWorldCoord);
				if(adjacentTownBlock != null && adjacentTownBlock.hasTown()) {
					return adjacentTownBlock;
				}
			}
		}
		return null;
	}
}
