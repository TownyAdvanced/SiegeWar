package com.gmail.goosius.siegewar.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

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

public class SiegeWarNationSetOccupationTaxAddonCommand extends BaseCommand implements TabExecutor {

	public SiegeWarNationSetOccupationTaxAddonCommand() {
		AddonCommand nationSetSiegeWarCommand = new AddonCommand(CommandType.NATION_SET, "occupationtax", this);
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

		int maxTaxPerPlot = SiegeWarSettings.getMaxOccupationTaxPerPlot();
		int taxPerPlot;
		if (args[0].equalsIgnoreCase("max")) {
			NationMetaDataController.setNationOccupationTaxPerPlot(nation, -1);
			taxPerPlot = maxTaxPerPlot;
			TownyMessaging.sendMsg(player, Translatable.of("msg_occupation_tax_set_max", getMoney(taxPerPlot)));
		} else {
			taxPerPlot = MathUtil.getPositiveIntOrThrow(args[0]);
			if (taxPerPlot > maxTaxPerPlot)
				Messaging.sendMsg(player, Translatable.of("msg_err_occupation_tax_cannot_be_more_than", maxTaxPerPlot));
			taxPerPlot = Math.min(maxTaxPerPlot, taxPerPlot);
			NationMetaDataController.setNationOccupationTaxPerPlot(nation, taxPerPlot);
			TownyMessaging.sendMsg(player, Translatable.of("msg_occupation_tax_set", getMoney(taxPerPlot)));
		}
	}

	private String getMoney(int tax) {
		return TownyEconomyHandler.isActive() ? TownyEconomyHandler.getFormattedBalance(tax) : String.valueOf(tax);
	}

	private void showHelp() {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/nation set occupationtax"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("/nation set occupationtax", "[amount]", "Set the amount to 'max' to automatically track the server-configured maximum."));
	}

}

