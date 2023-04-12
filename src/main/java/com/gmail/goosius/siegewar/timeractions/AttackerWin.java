package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;

/**
 * This class is responsible for processing all types of attacker wins
 *
 * @author Goosius
 */
public class AttackerWin {

	/**
	 * This method sets up the attacker as the siege winner
	 * SiegeStatus will already have been set
	 * 
	 * @param siege the siege
	 */
	public static void attackerWin(Siege siege) {
		siege.setSiegeWinner(SiegeSide.ATTACKERS);
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege);
		if(siege.getSiegeType() == SiegeType.CONQUEST) {
			if(siege.getStatus() == SiegeStatus.ATTACKER_DECISIVE_WIN || siege.getStatus() == SiegeStatus.DEFENDER_SURRENDER) {
				SiegeWarMoneyUtil.giveWarChestToWinner(siege, siege.getAttacker());
			} else {
				SiegeWarMoneyUtil.giveWarChestToBoth(siege, siege.getAttacker(), siege.getTown());
			}
		}
    }

}
