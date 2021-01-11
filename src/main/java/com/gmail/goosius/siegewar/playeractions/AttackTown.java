package com.gmail.goosius.siegewar.playeractions;


import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.block.Block;

/**
 * This class is responsible for processing requests to start siege attacks
 *
 * @author Goosius
 */
public class AttackTown {

	/**
	 * Process an attack town request
	 *
	 * This method does some final checks and if they pass, the attack is initiated.
	 *
	 * @param townOfAttackingPlayer town which is attacking.
	 * @param nationOfAttackingPlayer nation which is attacking.
	 * @param block the attack banner 
	 * @param townBlock the townblock where the attack is taking place.
	 * @param defendingTown the town about to be attacked
	 * @throws TownyException when attack cannot be made.
	 */
    public static void processAttackTownRequest(Town townOfAttackingPlayer,
    											Nation nationOfAttackingPlayer,
                                                Block block,
                                                TownBlock townBlock,
                                                Town defendingTown) throws TownyException {

		if (SiegeController.hasActiveSiege(defendingTown))
			throw new TownyException(Translation.of("msg_err_siege_war_cannot_join_siege"));

		if (defendingTown.isNeutral())
			throw new TownyException(Translation.of("msg_war_siege_err_cannot_attack_peaceful_town"));

		if (!(SiegeController.hasActiveSiege(defendingTown))
            	&& System.currentTimeMillis() < TownMetaDataController.getSiegeImmunityEndTime(defendingTown))
            throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_siege_immunity"));

		if (defendingTown.isRuined())
            throw new TownyException(Translation.of("msg_err_cannot_attack_ruined_town"));
		
        if (defendingTown.hasNation()) {
            Nation nationOfDefendingTown = defendingTown.getNation();

            if (nationOfAttackingPlayer == nationOfDefendingTown)
                throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_town_in_own_nation"));

            if (!nationOfAttackingPlayer.hasEnemy(nationOfDefendingTown))
                throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_non_enemy_nation"));
        }

        if (!SiegeWarDistanceUtil.isUndergroundBannerControlEnabledInWorld(block.getWorld()) && SiegeWarDistanceUtil.doesLocationHaveANonAirBlockAboveIt(block.getLocation()))
            throw new TownyException(Translation.of("msg_err_siege_war_banner_must_be_placed_above_ground"));

        if(!SiegeWarDistanceUtil.isBannerToTownElevationDifferenceOk(block, townBlock)) {
			throw new TownyException(Translation.of("msg_err_siege_war_cannot_place_banner_far_above_town"));
		}

		if (TownySettings.getNationRequiresProximity() > 0) {
			Coord capitalCoord = nationOfAttackingPlayer.getCapital().getHomeBlock().getCoord();
			Coord townCoord = defendingTown.getHomeBlock().getCoord();
			if (!nationOfAttackingPlayer.getCapital().getHomeBlock().getWorld().getName().equals(defendingTown.getHomeBlock().getWorld().getName())) {
				throw new TownyException(Translation.of("msg_err_nation_homeblock_in_another_world"));
			}
			double distance;
			distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
			if (distance > TownySettings.getNationRequiresProximity()) {
				throw new TownyException(Translation.of("msg_err_siege_war_town_not_close_enough_to_nation"));
			}
		}

        //Setup attack
        attackTown(block, nationOfAttackingPlayer, defendingTown);


    }


    private static void attackTown(Block block, Nation attackingNation, Town defendingTown) throws TownyException {
		//Create Siege
		String siegeName = attackingNation.getName() + "#vs#" + defendingTown.getName();
		TownyUniverse universe = TownyUniverse.getInstance();
		SiegeController.newSiege(siegeName);
		Siege siege = SiegeController.getSiege(siegeName);
		
		//Set values in siege object
		siege.setAttackingNation(attackingNation);
		siege.setDefendingTown(defendingTown);
		siege.setStatus(SiegeStatus.IN_PROGRESS);
		siege.setTownPlundered(false);
		siege.setTownInvaded(false);
		siege.setStartTime(System.currentTimeMillis());
		siege.setScheduledEndTime(
			(System.currentTimeMillis() +
				((long) (SiegeWarSettings.getWarSiegeMaxHoldoutTimeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS))));
		siege.setActualEndTime(0);
		siege.setFlagLocation(block.getLocation());
		siege.setWarChestAmount(SiegeWarMoneyUtil.getSiegeCost(defendingTown));
		
		SiegeController.setSiege(defendingTown, true);
		SiegeController.putTownInSiegeMap(defendingTown, siege);

		//Set town pvp and explosions to true.
		defendingTown.getPermissions().explosion = true;
		defendingTown.getPermissions().pvp = true;
		
		//Pay into warchest
		if (TownySettings.isUsingEconomy()) {
			try {
				//Pay upfront cost into warchest now
				attackingNation.getAccount().withdraw(siege.getWarChestAmount(), "Cost of starting a siege.");
				String moneyMessage =
					Translation.of("msg_siege_war_attack_pay_war_chest",
					attackingNation.getFormattedName(),
					TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));

				TownyMessaging.sendPrefixedNationMessage(attackingNation, moneyMessage);
				TownyMessaging.sendPrefixedTownMessage(defendingTown, moneyMessage);
			} catch (EconomyException e) {
				System.out.println("Problem paying into war chest");
				e.printStackTrace();
			}
		}

		//Save to DB
		SiegeController.saveSiege(siege);
		SiegeController.addSiegedTown(siege);
		universe.getDataSource().saveNation(attackingNation);
		universe.getDataSource().saveTown(defendingTown);

		//Send global message;
		if (siege.getDefendingTown().hasNation()) {
			Messaging.sendGlobalMessage(String.format(
				Translation.of("msg_siege_war_siege_started_nation_town"),
				attackingNation.getFormattedName(),
				defendingTown.getNation().getFormattedName(),
				defendingTown.getFormattedName()
			));
		} else {
			Messaging.sendGlobalMessage(String.format(
				Translation.of("msg_siege_war_siege_started_neutral_town"),
				attackingNation.getFormattedName(),
				defendingTown.getFormattedName()
			));
		}
    }
}
