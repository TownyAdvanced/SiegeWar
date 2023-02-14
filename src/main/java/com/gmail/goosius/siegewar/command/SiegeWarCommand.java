package com.gmail.goosius.siegewar.command;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.BookUtil;
import com.gmail.goosius.siegewar.utils.BossBarUtil;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SiegeWarCommand implements CommandExecutor, TabCompleter {
	
	private static final List<String> siegewarTabCompletes = Arrays.asList("collect", "town", "nation", "hud", "guide", "preference", "version", "nextsession");
	
	private static final List<String> siegewarNationTabCompletes = Arrays.asList("paysoldiers", "removeoccupation", "transferoccupation");

	private static final List<String> siegewarTownTabCompletes = Arrays.asList("inviteoccupation");

	private static final List<String> siegewarPreferenceTabCompletes = Arrays.asList("beacons", "bossbars");
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
			case "nation":
				if (args.length == 2)
					return NameUtil.filterByStart(siegewarNationTabCompletes, args[1]);
				if (args.length == 3 && args[1].equalsIgnoreCase("removeoccupation")) {
					return NameUtil.filterByStart(new ArrayList<>(TownOccupationController.getAllOccupiedTownNames()), args[2]);
				}
				if (args.length == 3 && args[1].equalsIgnoreCase("transferoccupation")) {
					return NameUtil.filterByStart(new ArrayList<>(TownOccupationController.getAllOccupiedTownNames()), args[2]);
				}
				if (args.length == 4 && args[1].equalsIgnoreCase("transferoccupation")) {
					return SiegeWarAdminCommand.getTownyStartingWith(args[3], "n");
				}
				break;
			case "town":
				if (args.length == 2)
					return NameUtil.filterByStart(siegewarTownTabCompletes, args[1]);
				if (args.length == 3 && args[1].equalsIgnoreCase("inviteoccupation")) {
					return SiegeWarAdminCommand.getTownyStartingWith(args[2], "n");
				}
				break;					
			case "hud":
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
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw guide", "", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw collect", "", Translatable.of("nation_help_11").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw town", "inviteoccupation [nation]", Translatable.of("nation_help_16").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "paysoldiers [amount]", Translatable.of("nation_help_12").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "removeoccupation [town]", Translatable.of("nation_help_14").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "transferoccupation [town] [nation]", Translatable.of("nation_help_15").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw preference", "beacons [on/off]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nextsession", "", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw version", "", ""));
	}

	private void showNationHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar nation"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "paysoldiers [amount]", Translatable.of("nation_help_12").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "removeoccupation [town]", Translatable.of("nation_help_14").forLocale(sender)));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw nation", "transferoccupation [town] [nation]", Translatable.of("nation_help_15").forLocale(sender)));
	}

	private void showTownHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/siegewar town"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/sw town", "inviteoccupation [nation]", Translatable.of("nation_help_16").forLocale(sender)));
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
		case "guide":
			parseSiegeWarGuideCommand(player);
			break;
		case "nation":
			parseSiegeWarNationCommand(player, StringMgmt.remFirstArg(args));
			break;
		case "nextsession":
			parseSiegeWarNextSessionCommand(player);
			break;
		case "town":
			parseSiegeWarTownCommand(player, StringMgmt.remFirstArg(args));
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

	private void parseSiegeWarGuideCommand(Player player) {
		BookUtil.buildBook(player);
		
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

		if(!error && incomeTypesCollected == 0)	{
			Messaging.sendErrorMsg(player, Translatable.of("msg_err_siege_war_collect_unavailable"));
		}
	}

	private void parseSiegeWarHudCommand(Player player, String[] args) {
		try {
			if (args.length == 0) {
				TownyMessaging.sendMessage(player, ChatTools.formatTitle("/siegewar hud"));
				TownyMessaging.sendMessage(player, ChatTools.formatCommand("Eg", "/sw hud", "[town]", ""));
			} else {
				Town town = TownyUniverse.getInstance().getTown(args[0]);
				if (town == null) 
					throw new Exception(Translatable.of("msg_err_town_not_registered", args[0]).forLocale(player));

				if (!SiegeController.getSiegedTowns().contains(town))
					throw new Exception(Translatable.of("msg_err_not_being_sieged", town.getName()).forLocale(player));

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
			player.sendMessage(Translatable.of("msg_err_command_disable").forLocale(player));
			return;
		}

		switch (args[0]) {
			case "paysoldiers":
				try {
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
					Messaging.sendErrorMsg(player, te.getMessage());
				}
				break;

			case "removeoccupation":
				try {
					String townName = args[1];

					//Ensure resident has a town & nation
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown() || !resident.getTown().hasNation())
						throw new TownyException(Translatable.of("msg_err_command_disable").forLocale(player));

					Town residentsTown = TownyAPI.getInstance().getResidentTownOrNull(resident);
					Nation residentsNation = TownyAPI.getInstance().getTownNationOrNull(residentsTown);

					//Ensure the specified town exists
					if (!TownyUniverse.getInstance().hasTown(townName))
						throw new TownyException(Translatable.of("msg_err_unknown_town").forLocale(player));

					//Ensure the specified town is occupied by the resident's nation
					Town townToRelease = TownyUniverse.getInstance().getTown(townName);
					if(!TownOccupationController.isTownOccupied(townToRelease))
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_town_not_occupied").forLocale(player));
					if(TownOccupationController.getTownOccupier(townToRelease) != residentsNation)
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_by_foreign_nation").forLocale(player));

					//Ensure besieged towns cannot be de-occupied
					if(SiegeController.hasActiveSiege(townToRelease))
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_of_besieged_town").forLocale(player));

					//Remove occupation
					TownOccupationController.removeTownOccupation(townToRelease);

					//Send global message
					TownyMessaging.sendGlobalMessage(Translatable.of("msg_remove_occupation_success", residentsNation.getName(), townToRelease.getName()).forLocale(player));
				} catch (Exception e) {
					Messaging.sendErrorMsg(player, e.getMessage());
				}
				break;

			case "transferoccupation":
				if (args.length < 3) {
					showNationHelp(player);
					return;
				}

				try {
					String townName = args[1];
					String nationName = args[2];

					//Ensure resident has a town & nation
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown() || !resident.getTown().hasNation())
						throw new TownyException(Translatable.of("msg_err_command_disable").forLocale(player));

					Town residentsTown = TownyAPI.getInstance().getResidentTownOrNull(resident);
					Nation residentsNation = TownyAPI.getInstance().getTownNationOrNull(residentsTown);

					//Ensure the specified town exists
					if (!TownyUniverse.getInstance().hasTown(townName))
						throw new TownyException(Translatable.of("msg_err_unknown_town").forLocale(player));

					//Ensure the specified town is occupied by the resident's nation
					Town townToTransfer = TownyUniverse.getInstance().getTown(townName);
					if(!TownOccupationController.isTownOccupied(townToTransfer))
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_town_not_occupied").forLocale(player));
					if(TownOccupationController.getTownOccupier(townToTransfer) != residentsNation)
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_by_foreign_nation").forLocale(player));

					//Ensure besieged towns cannot be de-occupied
					if(SiegeController.hasActiveSiege(townToTransfer))
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_of_besieged_town").forLocale(player));

					//Ensure the receiving nation exists
					if (!TownyUniverse.getInstance().hasNation(nationName))
						throw new TownyException(Translatable.of("msg_err_unknown_nation").forLocale(player));

					//Check distance
					Nation receivingNation = TownyUniverse.getInstance().getNation(nationName);
					if (TownySettings.getNationRequiresProximity() > 0) {
						if (!receivingNation.getCapital().getHomeBlock().getWorld().getName().equals(townToTransfer.getHomeBlock().getWorld().getName())) {
							throw new TownyException(Translatable.of("msg_err_town_and_capital_too_far_apart", townToTransfer.getName(), receivingNation.getName()).forLocale(player));
						}
						Coord capitalCoord = receivingNation.getCapital().getHomeBlock().getCoord();
						Coord townCoord = townToTransfer.getHomeBlock().getCoord();
						double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
						if (distance > TownySettings.getNationRequiresProximity()) {
							throw new TownyException(Translatable.of("msg_err_town_and_capital_too_far_apart", townToTransfer.getName(), receivingNation.getName()).forLocale(player));
						}						
					}

					//Ensure the king of the receiving nation is online
					Resident kingOfReceivingNation = receivingNation.getKing();
					if (!kingOfReceivingNation.isOnline()){
						throw new TownyException(Translatable.of("msg_err_cannot_transfer_occupation_king_not_online", receivingNation.getName(), kingOfReceivingNation.getName()).forLocale(player));
					}

					//Send request to king of receiving nation					
					TownyMessaging.sendMessage(kingOfReceivingNation, Translatable.of("msg_would_you_accept_transfer_of_occupied_town", townName, residentsNation.getName()).forLocale(player));
					Confirmation.runOnAccept(() -> {
						//Transfer occupation
						TownOccupationController.setTownOccupation(townToTransfer, receivingNation);					
						//Send global message
						TownyMessaging.sendGlobalMessage(Translatable.of("msg_transfer_occupation_success", residentsNation.getName(), townToTransfer.getName(), receivingNation.getName()));
					})	
					.sendTo(kingOfReceivingNation.getPlayer());

				} catch (Exception e) {
					Messaging.sendErrorMsg(player, e.getMessage());
				}
				break;
				
			default:
				showNationHelp(player);
		}
	}

	private void parseSiegeWarTownCommand(Player player, String[] args) {
		if (args.length < 2) {
			showTownHelp(player);
			return;
		}

		if (!player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWAR_TOWN.getNode(args[0]))) {
			player.sendMessage(Translatable.of("msg_err_command_disable").forLocale(player));
			return;
		}

		switch (args[0]) {

			case "inviteoccupation":
				try {
					//Ensure resident has town
					Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
					if (resident == null || !resident.hasTown())
						throw new TownyException(Translatable.of("msg_err_command_disable").forLocale(player));

					//Ensure the specified town is not peaceful
					Town townToTransfer = TownyAPI.getInstance().getResidentTownOrNull(resident);
					if (townToTransfer.isNeutral())
						throw new TownyException(Translatable.of("msg_err_cannot_invite_peaceful_town_occupation").forLocale(player));

					//Ensure the town is unoccupied
					if(TownOccupationController.isTownOccupied(townToTransfer))
						throw new TownyException(Translatable.of("msg_err_cannot_invite_occupation_town_already_occupied").forLocale(player));

					//Ensure town is not besieged
					if(SiegeController.hasActiveSiege(townToTransfer))
						throw new TownyException(Translatable.of("msg_err_cannot_change_occupation_of_besieged_town").forLocale(player));

					//Ensure the receiving nation exists
					String nameOfReceivingNation = args[1];
					if (!TownyUniverse.getInstance().hasNation(nameOfReceivingNation))
						throw new TownyException(Translatable.of("msg_err_unknown_nation").forLocale(player));

					//Check distance
					Nation receivingNation = TownyUniverse.getInstance().getNation(nameOfReceivingNation);
					if (TownySettings.getNationRequiresProximity() > 0) {
						Coord capitalCoord = receivingNation.getCapital().getHomeBlock().getCoord();
						Coord townCoord = townToTransfer.getHomeBlock().getCoord();
						if (!receivingNation.getCapital().getHomeBlock().getWorld().getName().equals(townToTransfer.getHomeBlock().getWorld().getName())) {
							throw new TownyException(Translatable.of("msg_err_town_and_capital_too_far_apart", townToTransfer.getName(), receivingNation.getName()).forLocale(player));
						}
						double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
						if (distance > TownySettings.getNationRequiresProximity()) {
							throw new TownyException(Translatable.of("msg_err_town_and_capital_too_far_apart", townToTransfer.getName(), receivingNation.getName()).forLocale(player));
						}						
					}

					//Ensure the king of the receiving nation is online
					Resident kingOfReceivingNation = receivingNation.getKing();
					if (!kingOfReceivingNation.isOnline()) {
						throw new TownyException(Translatable.of("msg_err_cannot_invite_occupation_king_not_online", receivingNation.getName(), kingOfReceivingNation.getName()).forLocale(player));
					}

					//Send request to king of receiving nation
					TownyMessaging.sendMessage(kingOfReceivingNation, Translatable.of("msg_would_you_accept_town_request_for_occupation", townToTransfer).forLocale(player));
					Confirmation.runOnAccept(() -> {
						//Occupy town
						TownOccupationController.setTownOccupation(townToTransfer, receivingNation);
						//Send global message
						TownyMessaging.sendGlobalMessage(Translatable.of("msg_invite_occupation_success", townToTransfer.getName(), receivingNation.getName()).forLocale(player));
					})
					.sendTo(kingOfReceivingNation.getPlayer());

				} catch (Exception e) {
					Messaging.sendErrorMsg(player, e.getMessage());
				}
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