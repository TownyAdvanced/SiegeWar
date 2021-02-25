package com.gmail.goosius.siegewar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.util.Colors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import com.gmail.goosius.siegewar.settings.Translation;
import org.bukkit.entity.Player;

import java.util.List;

public class Messaging {

	final static String prefix = Translation.of("plugin_prefix");
	
	public static void sendErrorMsg(CommandSender sender, String message) {
		//Ensure the sender is not null (i.e. is an online player who is not an npc)
        if(sender != null)
	        sender.sendMessage(prefix + Colors.Red + message);
	}

	public static void sendMsg(CommandSender sender, String message) {
        //Ensure the sender is not null (i.e. is an online player who is not an npc)
        if(sender != null)
    		sender.sendMessage(prefix + Colors.White + message);
	}
	
	public static void sendGlobalMessage(String message) {
		System.out.println(prefix + message);
		Bukkit.getOnlinePlayers().stream()
				.filter(p -> p != null)
				.filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
				.forEach(p -> sendMsg(p, prefix + message));
	}

	public static void sendGlobalMessage(String header, List<String> lines) {
		System.out.println(prefix + header);
		for(String line: lines) {
			System.out.println(line);
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
}
