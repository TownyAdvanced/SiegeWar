package com.gmail.goosius.siegewar.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
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
			if(SiegeWarSettings.isTrapWarfareMitigationEnabled()
					&& SiegeWarDistanceUtil.isLocationInActiveTimedPointZoneAndBelowSiegeBannerAltitude(event.getBlock().getLocation())) {
				event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + Translation.of("msg_err_cannot_alter_blocks_below_banner_in_timed_point_zone")));
				event.setCancelled(true);
				return;
			}
			if (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(event.getBlock())) {
				event.setMessage(Translation.of("msg_err_siege_war_cannot_destroy_siege_banner"));
				event.setCancelled(true);
				return;
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
				&& SiegeWarSettings.getSiegeZoneWildernessForbiddenBucketMaterials().contains(event.getMaterial())
				&& event.isInWilderness() 
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getLocation())) {
			event.setMessage(Translation.of("msg_war_siege_zone_bucket_emptying_forbidden"));
			event.setCancelled(true);
		}
	}
}
