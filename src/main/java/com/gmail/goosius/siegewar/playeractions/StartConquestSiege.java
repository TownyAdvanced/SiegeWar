package com.gmail.goosius.siegewar.playeractions;


import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSiegeWarStartEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to start siege attacks
 *
 * @author Goosius
 */
public class StartConquestSiege {

	/**
	 * Process an attack town request
	 *
	 * This method does some final checks and if they pass, the attack is initiated.
	 *
	 * @param player the player
	 * @param townOfSiegeStarter town
	 * @param nationOfSiegeStarter nation which is attacking.
	 * @param townBlock the townblock where the attack is taking place.
	 * @param targetTown the town about to be attacked
	 * @param bannerBlock the banner block
	 *
	 * @throws TownyException when attack cannot be made.
	 *
	 */
    public static void processStartRequest(Player player,
										   Town townOfSiegeStarter,
										   Nation nationOfSiegeStarter,
										   TownBlock townBlock,
										   Town targetTown,
										   Block bannerBlock) throws TownyException {

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToStartSiege(SiegeType.CONQUEST)))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		if (targetTown.hasNation()) {
            Nation nationOfDefendingTown = targetTown.getNation();

            if (nationOfSiegeStarter == nationOfDefendingTown)
                throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_town_in_own_nation"));

            if (!nationOfSiegeStarter.hasEnemy(nationOfDefendingTown))
                throw new TownyException(Translation.of("msg_err_siege_war_cannot_attack_non_enemy_nation"));
        }

		if (TownySettings.getNationRequiresProximity() > 0) {
			Coord capitalCoord = nationOfSiegeStarter.getCapital().getHomeBlock().getCoord();
			Coord townCoord = targetTown.getHomeBlock().getCoord();
			if (!nationOfSiegeStarter.getCapital().getHomeBlock().getWorld().getName().equals(targetTown.getHomeBlock().getWorld().getName())) {
				throw new TownyException(Translation.of("msg_err_nation_homeblock_in_another_world"));
			}
			double distance;
			distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
			if (distance > TownySettings.getNationRequiresProximity()) {
				throw new TownyException(Translation.of("msg_err_siege_war_town_not_close_enough_to_nation"));
			}
		}

		//Call event
		PreSiegeWarStartEvent preSiegeWarStartEvent = new PreSiegeWarStartEvent(townOfSiegeStarter, nationOfSiegeStarter, bannerBlock, townBlock, targetTown);
		Bukkit.getPluginManager().callEvent(preSiegeWarStartEvent);

		//Setup attack
		if (!preSiegeWarStartEvent.isCancelled()){
			startSiege(bannerBlock, nationOfSiegeStarter, townOfSiegeStarter, targetTown);
		} else {
			throw new TownyException(preSiegeWarStartEvent.getCancellationMsg());
		}
    }

    private static void startSiege(Block bannerBlock, Nation attackingNation, Town attackingTown, Town defendingTown) throws TownyException {
		//Create Siege
		SiegeController.newSiege(defendingTown);
		Siege siege = SiegeController.getSiege(defendingTown);
		
		//Set values in siege object
		siege.setNation(attackingNation);
		siege.setTown(defendingTown);
		siege.setStatus(SiegeStatus.IN_PROGRESS);
		siege.setTownPlundered(false);
		siege.setTownInvaded(false);
		siege.setStartTime(System.currentTimeMillis());
		siege.setScheduledEndTime(
			(System.currentTimeMillis() +
				((long) (SiegeWarSettings.getWarSiegeMaxHoldoutTimeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS))));
		siege.setActualEndTime(0);
		siege.setFlagLocation(bannerBlock.getLocation());
		siege.setWarChestAmount(SiegeWarMoneyUtil.getSiegeCost(defendingTown));
		
		SiegeController.setSiege(defendingTown, true);
		SiegeController.putTownInSiegeMap(defendingTown, siege);

		//Set town pvp and explosions to true.
		SiegeWarTownUtil.setTownPvpFlags(defendingTown, true);
		
		//Pay into warchest
		if (TownyEconomyHandler.isActive()) {
			//Pay upfront cost into warchest now
			attackingNation.getAccount().withdraw(siege.getWarChestAmount(), "Cost of starting a siege.");
			String moneyMessage =
				Translation.of("msg_siege_war_attack_pay_war_chest",
				attackingNation.getFormattedName(),
				TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));

			TownyMessaging.sendPrefixedNationMessage(attackingNation, moneyMessage);
			TownyMessaging.sendPrefixedTownMessage(defendingTown, moneyMessage);
		}

		//Save to DB
		SiegeController.saveSiege(siege);
		//SiegeController.addSiegedTown(siege);
		attackingNation.save();

		//Send global message;
		if (siege.getTown().hasNation()) {
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

		//Call event
		Bukkit.getPluginManager().callEvent(new SiegeWarStartEvent(siege, attackingTown, bannerBlock));
    }
}
