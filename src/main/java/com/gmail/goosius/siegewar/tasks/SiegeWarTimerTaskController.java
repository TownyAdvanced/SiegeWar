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
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;

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
	 * 
	 * A maximum of 1 siege will be ended when this method is called.
	 */
	public static void evaluateTimedSiegeOutcomes() {
		for (Siege siege : SiegeController.getSieges()) {
			if(evaluateTimedSiegeOutcome(siege))
				return;
		}
	}

	/**
	 * Evaluate the timed outcome of 1 siege
	 *
	 * @param siege the siege
	 * @return true if the siege was ended by this method call      	   	
	 */
	private static boolean evaluateTimedSiegeOutcome(Siege siege) {
		switch(siege.getStatus()) {
			case IN_PROGRESS:
				//If last battle session has been completed, end siege and choose winner
				if (siege.hasCompletedAllBattleSessions()) {
					SiegeController.endSiegeWithTimedWin(siege);
					return true;
				} else
					return false;

			case PENDING_DEFENDER_SURRENDER:
				if (siege.hasCompletedAllBattleSessions()) {
					SurrenderDefence.surrenderDefence(siege);
					return true;
				} else 
					return false;

			case PENDING_ATTACKER_ABANDON:
				if (siege.hasCompletedAllBattleSessions()) {
					AbandonAttack.abandonAttack(siege);
					return true;
				} else 
					return false;

			default:
				//Siege is inactive i.e. in the 'aftermath' phase
				//Wait for siege immunity timer to end then delete siege
				if (System.currentTimeMillis() > TownMetaDataController.getSiegeImmunityEndTime(siege.getTown())) {
					SiegeController.removeSiege(siege);
				}
				return false;
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


	public static void evaluateWarSickness() throws TownyException {
		SiegeWarSicknessUtil.evaluateWarSickness();
	}

	public static void evaluateBeacons() {
		if (SiegeWarSettings.getBeaconsEnabled())
			CosmeticUtil.evaluateBeacons();
	}
}
