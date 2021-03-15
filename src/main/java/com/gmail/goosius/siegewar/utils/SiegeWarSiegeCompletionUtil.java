package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;

/**
 * This class contains utility functions related to completing sieges
 *
 * @author Goosius
 */
public class SiegeWarSiegeCompletionUtil {

	/**
	 * This method adjusts siege values, depending on the new status, and who has won.
	 * 
	 * @param siege siege
	 * @param siegeStatus the new status of the siege
	 */
	public static void updateSiegeValuesToComplete(Siege siege,
												   SiegeStatus siegeStatus) {
		//Update values
		siege.setStatus(siegeStatus);
		siege.setAttackerBattlePoints(0);
		siege.setDefenderBattlePoints(0);
		siege.setBannerControllingSide(SiegeSide.NOBODY);
		siege.clearBannerControllingResidents();
		siege.clearBannerControlSessions();
		siege.setActualEndTime(System.currentTimeMillis());
		SiegeWarTimeUtil.activateSiegeImmunityTimer(siege.getTown(), siege);
		if (siegeStatus == SiegeStatus.DEFENDER_SURRENDER || siegeStatus == SiegeStatus.ATTACKER_WIN) {
			SiegeWarTimeUtil.activateRevoltImmunityTimer(siege.getTown()); //Prevent immediate revolt
		}
		SiegeWarTownUtil.setTownPvpFlags(siege.getTown(), false);
		CosmeticUtil.removeFakeBeacons(siege);

		//Save to db
		SiegeController.saveSiege(siege);
	}
}
