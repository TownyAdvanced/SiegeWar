package com.gmail.goosius.siegewar;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.util.Colors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.settings.Translation;

public class Messaging {

	final static String prefix = Translation.of("plugin_prefix");
	
	public static void sendErrorMsg(CommandSender sender, String message) {
		sender.sendMessage(prefix + Colors.Red + message);
	}

	public static void sendMsg(CommandSender sender, String message) {
		sender.sendMessage(prefix + Colors.White + message);
	}
	
	public static void sendGlobalMessage(String message) {
       for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null)
                try {
                    if (TownyUniverse.getInstance().getDataSource().getWorld(player.getLocation().getWorld().getName()).isUsingTowny())
                        sendMsg(player, message);
                } catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
        }
	}
}
