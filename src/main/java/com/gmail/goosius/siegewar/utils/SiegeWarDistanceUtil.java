package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

/**
 * This class contains utility functions related to calculating and validating distances
 *
 * @author Goosius
 */
public class SiegeWarDistanceUtil {

	private static final int TOWNBLOCKSIZE= TownySettings.getTownBlockSize();

	/**
	 * This method determines if the difference in elevation between a (attack banner) block, 
	 * and the average height of a town block,
	 * is acceptable,
	 * 
	 * The allowed limit is configurable.
	 * 
	 * @param block the attack banner
	 * @param townBlock the town block
	 * @return true if the difference in elevation is acceptable
	 */
	public static boolean isBannerToTownElevationDifferenceOk(Block block, TownBlock townBlock) {
		int allowedDownwardElevationDifference = SiegeWarSettings.getWarSiegeMaxAllowedBannerToTownDownwardElevationDifference();
		int averageDownwardElevationDifference = getAverageBlockToTownDownwardElevationDistance(block, townBlock);
		return averageDownwardElevationDifference <= allowedDownwardElevationDifference;
	}
	
	private static int getAverageBlockToTownDownwardElevationDistance(Block block, TownBlock townBlock) {
		int blockElevation = block.getY();
		
		Location topNorthWestCornerLocation = getTopNorthWestCornerLocation(townBlock.getWorldCoord());
		Location[] surfaceCornerLocations = new Location[4];
		surfaceCornerLocations[0] = SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation);
		surfaceCornerLocations[1] = SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation.add(TOWNBLOCKSIZE,0,0));
		surfaceCornerLocations[2] = SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation.add(0,0,TOWNBLOCKSIZE));
		surfaceCornerLocations[3] = SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation.add(TOWNBLOCKSIZE,0,TOWNBLOCKSIZE));
		
		int totalElevation = 0;
		for(Location surfaceCornerLocation: surfaceCornerLocations) {
			totalElevation += surfaceCornerLocation.getBlockY();
		}
		int averageTownElevation = totalElevation / 4;
		
		return blockElevation - averageTownElevation;
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

	public static boolean isInSiegeZone(Location location, Siege siege) {
		return areLocationsCloseHorizontally(location, siege.getFlagLocation(), SiegeWarSettings.getWarSiegeZoneRadiusBlocks());
	}

	public static boolean isInSiegeZone(Entity entity, Siege siege) {
		return areLocationsCloseHorizontally(entity.getLocation(), siege.getFlagLocation(), SiegeWarSettings.getWarSiegeZoneRadiusBlocks());
	}

	public static boolean isInTimedPointZone(Location location, Siege siege) {
		return areLocationsClose(location, siege.getFlagLocation(), TownySettings.getTownBlockSize(), SiegeWarSettings.getBannerControlVerticalDistanceBlocks());
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

	private static boolean areLocationsClose(Location location1, Location location2, int maxHorizontalDistance, int maxVerticalDistance) {
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return false;

		//Check horizontal distance
		double xzDistance = Math.sqrt(Math.pow(location1.getX() - location2.getX(), 2) + Math.pow(location1.getZ() - location2.getZ(), 2));
		if(xzDistance > maxHorizontalDistance)
			return false;

		//Check vertical distance
		double yDistance = Math.abs(Math.abs(location1.getY() - location2.getY()));
		if(yDistance > maxVerticalDistance)
			return false;

		return true;
	}

	//Check horizontal distance only
	public static boolean areLocationsCloseHorizontally(Location location1, Location location2, int radius) {
		if(!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName()))
			return false;

		//Check horizontal distance
		double xzDistance = Math.sqrt(Math.pow(location1.getX() - location2.getX(), 2) + Math.pow(location1.getZ() - location2.getZ(), 2));
		return xzDistance < radius;
	}

	private static Location getTopNorthWestCornerLocation(WorldCoord worldCoord) {
		int locX = worldCoord.getX() * TOWNBLOCKSIZE;
		int locZ = worldCoord.getZ() * TOWNBLOCKSIZE;
		return new Location(worldCoord.getBukkitWorld(), locX, 255, locZ);
	}

	/**
	 * This method is used in Anti-trap warfare mitigation
	 *
	 * @param location
	 * @return true of the location is in an active timed point zone AND below siege banner altitude
	 */
	public static boolean isLocationInActiveTimedPointZoneAndBelowSiegeBannerAltitude(Location location) {
		//Look through all sieges
		for (Siege siege : SiegeController.getSieges()) {
			if (siege.getStatus().isActive()
				&& isInTimedPointZone(location, siege)
				&& isBelowSiegeBannerAltitude(location, siege))
				return true;
		}
		//Location does not meet the criteria
		return false;
	}

	public static boolean isBelowSiegeBannerAltitude(Location location, Siege siege) {
		return location.getY() < siege.getFlagLocation().getY();
	}
	
	public static boolean campTooClose(Location location) {
		for (SiegeCamp camp : SiegeController.getSiegeCamps())
			if (isInSiegeCampZone(location, camp))
				return true;
		return false;
	}
	
	public static boolean isInSiegeCampZone(Location location, SiegeCamp camp) {
		return areLocationsClose(location, camp.getBannerBlock().getLocation(), TownySettings.getTownBlockSize(), SiegeWarSettings.getBannerControlVerticalDistanceBlocks());
	}
}
