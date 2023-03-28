package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class contains utility functions related to blocks 
 * (e.g. placing/breaking/analysing nearby blocks)
 *
 * @author Goosius
 */
public class SiegeWarBlockUtil {

	/**
	 * This method gets a list of all adjacent townblocks.
	 *
	 * @param block the block to start from
	 * @return list of all adjacent townblocks
	 */
	public static List<TownBlock> getAllAdjacentTownBlocks(Block block) {
		List<TownBlock> result = new ArrayList<>();
		result.addAll(getCardinalAdjacentTownBlocks(block));
		result.addAll(getNonCardinalAdjacentTownBlocks(block));
		return result;
	}

	/**
	 * This method gets a list of all adjacent sieges.
	 *
	 * @param block the block to start from
	 * @return list of all adjacent sieges (active or not)
	 */
	public static Set<Siege> getAllAdjacentSieges(Block block) {
		return getAllAdjacentTownBlocks(block).stream()
			.filter(tb -> tb.hasTown() && SiegeController.hasSiege(tb.getTownOrNull()))
			.map(tb -> SiegeController.getSiege(tb.getTownOrNull()))
			.collect(Collectors.toSet());
	}

	/**
	 * This method gets a list of adjacent cardinal townblocks, either N, S, E or W.
	 * 
	 * @param block the block to start from
	 * @return list of adjacent cardinal townblocks
	 */
	public static List<TownBlock> getCardinalAdjacentTownBlocks(Block block) {
		List<WorldCoord> coOrdinates = new ArrayList<>();
		WorldCoord startingCoOrdinate = WorldCoord.parseWorldCoord(block);
		coOrdinates.add(startingCoOrdinate.add(0,-1));
		coOrdinates.add(startingCoOrdinate.add(0,1));
		coOrdinates.add(startingCoOrdinate.add(1,0));
		coOrdinates.add(startingCoOrdinate.add(-1,0));
		return getTownBlocks(coOrdinates);
	}

	/**
	 * This method gets a list of adjacent non-cardinal townblocks, either NE, SE, SW or NW.
	 *
	 * @param block the block to start from
	 * @return list of adjacent noncardinal townblocks
	 */
	public static List<TownBlock> getNonCardinalAdjacentTownBlocks(Block block) {
		List<WorldCoord> coOrdinates = new ArrayList<>();
		WorldCoord startingCoOrdinate = WorldCoord.parseWorldCoord(block);
		coOrdinates.add(startingCoOrdinate.add(-1,1));
		coOrdinates.add(startingCoOrdinate.add(1,1));
		coOrdinates.add(startingCoOrdinate.add(1,-1));
		coOrdinates.add(startingCoOrdinate.add(-1,-1));
		return getTownBlocks(coOrdinates);
	}

	private static List<TownBlock> getTownBlocks(List<WorldCoord> coords) {
		return coords.stream()
			.filter(wc -> wc.hasTownBlock())
			.map(wc -> wc.getTownBlockOrNull())
			.collect(Collectors.toList());
	}

	/**
	 * 	Determine if the block is an active siege banner, or the support block.
	 * 	
	 * 	First look at the material of both the target block and the block above it.
	 * 	Return false if neither is a standing banner.
	 * 	
	 * 	Then look at all siege zones within 'in progress' sieges,
	 * 	and determine if the target block or block above it is a siege banner.
	 * 	
	 * 	Note that we don't try to look at the nearby townblocks to find nearby siege zones,
	 * 	....because mayor may have unclaimed townblocks after the siege started.
	 *
	 * @param block the block to be considered
	 * @return true if the block is near an active siege banner
	 */
	public static boolean isBlockNearAnActiveSiegeBanner(Block block) {
		
		//If either the target block or block above it is a standing coloured banner, continue, else return false
		if(isStandingColouredBanner(block) || isStandingColouredBanner(block.getRelative(BlockFace.UP))) {
			
			//Look through all siege zones
			Location locationOfBlock = block.getLocation();
			Location locationOfBlockAbove = block.getRelative(BlockFace.UP).getLocation();
			Location locationOfSiegeBanner;
			for (Siege siege : SiegeController.getSieges()) {

				if (!siege.getStatus().isActive()) {
					continue;
				}

				locationOfSiegeBanner = siege.getFlagLocation();
				if (locationOfBlock.equals(locationOfSiegeBanner) || locationOfBlockAbove.equals(locationOfSiegeBanner)) {
					return true;
				}
			}
		}
		
		//No active siege banner found near given block
		return false;
	}

