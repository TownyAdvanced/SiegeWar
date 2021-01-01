package com.gmail.goosius.siegewar.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

public class SiegeWarAdminCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewaradminTabCompletes = Arrays.asList("immunity");
	private static final List<String> siegewaradminImmunityTabCompletes = Arrays.asList("town","all");
	private static final List<String> siegewaradminImmunityAllTabCompletes = Arrays.asList("towns");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
		case "immunity":
			if (args.length == 2)
				return NameUtil.filterByStart(siegewaradminImmunityTabCompletes, args[1]);
			
			if (args.length == 3) {
				switch (args[1].toLowerCase()) {
				case "town":
					return getTownyStartingWith(args[2], "t");
				case "all":
					return NameUtil.filterByStart(siegewaradminImmunityAllTabCompletes, args[2]);
				}
			}
			
			if (args.length == 4) {
				if (args[1].equalsIgnoreCase("town"))
					return Arrays.asList("1","2","3","4","5","6");
				if (args[1].equalsIgnoreCase("all")) {
					return Arrays.asList("in nation", "1","2","3","4","5","6");
				}
			}
			
			if (args.length == 6) {
				if (args[1].equalsIgnoreCase("all") && args[4].equalsIgnoreCase("nation")) {
					return getTownyStartingWith(args[5], "n");
				}
			}
			
			if (args.length == 7) {
				return Arrays.asList("1","2","3","4","5","6");
			}
				
		
		default:
			return NameUtil.filterByStart(siegewaradminTabCompletes, args[0]);
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		parseSiegeWarAdminCommand((Player) sender, args);
		return true;
	}

	private void parseSiegeWarAdminCommand(CommandSender sender, String[] args) {
		switch (args[0]) {
		case "immunity":
			parseSiegeWarImmunityCommand(sender, StringMgmt.remFirstArg(args));
			break;
		default:
			sender.sendMessage(ChatTools.formatTitle("/siegewaradmin"));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity town [town_name] [hours]", ""));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity all towns in nation [nation_name] [hours]", ""));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/swa", "immunity all towns [hours]", ""));

		}
	}

	private void parseSiegeWarImmunityCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player
				&& !((Player)sender).hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN_IMMUNITY.getNode())) {
			sender.sendMessage(Translation.of("msg_command_disabled"));
			return;
		}
		if (args.length == 3 && args[0].equalsIgnoreCase("town")) {
			//1 town
			Town town = TownyUniverse.getInstance().getTown(args[1]);
			long durationMillis = (long)(Long.parseLong(args[2]) * TimeMgmt.ONE_HOUR_IN_MILLIS);
			TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			TownyMessaging.sendGlobalMessage(Translation.of("msg_set_siege_immunities_town", args[1], args[2]));

		} else if (args.length == 6
			&& args[0].equalsIgnoreCase("all")
			&& args[1].equalsIgnoreCase("towns")
			&& args[2].equalsIgnoreCase("in")
			&& args[3].equalsIgnoreCase("nation")) {
			//All towns in nation
			Nation nation = TownyUniverse.getInstance().getNation(args[4]);
			long durationMillis = (long)(Long.parseLong(args[5]) * TimeMgmt.ONE_HOUR_IN_MILLIS);
			for(Town town: nation.getTowns()) {
				TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			}
			TownyMessaging.sendGlobalMessage(Translation.of("msg_set_siege_immunities_nation", args[4], args[5]));

		} else if(args.length == 3
			&& args[0].equalsIgnoreCase("all")
			&& args[1].equalsIgnoreCase("towns")) {
			//All towns 
			long durationMillis = (long)(Long.parseLong(args[2]) * TimeMgmt.ONE_HOUR_IN_MILLIS);
			for(Town town: new ArrayList<>(TownyUniverse.getInstance().getTowns()))  {
				TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + durationMillis);
			}
			TownyMessaging.sendGlobalMessage(Translation.of("msg_set_siege_immunities_all", args[2]));
		} else {

			sender.sendMessage(ChatTools.formatTitle("/swa immunity"));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/swa immunity", "town [town_name] [hours]", ""));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/swa immunity", "all towns in nation [nation_name] [hours]", ""));
			sender.sendMessage(ChatTools.formatCommand("Eg", "/swa immunity", "all towns [hours]", ""));
		}	}


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

