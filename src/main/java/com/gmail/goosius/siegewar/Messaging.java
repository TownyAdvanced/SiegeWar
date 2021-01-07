package com.gmail.goosius.siegewar;

import com.palmergames.bukkit.util.Colors;
import org.bukkit.command.CommandSender;

import com.gmail.goosius.siegewar.settings.Translation;

public class Messaging {

	final static String prefix = Translation.of("plugin_prefix");
	
	public static void sendErrMessage(CommandSender sender, String message) {
		sender.sendMessage(prefix + Colors.Red + message);
	}

	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(prefix + Colors.White + message);
	}
}
