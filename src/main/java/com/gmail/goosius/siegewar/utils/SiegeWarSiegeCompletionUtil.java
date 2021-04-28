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
		SiegeWarImmunityUtil.grantSiegeImmunityAfterEndedSiege(siege.getTown(), siege);

		SiegeWarTownUtil.setPvpFlag(siege.getTown(), false);
		CosmeticUtil.removeFakeBeacons(siege);
		/*
		 * The siege is now historical rather than active.
		 * Thus, fix the attacker and defender as nations where possible,
		 * rather than towns.
		 *
		 * Thus, even if the town switches nation after the siege,
		 *   the correct historical nation participants will still be shown.
		 */
		siege.setAttacker(siege.getAttackingNationIfPossibleElseTown());
		siege.setDefender(siege.getDefendingNationIfPossibleElseTown());

		//Save to db
		SiegeController.saveSiege(siege);
	}
}
