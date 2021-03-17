package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;

/**
 * This class is responsible for processing all types of attacker wins
 *
 * @author Goosius
 */
public class AttackerWin {

	/**
	 * This method sets up the attacker as the siege winner
	 * 
	 * @param siege the siege
	 * @param siegeStatus the siege status
	 */
	public static void attackerWin(Siege siege, SiegeStatus siegeStatus) {
		switch(siege.getSiegeType()) {
			case CONQUEST:
			case SUPPRESSION:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getAttacker());
				SiegeWarTimeUtil.activateRevoltImmunityTimer(siege.getTown());
				break;
			case LIBERATION:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getAttacker());
				TownOccupationController.removeTownOccupation(siege.getTown());
				break;
			case REVOLT:
				TownOccupationController.removeTownOccupation(siege.getTown());
				break;
		}
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege, siegeStatus);
    }

}
