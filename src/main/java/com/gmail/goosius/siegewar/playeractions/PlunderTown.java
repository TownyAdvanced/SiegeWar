package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.EconomyAccount;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to plunder towns
 *
 * @author Goosius
 */
public class PlunderTown {

	/**
	 * Process a plunder town request
	 *
	 * This method does some final checks and if they pass, the plunder is executed.
	 *
	 * @param player The Player who placed the plunder chest
	 * @param siege  The Siege resulting in plunder.
	 * @throws TownyException when a plunder is not allowed.
	 */
	public static void processPlunderTownRequest(Player player, Siege siege) throws TownyException {
		plunderTown(siege, getPlunderingNationOrThrow(player, siege));
	}

	private static Nation getPlunderingNationOrThrow(Player player, Siege siege) throws TownyException {
		Town townToBePlundered = siege.getTown();
		TownyUniverse townyUniverse = TownyUniverse.getInstance();
		final Translator translator = Translator.locale(player);

		Resident resident = townyUniverse.getResident(player.getUniqueId());
		if (resident == null)
			throw new TownyException(translator.of("msg_err_not_registered_1", player.getName()));

		if(!resident.hasTown())
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_town_member"));

		Nation plunderingNation = resident.getNationOrNull();
		if(plunderingNation == null)
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_nation_member"));

		if (!townyUniverse.getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_PLUNDER.getNode()))
			throw new TownyException(translator.of("msg_err_cannot_plunder_not_enough_permissions"));

		if(siege.isTownPlundered())
			throw new TownyException(translator.of("msg_err_siege_war_town_already_plundered", townToBePlundered.getName()));

		if(siege.isRevoltSiege()) {
			// If the rebels won, plunder is not possible
			if (siege.getStatus() == SiegeStatus.DEFENDER_WIN || siege.getStatus() == SiegeStatus.ATTACKER_ABANDON)
				throw new TownyException(translator.of("msg_err_siege_war_plunder_not_possible_rebels_won"));
		}

		// Ensure the attacking nation has completed the win
		if(siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.ATTACKER_CLOSE_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
			throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_victory"));

		// Ensure tha attempted plunderer is from the victorious nation
		if(plunderingNation != siege.getAttacker())
			throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_victory"));

		// Return the victorious nation, which has defeated the revolting town.
		return (Nation)siege.getAttacker();
	}

