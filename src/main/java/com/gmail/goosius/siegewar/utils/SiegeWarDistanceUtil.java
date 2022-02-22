package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.util.MathUtil;

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
}
