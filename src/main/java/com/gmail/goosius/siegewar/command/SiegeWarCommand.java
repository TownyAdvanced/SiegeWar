package com.gmail.goosius.siegewar.command;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.BookUtil;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SiegeWarCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewarTabCompletes = Arrays.asList("collect", "nation", "hud", "guide", "preference", "version");
	
	private static final List<String> siegewarNationTabCompletes = Arrays.asList("paysoldiers", "release");

	private static final List<String> siegewarPreferenceTabCompletes = Arrays.asList("beacons");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
			case "nation":
				if (args.length == 2)
					return NameUtil.filterByStart(siegewarNationTabCompletes, args[1]);
				if (args.length == 3 && args[1].equalsIgnoreCase("release")) {
					return NameUtil.filterByStart(new ArrayList<>(TownOccupationController.getAllOccupiedTownNames()), args[2]);
				}
				break;
			case "hud":
				if (args.length == 2)
					return NameUtil.filterByStart(new ArrayList<>(SiegeController.getSiegedTownNames()), args[1]);
				break;
			case "preference":
				if (args.length == 2)
					return NameUtil.filterByStart(siegewarPreferenceTabCompletes, args[1]);
				if (args.length == 3)
					return NameUtil.filterByStart(Arrays.asList("on", "off"), args[2]);
				break;
		}

		if (args.length == 1)
			return NameUtil.filterByStart(siegewarTabCompletes, args[0]);
		else
			return Collections.emptyList();
	}

	private void showSiegeWarHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/siegewar"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw hud", "[town]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw guide", "", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw collect", "", Translation.of("nation_help_11")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "paysoldiers [amount]", Translation.of("nation_help_12")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "release [town]", Translation.of("nation_help_13")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw preference", "beacons [on/off]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw version", "", ""));
	}

	private void showNationHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/siegewar nation"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "paysoldiers [amount]", Translation.of("nation_help_12")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "release [town]", Translation.of("nation_help_13")));
	}

	private void showPreferenceHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/siegewar preference"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw", "preference beacons [on/off]", ""));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player && args.length > 0)
			parseSiegeWarCommand((Player) sender, args);
		else 
			showSiegeWarHelp(sender);

		return true;
	}

	private void parseSiegeWarCommand(Player player, String[] args) {

		//This permission check handles all the perms checks except for nation
		if(!args[0].equalsIgnoreCase("nation")) {
			if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR.getNode(args[0]))) {
				Messaging.sendErrorMsg(player, Translation.of("msg_err_command_disable"));
				return;
			}
		}

		switch (args[0]) {
		case "collect":
			parseSiegeWarCollectCommand(player);
			break;
		case "focus":
		case "hud":
			parseSiegeWarHudCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "guide":
			parseSiegeWarGuideCommand(player);
			break;
		case "nation":
			parseSiegeWarNationCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "preference":
			parseSiegewarPreferenceCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "v":
		case "version":
			parseSiegewarVersionCommand(player);
			break;
		default:
			showSiegeWarHelp(player);
		}
	}

	private void parseSiegeWarGuideCommand(Player player) {
		BookUtil.buildBook(player);
		
	}

	private void parseSiegeWarCollectCommand(Player player) {
		if(!TownyEconomyHandler.isActive())
			return;

		int incomeTypesCollected = 0;
		boolean error = false;
		try {
			if(SiegeWarMoneyUtil.collectNationRefund(player))
				incomeTypesCollected++;
		} catch (Exception e) {
			error = true;
			player.sendMessage(e.getMessage());
		}

		try {
			if(SiegeWarMoneyUtil.collectPlunder(player))
				incomeTypesCollected++;
		} catch (Exception e) {
			error = true;
			player.sendMessage(e.getMessage());
		}

		try {
			if(SiegeWarMoneyUtil.collectMilitarySalary(player))
				incomeTypesCollected++;
		} catch (Exception e) {
			error = true;
			player.sendMessage(e.getMessage());
		}

		if(!error && incomeTypesCollected == 0)	{
			Messaging.sendErrorMsg(player, Translation.of("msg_err_siege_war_collect_unavailable"));
		}
	}

	private void parseSiegeWarHudCommand(Player player, String[] args) {
		try {
			if (args.length == 0) {
				player.sendMessage(ChatTools.formatTitle("/siegewar hud"));
				player.sendMessage(ChatTools.formatCommand("Eg", "/sw hud", "[town]", ""));
			} else {
				Town town = TownyUniverse.getInstance().getTown(args[0]);
				if (town == null) 
					throw new Exception(Translation.of("msg_err_town_not_registered", args[0]));

				if (!SiegeController.getSiegedTowns().contains(town))
					throw new Exception(Translation.of("msg_err_not_being_sieged", town.getName()));

				SiegeWar.getSiegeHUDManager().toggleWarHud(player, SiegeController.getSiege(town));
			}
		} catch (Exception e) {
			Messaging.sendErrorMsg(player, e.getMessage());
		}
	}

	private void parseSiegeWarNationCommand(Player player, String[] args) {
		if (args.length < 2) {
			showNationHelp(player);
			return;
		}

		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_NATION.getNode(args[0]))) {
			player.sendMessage(Translation.of("msg_err_command_disable"));
			return;
		}
			
		switch (args[0]) {
			case "paysoldiers":
				try {
					if(!TownyEconomyHandler.isActive())
						return;

					if (!SiegeWarSettings.getWarSiegeMilitarySalaryEnabled()) {
						player.sendMessage(Translation.of("msg_err_command_disable"));
						return;
					}

					//Ensure resident has a town & nation
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown() || !resident.getTown().hasNation())
						throw new TownyException(Translation.of("msg_err_command_disable"));

					Town town = TownyAPI.getInstance().getResidentTownOrNull(resident);
					Nation nation = TownyAPI.getInstance().getTownNationOrNull(town);

					//Get the integer amount
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						showNationHelp(player);
						return;
					}

					if(amount < 1) {
						showNationHelp(player);
						return;
					}

					if(!nation.getAccount().canPayFromHoldings(amount))
						throw new TownyException(Translation.of("msg_err_siege_war_not_enough_nation_funds_to_pay"));

					//Deduct from nation bank
					nation.getAccount().withdraw(amount, "Military Salaries");

					//Pay soldiers
					int soldierShare;
					Map<Resident, Integer> soldierShareMap = new HashMap<>();
					for(Resident possibleSoldier: nation.getResidents()) {
						for (String perm : TownyPerms.getResidentPerms(possibleSoldier).keySet()) {
							if (perm.startsWith("towny.nation.siege.pay.grade.")) {
								soldierShare = Integer.parseInt(perm.replace("towny.nation.siege.pay.grade.", ""));
								soldierShareMap.put(resident, soldierShare);
								break; //Next resident please
							}
						}
					}
					boolean soldiersPaid =
							SiegeWarMoneyUtil.distributeMoneyAmongSoldiers(
							amount,
							null,
							soldierShareMap,
							"Military Salary",
							false);

					if(soldiersPaid)
						TownyMessaging.sendPrefixedNationMessage(nation, String.format(Translation.of("msg_siege_war_soldiers_paid"), TownyEconomyHandler.getFormattedBalance(amount)));
					else
						throw new TownyException(Translation.of("msg_err_siege_war_no_soldiers_to_pay"));

				} catch (TownyException te) {
					Messaging.sendErrorMsg(player, te.getMessage());
				} catch (EconomyException ee) {
					Messaging.sendErrorMsg(player, ee.getMessage());
				}
				break;

			case "release":
				try {
					String townName = args[1];

					//Check for permission
					if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_NATION_RELEASE.getNode()))
						throw new TownyException(Translation.of("msg_err_action_disable"));

					//Ensure resident has a town & nation
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown() || !resident.getTown().hasNation())
						throw new TownyException(Translation.of("msg_err_command_disable"));

					Town residentsTown = TownyAPI.getInstance().getResidentTownOrNull(resident);
					Nation residentsNation = TownyAPI.getInstance().getTownNationOrNull(residentsTown);

					//Ensure the specified town exists
					if (!TownyUniverse.getInstance().hasTown(townName))
						throw new TownyException(Translation.of("msg_err_unknown_town"));

					//Ensure the specified town is occupied by the resident's nation
					Town townToRelease = TownyUniverse.getInstance().getTown(townName);
					if(!TownOccupationController.isTownOccupied(townToRelease))
						throw new TownyException(Translation.of("msg_err_cannot_release_town_not_occupied_by_nation"));
					if(TownOccupationController.getTownOccupier(townToRelease) != residentsNation)
						throw new TownyException(Translation.of("msg_err_cannot_release_town_not_occupied_by_nation"));

					//Ensure besieged towns cannot be released
					if(SiegeController.hasActiveSiege(townToRelease))
						throw new TownyException(Translation.of("msg_err_cannot_release_besieged_town"));

					//Release town
					TownOccupationController.removeTownOccupation(townToRelease);

					//Send messages
					TownyMessaging.sendPrefixedTownMessage(townToRelease, String.format(Translation.of("msg_town_released_from_occupation"), residentsNation));
					TownyMessaging.sendPrefixedNationMessage(residentsNation, String.format(Translation.of("msg_foreign_town_released_from_occupation"), townToRelease.getName()));
				} catch (Exception e) {
					Messaging.sendErrorMsg(player, e.getMessage());
				}
				break;

			default:
				showNationHelp(player);
		}
	}

	private void parseSiegewarPreferenceCommand(Player player, String[] args) {
		if (args.length >= 2) {
			Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
			switch(args[0].toLowerCase()) {
				case "beacons": {
					if (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
						Messaging.sendMsg(player, Translation.of("msg_err_invalid_bool"));
						return;
					}
					boolean current = ResidentMetaDataController.getBeaconsDisabled(resident);
					boolean disabled = args[1].equalsIgnoreCase("off");
					ResidentMetaDataController.setBeaconsDisabled(resident, disabled);
					if (current != disabled)
						CosmeticUtil.removeFakeBeacons(player);
					Messaging.sendMsg(player, Translation.of("msg_beacon_preference_set", args[1].toUpperCase()));
					break;
				}
				default:
					showPreferenceHelp(player);
			}
		} else
			showPreferenceHelp(player);
	}

	private void parseSiegewarVersionCommand(Player player) {
		Messaging.sendMsg(player, Translation.of("msg_siege_war_version", SiegeWar.getSiegeWar().getVersion()));
		return;
	}
}