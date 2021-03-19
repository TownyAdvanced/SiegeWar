package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

		TownyUniverse universe = TownyUniverse.getInstance();
		Resident resident = universe.getResident(player.getUniqueId());
        if (resident == null)
        	throw new TownyException(Translation.of("msg_err_not_registered_1", player.getName()));
        
		if(!resident.hasTown())
			throw new TownyException(Translation.of("msg_err_siege_war_action_not_a_town_member"));

		Town townOfPlunderingResident = resident.getTown();
		if(!townOfPlunderingResident.hasNation())
			throw new TownyException(Translation.of("msg_err_siege_war_action_not_a_nation_member"));


		if(townOfPlunderingResident == townToBePlundered)
			throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_own_town"));

		Siege siege = SiegeController.getSiege(townToBePlundered);
		if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
			throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_without_victory"));
		
		if(townOfPlunderingResident.getNation() != siege.getNation())
			throw new TownyException(Translation.of("msg_err_siege_war_cannot_plunder_without_victory"));
		
        if(siege.isTownPlundered())
            throw new TownyException(String.format(Translation.of("msg_err_siege_war_town_already_plundered"), townToBePlundered.getName()));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_PLUNDER.getNode()))
            throw new TownyException(Translation.of("msg_err_command_disable"));
        
        plunderTown(siege, townToBePlundered, siege.getNation());

    }

    private static void plunderTown(Siege siege, Town town, Nation nation) {
		boolean townNewlyBankrupted = false;
		boolean townDestroyed = false;

		double totalPlunderAmount =
				SiegeWarSettings.getWarSiegeAttackerPlunderAmountPerPlot()
				* town.getTownBlocks().size()
				* SiegeWarMoneyUtil.getMoneyMultiplier(town);
		
		try {
			//Redistribute money
			if(town.getAccount().canPayFromHoldings(totalPlunderAmount)) {
				//Town can afford plunder
				transferPlunderToSiegeAttackers(siege, totalPlunderAmount, true);

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
					if (town.getAccount().getHoldingBalance() - totalPlunderAmount < town.getAccount().getDebtCap() * -1)
						totalPlunderAmount = town.getAccount().getDebtCap() - Math.abs(town.getAccount().getHoldingBalance());
						
					// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
					town.getAccount().withdraw(totalPlunderAmount, "Plunder by " + nation.getName());
					// And deposit it into the nation.
					transferPlunderToSiegeAttackers(siege, totalPlunderAmount, false);

				} else {
					// Not able to go bankrupt, they are destroyed, pay what they can.
					totalPlunderAmount = town.getAccount().getHoldingBalance();
					transferPlunderToSiegeAttackers(siege, totalPlunderAmount, true);
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

	private static void transferPlunderToSiegeAttackers(Siege siege, double totalPlunderAmount, boolean removeMoneyFromTownBank) throws EconomyException {
		Town town = siege.getTown();
		Nation nation = siege.getNation();

		String distributionRatio = SiegeWarSettings.getWarSiegePlunderDistributionRatio();

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
		Resident resident;
		Map<Resident, Integer> residentSharesMap = new HashMap<>();
		for(Map.Entry<String, Integer> uuidShareMapEntry: siege.getAttackerSiegeContributors().entrySet()) {
			resident = TownyUniverse.getInstance().getResident(UUID.fromString(uuidShareMapEntry.getKey()));
			if(resident != null) {
				residentSharesMap.put(resident, uuidShareMapEntry.getValue());
			}
		}
		boolean soldiersPaid = SiegeWarMoneyUtil.distributeMoneyAmongSoldiers(
				totalPlunderForSoldiers,
				town,
				residentSharesMap,
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
}
