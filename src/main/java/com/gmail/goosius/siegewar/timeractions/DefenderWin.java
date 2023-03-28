package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;

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
    	siege.setSiegeWinner(SiegeSide.DEFENDERS);
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege, siegeStatus);
		switch(siege.getSiegeType()) {
			case CONQUEST:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefendingNationIfPossibleElseTown());
				break;
			case REVOLT:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefender());
				break;
		}
    }

}
