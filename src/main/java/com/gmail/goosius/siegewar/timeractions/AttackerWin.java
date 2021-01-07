package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.palmergames.bukkit.towny.object.Nation;
import com.gmail.goosius.siegewar.settings.Translation;

/**
 * This class is responsible for processing siege attacker wins
 *
 * @author Goosius
 */
public class AttackerWin {

	/**
	 * This method triggers siege values to be updated for an attacker win
	 * 
	 * @param siege the siege
	 * @param winnerNation the winning nation
	 */
	public static void attackerWin(Siege siege, Nation winnerNation) {
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.ATTACKER_WIN);

		Messaging.sendGlobalMessage(Translation.of("msg_siege_war_attacker_win", 
		    winnerNation.getFormattedName(),
			siege.getDefendingTown().getFormattedName()
		));

		SiegeWarMoneyUtil.giveWarChestToAttackingNation(siege);
    }
}
