package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
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
	 * SiegeStatus will already have been set
	 *
	 * @param siege the siege
	 */
    public static void defenderWin(Siege siege) {
    	siege.setSiegeWinner(SiegeSide.DEFENDERS);
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege);
		if(siege.getSiegeType() == SiegeType.CONQUEST) {
			if(siege.getStatus() == SiegeStatus.DEFENDER_WIN) {
				SiegeWarMoneyUtil.giveWarChestToWinner(siege, siege.getTown());
			} else {
				SiegeWarMoneyUtil.giveWarChestToBoth(siege, siege.getTown(), siege.getAttacker());
			}
		}
    }

}
