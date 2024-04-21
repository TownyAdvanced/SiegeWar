package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.events.SiegeEndEvent;
import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.Bukkit;

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
	 */
	public static void setCommonSiegeCompletionValues(Siege siege) {
		//Update values
		SiegeWarImmunityUtil.grantSiegeImmunityAfterEndedSiege(siege.getTown());
		SiegeWarImmunityUtil.grantRevoltImmunityAfterEndedSiege(siege.getTown());
		CosmeticUtil.removeFakeBeacons(siege);
		/*
		 * The siege is now historical rather than active.
		 * 
		 * To ensure that the correct historical participants are always displayed,
		 * we now set the attackerName and defenderName String variables,
		 * and use them to display the participants.
		 *
		 * These 2 variables will not change.
		 */
		siege.setAttackerName(siege.getAttackingNationIfPossibleElseTown().getName());
		siege.setDefenderName(siege.getDefendingNationIfPossibleElseTown().getName());

		//Save to db
		SiegeController.saveSiege(siege);

		//Do this before event so a plugin could edit in event
		SiegeWarBannerLoreUtil.endSiege(siege);

		//Fire SiegeEnded event
		Bukkit.getPluginManager().callEvent(new SiegeEndEvent(siege));
	}
}
