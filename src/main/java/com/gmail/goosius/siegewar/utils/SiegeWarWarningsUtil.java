package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SiegeWarWarningsUtil {
    
    /**
     * Send bad configs warnings
     *
     * @param sender the CommandSender to send the message to
     */
    public static void sendWarningsIfConfigsBad(CommandSender sender) {
        if(sender instanceof Player && !sender.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN_BADCONFIGWARNINGS.getNode())) {
            return;
        }
        sendWarningIfPlunderConfigBad(sender);
    }

    private static void sendWarningIfPlunderConfigBad(CommandSender sender) {
        double actualConfiguredValue = SiegeWarSettings.getWarSiegePlunderAmountPerPlot();
        double idealConfiguredValue = calculateIdealPlunderValue();
        double toleranceAmount = idealConfiguredValue * SiegeWarSettings.getBadConfigWarningsTolerancePercentage() / 100;
        double lowerBound = idealConfiguredValue - toleranceAmount;
        double upperBound = idealConfiguredValue + toleranceAmount;
        if(actualConfiguredValue > upperBound) {
            Messaging.sendErrorMsg(sender, Translatable.of("msg_err_plunder_configured_too_high", "" + idealConfiguredValue , "" + actualConfiguredValue));
        } else if (actualConfiguredValue < lowerBound) {
            Messaging.sendErrorMsg(sender, Translatable.of("msg_err_plunder_configured_too_low", "" + idealConfiguredValue, "" + actualConfiguredValue));
        }
    }

    public static double calculateIdealPlunderValue() {
        double allMoney = SiegeWarMoneyUtil.getEstimatedTotalMoneyInEconomy();
        double numTownBlocks = TownyAPI.getInstance().getTownBlocks().size();
        double valuePerTownBlock = allMoney / numTownBlocks;
        double idealPlunderValue = valuePerTownBlock * SiegeWarSettings.getBadConfigWarningsIdealPlunderPercentage() / 100;
        return idealPlunderValue;
    }
}

