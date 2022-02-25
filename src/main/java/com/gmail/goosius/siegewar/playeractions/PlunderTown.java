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
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;
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
	 * @param player the player who placed the plunder chest
	 * @param townToBePlundered the town to be plundered
	 * @throws TownyException when a plunder is not allowed.
	 */
    public static void processPlunderTownRequest(Player player,
												 Town townToBePlundered) throws TownyException {
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		if (!townyUniverse.getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_PLUNDER.getNode()))
			throw new TownyException(Translation.of("msg_err_command_disable"));

		Resident resident = townyUniverse.getResident(player.getUniqueId());
        if (resident == null)
        	throw new TownyException(Translation.of("msg_err_not_registered_1", player.getName()));
        
		if(!resident.hasTown())
			throw new TownyException(Translation.of("msg_err_siege_war_action_not_a_town_member"));

		Town townOfPlunderingResident = resident.getTown();
		if(!townOfPlunderingResident.hasNation())
			throw new TownyException(Translation.of("msg_err_siege_war_action_not_a_nation_member"));

		Siege siege = SiegeController.getSiege(townToBePlundered);
		if(siege.isTownPlundered())
			throw new TownyException(String.format(Translation.of("msg_err_siege_war_town_already_plundered"), townToBePlundered.getName()));

		Nation nationOfPlunderingResident = townOfPlunderingResident.getNation();

		if(siege.getSiegeType() == SiegeType.REVOLT) {
			if(siege.getStatus() == SiegeStatus.ATTACKER_WIN
				|| siege.getStatus() == SiegeStatus.DEFENDER_SURRENDER) {
				throw new TownyException(Translation.of("msg_err_siege_war_plunder_not_possible_rebels_won"));
			}
			if(siege.getStatus() != SiegeStatus.DEFENDER_WIN
				&& siege.getStatus() != SiegeStatus.ATTACKER_ABANDON) {
				throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_without_victory"));
			}
			if(nationOfPlunderingResident != siege.getDefender())
				throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_without_victory"));

			plunderTown(siege, townToBePlundered, (Nation)siege.getDefender());
		} else {
			if(siege.getStatus() != SiegeStatus.ATTACKER_WIN
				&& siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER) {
				throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_without_victory"));
			}
			if(nationOfPlunderingResident != siege.getAttacker())
				throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_without_victory"));

			plunderTown(siege, townToBePlundered, (Nation)siege.getAttacker());
		}
    }

    private static void plunderTown(Siege siege, Town town, Nation nation) {
		boolean townNewlyBankrupted = false;
		boolean townDestroyed = false;

		double totalPlunderAmount =
				SiegeWarSettings.getWarSiegeAttackerPlunderAmountPerPlot()
				* town.getTownBlocks().size()
				* SiegeWarMoneyUtil.getMoneyMultiplier(town);
		
		//Redistribute money
		if(town.getAccount().canPayFromHoldings(totalPlunderAmount)) {
			//Town can afford plunder
			transferPlunderToNation(siege, nation, totalPlunderAmount, true);

			NationMetaDataController.setTotalPlunderGained(nation, NationMetaDataController.getTotalPlunderGained(nation) + (int) totalPlunderAmount);
			if (town.hasNation()) {
				Nation townNation = TownyAPI.getInstance().getTownNationOrNull(town);
				NationMetaDataController.setTotalPlunderLost(townNation, NationMetaDataController.getTotalPlunderLost(townNation) + (int) totalPlunderAmount);
			}
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
				if (town.getAccount().getHoldingBalance() - totalPlunderAmount < town.getAccount().getDebtCap() * -1) {
					if(town.getAccount().getHoldingBalance() > 0) {
						totalPlunderAmount = town.getAccount().getDebtCap() + Math.abs(town.getAccount().getHoldingBalance());
					} else {
						totalPlunderAmount = town.getAccount().getDebtCap() - Math.abs(town.getAccount().getHoldingBalance());
						
					}
				}
				// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
				town.getAccount().withdraw(totalPlunderAmount, "Plunder by " + nation.getName());
				// And deposit it into the nation.
				transferPlunderToNation(siege, nation, totalPlunderAmount, false);

			} else {
				// Not able to go bankrupt, they are destroyed, pay what they can.
				totalPlunderAmount = town.getAccount().getHoldingBalance();
				transferPlunderToNation(siege, nation, totalPlunderAmount, true);
				townDestroyed = true;
			}
		}

		//Set siege plundered flag
		siege.setTownPlundered(true);

		//Save data
		if(townDestroyed) {
			TownyUniverse.getInstance().getDataSource().removeTown(town);
		} else {
			SiegeController.saveSiege(siege);
		}

		//Send plunder success messages
		if (town.hasNation()) {
			Messaging.sendGlobalMessage(
				Translation.of("msg_siege_war_nation_town_plundered",
				town.getName(),
				TownyEconomyHandler.getFormattedBalance(totalPlunderAmount),
				nation.getName()
			));
		} else {
			Messaging.sendGlobalMessage(
				Translation.of("msg_siege_war_neutral_town_plundered",
				town.getName(),
				TownyEconomyHandler.getFormattedBalance(totalPlunderAmount),
				nation.getName()
			));
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
