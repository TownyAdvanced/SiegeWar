package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
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
        if(!TownyEconomyHandler.isActive())
            return;
        if(sender instanceof Player && !sender.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_COMMAND_SIEGEWARADMIN_BADCONFIGWARNINGS.getNode()))
            return;
        boolean warningSent = false;
        if(SiegeWarSettings.getWarSiegePlunderAmountPerPlot() > 0) {
            warningSent = warningSent | sendWarningIfPlunderConfigBad(sender);
        }
        if(SiegeWarSettings.getWarSiegeWarchestCostPerPlot() > 0) {
            warningSent = warningSent | sentWarningIfWarChestBad(sender);
        }
        if(SiegeWarSettings.getWarSiegeUpfrontCostPerPlot() > 0) {
            warningSent = warningSent | sendWarningIfUpfrontCostBad(sender);
        }
        if(SiegeWarSettings.getMaxOccupationTaxPerPlot() > 0) {
            warningSent = warningSent | sendWarningIfOccupationTaxBad(sender);
        }        
        if(warningSent) {
            Messaging.sendErrorMsg(sender, Translatable.of("msg_err_note_about_warnings"));
        }
    }

    private static boolean sendWarningIfPlunderConfigBad(CommandSender sender) {
        double idealConfiguredValue = calculateIdealPlunderValue();
        double actualConfiguredValue = SiegeWarSettings.getWarSiegePlunderAmountPerPlot();
        String configIdentifier = "war:siege:money:plunder:amount_per_plot";
        return sendBadWarConfigWarning(sender, idealConfiguredValue, actualConfiguredValue, configIdentifier);
    }

    private static boolean sentWarningIfWarChestBad(CommandSender sender) {
        double idealConfiguredValue = calculateIdealWarChestValue();
        double actualConfiguredValue = SiegeWarSettings.getWarSiegeWarchestCostPerPlot();
        String configIdentifier = "war:siege:money:warchest_cost_per_plot";
        return sendBadWarConfigWarning(sender, idealConfiguredValue, actualConfiguredValue, configIdentifier);
    }

    private static boolean sendWarningIfUpfrontCostBad(CommandSender sender) {
        double idealConfiguredValue = calculateIdealUpfrontCostValue();
        double actualConfiguredValue = SiegeWarSettings.getWarSiegeUpfrontCostPerPlot();
        String configIdentifier = "war:siege:money:upfront_cost_per_plot";
        return sendBadWarConfigWarning(sender, idealConfiguredValue, actualConfiguredValue, configIdentifier);
    }

    private static boolean sendWarningIfOccupationTaxBad(CommandSender sender) {
        double idealConfiguredValue = calculateIdealOccupationTaxValue();
        double actualConfiguredValue = SiegeWarSettings.getMaxOccupationTaxPerPlot();
        String configIdentifier = "war:siege:money:max_occupation_tax_per_plot";
        return sendBadWarConfigWarning(sender, idealConfiguredValue, actualConfiguredValue, configIdentifier);
    }

    private static boolean sendBadWarConfigWarning(CommandSender sender,
                                                double idealConfiguredValue,
                                                double actualConfiguredValue,
                                                String configIdentifier) {
        double toleranceAmount = idealConfiguredValue * SiegeWarSettings.getBadConfigWarningsTolerancePercentage() / 100;
        double lowerBound = idealConfiguredValue - toleranceAmount;
        double upperBound = idealConfiguredValue + toleranceAmount;
        if(actualConfiguredValue > upperBound) {
            Messaging.sendErrorMsg(sender, Translatable.of("msg_err_value_configured_too_high", configIdentifier, "" + idealConfiguredValue , "" + actualConfiguredValue));
            return true;
        } else if (actualConfiguredValue < lowerBound) {
            Messaging.sendErrorMsg(sender, Translatable.of("msg_err_value_configured_too_low", configIdentifier, "" + idealConfiguredValue, "" + actualConfiguredValue));
            return true;
        } else {
            return false;
        }
    }

    public static double calculateIdealPlunderValue() {
        return calculateValuePerTownBlock() * SiegeWarSettings.getBadConfigWarningsIdealPlunderPercentage() / 100;
    }

    public static double calculateIdealWarChestValue() {
        return calculateValuePerTownBlock() * SiegeWarSettings.getBadConfigWarningsIdealWarchestPercentage() / 100;
    }

    public static double calculateIdealUpfrontCostValue() {
        return calculateValuePerTownBlock() * SiegeWarSettings.getBadConfigWarningsIdealUpfrontCostPercentage() / 100;
    }

    public static double calculateIdealOccupationTaxValue() {
        return calculateValuePerTownBlock() * SiegeWarSettings.getBadConfigWarningsIdealOccupationTaxPercentage() / 100;
    }

    private static double calculateValuePerTownBlock() {
        double allMoney = SiegeWarMoneyUtil.getEstimatedTotalMoneyInEconomy();
        double numTownBlocks = TownyAPI.getInstance().getTownBlocks().size();
        double valuePerTownBlock = allMoney / numTownBlocks;
        return valuePerTownBlock;
    }
}

