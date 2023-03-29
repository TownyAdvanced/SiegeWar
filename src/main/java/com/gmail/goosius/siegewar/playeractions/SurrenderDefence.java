package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.timeractions.AttackerWin;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to surrender siege defences
 *
 * @author Goosius
 */
public class SurrenderDefence {

	public static void processSurrenderDefenceRequest(Player player, Siege siege) throws TownyException {
		if(!SiegeWarSettings.getWarSiegeSurrenderEnabled())
			throw new TownyException(Translatable.of("msg_err_action_disable").forLocale(player));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToSurrenderDefence(siege.getSiegeType())))
			throw new TownyException(Translatable.of("msg_err_action_disable").forLocale(player));
		
		Confirmation
			.runOnAccept(()-> surrenderDefence(siege, siege.getTimeUntilSurrenderConfirmationMillis()))
			.runOnCancel(()-> Messaging.sendMsg(player, Translatable.of("msg_surrender_action_cancelled")))
			.sendTo(player);
	}

    public static void surrenderDefence(Siege siege, long timeUntilSurrenderConfirmation) {
		//Send global message
		Messaging.sendGlobalMessage(getSurrenderMessage(siege, timeUntilSurrenderConfirmation));
		//Do surrender
		if(timeUntilSurrenderConfirmation > 0) {
			//Pending surrender
			siege.setStatus(SiegeStatus.PENDING_DEFENDER_SURRENDER);
			SiegeController.saveSiege(siege);
		} else {
			//Immediate surrender
			AttackerWin.attackerWin(siege, SiegeStatus.DEFENDER_SURRENDER);
		}
	}

	private static String getSurrenderMessage(Siege siege, long timeUntilSurrenderConfirmation) {
		String key = String.format("msg_%s_siege_defender_surrender", siege.getSiegeType().toString().toLowerCase());
		String message = "";
		switch(siege.getSiegeType()) {
			case CONQUEST:
				message = Translation.of(key,
						siege.getTown().getName(),
						siege.getAttacker().getName());
				break;
			case REVOLT:
				message = Translation.of(key,
						siege.getTown().getName(),
						siege.getDefender().getName());
				break;
		}

		if(timeUntilSurrenderConfirmation > 0) {
			message += Translation.of("msg_pending_attacker_victory", TimeMgmt.getFormattedTimeValue(timeUntilSurrenderConfirmation));
		} else {
			String key2 = String.format("msg_%s_siege_attacker_win_result", siege.getSiegeType().toString().toLowerCase());
			message += Translation.of(key2);
		}

		return message;
	}
}
