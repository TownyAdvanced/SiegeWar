package com.gmail.goosius.siegewar.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Settings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

public class SiegeWarAdminCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewaradminTabCompletes = Arrays.asList("immunity","reload");
	private static final List<String> siegewaradminImmunityTabCompletes = Arrays.asList("town","nation","all");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
		case "immunity":
			if (args.length == 2)
				return NameUtil.filterByStart(siegewaradminImmunityTabCompletes, args[1]);
			
			if (args.length == 3) {
				switch (args[1].toLowerCase()) {
				case "town":
					return getTownyStartingWith(args[2], "t");
				case "nation":
					return getTownyStartingWith(args[2], "n");
				case "all":
					return Arrays.asList("towns");
				}
			}
			
			if (args.length == 4) {
				if (args[1].equalsIgnoreCase("town") || args[1].equalsIgnoreCase("nation"))
					return Arrays.asList("1","2","3","4","5","6");
				if (args[1].equalsIgnoreCase("all") && args[2].equalsIgnoreCase("towns")) {
					return Arrays.asList("1","2","3","4","5","6");
				}
			}
		
		default:
			if (args.length == 1)
				return NameUtil.filterByStart(siegewaradminTabCompletes, args[0]);
			else
				return Collections.emptyList();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		parseSiegeWarAdminCommand((Player) sender, args);
		return true;
	}

	private void parseSiegeWarAdminCommand(CommandSender sender, String[] args) {
		/*
		 * Parse Command.
		 */
		if (args.length > 0) {
			if (sender instanceof Player
					&& !((Player)sender).hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN.getNode(args[0]))) {
				Messaging.sendErrMessage(sender, Translation.of("msg_err_command_disable"));
				return;
			}
			switch (args[0]) {
			case "reload":
				parseSiegeWarReloadCommand(sender);
				break;
			case "immunity":
				parseSiegeWarImmunityCommand(sender, StringMgmt.remFirstArg(args));
				break;
				
			/*
			 * Show help if no command found.
			 */
			default:
				showHelp(sender);
			}
		} else {
			if (sender instanceof Player
					&& !((Player)sender).hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN.getNode())) {
				Messaging.sendErrMessage(sender, Translation.of("msg_err_command_disable"));
				return;
			}
			showHelp(sender);
		}
	}
	
	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/siegewaradmin"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "reload", Translation.of("admin_help_1")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity town [town_name] [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity nation [nation_name] [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity all towns [hours]", ""));
	
	}
	
	private void showImmunityHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/swa immunity"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa immunity", "town [town_name] [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity nation [nation_name] [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa immunity", "all towns [hours]", ""));

	}

	private void parseSiegeWarReloadCommand(CommandSender sender) {
		if (Settings.loadSettingsAndLang()) {
			Messaging.sendMessage(sender, Translation.of("config_and_lang_file_reloaded_successfully"));
			return;
		}
		
		Messaging.sendErrMessage(sender, Translation.of("config_and_lang_file_could_not_be_loaded"));
	}

	private void parseSiegeWarImmunityCommand(CommandSender sender, String[] args) {
		try {
			Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			Messaging.sendMessage(sender, Translation.of("msg_error_must_be_num"));
			showImmunityHelp(sender);
			return;
		}
		long durationMillis = (long)(Long.parseLong(args[2]) * TimeMgmt.ONE_HOUR_IN_MILLIS);

		if (args.length == 3 && args[0].equalsIgnoreCase("town")) {
			//town {townname} {hours}
			Town town = TownyUniverse.getInstance().getTown(args[1]);
			if (town == null) {
				Messaging.sendErrMessage(sender, Translation.of("msg_err_not_registered_1", args[1]));
				return;
			}
			TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			TownyMessaging.sendPrefixedTownMessage(town, Translation.of("msg_set_siege_immunities_town", args[1], args[2]));
			Messaging.sendMessage(sender, Translation.of("msg_set_siege_immunities_town", args[1], args[2]));

		} else if (args.length == 3 && args[0].equalsIgnoreCase("nation")) {
			//nation {nationname} {hours}
			Nation nation = TownyUniverse.getInstance().getNation(args[1]);
			if (nation == null) {
				Messaging.sendErrMessage(sender, Translation.of("msg_err_not_registered_1", args[1]));
				return;
			}
			for(Town town: nation.getTowns()) {
				TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			}
			TownyMessaging.sendPrefixedNationMessage(nation, Translation.of("msg_set_siege_immunities_nation", args[1], args[2]));
			Messaging.sendMessage(sender, Translation.of("msg_set_siege_immunities_nation", args[1], args[2]));

		} else if(args.length == 3
			&& args[0].equalsIgnoreCase("all")
			&& args[1].equalsIgnoreCase("towns")) {
			//all towns 
			for(Town town: new ArrayList<>(TownyUniverse.getInstance().getTowns()))  {
				TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			}
			TownyMessaging.sendGlobalMessage(Translation.of("msg_set_siege_immunities_all", args[2]));

		} else {
			showImmunityHelp(sender);
		}	
	}


	/**
	 * Returns a List<String> containing strings of resident, town, and/or nation names that match with arg.
	 * Can check for multiple types, for example "rt" would check for residents and towns but not nations or worlds.
	 *
	 * @param arg the string to match with the chosen type
	 * @param type the type of Towny object to check for, can be r(esident), t(own), n(ation), w(orld), or any combination of those to check
	 * @return Matches for the arg with the chosen type
	 */
	static List<String> getTownyStartingWith(String arg, String type) {

		List<String> matches = new ArrayList<>();
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		if (type.contains("r")) {
			matches.addAll(townyUniverse.getResidentsTrie().getStringsFromKey(arg));
		}

		if (type.contains("t")) {
			matches.addAll(townyUniverse.getTownsTrie().getStringsFromKey(arg));
		}

		if (type.contains("n")) {
			matches.addAll(townyUniverse.getNationsTrie().getStringsFromKey(arg));
		}

		if (type.contains("w")) { // There aren't many worlds so check even if arg is empty
			matches.addAll(NameUtil.filterByStart(NameUtil.getNames(townyUniverse.getWorldMap().values()), arg));
		}

		return matches;
	}
}

