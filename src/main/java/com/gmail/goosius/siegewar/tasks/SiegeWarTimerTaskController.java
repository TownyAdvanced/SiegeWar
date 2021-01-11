package com.gmail.goosius.siegewar.tasks;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.timeractions.AttackerWin;
import com.gmail.goosius.siegewar.timeractions.DefenderWin;
import com.gmail.goosius.siegewar.utils.*;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.util.TimeMgmt;

/**
 * This class intercepts siege related instructions coming from timer tasks.
 * and takes action as appropriate
 *
 * @author Goosius
 */
public class SiegeWarTimerTaskController {

	/**
	 * Evaluate timed siege outcomes
	 * e.g. who wins if siege victory timer runs out ?
	 */
	public static void evaluateTimedSiegeOutcomes() {
		for (Siege siege : SiegeController.getSieges()) {
			evaluateTimedSiegeOutcome(siege);
		}
	}

	/**
	 * Evaluate the timed outcome of 1 siege
	 *
	 * @param siege
	 */
	private static void evaluateTimedSiegeOutcome(Siege siege) {
		switch(siege.getStatus()) {
			case IN_PROGRESS:
				//If scheduled end time has arrived, choose winner
				if (System.currentTimeMillis() > siege.getScheduledEndTime()) {
					TownyObject siegeWinner = SiegeWarPointsUtil.calculateSiegeWinner(siege);
					if (siegeWinner instanceof Town) {
						DefenderWin.defenderWin(siege, (Town) siegeWinner);
					} else {
						AttackerWin.attackerWin(siege, (Nation) siegeWinner);
					}

					//Save changes to db
					TownyUniverse.getInstance().getDataSource().saveTown(siege.getDefendingTown());
				}
				break;

			case PENDING_DEFENDER_SURRENDER:
				if(siege.getDurationMillis() > (SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeSurrenderHours() * TimeMgmt.ONE_HOUR_IN_MILLIS )) {
					SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_SURRENDER);
					SiegeWarMoneyUtil.giveWarChestToAttackingNation(siege);
				}
				break;

			case PENDING_ATTACKER_ABANDON:
				if(siege.getDurationMillis() > (SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeAbandonHours() * TimeMgmt.ONE_HOUR_IN_MILLIS )) {
					SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.ATTACKER_ABANDON);
					SiegeWarMoneyUtil.giveWarChestToDefendingTown(siege);
				}
				break;

			default:
				//Siege is inactive
				//Wait for siege immunity timer to end then delete siege
				if (System.currentTimeMillis() > TownMetaDataController.getSiegeImmunityEndTime(siege.getDefendingTown())) {
					SiegeController.removeSiege(siege, SiegeSide.NOBODY);
				}
		}
	}

	/**
	 * Evaluate the visibility of players on the dynmap
	 * when using the 'tactical visibility' feature
	 */
	public static void evaluateTacticalVisibility() {
		if (SiegeWarSettings.getWarSiegeTacticalVisibilityEnabled()) {
			SiegeWarDynmapUtil.evaluatePlayerTacticalInvisibility();
		}
	}

	/**
	 * Evaluate banner control for all sieges
	 */
	public static void evaluateBannerControl() {
		for (Siege siege : SiegeController.getSieges()) {
			SiegeWarBannerControlUtil.evaluateBannerControl(siege);
		}
	}

	public static void updatePopulationBasedSiegePointModifiers() {
		if(SiegeWarSettings.getWarSiegePopulationBasedPointBoostsEnabled()) {
			SiegeWarPointsUtil.updatePopulationBasedSiegePointModifiers();
		}
	}

	public static void evaluateBattleSessions() {
		if (SiegeWarSettings.isWarSiegeBattleSessionsEnabled()) {
			SiegeWarBattleSessionUtil.evaluateBattleSessions();
		}
	}

	public static void punishPeacefulPlayersInActiveSiegeZones() {
		if(SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
			TownPeacefulnessUtil.punishPeacefulPlayersInActiveSiegeZones();
		}
	}
}