	/**
	 * 	Determine if the block is a siege banner during a SiegeCamp session, or the support block.
	 * 	
	 * 	First look at the material of both the target block and the block above it.
	 * 	Return false if neither is a standing banner.
	 * 	
	 * 	Then look at all SiegeCamps and determine if it is the block.
	 * 	
	 * 	Note that we don't try to look at the nearby townblocks to find nearby siege zones,
	 * 	....because mayor may have unclaimed townblocks after the siege started.
	 *
	 * @param block the block to be considered
	 * @return true if the block is near an active siege banner
	 */
	public static boolean isBlockNearAnActiveSiegeCampBanner(Block block) {
		
		//If either the target block or block above it is a standing coloured banner, continue, else return false
		if(isStandingColouredBanner(block) || isStandingColouredBanner(block.getRelative(BlockFace.UP))) {
			
			//Look through all siegecamps
			Location locationOfBlock = block.getLocation();
			Location locationOfBlockAbove = block.getRelative(BlockFace.UP).getLocation();
			Location locationOfSiegeBanner;
			for (SiegeCamp siegeCamp : SiegeController.getSiegeCamps()) {

				locationOfSiegeBanner = siegeCamp.getBannerBlock().getLocation();
				if (locationOfBlock.equals(locationOfSiegeBanner) || locationOfBlockAbove.equals(locationOfSiegeBanner)) {
					return true;
				}
			}
		}
		
		//No siegecamp banner found near given block
		return false;
	}

	private static boolean isStandingColouredBanner(Block block) {
		switch (block.getType()) {
			case BLACK_BANNER:
			case BLUE_BANNER:
			case BROWN_BANNER:
			case CYAN_BANNER:
			case GRAY_BANNER:
			case GREEN_BANNER:
			case LIGHT_BLUE_BANNER:
			case LIGHT_GRAY_BANNER:
			case LIME_BANNER:
			case MAGENTA_BANNER:
			case ORANGE_BANNER:
			case PINK_BANNER:
			case PURPLE_BANNER:
			case RED_BANNER:
			case YELLOW_BANNER:
				return true;
			case WHITE_BANNER:
				return ((Banner) block.getState()).getPatterns().size() > 0;
			default:
				return false;
		}
	}

	/**
	 * This method determines if the supporting block is unstable (e.g. sand,gravel)
	 *
	 * @param block the block
	 * @return true if support block is unstable
	 */
	public static boolean isSupportBlockUnstable(Block block) {
		Material blockBelowBanner = block.getRelative(BlockFace.DOWN).getType();
		if (Tag.BANNERS.isTagged(blockBelowBanner) || Tag.SIGNS.isTagged(blockBelowBanner) || Tag.WALL_SIGNS.isTagged(blockBelowBanner)
			|| Tag.LOGS.isTagged(blockBelowBanner) || Tag.LEAVES.isTagged(blockBelowBanner) || Tag.ANVIL.isTagged(blockBelowBanner)
			|| Tag.DOORS.isTagged(blockBelowBanner))
			return true;
		switch(blockBelowBanner) {
			case AIR:
			case CAVE_AIR:
			case VOID_AIR:
			case GRAVEL:
			case SAND:
			case RED_SAND:
			case CACTUS:
			case NETHER_WART_BLOCK:
			case WARPED_WART_BLOCK:
			case LANTERN:
			case SOUL_LANTERN:
			case MUSHROOM_STEM:
			case RED_MUSHROOM_BLOCK:
			case BROWN_MUSHROOM_BLOCK:
			case BAMBOO:
			case TURTLE_EGG:
				return true;
			default:
				return false;
		}
	}

}
