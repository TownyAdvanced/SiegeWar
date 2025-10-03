package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.enums.SiegeSide;
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
		SiegeWarMoneyUtil.handleWarChest(siege, siege.getAttacker());
	}
}
