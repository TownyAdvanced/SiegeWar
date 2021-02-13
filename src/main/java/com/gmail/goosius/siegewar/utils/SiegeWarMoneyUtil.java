package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;

import com.palmergames.bukkit.towny.permissions.TownyPerms;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SiegeWarMoneyUtil {

	/**
	 * This method gives the war chest to the attacking nation
	 *
	 * @param siege the siege
	 */
	public static void giveWarChestToAttackingNation(Siege siege) {
		Nation winnerNation = siege.getAttackingNation();
		if (TownySettings.isUsingEconomy()) {
			try {
				winnerNation.getAccount().deposit(siege.getWarChestAmount(), "War Chest Captured/Returned");
				String message =
					Translation.of("msg_siege_war_attack_recover_war_chest",
					winnerNation.getFormattedName(),
					TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));

				//Send message to nation(
				TownyMessaging.sendPrefixedNationMessage(winnerNation, message);
				//Send message to town
				TownyMessaging.sendPrefixedTownMessage(siege.getDefendingTown(), message);
			} catch (Exception e) {
				System.out.println("Problem paying war chest(s) to winner nation");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method gives one war chest to the defending town
	 *
	 * @param siege the siege zone
	 */
	public static void giveWarChestToDefendingTown(Siege siege) {
		Town winnerTown= siege.getDefendingTown();
		if(TownySettings.isUsingEconomy()) {
			try {
				winnerTown.getAccount().deposit(siege.getWarChestAmount(), "War Chest Captured");
				String message =
					Translation.of("msg_siege_war_attack_recover_war_chest",
					winnerTown.getFormattedName(),
					TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));

				//Send message to nation
				TownyMessaging.sendPrefixedNationMessage(siege.getAttackingNation(), message);
				//Send message to town
				TownyMessaging.sendPrefixedTownMessage(winnerTown, message);
			} catch (EconomyException e) {
				System.out.println("Problem paying war chest(s) to winner town");
				e.printStackTrace();
			}
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
			return 1 + ((extraMoneyPercentage / 100) * (TownySettings.calcTownLevelId(town) -1));
		}
	}

	/**
	 * If the player is due a nation refund, pays the refund to the player
	 *
	 * @param player collecting the nation refund.
	 * @return true if payment is made
	 *        false if payment cannot be made for various reasons.
	 */
	public static boolean collectNationRefund(Player player) throws Exception {
		if (!(SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeRefundInitialNationCostOnDelete())) {
			return false;
		}
		return collectIncome(player, "Nation Refund",
				"msg_siege_war_nation_refund_collected");
	}

	/**
	 * If the player is due plunder, pays the plunder to the player
	 *
	 * @param player collecting the plunder
	 * @return true if payment is made
	 *         false if payment cannot be made for various reasons.
	 */
	public static boolean collectPlunder(Player player) throws Exception {
		if(!SiegeWarSettings.getWarSiegeEnabled() || !SiegeWarSettings.getWarSiegePlunderEnabled()) {
			return false;
		}
		return collectIncome(player, "Plunder",
				"msg_siege_war_plunder_collected");
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
			case "nation refund":
				incomeAmount = ResidentMetaDataController.getNationRefundAmount(resident);
				break;
			case "plunder":
				incomeAmount = ResidentMetaDataController.getPlunderAmount(resident);
				break;
			case "military salary":
				incomeAmount = ResidentMetaDataController.getMilitarySalaryAmount(resident);
				break;
			default:
				throw new TownyException("Unknown income type");
		}

		if(incomeAmount != 0) {
			resident.getAccount().deposit(incomeAmount, reason);
			switch(reason.toLowerCase()) {
				case "nation refund":
					ResidentMetaDataController.clearNationRefund(resident);
				break;
				case "plunder":
					ResidentMetaDataController.clearPlunder(resident);
				break;
				case "military salary":
					ResidentMetaDataController.clearMilitarySalary(resident);
				break;
				default:
					throw new TownyException("Unknown income type");
			}
			Messaging.sendMsg(player, Translation.of(successMessageLangId, TownyEconomyHandler.getFormattedBalance(incomeAmount)));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Refund some of the initial setup cost to the king
	 * 
	 * @param king Resident to grant the refund to.
	 */
	public static void makeNationRefundAvailable(Resident king) {
		int amountToRefund = (int)(TownySettings.getNewNationPrice() * 0.01 * SiegeWarSettings.getWarSiegeNationCostRefundPercentageOnDelete());
		
		// Makes the nation refund available. Player can do "/sw collect" later to claim money.
		ResidentMetaDataController.addNationRefundAmount(king, amountToRefund);

		Messaging.sendMsg(king.getPlayer(),
				Translation.of("msg_siege_war_nation_refund_available", TownyEconomyHandler.getFormattedBalance(amountToRefund)));
	}

	/**
	 * Make some plunder money available to a resident
	 *
	 * @param soldier the resident to grant the plunder to.
	 * @param plunderAmount the plunder amount
	 */
	public static void makePlunderAvailable(Resident soldier, int plunderAmount) {
		// Makes the plunder available. Player can do "/sw collect" later to claim money.
		ResidentMetaDataController.addPlunderAmount(soldier, plunderAmount);
		Messaging.sendMsg(soldier.getPlayer(),
				Translation.of("msg_siege_war_plunder_available", TownyEconomyHandler.getFormattedBalance(plunderAmount)));
	}

	/**
	 * Make some military salary money available to a resident
	 *
	 * @param soldier the resident to grant the amount to.
	 * @param militarySalaryAmount the amount
	 */
	public static void makeMilitarySalaryAvailable(Resident soldier, int militarySalaryAmount) {
		// Makes the plunder available. Player can do "/sw collect" later to claim money.
		ResidentMetaDataController.addMilitarySalaryAmount(soldier, militarySalaryAmount);
		Messaging.sendMsg(soldier.getPlayer(),
				Translation.of("msg_siege_war_military_salary_available", TownyEconomyHandler.getFormattedBalance(militarySalaryAmount)));
	}

	public static double getSiegeCost(Town town) {
		if (town.isCapital())
			return SiegeWarSettings.getWarSiegeAttackerCostUpFrontPerPlot()
				* (1 + SiegeWarSettings.getWarSiegeCapitalCostIncreasePercentage()/100)
				* town.getTownBlocks().size()
				* getMoneyMultiplier(town);
		else
			return SiegeWarSettings.getWarSiegeAttackerCostUpFrontPerPlot()
			* town.getTownBlocks().size()
			* getMoneyMultiplier(town);
	}

	/**
	 *
	 * @param totalAmountForSoldiers
	 * @param town
	 * @param nation
	 * @param reason
	 * @param removeMoneyFromTownBank
	 * @return true if money was paid. False if there were no soldiers
	 * @throws EconomyException
	 * @throws TownyException
	 */
	public static boolean distributeMoneyAmongSoldiers(double totalAmountForSoldiers,
													Town town,
													Nation nation,
													String reason,
													boolean removeMoneyFromTownBank) throws EconomyException {
		//Withdraw money from town if needed
		if (removeMoneyFromTownBank) {
			town.getAccount().withdraw(totalAmountForSoldiers, reason);
		}

		//Identify soldiers who need to be paid
		int totalArmyShares = 0;
		int soldierShare;
		Map<Resident, Integer> soldierShareMap = new HashMap<>();
		for(Resident resident: nation.getResidents()) {
			for (String perm : TownyPerms.getResidentPerms(resident).keySet()) {
				if (perm.startsWith("towny.nation.siege.pay.grade.")) {
					soldierShare = Integer.parseInt(perm.replace("towny.nation.siege.pay.grade.", ""));
					soldierShareMap.put(resident, soldierShare);
					totalArmyShares += soldierShare;
					break; //Next resident please
				}
			}
		}

		if(soldierShareMap.size() == 0)
			return false;

		//Calculate how much 1 share is worth
		double amountValueOfOneShare = totalAmountForSoldiers / totalArmyShares;

		//Pay each soldier
		int amountToPaySoldier;
		for(Map.Entry<Resident,Integer> entry: soldierShareMap.entrySet()) {
			amountToPaySoldier = (int)((amountValueOfOneShare * entry.getValue())); //Round down to avoid exploits for making extra money
			switch(reason.toLowerCase()) {
				case "plunder":
					makePlunderAvailable(entry.getKey(), amountToPaySoldier);
				break;
				case "military salary":
					makeMilitarySalaryAvailable(entry.getKey(), amountToPaySoldier);
				break;
				default:
					throw new RuntimeException("Unknown Income Type");
			}
		}
		return true;
	}
}
