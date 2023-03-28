package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;
import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class SiegeWarSafeModeListener implements Listener {

	private final SiegeWar plugin;

	public SiegeWarSafeModeListener(SiegeWar instance) {
		plugin = instance;
	}

	private void sendErrorMessage(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + message));
	}
	
	private String getActionErrMsg() {
		return "SiegeWar could not load and is in safe mode, action declined.";
	}

	private String getShortTickErrMsg() {
		return "SiegeWar could not load and is in safe mode.";
	}

	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerBreakDuringSafemode (BlockBreakEvent event) {
		if (!plugin.isError())
			return;
		sendErrorMessage(event.getPlayer(), getActionErrMsg());
		event.setCancelled(true);
	}
	
	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerBuildDuringSafemode (BlockPlaceEvent event) {
		if (!plugin.isError())
			return;
		sendErrorMessage(event.getPlayer(), getActionErrMsg());
		event.setCancelled(true);
	}
	
	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onTownClaimDuringSafemode (TownPreClaimEvent event) {
		if (!plugin.isError())
			return;
		event.setCancelMessage(getActionErrMsg());
		event.setCancelled(true);
	}

	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onTownLeaveNationDuringSafemode (NationPreTownLeaveEvent event) {
		if (!plugin.isError())
			return;
		event.setCancelMessage(getActionErrMsg());
		event.setCancelled(true);
	}

	@EventHandler
	public void onShortTime(NewShortTimeEvent event) {
		if (!plugin.isError())
			return;
		
		Bukkit.getServer().getOnlinePlayers().stream()
		.filter(player -> player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN.getNode()))
		.forEach(player -> sendErrorMessage(player, getShortTickErrMsg()));
	}

}
