package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;

/**
 * This class contains utility functions related to completing sieges
 *
 * @author Goosius
 */
public class SiegeWarSiegeCompletionUtil {

	/**
	 * This method sets common values in the siege & town objects,
	 * to reflect siege completion
	 * 
	 * @param siege siege
	 * @param siegeStatus the new status of the siege
	 */
	public static void setCommonSiegeCompletionValues(Siege siege,
													  SiegeStatus siegeStatus) {
		//Update values
		siege.setStatus(siegeStatus);
		siege.setAttackerBattlePoints(0);
		siege.setDefenderBattlePoints(0);
		siege.setBannerControllingSide(SiegeSide.NOBODY);
		siege.clearBannerControllingResidents();
		siege.clearBannerControlSessions();
		siege.setActualEndTime(System.currentTimeMillis());
		SiegeWarTimeUtil.activateSiegeImmunityTimers(siege.getTown(), siege);
		if(SiegeWarSettings.isHomeDefenceSiegeEffectsEnabled() && siege.getTown().hasNation()) {
			SiegeWarTownUtil.setPvpFlagsOfAllNationHomeTowns(siege.getTown(), false);
		} else {
			SiegeWarTownUtil.setTownPvpFlags(siege.getTown(), false);
		}
		CosmeticUtil.removeFakeBeacons(siege);

		//Save to db
		SiegeController.saveSiege(siege);
	}
}
