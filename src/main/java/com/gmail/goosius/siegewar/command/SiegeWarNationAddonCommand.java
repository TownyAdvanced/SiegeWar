package com.gmail.goosius.siegewar.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;

public class SiegeWarNationAddonCommand implements TabExecutor {

	public SiegeWarNationAddonCommand() {
		AddonCommand nationSiegeWarCommand = new AddonCommand(CommandType.NATION, "siegewar", this);
		TownyCommandAddonAPI.addSubCommand(nationSiegeWarCommand);
	}
	
	private CommandSender sender;

	private static final List<String> nationSiegeWarTabCompletes = Arrays.asList(
			"occupiedhometowns",
			"occupiedforeigntowns"
	);

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		switch (args[0].toLowerCase()) {
		case "occupiedhometowns":
		case "occupiedforeigntowns":
			if (args.length == 2)
				return SiegeWarAdminCommand.getTownyStartingWith(args[1], "n");
		}
		
		if (args.length == 1)
			return NameUtil.filterByStart(nationSiegeWarTabCompletes, args[0]);
		else
			return Collections.emptyList();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.sender = sender;
		parseNationSiegeWarCommand(args);
		return true;
	}

	private void parseNationSiegeWarCommand(String[] args) {
		if (args.length == 0) {
			showHelp();
			return;
		}
		Nation nation = null;
		if (args.length == 1 && sender instanceof Player) {
			nation = TownyAPI.getInstance().getResident((Player) sender).getNationOrNull();
			if (nation == null) {
				TownyMessaging.sendErrorMsg(Translatable.of("msg_err_dont_belong_nation"));
				return;
			}
		} else if (args.length == 1) {
			showHelp();
			return;
		} else if (args.length == 2) {
			nation = TownyAPI.getInstance().getNation(args[1]);
			if (nation == null) {
				TownyMessaging.sendErrorMsg(Translatable.of("msg_err_invalid_name", args[2]));
				return;
			}
		}		
		
		switch (args[0].toLowerCase()) {
		case "occupiedhometowns":
			Messaging.sendMsg(sender, getFormattedStrings(Translation.of("status_nation_occupied_home_towns"), TownOccupationController.getOccupiedHomeTowns(nation)));
			break;
		case "occupiedforeigntowns":
			Messaging.sendMsg(sender, getFormattedStrings(Translation.of("status_nation_occupied_foreign_towns"), TownOccupationController.getOccupiedForeignTowns(nation)));
			break;
		default:
			showHelp();
		}
		
	}

	private String getFormattedStrings(String prefix, List<Town> list) {
		return String.format(prefix, list.size()) + getFormattedTownList(list);
	}

	private static String getFormattedTownList(List<Town> towns) {
		List<String> lines = new ArrayList<>();
		boolean longname = towns.size() < 20;
		for (Town town : towns) {
			lines.add(longname ? town.getFormattedName() : town.getName());
		}
		return StringMgmt.join(lines, ", ");
	}

	private void showHelp() {
		sender.sendMessage(ChatTools.formatTitle("/nation siegewar"));
		sender.sendMessage(ChatTools.formatCommand("/nation siegewar", "occupiedhometowns [nation]", ""));
		sender.sendMessage(ChatTools.formatCommand("/nation siegewar", "occupiedforeigntowns [nation]", ""));		
	}

}

