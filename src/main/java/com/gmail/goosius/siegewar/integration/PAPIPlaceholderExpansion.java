package com.gmail.goosius.siegewar.integration;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

public class PAPIPlaceholderExpansion extends PlaceholderExpansion {

	@Override
	public @NotNull String getIdentifier() {
		return "siegewar";
	}

	@Override
	public @NotNull String getAuthor() {
		return "TownyAdvanced Team and Contributors";
	}

	@Override
	public @NotNull String getVersion() {
		return SiegeWar.getSiegeWar().getVersion();
	}

	@Override
	public String onRequest(OfflinePlayer player, String identifier) {
		return ChatColor.translateAlternateColorCodes('&', getOfflinePlayerPlaceholder(player, identifier));
	}

	private String getOfflinePlayerPlaceholder(OfflinePlayer player, String identifier) {
		if (player == null && !identifier.startsWith("top_")) {
			return "";
		}

		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());

		if (resident == null)
			return "";
		
		return "";
	}

}