	private static void plunderTown(Siege siege, Nation nation) {
		Town town = siege.getTown();
		boolean townNewlyBankrupted = false;
		boolean townDestroyed = false;
		boolean townHasNation = town.hasNation();
		String townName = town.getName();

		double totalPlunderAmount =
				SiegeWarSettings.getWarSiegePlunderAmountPerPlot()
				* town.getTownBlocks().size()
				* SiegeWarMoneyUtil.getMoneyMultiplier(town);
		
		//Redistribute money
		
		if (SiegeWarSettings.isPlunderPaidOutOverDays()) {
			// Plunder is paid out of server, and paid back over time.
			totalPlunderAmount = createPlunderForNation(siege, nation, townName, totalPlunderAmount);
			createDailyPaymentsForTown(town, totalPlunderAmount);
		} else {
			// Plunder is paid immediately by the sieged Town.
			if(town.getAccount().canPayFromHoldings(totalPlunderAmount)) {
				// Town can afford plunder
				transferPlunderToNation(siege, nation, totalPlunderAmount, true);
			} else {
				// Town cannot afford plunder

				// Plunder can bankrupt and destroy a town.
				double townBalance = town.getAccount().getHoldingBalance();
				if (TownySettings.isTownBankruptcyEnabled()) {
					// The town is going to go bankrupt in order to pay the plunder costs.
					// Mark them as newly bankrupt for message later on.
					townNewlyBankrupted = true;

					// Set the Town's debtcap fresh.
					town.getAccount().setDebtCap(MoneyUtil.getEstimatedValueOfTown(town));

					// This will drop their actualPlunder amount to what the town's debt cap will allow. 
					// Enabling a town to go only so far into debt to pay the plunder cost.
					double debtCap = town.getAccount().getDebtCap();
					if (townBalance - totalPlunderAmount < debtCap * -1) {
						totalPlunderAmount = townBalance > 0
							? debtCap + Math.abs(townBalance)
							: debtCap - Math.abs(townBalance);
					}
					// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
					town.getAccount().withdraw(totalPlunderAmount, "Plunder by " + nation.getName());
					// And deposit it into the nation.
					transferPlunderToNation(siege, nation, totalPlunderAmount, false);

				} else {
					// Not able to go bankrupt, they are destroyed, pay what they can.
					totalPlunderAmount = townBalance;
					transferPlunderToNation(siege, nation, totalPlunderAmount, true);
					townDestroyed = true;
				}
			}

		}
		
		// Record the plunder values for history.
		NationMetaDataController.setTotalPlunderGained(nation, NationMetaDataController.getTotalPlunderGained(nation) + (int) totalPlunderAmount);
		if (townHasNation)
			NationMetaDataController.setTotalPlunderLost(town.getNationOrNull(), NationMetaDataController.getTotalPlunderLost(town.getNationOrNull()) + (int) totalPlunderAmount);

		//Set siege plundered flag
		siege.setTownPlundered(true);

		//Save data
		if(townDestroyed) {
			TownyUniverse.getInstance().getDataSource().removeTown(town);
		} else {
			SiegeController.saveSiege(siege);
		}

		//Send plunder success messages
		String message = townHasNation ? "msg_siege_war_nation_town_plundered" : "msg_siege_war_neutral_town_plundered";
		Messaging.sendGlobalMessage(
				Translatable.of(message,
						townName,
						TownyEconomyHandler.getFormattedBalance(totalPlunderAmount),
						nation.getName()));

		//Send town bankrupted/destroyed message
		if(townNewlyBankrupted) {
			Messaging.sendGlobalMessage(
				Translatable.of("msg_siege_war_town_bankrupted_from_plunder",
						townName,
						nation.getFormattedName()));
		} else if (townDestroyed) {
			Messaging.sendGlobalMessage(
				Translatable.of("msg_siege_war_town_ruined_from_plunder",
						townName,
						nation.getFormattedName()));
		}
	}

	private static void transferPlunderToNation(Siege siege, Nation nation, double totalPlunderAmount, boolean removeMoneyFromTownBank) {
		Town town = siege.getTown();

		//Pay nation bank
		if(removeMoneyFromTownBank) {
			town.getAccount().payTo(totalPlunderAmount, nation, "Plunder");
		} else {
			nation.getAccount().deposit(totalPlunderAmount, "Plunder of " + town.getName());
		}
	}

	private static double createPlunderForNation(Siege siege, Nation nation, String townname, double totalPlunderAmount) {
		//Pay nation bank
		if(TownySettings.isEcoClosedEconomyEnabled()) {
			totalPlunderAmount = Math.min(EconomyAccount.SERVER_ACCOUNT.getHoldingBalance(), totalPlunderAmount);
			EconomyAccount.SERVER_ACCOUNT.payTo(totalPlunderAmount, nation.getAccount(), "Plunder of " + townname);
		} else {
			nation.getAccount().deposit(totalPlunderAmount, "Plunder of " + townname);
		}
		return totalPlunderAmount;
	}

	private static void createDailyPaymentsForTown(Town town, double totalPlunderAmount) {
		int days = SiegeWarSettings.plunderDays();
		int payment = (int) totalPlunderAmount / days;
		TownMetaDataController.setDailyPlunderDebt(town, payment);
		TownMetaDataController.setPlunderDebtDays(town, days);
		TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_plunder_debt_earned", TownyEconomyHandler.getFormattedBalance(payment), days));
	}
}
