package com.gmail.goosius.siegewar.command;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.BossBarUtil;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSpawnUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SiegeWarCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewarTabCompletes = Arrays.asList("collect", "town", "nation", "hud", "preference", "version", "nextsession", "spawn");

	private static final List<String> siegewarTownTabCompletes = Arrays.asList("togglepeaceful");
	
	private static final List<String> siegewarNationTabCompletes = Arrays.asList("paysoldiers");

	private static final List<String> siegewarPreferenceTabCompletes = Arrays.asList("beacons", "bossbars");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
			case "nation":
				if (args.length == 2)
					return NameUtil.filterByStart(siegewarNationTabCompletes, args[1]);
				break;
			case "town":
				if (args.length == 2)
					return NameUtil.filterByStart(siegewarTownTabCompletes, args[1]);
				break;
			case "hud":
			case "spawn":
				if (args.length == 2)
					return NameUtil.filterByStart(new ArrayList<>(SiegeController.getNamesOfActivelySiegedTowns()), args[1]);
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
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw hud", "[town]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw spawn", "[town]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw collect", "", Translatable.of("nation_help_11").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "paysoldiers [amount]", Translatable.of("nation_help_12").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw town", "togglepeaceful", Translatable.of("town_help_toggle_peaceful").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nextsession", "", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw version", "", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw preference", "beacons [on/off]", ""));
	}

	private void showSpawnHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar spawn"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw spawn", "[town]", ""));
	}

	private void showNationHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar nation"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "paysoldiers [amount]", Translatable.of("nation_help_12").forLocale(sender)));
	}

	private void showTownHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar town"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw town", "togglepeaceful", Translatable.of("town_help_toggle_peaceful").forLocale(sender)));
	}

	private void showPreferenceHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar preference"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw", "preference beacons [on/off]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw", "preference bossbars [on/off]", ""));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player && args.length > 0)
			parseSiegeWarCommand((Player) sender, args);
		else 
			showSiegeWarHelp(sender);

		return true;
	}

	private void parseSiegeWarCommand(Player player, String[] args) {

		//This permission check handles all the perms checks except for nation & town
		if(!args[0].equalsIgnoreCase("nation") && !args[0].equalsIgnoreCase("town")) {
			if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR.getNode(args[0]))) {
				Messaging.sendErrorMsg(player, Translatable.of("msg_err_command_disable"));
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
		case "spawn":
			parseSiegeWarSpawnCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "town":
			parseSiegeWarTownCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "nation":
			parseSiegeWarNationCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "nextsession":
			parseSiegeWarNextSessionCommand(player);
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

	private void parseSiegeWarNextSessionCommand(Player player) {
		BattleSession session = BattleSession.getBattleSession();
		if (session.isActive())
			Messaging.sendMsg(player, Translatable.of("msg_session_is_active_now"));
		else {
			Translatable message = Translatable.of("msg_next_session_cannot_be_determined");
			if (session.getScheduledStartTime() != null) {
				long timeRemaining = session.getScheduledStartTime() - System.currentTimeMillis(); 
				message = Translatable.of("msg_next_siege_session_in_minutes", TimeMgmt.getFormattedTimeValue(timeRemaining));
			}
			Messaging.sendMsg(player, message);
		}
		
	}

	private void parseSiegeWarCollectCommand(Player player) {
		if(!TownyEconomyHandler.isActive())
			return;

		int incomeTypesCollected = 0;
		boolean error = false;

		try {
			if(SiegeWarMoneyUtil.collectMilitarySalary(player))
				incomeTypesCollected++;
		} catch (Exception e) {
			error = true;
			player.sendMessage(e.getMessage());
		}

		try {
			if(SiegeWarMoneyUtil.claimNationRefund(player))
				incomeTypesCollected++;
		} catch (Exception e) {
			error = true;
			player.sendMessage(e.getMessage());
		}

		if(!error && incomeTypesCollected == 0)	{
			Messaging.sendErrorMsg(player, Translatable.of("msg_err_siege_war_collect_unavailable"));
		}
	}

	private void parseSiegeWarHudCommand(Player player, String[] args) {
		try {
			if(SiegeWar.isFoliaClassPresent()) {
				Town town = TownyUniverse.getInstance().getTown(args[0]);
				if (town == null)
					throw new TownyException(Translatable.of("msg_err_town_not_registered", args[0]));

				if (!SiegeController.getSiegedTowns().contains(town))
					throw new TownyException(Translatable.of("msg_err_not_being_sieged", town.getName()));
				Siege siege = SiegeController.getSiege(town);
				final Translator translator = Translator.locale(player);
				player.sendMessage(ChatColor.GOLD + "§l" + SiegeHUDManager.checkLength(siege.getTown().getName()) + " " + translator.of("hud_title"));
				player.sendMessage(ChatColor.GOLD + "- Siege Type" + ": " + ChatColor.WHITE + SiegeHUDManager.checkLength(siege.getSiegeType().getTranslatedName().forLocale(player)));
				player.sendMessage(ChatColor.GOLD + "- Attackers" + ": " + ChatColor.WHITE + SiegeHUDManager.checkLength(siege.getAttackerNameForDisplay()));
				player.sendMessage(ChatColor.GOLD + "- Defenders" + ": " + ChatColor.WHITE + SiegeHUDManager.checkLength(siege.getDefenderNameForDisplay()));
				player.sendMessage(ChatColor.GOLD + "- Balance" + ": " + ChatColor.WHITE + siege.getSiegeBalance().toString());
				player.sendMessage(ChatColor.GOLD + "- Progress" + ": " + ChatColor.WHITE + siege.getNumBattleSessionsCompleted() + "/" + SiegeWarSettings.getSiegeDurationBattleSessions());
				player.sendMessage(ChatColor.GOLD + "- Status" + ": " + ChatColor.WHITE + siege.getStatus().getName());
				if (TownyEconomyHandler.isActive()) {
					player.sendMessage(ChatColor.GOLD + "- Warchest" + ": " + ChatColor.WHITE + TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));
				} else {
					player.sendMessage(ChatColor.GOLD + "- Warchest" + ": " + ChatColor.WHITE + "-");
				}
				player.sendMessage(ChatColor.GOLD + "- Banner Control" + ": " + ChatColor.WHITE + siege.getBannerControllingSide().getFormattedName().forLocale(player) + (siege.getBannerControllingSide() == SiegeSide.NOBODY ? "" : " (" + siege.getBannerControllingResidents().size() + ")"));
				player.sendMessage(ChatColor.GOLD + "- Attacker Points" + ": " + ChatColor.WHITE + siege.getFormattedAttackerBattlePoints());
				player.sendMessage(ChatColor.GOLD + "- Defender Points" + ": " + ChatColor.WHITE + siege.getFormattedDefenderBattlePoints());
				player.sendMessage(ChatColor.GOLD + "- Time Remaining" + ": " + ChatColor.WHITE + siege.getFormattedBattleTimeRemaining(translator));
				return;
			}
			if (args.length == 0) {
				TownyMessaging.sendMessage(player, ChatTools.formatTitle("/siegewar hud"));
				TownyMessaging.sendMessage(player, ChatTools.formatCommand("Eg", "/sw hud", "[town]", ""));
			} else {
				Town town = TownyUniverse.getInstance().getTown(args[0]);
				if (town == null) 
					throw new TownyException(Translatable.of("msg_err_town_not_registered", args[0]));

				if (!SiegeController.getSiegedTowns().contains(town))
					throw new TownyException(Translatable.of("msg_err_not_being_sieged", town.getName()));

				SiegeWar.getSiegeHUDManager().toggleWarHud(player, SiegeController.getSiege(town));
			}
		} catch (TownyException e) {
			Messaging.sendErrorMsg(player, e.getMessage(player));
		}
	}

	private void parseSiegeWarSpawnCommand(Player player, String[] args) {
		try {
			if (args.length == 0) {
				showSpawnHelp(player);
			} else {
				Town town = TownyUniverse.getInstance().getTown(args[0]);
				if (town == null)
					throw new TownyException(Translatable.of("msg_err_town_not_registered", args[0]));
				SiegeWarSpawnUtil.evaluateSpawnToSiegeRequest(player, town);
			}
		} catch (TownyException e) {
			Messaging.sendErrorMsg(player, e.getMessage(player));
		}
	}
	
	private void parseSiegeWarNationCommand(Player player, String[] args) {
		if (args.length == 0) {
			showNationHelp(player);
			return;
		}

		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_NATION.getNode(args[0]))) {
			player.sendMessage(Translatable.of("msg_err_command_disable").forLocale(player));
			return;
		}

		switch (args[0]) {
			case "paysoldiers":
				try {
					if (args.length != 2) {
						showNationHelp(player);
						return;
					}
					if(!TownyEconomyHandler.isActive())
						return;

					if (!SiegeWarSettings.getWarSiegeMilitarySalaryEnabled()) {
						player.sendMessage(Translatable.of("msg_err_command_disable").forLocale(player));
						return;
					}

					//Ensure resident has a town & nation
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown() || !resident.getTown().hasNation())
						throw new TownyException(Translatable.of("msg_err_command_disable").forLocale(player));

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
						throw new TownyException(Translatable.of("msg_err_siege_war_not_enough_nation_funds_to_pay").forLocale(player));

					//Deduct from nation bank
					nation.getAccount().withdraw(amount, "Military Salaries");

					//Pay soldiers
					int soldierShare;
					Map<Resident, Integer> soldierShareMap = new HashMap<>();
					for(Resident possibleSoldier: nation.getResidents()) {
						for (String perm : TownyPerms.getResidentPerms(possibleSoldier).keySet()) {
							if (perm.startsWith("towny.nation.siege.pay.grade.")) {
								soldierShare = Integer.parseInt(perm.replace("towny.nation.siege.pay.grade.", ""));
								soldierShareMap.put(possibleSoldier, soldierShare);
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
						TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_siege_war_soldiers_paid", TownyEconomyHandler.getFormattedBalance(amount)).forLocale(player));
					else
						throw new TownyException(Translatable.of("msg_err_siege_war_no_soldiers_to_pay").forLocale(player));

				} catch (TownyException te) {
					Messaging.sendErrorMsg(player, te.getMessage(player));
				}
				break;

			default:
				showNationHelp(player);
		}
	}

	private void parseSiegeWarTownCommand(Player player, String[] args) {
		if (args.length == 0) {
			showSiegeWarHelp(player);
			return;
		}

		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_TOWN.getNode(args[0]))) {
			player.sendMessage(Translatable.of("msg_err_command_disable").forLocale(player));
			return;
		}

		switch (args[0]) {

			case "togglepeaceful":
				if (args.length != 1) {
					showTownHelp(player);
					return;
				}
				SiegeWarTownPeacefulnessUtil.toggleTownPeacefulness(player);
				break;

			default:
				showTownHelp(player);
		}
	}

	private void parseSiegewarPreferenceCommand(Player player, String[] args) {
		if (args.length >= 2) {
			if (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
				Messaging.sendMsg(player, Translatable.of("msg_err_invalid_bool"));
				return;
			}
			Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
			switch(args[0].toLowerCase()) {
				case "beacons": {
					boolean current = ResidentMetaDataController.getBeaconsDisabled(resident);
					boolean disabled = args[1].equalsIgnoreCase("off");
					ResidentMetaDataController.setBeaconsDisabled(resident, disabled);
					if (current != disabled)
						CosmeticUtil.removeFakeBeacons(player);
					Messaging.sendMsg(player, Translatable.of("msg_beacon_preference_set", args[1].toUpperCase()));
					break;
				}
				case "bossbars": {
					boolean disabled = args[1].equalsIgnoreCase("off");
					ResidentMetaDataController.setBossBarsDisabled(resident, disabled);
					if (disabled)
						BossBarUtil.removeBossBars(player);
					Messaging.sendMsg(player, Translatable.of("msg_bossbar_preference_set", args[1].toUpperCase()));
					break;
				}
				default:
					showPreferenceHelp(player);
			}
		} else
			showPreferenceHelp(player);
	}

	private void parseSiegewarVersionCommand(Player player) {
		Messaging.sendMsg(player, Translatable.of("msg_siege_war_version", SiegeWar.getSiegeWar().getVersion()));
		return;
	}
}