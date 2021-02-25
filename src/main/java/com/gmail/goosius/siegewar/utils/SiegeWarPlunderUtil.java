package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import java.util.Set;

public class SiegeWarPlunderUtil {

    public static double attackerPlundersTown(Siege siege, double totalPlunderAmount) {
        //Take from town
        double actualPlunderAmount = takePlunderFromTown(siege, siege.getDefendingTown(), siege.getAttackingNation(), totalPlunderAmount);
        //Distribute to nation & attacking soldiers
        givePlunderTo(siege.getAttackingNation(), siege.getAttackerSiegeContributionHistory(), totalPlunderAmount);
        return actualPlunderAmount;
    }

    public static double defenderPlundersWarchest(Siege siege, double amountToPlunder) {
        //Take from war chest
        siege.setWarChestAmount(siege.getWarChestAmount() - amountToPlunder);
        //Distribute to town & defending soldiers
        givePlunderTo(siege.getDefendingTown(), siege.getDefenderSiegeContributionHistory(), amountToPlunder);
        return amountToPlunder;
    }




    private static double takePlunderFromTown(Siege siege, Town town, Nation nation, double totalPlunderAmount) {
        boolean townNewlyBankrupted = false;
        boolean townDestroyed = false;

        try {
            if(town.getAccount().canPayFromHoldings(totalPlunderAmount)) {
                //Town can afford plunder
                town.getAccount().withdraw(totalPlunderAmount, "Plunder of town by attackers");
            } else {
                //Town cannot afford plunder
                if (TownySettings.isTownBankruptcyEnabled()) {
                    // If able, they will go into bankrupcty.

                    // Set the Town's debtcap fresh.
                    town.getAccount().setDebtCap(MoneyUtil.getEstimatedValueOfTown(town));

                    // Mark them as newly bankrupt for message later on.
                    townNewlyBankrupted = true;

                    // This will drop their actualPlunder amount to what the town's debt cap will allow.
                    // Enabling a town to go only so far into debt to pay the plunder cost.
                    if (town.getAccount().getHoldingBalance() - totalPlunderAmount < town.getAccount().getDebtCap() * -1)
                        totalPlunderAmount = town.getAccount().getDebtCap() - Math.abs(town.getAccount().getHoldingBalance());

                    // Charge the town (using .withdraw() which will allow for going into bankruptcy.)
                    town.getAccount().withdraw(totalPlunderAmount, "Plunder of town by attackers");
                } else {
                    // Not able to go bankrupt, they are destroyed, pay what they can.
                    totalPlunderAmount = town.getAccount().getHoldingBalance();
                    townDestroyed = true;
                }
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }

        //Set siege plundered flag
        siege.setTownPlundered(true);

        //Save data
        if(townDestroyed) {
            TownyUniverse.getInstance().getDataSource().removeTown(town);
        } else {
            SiegeController.saveSiege(siege);
        }

        //Send town bankrupted/destroyed message
        if(townNewlyBankrupted) {
            Messaging.sendGlobalMessage(
                    Translation.of("msg_siege_war_town_bankrupted_from_plunder",
                            town,
                            nation.getFormattedName()));
        } else if (townDestroyed) {
            Messaging.sendGlobalMessage(
                    Translation.of("msg_siege_war_town_ruined_from_plunder",
                            town,
                            nation.getFormattedName()));
        }

        return totalPlunderAmount; //Return the actual amount which was plundered
    }



    private static void givePlunderTo(Government receivingGovernment, Set<Resident> receivingPlayers, double amountToPlunder) {
        String distributionRatio = SiegeWarSettings.getWarSiegeSpoilsDistributionRatio();

        //Calculate total plunder for nation & soldiers
        String[] nationSoldierRatios = distributionRatio.split(":");
        int nationRatio = Integer.parseInt(nationSoldierRatios[0]);
        int soldierRatio = Integer.parseInt(nationSoldierRatios[1]);
        int totalRatio = nationRatio + soldierRatio;
        double totalPlunderForNation = totalPlunderAmount / totalRatio * nationRatio;
        double totalPlunderForSoldiers = totalPlunderAmount - totalPlunderForNation;

        //Pay nation
        if(removeMoneyFromTownBank) {
            town.getAccount().payTo(totalPlunderForNation, nation, "Plunder");
        } else {
            nation.getAccount().deposit(totalPlunderForNation, "Plunder of " + town.getName());
        }

        //Pay soldiers
        boolean soldiersPaid = SiegeWarMoneyUtil.distributeMoneyAmongSoldiers(
                totalPlunderForSoldiers,
                town,
                nation,
                "Plunder",
                removeMoneyFromTownBank);

        //If there were no soldiers, give money to nation
        if(!soldiersPaid) {
            if(removeMoneyFromTownBank) {
                town.getAccount().payTo(totalPlunderForSoldiers, nation, "Plunder");
            } else {
                nation.getAccount().deposit(totalPlunderForSoldiers, "Plunder of " + town.getName());
            }
        }
    }

    public static void givePlunderToNation(Nation nation) {
        //Note...... the nation stats must reflect when ANY nation soldiers steals money. Right????

        NationMetaDataController.setTotalPlunderGained(nation, NationMetaDataController.getTotalPlunderGained(nation) + (int) totalPlunderAmount);
        if (town.hasNation())
            try {
                NationMetaDataController.setTotalPlunderLost(town.getNation(), NationMetaDataController.getTotalPlunderLost(town.getNation()) + (int) totalPlunderAmount);
            } catch (NotRegisteredException ignored) {}
    }

}
