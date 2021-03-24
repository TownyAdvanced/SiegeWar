package com.gmail.goosius.siegewar.tasks;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.playeractions.SurrenderDefence;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.timeractions.AttackerTimedWin;
import com.gmail.goosius.siegewar.timeractions.DefenderTimedWin;
import com.gmail.goosius.siegewar.utils.*;

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
					if (siege.getSiegeBalance() > 0) {
						AttackerTimedWin.attackerTimedWin(siege);
					} else {
						DefenderTimedWin.defenderTimedWin(siege);
					}
				}
				break;

			case PENDING_DEFENDER_SURRENDER:
				if(siege.getTimeUntilSurrenderConfirmationMillis() < 0)
					SurrenderDefence.surrenderDefence(siege, 0);
				break;

			case PENDING_ATTACKER_ABANDON:
				if(siege.getTimeUntilAbandonConfirmationMillis() < 0)
					SurrenderDefence.surrenderDefence(siege, 0);
				break;

			default:
				//Siege is inactive i.e. in the 'aftermath' phase
				//Wait for siege immunity timer to end then delete siege
				if (System.currentTimeMillis() > TownMetaDataController.getSiegeImmunityEndTime(siege.getTown())) {
					SiegeController.removeSiege(siege, SiegeSide.NOBODY);
				}
		}
	}

	/**
	 * Evaluate the visibility of players on the dynmap
	 * when using the 'map sneaking' feature
	 */
	public static void evaluateMapSneaking() {
		if (SiegeWarSettings.getWarSiegeMapSneakingEnabled()) {
			SiegeWarDynmapUtil.evaluatePlayerMapSneaking();
		}
	}

	/**
	 * Evaluate banner control for all sieges
	 */
	public static void evaluateBannerControl() {
		if(BattleSession.getBattleSession().isActive()) {
			for (Siege siege : SiegeController.getSieges()) {
				SiegeWarBannerControlUtil.evaluateBannerControl(siege);
			}
		}
	}

	public static void updatePopulationBasedBattlePointModifiers() {
		if(SiegeWarSettings.getWarSiegePopulationBasedPointBoostsEnabled()) {
			SiegeWarScoringUtil.updatePopulationBasedBattlePointModifiers();
		}
	}

	public static void evaluateBattleSessions() {
		SiegeWarBattleSessionUtil.evaluateBattleSessions();
	}

	public static void punishPeacefulPlayersInActiveSiegeZones() {
		if(SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
			TownPeacefulnessUtil.punishPeacefulPlayersInActiveSiegeZones();
		}
	}

	public static void punishNonSiegeParticipantsInSiegeZone() {
		if (SiegeWarSettings.getPunishingNonSiegeParticipantsInSiegeZone()) {
			SiegeWarSicknessUtil.punishNonSiegeParticipantsInSiegeZone();
		}
	}

	public static void evaluateCannonSessions() {
		if(SiegeWar.getCannonsPluginIntegrationEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			SiegeWarCannonsUtil.evaluateCannonSessions();
		}
	}

	public static void evaluateBeacons() {
		if (SiegeWarSettings.getBeaconsEnabled())
			CosmeticUtil.evaluateBeacons();
	}
}
