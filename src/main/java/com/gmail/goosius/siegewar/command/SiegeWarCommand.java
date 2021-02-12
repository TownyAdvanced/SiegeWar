package com.gmail.goosius.siegewar.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.BookUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;

public class SiegeWarCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewarTabCompletes = Arrays.asList("nation", "hud", "guide");
	
	private static final List<String> siegewarNationTabCompletes = Arrays.asList("refund");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
		case "nation":
			if (args.length > 1)
				return NameUtil.filterByStart(siegewarNationTabCompletes, args[1]);
		case "hud":
			if (args.length > 1)
				return NameUtil.filterByStart(new ArrayList<String>(SiegeController.getSiegedTownNames()), args[1]);
		default:
			return NameUtil.filterByStart(siegewarTabCompletes, args[0]);
		}
	}

	private void showSiegeWarHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/siegewar"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw collect", "refund", Translation.of("nation_help_11")));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw hud", "[town]", ""));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw guide", "", ""));
	}
	
	private void showNationHelp(CommandSender sender) {
		sender.sendMessage(ChatTools.formatTitle("/siegewar nation"));
		sender.sendMessage(ChatTools.formatCommand("Eg", "/sw nation", "refund", Translation.of("nation_help_11")));			
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player && args.length > 0)
			parseSiegeWarCommand((Player) sender, args);
		else 
			showSiegeWarHelp(sender);

		return true;
	}

	private void parseSiegeWarCommand(Player player, String[] args) {
		
		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR.getNode(args[0]))) {
			Messaging.sendErrorMsg(player, Translation.of("msg_err_command_disable"));
			return;
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

		default:
			showSiegeWarHelp(player);
		}
	}

	private void parseSiegeWarGuideCommand(Player player) {
		BookUtil.buildBook(player);
		
	}

	private void parseSiegeWarCollectCommand(Player player) {
		boolean moneyCollected = false;
		try {
			moneyCollected = SiegeWarMoneyUtil.collectNationRefund(player);
		} catch (Exception e) {
			player.sendMessage(e.getMessage());
		}

		try {
			moneyCollected = SiegeWarMoneyUtil.collectPlunder(player);
		} catch (Exception e) {
			player.sendMessage(e.getMessage());
		}

		try {
			moneyCollected = SiegeWarMoneyUtil.collectMilitarySalary(player);
		} catch (Exception e) {
			player.sendMessage(e.getMessage());
		}

		if(!moneyCollected)	{
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
		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_NATION.getNode(args[0]))) {
			player.sendMessage(Translation.of("msg_err_command_disable"));
			return;
		}

		if (args.length < 2) {
			showNationHelp(player);
			return;
		}

		switch (args[0]) {
			case "paysoldiers":
				try {
					if (!SiegeWarSettings.getWarSiegeMilitarySalaryEnabled()) {
						player.sendMessage(Translation.of("msg_err_command_disable"));
						return;
					}

					if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_NATION_PAYSOLDIERS.getNode())) {
						player.sendMessage(Translation.of("msg_err_command_disable"));
						return;
					}

					//Ensure resident has a town & nation
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown() || !resident.getTown().hasNation())
						throw new TownyException(Translation.of("msg_err_command_disable"));

					Town town;
					Nation nation = null;
					try {
						town = resident.getTown();
						nation = town.getNation();
					} catch (NotRegisteredException ignored) {
					}

					//Get the integer amount
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						showNationHelp(player);
						return;
					}

					//Pay soldiers
					SiegeWarMoneyUtil.distributeMoneyAmongSoldiers(
							amount,
							null,
							nation,
							"Military Salary",
							false);

				} catch (TownyException te) {
					Messaging.sendErrorMsg(player, te.getMessage());
				} catch (EconomyException ee) {
					Messaging.sendErrorMsg(player, ee.getMessage());
				}
				break;
			default:
				showNationHelp(player);
		}
	}
}