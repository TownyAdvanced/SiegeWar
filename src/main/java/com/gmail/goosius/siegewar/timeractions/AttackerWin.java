package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
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
	 * 
	 * @param siege the siege
	 * @param siegeStatus the siege status
	 */
	public static void attackerWin(Siege siege, SiegeStatus siegeStatus) {
		siege.setSiegeWinner(SiegeSide.ATTACKERS);
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege, siegeStatus);
		switch(siege.getSiegeType()) {
			case CONQUEST:
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getAttacker());
				break;
			case REVOLT:
				TownOccupationController.removeTownOccupation(siege.getTown());
				break;
		}
    }

}
