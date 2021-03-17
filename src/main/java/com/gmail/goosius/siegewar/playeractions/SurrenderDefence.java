package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.timeractions.AttackerWin;
import com.gmail.goosius.siegewar.timeractions.DefenderWin;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to surrender siege defences
 *
 * @author Goosius
 */
public class SurrenderDefence {

	public static void processSurrenderDefenceRequest(Player player, Siege siege) throws TownyException {
		if(!SiegeWarSettings.getWarSiegeAbandonEnabled())
			throw new TownyException(Translation.of("msg_err_action_disable"));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToSurrenderDefence(siege.getSiegeType())))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		surrenderDefence(siege, siege.getTimeUntilSurrenderConfirmationMillis());
	}

    public static void surrenderDefence(Siege siege, long timeUntilSurrenderConfirmation) {
		if(timeUntilSurrenderConfirmation > 0) {
			//Pending surrender
			siege.setStatus(SiegeStatus.PENDING_DEFENDER_SURRENDER);
			SiegeController.saveSiege(siege);
		} else {
			//Immediate surrender
			AttackerWin.attackerWin(siege, SiegeStatus.DEFENDER_SURRENDER);
		}

		//Send global message
		Messaging.sendGlobalMessage(getSurrenderMessage(siege, timeUntilSurrenderConfirmation));
	}

	private static String getSurrenderMessage(Siege siege, long timeUntilAbandonConfirmation) {
		String key = String.format("msg_%s_siege_defender_surrender", siege.getSiegeType().toString().toLowerCase());
		String message = "";
		switch(siege.getSiegeType()) {
			case CONQUEST:
			case SUPPRESSION:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getAttacker().getFormattedName());
				break;
			case LIBERATION:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getDefender().getFormattedName(),
						siege.getAttacker().getFormattedName());
				break;
			case REVOLT:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getDefender().getFormattedName());
				break;
		}

		if(timeUntilAbandonConfirmation > 0) {
			message += Translation.of("msg_pending_attacker_victory", timeUntilAbandonConfirmation);
		} else {
			message += Translation.of("msg_immediate_attacker_victory");
		}

		return message;
	}
}
