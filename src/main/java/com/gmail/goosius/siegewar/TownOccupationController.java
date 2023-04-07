package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMilitaryRanksUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TownOccupationController {

	public static boolean isTownOccupied(Town town) {
		return town.isConquered() && town.hasNation();
	}

	/**
	 * Gets the occupier of the town
	 * This method is depreciated, because with SW 2.0.0, this can be done simply with town.getNationOrNull().
	 *
	 * @param town the town
	 * @return the occupier of the town, or null
	 */
	@Deprecated
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
        //If the town has a nation which is different than the incoming one, remove town from nation
		Nation existingNation = targetTown.getNationOrNull();
		if(existingNation != null && existingNation != occupyingNation) {
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

		//Remove military ranks
		SiegeWarMilitaryRanksUtil.removeMilitaryRanksFromTownResidents(targetTown);
		
		//Save data
		targetTown.save();
		occupyingNation.save();
		if(existingNation != null && existingNation != occupyingNation) {
			existingNation.save();
		}
    }

	public static double getNationOccupationTax(Town town) {
		if(!isTownOccupied(town))
			return 0;

		Nation nation = town.getNationOrNull();
		double occupationTaxPerPlot = NationMetaDataController.getNationOccupationTaxPerPlot(nation);
		if(occupationTaxPerPlot == -1) {
			occupationTaxPerPlot = SiegeWarSettings.getMaxOccupationTaxPerPlot();
		}

		return occupationTaxPerPlot * town.getNumTownBlocks();
	}
	
	public static void collectNationOccupationTax() {
		if (!TownyEconomyHandler.isActive())
			return;
		for (Nation nation : new ArrayList<>(TownyAPI.getInstance().getNations())) {
			double taxPerPlot = NationMetaDataController.getNationOccupationTaxPerPlot(nation); 

			if(taxPerPlot == -1) 
				taxPerPlot = SiegeWarSettings.getMaxOccupationTaxPerPlot();

			if (taxPerPlot > 0)
					collectNationOccupationTax(nation, taxPerPlot);
		}
	}

	private static void collectNationOccupationTax(Nation nation, double taxPerPlot) {
		double taxesPaid = 0;
		for (Town town : new ArrayList<>(nation.getTowns()))
			if (TownOccupationController.isTownOccupied(town))
				taxesPaid += collectNationOccupationTax(nation, taxPerPlot, town);
		
		TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_occupation_taxes_collected_totaling", getMoney(taxesPaid)));
	}

	private static double collectNationOccupationTax(Nation nation, double taxPerPlot, Town town) {
		double tax = taxPerPlot * town.getNumTownBlocks();

		if (town.getAccount().canPayFromHoldings(tax)) {
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_occupation_tax_paid", getMoney(tax)));
			town.getAccount().payTo(tax, nation.getAccount(), "Nation Occupation Tax");
			return tax;
		}

		if (TownySettings.isTownBankruptcyEnabled()) {
			// Set the Town's debtcap fresh.
			town.getAccount().setDebtCap(MoneyUtil.getEstimatedValueOfTown(town));
			double debtCap = town.getAccount().getDebtCap();

			if (town.getAccount().getHoldingBalance() - tax < debtCap * -1) {
				// The Town cannot afford to pay the nation occupation tax.
				Messaging.sendGlobalMessage(Translatable.of("msg_occupation_tax_cannot_be_paid", town.getName()));
				removeTownOccupation(town);
				TownyUniverse.getInstance().getDataSource().removeTown(town);
				return 0;
			}

			// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
			town.getAccount().withdraw(tax, "Nation Occupation Tax paid to " + nation.getName());
			nation.getAccount().deposit(tax, "Nation Occupation Tax from " + town.getName());

			if(town.isBankrupt()) {
				TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_occupation_tax_paid_with_debt", getMoney(tax)));
			} else {
				TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_occupation_tax_paid_bankrupt", getMoney(tax)));
			}

			return tax;
		} else {
			Messaging.sendGlobalMessage(Translatable.of("msg_occupation_tax_cannot_be_paid", town.getName()));
			removeTownOccupation(town);
			TownyUniverse.getInstance().getDataSource().removeTown(town);
			return 0;
		}
	}

	private static String getMoney(double amount) {
		return TownyEconomyHandler.getFormattedBalance(amount);
	}

	/**
	 * Get the list of occupied foreign towns.
	 * This method is depreciated, because with SW 2.0.0, occupied foreign towns do not exist.
	 * 
	 * @param nation the nation
	 * @return an empty list
	 */
	@Deprecated
	public static List<String> getOccupiedForeignTowns(Nation nation) {
		return new ArrayList<>();
	}
}

