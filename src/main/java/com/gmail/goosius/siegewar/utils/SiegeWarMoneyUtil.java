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

import org.bukkit.entity.Player;

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
	 * @return false if payment cannot be made for various reasons.
	 */
	public static boolean collectNationRefund(Player player) throws Exception {
		if(!(SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeRefundInitialNationCostOnDelete())) {
			return false;
		}

		Resident formerKing = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (formerKing == null)
			return false;

		if(ResidentMetaDataController.getNationRefundAmount(formerKing) != 0) {
			int refundAmount = ResidentMetaDataController.getNationRefundAmount(formerKing);
			formerKing.getAccount().deposit(refundAmount, "Nation Refund");
			ResidentMetaDataController.clearNationRefund(formerKing);
			Messaging.sendMsg(player, Translation.of("msg_siege_war_nation_refund_collected", TownyEconomyHandler.getFormattedBalance(refundAmount)));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If the player is due plunder, pays the plunder to the player
	 *
	 * @param player collecting the plunder
	 * @return true if payment is made
	 * @return false if payment cannot be made for various reasons.
	 */
	public static boolean collectPlunder(Player player) throws Exception {
		if(!SiegeWarSettings.getWarSiegeEnabled() || !SiegeWarSettings.getWarSiegePlunderEnabled()) {
			return false;
		}

		Resident soldier = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (soldier == null)
			return false;

		int plunderAmount = ResidentMetaDataController.getPlunderAmount(soldier);
		if(plunderAmount != 0) {
			soldier.getAccount().deposit(plunderAmount, "Plunder");
			ResidentMetaDataController.clearPlunder(soldier);
			Messaging.sendMsg(player, Translation.of("msg_siege_war_plunder_collected", TownyEconomyHandler.getFormattedBalance(plunderAmount)));
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
				Translation.of("msg_siege_war_nation_refund_available", TownyEconomyHandler.getFormattedBalance(plunderAmount)));
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
}
