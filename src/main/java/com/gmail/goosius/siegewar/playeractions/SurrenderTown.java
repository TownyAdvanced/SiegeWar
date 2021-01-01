package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;

/**
 * This class is responsible for processing requests to surrender towns
 *
 * @author Goosius
 */
public class SurrenderTown {

    public static void defenderSurrender(Siege siege) {

		long timeUntilSurrenderConfirmation = siege.getTimeUntilSurrenderConfirmationMillis();

		if(timeUntilSurrenderConfirmation > 0) {
			//Pending surrender
			siege.setStatus(SiegeStatus.PENDING_DEFENDER_SURRENDER);
			SiegeController.saveSiege(siege);
			TownyMessaging.sendGlobalMessage(String.format(
				Translation.of("msg_siege_war_pending_town_surrender"),
				siege.getDefendingTown().getFormattedName(),
				siege.getAttackingNation().getFormattedName(),
				TimeMgmt.getFormattedTimeValue(timeUntilSurrenderConfirmation)));
		} else {
			//Immediate surrender
			SiegeWarMoneyUtil.giveWarChestToAttackingNation(siege);
			SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_SURRENDER);
			TownyMessaging.sendGlobalMessage(String.format(
				Translation.of("msg_siege_war_town_surrender"),
				siege.getDefendingTown().getFormattedName(),
				siege.getAttackingNation().getFormattedName()));
		}
    }
}
