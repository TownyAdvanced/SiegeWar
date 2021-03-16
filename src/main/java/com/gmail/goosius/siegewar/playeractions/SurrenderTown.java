package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to surrender towns
 *
 * @author Goosius
 */
public class SurrenderTown {

    public static void surrenderTown(Siege siege) {

		long timeUntilSurrenderConfirmation = siege.getTimeUntilSurrenderConfirmationMillis();

		if(timeUntilSurrenderConfirmation > 0) {
			//Pending surrender
			siege.setStatus(SiegeStatus.PENDING_DEFENDER_SURRENDER);
			SiegeController.saveSiege(siege);
			Messaging.sendGlobalMessage(siege.getMsgPlayerEndSiegeEarly(SiegeStatus.PENDING_DEFENDER_SURRENDER, timeUntilSurrenderConfirmation));
		} else {
			//Immediate surrender
			SiegeWarMoneyUtil.giveWarChestToAttackingNation(siege);
			SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_SURRENDER);
			Messaging.sendGlobalMessage(siege.getMsgPlayerEndSiegeEarly(SiegeStatus.DEFENDER_SURRENDER, 0));
		}
    }

	public static void processSurrenderTownRequest(Player player, Siege siege) throws TownyException {
		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, siege.getSiegeType().getPermissionNodeToAbandonAttack().getNode()))
			throw new TownyException(Translation.of("msg_err_command_disable"));

		surrenderTown(siege);
	}
}
