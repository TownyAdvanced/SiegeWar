package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.playeractions.PlaceBlock;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBurnEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.gmail.goosius.siegewar.settings.Translation;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarActionListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarActionListener(SiegeWar siegeWar) {

		plugin = siegeWar;
	}
	
	@EventHandler
	public void onBlockBuild(TownyBuildEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled())
			PlaceBlock.evaluateSiegeWarPlaceBlockRequest(event.getPlayer(), event.getBlock(), event);
	}


	/*
	 * SW will prevent an block break from altering an area around a banner.
	 */
	@EventHandler
	public void onBlockBreak(TownyDestroyEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) 
			if (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(event.getBlock())) {
				event.setMessage(Translation.of("msg_err_siege_war_cannot_destroy_siege_banner"));
				event.setCancelled(true);
			}
	}
	
	/*
	 * SW will prevent fire from altering an area around a banner.
	 */
	@EventHandler
	public void onBurn(TownyBurnEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(event.getBlock())) {	
			event.setCancelled(true);	
		}
	}
	
	/*
	 * SW will prevent an explosion from altering an area around a banner.
	 */
	@EventHandler
	public void onBlockExplode(TownyExplodingBlocksEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			List<Block> blockList = event.getTownyFilteredBlockList();
			List<Block> filteredList = new ArrayList<>();
			for (Block block : blockList) {
				if (!SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block)) {
					filteredList.add(block);
				}
			}
			event.setBlockList(filteredList);
		}
	}
	
	/*
	 * SW can affect the emptying of buckets, which could affect a banner.
	 */
	@EventHandler
	public void onBucketUse(TownyBuildEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled() && event.isInWilderness() && SiegeWarSettings.isWarSiegeZoneBucketEmptyingRestrictionsEnabled() && SiegeWarSettings.getWarSiegeZoneBucketEmptyingRestrictionsMaterials().contains(event.getMaterial())) {
			if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getLocation())) {
				event.setMessage(Translation.of("msg_war_siege_zone_bucket_emptying_forbidden"));
				event.setCancelled(true);
			}
		}
	}

}
