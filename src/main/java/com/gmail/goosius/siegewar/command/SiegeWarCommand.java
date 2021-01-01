package com.gmail.goosius.siegewar.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;

public class SiegeWarCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewarTabCompletes = Arrays.asList("nation");
	
	private static final List<String> siegewarNationTabCompletes = Arrays.asList("refund");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
		case "nation":
			return NameUtil.filterByStart(siegewarNationTabCompletes, args[1]);
		
		default:
			return NameUtil.filterByStart(siegewarTabCompletes, args[0]);
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player)
			parseSiegeWarCommand((Player) sender, args);
		else {
			sender.sendMessage(ChatTools.formatTitle("/siegewar"));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "refund", Translation.of("nation_help_11")));
		}
		return true;
	}

	private void parseSiegeWarCommand(Player player, String[] args) {
		switch (args[0]) {
		case "nation":
			parseSiegeWarNationCommand(player, StringMgmt.remFirstArg(args));
			break;
		default:
			player.sendMessage(ChatTools.formatTitle("/siegewar"));
			player.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "refund", Translation.of("nation_help_11")));
		}
	}

	private void parseSiegeWarNationCommand(Player player, String[] remFirstArg) {
		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_NATION_REFUND.getNode())) {
			player.sendMessage(Translation.of("msg_command_disabled"));
			return;
		}
		try {
			SiegeWarMoneyUtil.claimNationRefund(player);
		} catch (Exception e) {
			player.sendMessage(e.getMessage());
		}
	}

}
