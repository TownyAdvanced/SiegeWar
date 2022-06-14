package com.gmail.goosius.siegewar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.utils.TownyComponents;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Messaging {

	final static String prefix = Translation.of("siegewar_plugin_prefix");
	
	public static void sendErrorMsg(CommandSender sender, String message) {
		//Ensure the sender is not null (i.e. is an online player who is not an npc)
        if(sender != null)
            TownyMessaging.sendMessage(sender, TownyComponents.miniMessage(prefix + "<red>" + message));
	}

	public static void sendMsg(CommandSender sender, String message) {
        //Ensure the sender is not null (i.e. is an online player who is not an npc)
        if(sender != null)
            TownyMessaging.sendMessage(sender, TownyComponents.miniMessage(prefix + "<white>" + message));
	}
	
	public static void sendGlobalMessage(String message) {
		SiegeWar.info(message);
		Bukkit.getOnlinePlayers().stream()
				.filter(p -> p != null)
				.filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
				.forEach(p -> sendMsg(p, message));
	}

	public static void sendGlobalMessage(String header, List<String> lines) {
		SiegeWar.info(header);
		for(String line: lines) {
			SiegeWar.info(line);
		}
		for(Player player: Bukkit.getOnlinePlayers()) {
			if(player != null && TownyAPI.getInstance().isTownyWorld(player.getLocation().getWorld())) {
				player.sendMessage(prefix + header);
				for(String line: lines) {
					player.sendMessage(line);
				}
			}
		}
	}
	
	public static void sendErrorMsg(CommandSender sender, Translatable message) {
		// Ensure the sender is not null (i.e. is an online player who is not an npc)
		if (sender != null)
			TownyMessaging.sendMessage(sender, TownyComponents.miniMessage(prefix + "<red>" + message.forLocale(sender)));
	}

	public static void sendMsg(CommandSender sender, Translatable message) {
		// Ensure the sender is not null (i.e. is an online player who is not an npc)
		if (sender != null)
			TownyMessaging.sendMessage(sender, TownyComponents.miniMessage(prefix + "<white>" + message.forLocale(sender)));
	}
	
	public static void sendGlobalMessage(Translatable message) {
		SiegeWar.info(message.defaultLocale());
		Bukkit.getOnlinePlayers().stream()
				.filter(p -> p != null)
				.filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
				.forEach(p -> sendMsg(p, message));
	}

	public static void sendGlobalMessage(Translatable header, List<Translatable> lines) {
		SiegeWar.info(header.defaultLocale());
		for(Translatable line: lines) {
			SiegeWar.info(line.defaultLocale());
		}
		for(Player player: Bukkit.getOnlinePlayers()) {
			if(player != null && TownyAPI.getInstance().isTownyWorld(player.getLocation().getWorld())) {
				player.sendMessage(prefix + header.forLocale(player));
				for(Translatable line: lines) {
					sendMsg(player, line);
				}
			}
		}
	}
	
	public static void sendGlobalMessage(Translatable[] lines) {
		for(Translatable line: lines) {
			SiegeWar.info(line.defaultLocale());
		}
		for(Player player: Bukkit.getOnlinePlayers()) {
			if(player != null && TownyAPI.getInstance().isTownyWorld(player.getLocation().getWorld())) {
				for(Translatable line: lines) {
					sendMsg(player, line);
				}
			}
		}
	}
}
