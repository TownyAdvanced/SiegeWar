package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
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
		final Translator translator = Translator.locale(Translation.getLocale(player));

		if (!townyUniverse.getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_PLUNDER.getNode()))
			throw new TownyException(translator.of("msg_err_command_disable"));

		Resident resident = townyUniverse.getResident(player.getUniqueId());
		if (resident == null)
			throw new TownyException(translator.of("msg_err_not_registered_1", player.getName()));

		if(!resident.hasTown())
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_town_member"));

		Nation plunderingNation = resident.getNationOrNull();
		if(plunderingNation == null)
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_nation_member"));

		if(siege.isTownPlundered())
			throw new TownyException(translator.of("msg_err_siege_war_town_already_plundered", townToBePlundered.getName()));

		if(siege.getSiegeType() == SiegeType.REVOLT) {
			// A revolt siege means a town was rebelling against their Occupying nation.
			
			// Rebels do not plunder when they win or the occupying nation surrenders.
			if(siege.getStatus() == SiegeStatus.ATTACKER_WIN || siege.getStatus() == SiegeStatus.DEFENDER_SURRENDER)
				throw new TownyException(translator.of("msg_err_siege_war_plunder_not_possible_rebels_won"));

			// The Siege hasn't been won.
			if(siege.getStatus() != SiegeStatus.DEFENDER_WIN && siege.getStatus() != SiegeStatus.ATTACKER_ABANDON)
				throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_victory"));

			// It is not the occupying nation trying to plunder this siege.
			if(plunderingNation != siege.getDefender())
				throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_victory"));

			// Return the occupying nation of the sieged town, which has defeated the revolting town.
			return (Nation)siege.getDefender();

		} else {
			// Any other type of Siege aside from Revolt.
			
			// The Siege hasn't been won.
			if(siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
				throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_victory"));

			// It is not the attacking Nation which is trying to plunder.
			if(plunderingNation != siege.getAttacker())
				throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_victory"));

			// Return the Nation which started the Siege against the town.
			return (Nation)siege.getAttacker();
		}
	}

	private static void plunderTown(Siege siege, Nation nation) {
		Town town = siege.getTown();
		boolean townNewlyBankrupted = false;
		boolean townDestroyed = false;
		boolean townHasNation = town.hasNation();
		String townName = town.getName();

		double totalPlunderAmount =
				SiegeWarSettings.getWarSiegeAttackerPlunderAmountPerPlot()
				* town.getTownBlocks().size()
				* SiegeWarMoneyUtil.getMoneyMultiplier(town);
		
		//Redistribute money
		if(town.getAccount().canPayFromHoldings(totalPlunderAmount)) {
			//Town can afford plunder
			transferPlunderToNation(siege, nation, totalPlunderAmount, true);
		} else {
			//Town cannot afford plunder
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
	
}
