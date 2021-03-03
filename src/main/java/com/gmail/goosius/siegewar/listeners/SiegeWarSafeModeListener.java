package com.gmail.goosius.siegewar.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;

public class SiegeWarSafeModeListener implements Listener {

	private void sendErrorMessage(Player player) {
		Messaging.sendErrorMsg(player, getErrMsg());
	}
	
	private String getErrMsg() {
		return "SiegeWar could not load and is in safe mode, action declined.";
	}
	
	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerBreakDuringSafemode (BlockBreakEvent event) {
		if (!SiegeWar.isError())
			return;
		sendErrorMessage(event.getPlayer());
		event.setCancelled(true);
	}
	
	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerBuildDuringSafemode (BlockPlaceEvent event) {
		if (!SiegeWar.isError())
			return;
		sendErrorMessage(event.getPlayer());
		event.setCancelled(true);
	}
	
	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onTownClaimDuringSafemode (TownPreClaimEvent event) {
		if (!SiegeWar.isError())
			return;
		
		event.setCancelMessage(getErrMsg());
		event.setCancelled(true);
	}

	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onTownLeaveNationDuringSafemode (NationPreTownLeaveEvent event) {
		if (!SiegeWar.isError())
			return;
		
		event.setCancelMessage(getErrMsg());
		event.setCancelled(true);
	}	
	
}
