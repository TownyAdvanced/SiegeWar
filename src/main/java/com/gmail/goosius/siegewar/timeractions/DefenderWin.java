package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;

/**
 * This class is responsible for processing siege defender wins
 *
 * @author Goosius
 */
public class DefenderWin
{
	/**
	 * This method triggers siege values to be updated for a defender win
	 *
	 * @param siege the siege
	 * @param siegeStatus the siege status
	 */
    public static void defenderWin(Siege siege, SiegeStatus siegeStatus) {
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege, siegeStatus);
		switch(siege.getSiegeType()) {
			case CONQUEST:
			case LIBERATION:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefender());
				break;
			case SUPPRESSION:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefender());
				TownOccupationController.removeTownOccupation(siege.getTown());
				break;
			case REVOLT:
				SiegeWarTimeUtil.activateRevoltImmunityTimer(siege.getTown());
				break;
		}
    }
}
