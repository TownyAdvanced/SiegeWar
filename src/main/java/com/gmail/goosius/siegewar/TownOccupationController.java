package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.MoneyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TownOccupationController {

	public static boolean isTownOccupied(Town town) {
		return town.isConquered();
	}

	public static Nation getTownOccupier(Town town) {
		return town.getNationOrNull();
	}

	public static boolean isTownOccupiedByNation(Nation nation, Town town) {
		return isTownOccupied(town) && town.getNationOrNull() == nation;
	}

    public static void removeTownOccupation(Town occupiedTown) {
		//Remove the town from its nation, if it has one
		Nation existingNation = null;
		if(occupiedTown.hasNation()) {
			existingNation = occupiedTown.getNationOrNull();
			occupiedTown.removeNation();
		}

		//Set town occupied flag
		occupiedTown.setConquered(false);

		//Save data
		occupiedTown.save();
		if(existingNation != null) {
			existingNation.save();
		}
    }

    public static void setTownOccupation(Town targetTown, @NotNull Nation occupyingNation) {
        //Remove the town from its existing nation, if it has one
		Nation existingNation = null;
		if(targetTown.hasNation()) {
			existingNation = targetTown.getNationOrNull();
			targetTown.removeNation();
		}

		//Add the town to the new nation
		try {
			targetTown.setNation(occupyingNation);
		} catch (AlreadyRegisteredException are) {
			//This exception should not happen, because we removed the nation just above
			are.printStackTrace();
			return;
		}

		//Set town occupied flag
		targetTown.setConquered(true);

		//Save data
		targetTown.save();
		occupyingNation.save();
		if(existingNation != null) {
			existingNation.save();
		}
    }

	public static void chargeNationPeacefulOccupationTax() {
		if (!TownyEconomyHandler.isActive())
			return;
		for (Nation nation : new ArrayList<>(TownyAPI.getInstance().getNations())) {
			double tax = NationMetaDataController.getNationPeacefulOccupationTax(nation); 
			if (tax > 0)
				collectNationPeacefulOccupationTax(nation, tax);
		}
	}

	private static void collectNationPeacefulOccupationTax(Nation nation, double tax) {
		double taxesPaid = 0;
		for (Town town : new ArrayList<>(nation.getTowns()))
			if (town.isNeutral() && TownOccupationController.isTownOccupied(town))
				taxesPaid += collectNationPeacefulOccupationTax(nation, tax, town);
		
		TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_peaceful_occupation_taxes_paid_totaling", getMoney(taxesPaid)));
	}

	private static double collectNationPeacefulOccupationTax(Nation nation, double tax, Town town) {
		if (town.getAccount().canPayFromHoldings(tax)) {
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_peaceful_occupation_tax_payed", getMoney(tax)));
			town.getAccount().payTo(tax, nation.getAccount(), "Nation Peaceful Occupation Tax");
			return tax;
		}

		if (TownySettings.isTownBankruptcyEnabled()) {
			// Set the Town's debtcap fresh.
			town.getAccount().setDebtCap(MoneyUtil.getEstimatedValueOfTown(town));
			double debtCap = town.getAccount().getDebtCap();

			if (town.getAccount().getHoldingBalance() - tax < debtCap * -1) {
				// The Town cannot afford to pay the nation occupation tax.
				Messaging.sendGlobalMessage(Translatable.of("msg_peaceful_occupation_tax_cannot_be_payed", town.getName()));
				removeTownOccupation(town);
				TownyUniverse.getInstance().getDataSource().removeTown(town);
				return 0;
			}

			// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_peaceful_occupation_tax_payed_bankrupt", getMoney(tax)));
			town.getAccount().withdraw(tax, "Nation Peaceful Occupation Tax paid to " + nation.getName());
			nation.getAccount().deposit(tax, "Nation Peaceful Occupation Tax from " + town.getName());
			return tax;
		} else {
			Messaging.sendGlobalMessage(Translatable.of("msg_peaceful_occupation_tax_cannot_be_payed", town.getName()));
			removeTownOccupation(town);
			TownyUniverse.getInstance().getDataSource().removeTown(town);
			return 0;
		}
	}

	private static String getMoney(double amount) {
		return TownyEconomyHandler.getFormattedBalance(amount);
	}
}

