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
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Settings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.timeractions.AttackerWin;
import com.gmail.goosius.siegewar.timeractions.DefenderWin;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

public class SiegeWarAdminCommand implements CommandExecutor, TabCompleter {

	private static final List<String> siegewaradminTabCompletes = Arrays.asList("immunity","reload","siege","town");
	private static final List<String> siegewaradminImmunityTabCompletes = Arrays.asList("town","nation","alltowns");
	private static final List<String> siegewaradminSiegeTabCompletes = Arrays.asList("setpoints","end");
	private static final List<String> siegewaradminTownTabCompletes = Arrays.asList("setcaptured","setplundered");

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
				case "alltowns":
					return Arrays.asList("0","1","2","3","4","5","6");
				}
			}
			
			if (args.length == 4) {
				if (args[1].equalsIgnoreCase("town") || args[1].equalsIgnoreCase("nation"))
					return Arrays.asList("0","1","2","3","4","5","6");
			}
		case "siege":
			if (((Player)sender).hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN_SIEGE.getNode())) {
				if (args.length == 2)
					return NameUtil.filterByStart(new ArrayList<>(SiegeController.getSiegedTownNames()), args[1]);

				if (args.length == 3)
					return NameUtil.filterByStart(siegewaradminSiegeTabCompletes, args[2]);

				if (args.length == 4 ) {
					if (args[2].equalsIgnoreCase("addcontrol") || args[2].equalsIgnoreCase("removecontrol"))
						return getTownyStartingWith(args[3], "r");
				}
			}
		case "town":
			if (args.length == 2)
				return getTownyStartingWith(args[1], "t");

			if (args.length == 3)
				return NameUtil.filterByStart(siegewaradminTownTabCompletes, args[2]);

			if (args.length == 4)
				if (args[2].equalsIgnoreCase("setcaptured") || args[2].equalsIgnoreCase("setplundered"))
					return Arrays.asList("true","false");
		default:
			if (args.length == 1)
				return NameUtil.filterByStart(siegewaradminTabCompletes, args[0]);
			else
				return Collections.emptyList();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		parseSiegeWarAdminCommand(sender, args);
		return true;
	}

	private void parseSiegeWarAdminCommand(CommandSender sender, String[] args) {
		/*
		 * Parse Command.
		 */
		if (args.length > 0) {
			if (sender instanceof Player && !((Player)sender).hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN.getNode(args[0]))) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_command_disable"));
				return;
			}
			switch (args[0]) {
			case "reload":
				parseSiegeWarReloadCommand(sender);
				break;
			case "immunity":
				parseSiegeWarImmunityCommand(sender, StringMgmt.remFirstArg(args));
				break;
			case "siege":
				parseSiegeWarSiegeCommand(sender, StringMgmt.remFirstArg(args));
				break;
			case "town":
				parseSiegeWarTownCommand(sender, StringMgmt.remFirstArg(args));
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
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_command_disable"));
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
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity alltowns [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "siege [town_name] setpoints [points]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "siege [town_name] end", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "town [town_name] setplundered [true/false]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "town [town_name] setcaptured [true/false]", ""));
	}
	
	private void showImmunityHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/swa immunity"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity town [town_name] [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity nation [nation_name] [hours]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity alltowns [hours]", ""));
	}

	private void showSiegeHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/swa siege"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "siege [town_name] setpoints [points]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "siege [town_name] end", ""));
	}

	private void showTownHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/swa town"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "town [town_name] setplundered [true/false]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "town [town_name] setcaptured [true/false]", ""));
	}

	private void parseSiegeWarReloadCommand(CommandSender sender) {
		if (Settings.loadSettingsAndLang()) {
			Messaging.sendMsg(sender, Translation.of("config_and_lang_file_reloaded_successfully"));
			return;
		}
		
		Messaging.sendErrorMsg(sender, Translation.of("config_and_lang_file_could_not_be_loaded"));
	}

	private void parseSiegeWarImmunityCommand(CommandSender sender, String[] args) {
		try {
			if (args[0].equalsIgnoreCase("alltowns"))
				Integer.parseInt(args[1]);
			else
				Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			Messaging.sendMsg(sender, Translation.of("msg_error_must_be_num"));
			showImmunityHelp(sender);
			return;
		}

		if (args.length == 3 && args[0].equalsIgnoreCase("town")) {
			//town {townname} {hours}
			Town town = TownyUniverse.getInstance().getTown(args[1]);
			if (town == null) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_not_registered_1", args[1]));
				return;
			}
			long durationMillis = (long)(Long.parseLong(args[2]) * TimeMgmt.ONE_HOUR_IN_MILLIS);
			TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			TownyMessaging.sendPrefixedTownMessage(town, Translation.of("msg_set_siege_immunities_town", args[1], args[2]));
			Messaging.sendMsg(sender, Translation.of("msg_set_siege_immunities_town", args[1], args[2]));

		} else if (args.length == 3 && args[0].equalsIgnoreCase("nation")) {
			//nation {nationname} {hours}
			Nation nation = TownyUniverse.getInstance().getNation(args[1]);
			if (nation == null) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_not_registered_1", args[1]));
				return;
			}
			long durationMillis = (long)(Long.parseLong(args[2]) * TimeMgmt.ONE_HOUR_IN_MILLIS);
			for (Town town : nation.getTowns()) {
				TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			}
			TownyMessaging.sendPrefixedNationMessage(nation, Translation.of("msg_set_siege_immunities_nation", args[1], args[2]));
			Messaging.sendMsg(sender, Translation.of("msg_set_siege_immunities_nation", args[1], args[2]));

		} else if(args.length == 2
			&& args[0].equalsIgnoreCase("alltowns")) {
			//all towns
			long durationMillis = (long)(Long.parseLong(args[1]) * TimeMgmt.ONE_HOUR_IN_MILLIS);
			for (Town town : new ArrayList<>(TownyUniverse.getInstance().getTowns()))  {
				TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			}
			Messaging.sendGlobalMessage(Translation.of("msg_set_siege_immunities_all", args[1]));

		} else {
			showImmunityHelp(sender);
		}
	}

	private void parseSiegeWarSiegeCommand(CommandSender sender, String[] args) {
		if (args.length >= 2) {
			Town town = TownyUniverse.getInstance().getTown(args[0]);
			Siege siege = SiegeController.getSiege(town);

			if (town == null) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_town_not_registered", args[0]));
				return;
			}
			if (!SiegeController.hasActiveSiege(town)) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_not_being_sieged", town.getName()));
				return;
			}

			switch(args[1].toLowerCase()) {
				case "setpoints":
					if (args.length < 3) {
						showSiegeHelp(sender);
					}
					try {
						Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						Messaging.sendErrorMsg(sender, Translation.of("msg_error_must_be_num"));
						return;
					}

					int newPoints = Integer.parseInt(args[2]);
					siege.setSiegePoints(newPoints);
					SiegeController.saveSiege(siege);
					Messaging.sendMsg(sender, Translation.of("msg_swa_set_points_success", newPoints, town.getName()));
					return;

				case "end":
					if (siege.getSiegePoints() < 1)
						DefenderWin.defenderWin(siege, siege.getDefendingTown());
					else
						AttackerWin.attackerWin(siege, siege.getAttackingNation());
					return;
			}

		} else
			showSiegeHelp(sender);
	}

	private void parseSiegeWarTownCommand(CommandSender sender, String[] args) {
		if (args.length >= 3) {
			Town town = TownyUniverse.getInstance().getTown(args[0]);
			if (town == null) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_town_not_registered", args[0]));
				return;
			}
			if (!SiegeController.hasActiveSiege(town) && args[1].equalsIgnoreCase("setplundered")) {
				Messaging.sendErrorMsg(sender, Translation.of("msg_err_not_being_sieged", town.getName()));
				return;
			}

			switch (args[1].toLowerCase()) {
				case "setplundered": {
					Boolean plundered = Boolean.parseBoolean(args[2]);
					Siege siege = SiegeController.getSiege(town);
					siege.setTownPlundered(plundered);
					SiegeController.saveSiege(siege);
					Messaging.sendMsg(sender, Translation.of("msg_swa_set_plundered_success", plundered.toString().toUpperCase(), town.getName()));
					return;
				}
				case "setcaptured": {
					Boolean captured = Boolean.parseBoolean(args[2]);
					town.setConquered(captured);
					if (SiegeController.hasActiveSiege(town)) {
						Siege siege = SiegeController.getSiege(town);
						siege.setTownInvaded(captured);
						SiegeController.saveSiege(siege);
					}
					Messaging.sendMsg(sender, Translation.of("msg_swa_set_captured_success", captured.toString().toUpperCase(), town.getName()));
					return;
				}
			}
		} else
			showTownHelp(sender);
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

