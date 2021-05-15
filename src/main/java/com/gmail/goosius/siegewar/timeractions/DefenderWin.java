package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarImmunityUtil;

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
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefendingNationIfPossibleElseTown());
				break;
			case LIBERATION:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefender());
				break;
			case SUPPRESSION:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefendingNationIfPossibleElseTown());
				TownOccupationController.removeTownOccupation(siege.getTown());
				break;
			case REVOLT:
				SiegeWarImmunityUtil.activateRevoltImmunityTimer(siege.getTown());
				break;
		}
    }

}
