package com.gmail.goosius.siegewar.command;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.MathUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SiegeWarNationSetPeacefulOccupationTaxAddonCommand extends BaseCommand implements TabExecutor {

	public SiegeWarNationSetPeacefulOccupationTaxAddonCommand() {
		AddonCommand nationSetSiegeWarCommand = new AddonCommand(CommandType.NATION_SET, "peacefuloccupationtax", this);
		TownyCommandAddonAPI.addSubCommand(nationSetSiegeWarCommand);
	}
	
	private CommandSender sender;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.sender = sender;
		try {
			parseNationSetOccupationTaxCommand(args);
		} catch (TownyException e) {
			Messaging.sendErrorMsg(sender, e.getMessage(sender));
		}
		return true;
	}

	private void parseNationSetOccupationTaxCommand(String[] args) throws TownyException {
		if (args.length == 0) {
			showHelp();
			return;
		}

		Player player = catchConsole(sender);
		Nation nation = getNationFromPlayerOrThrow(player);
		int tax = MathUtil.getPositiveIntOrThrow(args[0]);
		
		int maxNationOccupationTax = SiegeWarSettings.maxNationPeacefulOccupationTax();
		if (tax > maxNationOccupationTax)
			Messaging.sendMsg(player, Translatable.of("msg_err_peaceful_occupation_tax_cannot_be_more_than", maxNationOccupationTax));
		tax = Math.min(maxNationOccupationTax, tax);
		NationMetaDataController.setNationPeacefulOccupationTax(nation, tax);
		TownyMessaging.sendMsg(player, Translatable.of("msg_peaceful_occupation_tax_set", getMoney(tax)));
	}

	private String getMoney(int tax) {
		return TownyEconomyHandler.isActive() ? TownyEconomyHandler.getFormattedBalance(tax) : String.valueOf(tax);
	}

	private void showHelp() {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/nation set peacefuloccupationtax"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("/nation set peacefuloccupationtax", "[amount]", ""));
	}

}

