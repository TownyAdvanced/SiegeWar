package com.gmail.goosius.siegewar.tasks;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.playeractions.AbandonAttack;
import com.gmail.goosius.siegewar.playeractions.SurrenderDefence;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBannerControlUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarBattleSessionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSicknessUtil;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;

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
				//If last battle session has been completed, end siege and choose winner
				if (siege.getNumBattleSessionsCompleted() >= SiegeWarSettings.getSiegeDurationBattleSessions())
					SiegeController.endSiegeWithTimedWin(siege);
				break;

			case PENDING_DEFENDER_SURRENDER:
				if (siege.getNumBattleSessionsCompleted() >= SiegeWarSettings.getSiegeDurationBattleSessions())
					SurrenderDefence.surrenderDefence(siege);
				break;

			case PENDING_ATTACKER_ABANDON:
				if (siege.getNumBattleSessionsCompleted() >= SiegeWarSettings.getSiegeDurationBattleSessions())
					AbandonAttack.abandonAttack(siege);
				break;

			default:
				//Siege is inactive i.e. in the 'aftermath' phase
				//Wait for siege immunity timer to end then delete siege
				if (System.currentTimeMillis() > TownMetaDataController.getSiegeImmunityEndTime(siege.getTown())) {
					SiegeController.removeSiege(siege);
				}
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

	public static void evaluateBattleSessions() {
		SiegeWarBattleSessionUtil.evaluateBattleSessions();
	}


	public static void evaluateWarSickness() {
		SiegeWarSicknessUtil.evaluateWarSickness();
	}

	public static void evaluateBeacons() {
		if (SiegeWarSettings.getBeaconsEnabled())
			CosmeticUtil.evaluateBeacons();
	}
}
