package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.playeractions.DestroyBlock;
import com.gmail.goosius.siegewar.playeractions.PlaceBlock;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBurnEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			try {
				DestroyBlock.evaluateSiegeWarDestroyBlockRequest(event);
			} catch (TownyException e) {
				event.setCancelled(true);
				event.setCancelMessage(e.getMessage());
			}
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
	 * SW can affect the emptying of buckets, which could affect a banner.
	 */
	@EventHandler
	public void onBucketUse(TownyBuildEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled() 
				&& !event.isCancelled()
				&& SiegeWarSettings.getSiegeZoneWildernessForbiddenBucketMaterials().contains(event.getMaterial())
				&& event.isInWilderness() 
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getLocation())) {
			event.setCancelMessage(Translatable.of("msg_war_siege_zone_bucket_emptying_forbidden").forLocale(event.getPlayer()));
			event.setCancelled(true);
		}
	}
}
