package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.timeractions.AttackerWin;
import com.gmail.goosius.siegewar.utils.SiegeWarTownOccupationUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translatable;
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

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_SURRENDER.getNode()))
			throw new TownyException(Translatable.of("msg_err_action_disable").forLocale(player));

		if (siege.getSiegeType() == SiegeType.CONQUEST && SiegeWarTownOccupationUtil.isTownOccupied(siege.getTown()))
			throw new TownyException(Translatable.of("msg_err_siege_occupied_towns_cannot_surrender_in_conquest_sieges").forLocale(player));

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

	private static Translatable getSurrenderMessage(Siege siege, long timeUntilSurrenderConfirmation) {
		String key = String.format("msg_%s_siege_defender_surrender", siege.getSiegeType().toLowerCase());
		Translatable message = Translatable.of(key,
						siege.getTown().getName(),
						siege.getAttacker().getName());

		if(timeUntilSurrenderConfirmation > 0) {
			message.append(Translatable.of("msg_pending_attacker_victory", TimeMgmt.getFormattedTimeValue(timeUntilSurrenderConfirmation)));
		} else {
			String key2 = String.format("msg_%s_siege_attacker_win_result", siege.getSiegeType().toLowerCase());
			message.append(Translatable.of(key2));
		}

		return message;
	}
}
