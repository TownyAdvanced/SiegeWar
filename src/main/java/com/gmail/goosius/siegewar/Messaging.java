package com.gmail.goosius.siegewar;

import org.bukkit.Color;
import org.bukkit.command.CommandSender;

import com.palmergames.bukkit.towny.object.Translation;

public class Messaging {

	final static String prefix = Translation.of("plugin_prefix");
	
	public static void sendErrMessage(CommandSender sender, String message) {
		sender.sendMessage(prefix + Color.RED + message);
	}

	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(prefix + Color.WHITE + message);
	}
}
