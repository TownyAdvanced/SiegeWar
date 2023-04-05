package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;

public class SiegeWarMoneyUtil {

	public static void giveWarChestTo(Siege siege, Government government) {
		if(TownyEconomyHandler.isActive()) {
			government.getAccount().deposit(siege.getWarChestAmount(), "War Chest Captured");
			Translatable message =
					Translatable.of("msg_siege_war_attack_recover_war_chest",
							government.getFormattedName(),
							TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));

			//Send message to attacker
			if (siege.getAttacker() instanceof Nation)
				TownyMessaging.sendPrefixedNationMessage((Nation)siege.getAttacker(), message);
			else
				TownyMessaging.sendPrefixedTownMessage((Town)siege.getAttacker(), message);

			//Send message to defender
			if (siege.getDefender() instanceof Nation)
				TownyMessaging.sendPrefixedNationMessage((Nation)siege.getDefender(), message);
			else
				TownyMessaging.sendPrefixedTownMessage((Town)siege.getDefender(), message);
		}
	}

	/**
	 * Gets the siegewar money multiplier for the given town
	 *
	 * @param town the town to consider
	 * @return the multiplier
	 */
	public static double getMoneyMultiplier(Town town) {
		double extraMoneyPercentage = SiegeWarSettings.getWarSiegeExtraMoneyPercentagePerTownLevel();

		if(extraMoneyPercentage == 0) {
			return 1;
		} else {
			return 1 + ((extraMoneyPercentage / 100) * (town.getLevelID() -1));
		}
	}

	/**
	 * If the player is due military salary, pays it to the player
	 *
	 * @param player collecting the military salary
	 * @return true if payment is made
	 *         false if payment cannot be made for various reasons.
	 */
	public static boolean collectMilitarySalary(Player player) throws Exception {
		if(!SiegeWarSettings.getWarSiegeEnabled() || !SiegeWarSettings.getWarSiegeMilitarySalaryEnabled()) {
			return false;
		}
		return collectIncome(player, "Military Salary",
				"msg_siege_war_military_salary_collected");
	}

	/**
	 * If the player is due an income, pays it to the player
	 *
	 * @param player collecting the military salary
	 * @param reason reason for payment
	 * @param successMessageLangId relevant lang string id
	 * @return true if payment is made
	 *         false if payment cannot be made for various reasons.
	 */
	private static boolean collectIncome(Player player,
										 String reason,
										 String successMessageLangId) throws Exception {
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null)
			return false;

		int incomeAmount;
		switch(reason.toLowerCase()) {
			case "military salary":
				incomeAmount = ResidentMetaDataController.getMilitarySalaryAmount(resident);
				break;
			default:
				throw new TownyException("Unknown income type");
		}

		if(incomeAmount != 0) {
			resident.getAccount().deposit(incomeAmount, reason);
			switch(reason.toLowerCase()) {
				case "military salary":
					ResidentMetaDataController.clearMilitarySalary(resident);
				break;
				default:
					throw new TownyException("Unknown income type");
			}
			Messaging.sendMsg(player, Translatable.of(successMessageLangId, TownyEconomyHandler.getFormattedBalance(incomeAmount)));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Make some military salary money available to a resident
	 *
	 * @param soldier the resident to grant the amount to.
	 * @param militarySalaryAmount the amount
	 */
	public static void makeMilitarySalaryAvailable(Resident soldier, int militarySalaryAmount) {
		// Makes the military salary available. Player can do "/sw collect" later to claim money.
		ResidentMetaDataController.addMilitarySalaryAmount(soldier, militarySalaryAmount);
		Messaging.sendMsg(soldier.getPlayer(),
				Translatable.of("msg_siege_war_military_salary_available", TownyEconomyHandler.getFormattedBalance(militarySalaryAmount)));
	}

	public static double calculateUpfrontSiegeStartCost(Town town) {
		double cost = SiegeWarSettings.getWarSiegeUpfrontCostPerPlot()
				* town.getNumTownBlocks();
		cost = applyMoneyModifiers(cost, town);
		return cost;
	}

	public static double calculateWarchestCost(Town town) {
		double cost = SiegeWarSettings.getWarSiegeWarchestCostPerPlot()
				* town.getTownBlocks().size();
		cost = applyMoneyModifiers(cost, town);
		return  cost;
	}

	public static double calculateTotalSiegeStartCost(Town town) {
		return calculateUpfrontSiegeStartCost(town) + calculateWarchestCost(town);
	}

	private static double applyMoneyModifiers(double cost, Town town) {
		//Increase cost if town is capitol
		if(SiegeWarSettings.getWarSiegeCapitalCostIncreasePercentage() > 0
			&& town.isCapital()) {
			cost *= (1 + (SiegeWarSettings.getWarSiegeCapitalCostIncreasePercentage() / 100));
		}

		//Increase cost due to money multiplier & town size
		if(SiegeWarSettings.getWarSiegeExtraMoneyPercentagePerTownLevel() > 0) {
			cost *= getMoneyMultiplier(town);
		}

		return cost;
	}

	/**
	 *
	 * @param totalAmountForSoldiers total amount
	 * @param town the town which pays
	 * @param soldierSharesMap the shares of soldiers
	 * @param reason reason for payment
	 * @param removeMoneyFromTownBank if true, remove money from town
	 * @return true if money was paid. False if there were no soldiers
	 */
	public static boolean distributeMoneyAmongSoldiers(double totalAmountForSoldiers,
													Town town,
													Map<Resident, Integer> soldierSharesMap,
													String reason,
													boolean removeMoneyFromTownBank) {
		if(soldierSharesMap.size() == 0)
			return false;

		//Withdraw money from town if needed
		if (removeMoneyFromTownBank) {
			town.getAccount().withdraw(totalAmountForSoldiers, reason);
		}

		//Find out total shares within the army
		int totalArmyShares = 0;
		for(Integer share: soldierSharesMap.values()) {
			totalArmyShares += share;
		}

		//Calculate how much 1 share is worth
		double amountValueOfOneShare = totalAmountForSoldiers / totalArmyShares;

		//Pay each soldier
		int amountToPaySoldier;
		for(Map.Entry<Resident,Integer> soldierShareEntry: soldierSharesMap.entrySet()) {
			amountToPaySoldier = (int)((amountValueOfOneShare * soldierShareEntry.getValue())); //Round down to avoid exploits for making extra money
			switch(reason.toLowerCase()) {
				case "military salary":
					makeMilitarySalaryAvailable(soldierShareEntry.getKey(), amountToPaySoldier);
				break;
				default:
					throw new RuntimeException("Unknown Income Type");
			}
		}
		return true;
	}

	/**
	 * Can the nation afford to start their siege?
	 * 
	 * @param nation Nation starting a siege.
	 * @param town Town being sieged.
	 * @throws TownyException thrown if the economy is off, or the nation cannot pay.
	 */
	public static void canNationPayCostToSiegeTown(Nation nation, Town town) throws TownyException {
		double cost = calculateTotalSiegeStartCost(town);
		if (!TownyEconomyHandler.isActive())
			throw new TownyException(Translatable.of("msg_err_no_siege_economy_not_active"));
		if (!nation.getAccount().canPayFromHoldings(cost))
			throw new TownyException(Translatable.of("msg_err_you_cannot_afford_to_siege_for_x", TownyEconomyHandler.getFormattedBalance(cost)));
	}

	public static void payDailyPlunderDebt() {
		for (Town town : new ArrayList<>(TownyUniverse.getInstance().getTowns()))
			payDailyPlunderDebt(town);
	}

	private static void payDailyPlunderDebt(Town town) {
		if (!TownMetaDataController.hasPlunderDebt(town))
			return;
		int days = TownMetaDataController.getPlunderDebtDays(town);

		payPlunderDebt(town, TownMetaDataController.getDailyPlunderDebt(town));

		if (days <= 1)
			TownMetaDataController.removePlunderDebt(town);
		else 
			TownMetaDataController.setPlunderDebtDays(town, days - 1);
	}

	private static void payPlunderDebt(Town town, double amount) {
		if (amount <= 0)
			return;

		if (town.getAccount().canPayFromHoldings(amount)) {
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_plunder_debt_payed", getMoney(amount)));
			town.getAccount().withdraw(amount, "Daily Plunder Debt Repayment");
			return;
		}

		if (TownySettings.isTownBankruptcyEnabled()) {
			// Set the Town's debtcap fresh.
			town.getAccount().setDebtCap(MoneyUtil.getEstimatedValueOfTown(town));
			double debtCap = town.getAccount().getDebtCap();

			if (town.getAccount().getHoldingBalance() - amount < debtCap * -1) {
				// The Town cannot afford to pay their plunder debt.
				Messaging.sendGlobalMessage(Translatable.of("msg_plunder_debt_cannot_be_payed", town.getName()));
				TownyUniverse.getInstance().getDataSource().removeTown(town);
				return;
			}

			// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_plunder_debt_payed_bankrupt", getMoney(amount)));
			town.getAccount().withdraw(amount, "Daily Plunder Debt Repayment");

		} else {
			Messaging.sendGlobalMessage(Translatable.of("msg_plunder_debt_cannot_be_payed", town.getName()));
			TownyUniverse.getInstance().getDataSource().removeTown(town);
		}
	}

	private static String getMoney(double amount) {
		return TownyEconomyHandler.getFormattedBalance(amount);
	}

	public static void makeNationRefundAvailable(Resident king) {
		//Refund some of the initial setup cost to the king
		if (TownySettings.isUsingEconomy()
			&& SiegeWarSettings.getWarSiegeNationCostRefundPercentageOnDelete() > 0) {

			//Make the nation refund available
			//The player can later do "/n claim refund" to receive the money
			int amountToRefund = (int)(TownySettings.getNewNationPrice() * 0.01 * SiegeWarSettings.getWarSiegeNationCostRefundPercentageOnDelete());
			ResidentMetaDataController.addNationRefundAmount(king, amountToRefund);

			//If king is online, send message
			if(king.isOnline()) {
				Messaging.sendMsg(king.getPlayer(),
					Translatable.of("msg_siege_war_nation_refund_available",
								TownyEconomyHandler.getFormattedBalance(amountToRefund)).forLocale(king.getPlayer()));
			}
		}
	}

	/**
	 * If the player is due a nation refund, pays the refund to the player
	 *
	 * @param player claiming the nation refund.
	 * @throws TownyException when payment cannot be made for various reasons.
	 */
	public static boolean claimNationRefund(Player player) throws TownyException {
		if (!TownySettings.isUsingEconomy()
				|| SiegeWarSettings.getWarSiegeNationCostRefundPercentageOnDelete() == 0) {
			throw new TownyException(Translatable.of("msg_err_command_disable"));
		}
		Resident formerKing = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (formerKing == null)
			throw new TownyException(Translatable.of("msg_err_not_registered_1", player.getName()));

		int refundAmount = ResidentMetaDataController.getNationRefundAmount(formerKing);
		if(refundAmount != 0) {
			formerKing.getAccount().deposit(refundAmount, "Nation Refund");
			ResidentMetaDataController.setNationRefundAmount(formerKing, 0);
			Messaging.sendMsg(player, Translatable.of("msg_siege_war_nation_refund_claimed", TownyEconomyHandler.getFormattedBalance(refundAmount)));
			return true;
		} else {
			throw new TownyException(Translatable.of("msg_err_siege_war_nation_refund_unavailable"));
		}
	}


	/**
	 * Pay the upfront cost of starting the siege
	 * 
	 * @param siege the siege
	 */
	public static void payUpfrontSiegeStartCost(Siege siege) {
		double cost = SiegeWarMoneyUtil.calculateUpfrontSiegeStartCost(siege.getTown());
		if(TownyEconomyHandler.isActive()) {
			if(siege.getSiegeType() == SiegeType.CONQUEST) {
				siege.getAttacker().getAccount().withdraw(cost, "Upfront cost of starting siege.");
				Translatable moneyMessage =
						Translatable.of("msg_nation_pay_upfront_siege_cost",
								TownyEconomyHandler.getFormattedBalance(cost));
				TownyMessaging.sendPrefixedNationMessage((Nation)siege.getAttacker(), moneyMessage);
			} else if(siege.getSiegeType() == SiegeType.REVOLT) {
				siege.getTown().getAccount().withdraw(cost, "Upfront cost of starting siege.");
				Translatable moneyMessage =
						Translatable.of("msg_town_pay_upfront_siege_cost",
								TownyEconomyHandler.getFormattedBalance(cost));
				TownyMessaging.sendPrefixedTownMessage(siege.getTown(), moneyMessage);
			}
		}
	}

}
