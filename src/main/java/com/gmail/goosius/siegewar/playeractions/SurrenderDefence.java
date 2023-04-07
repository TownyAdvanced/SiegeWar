package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.timeractions.AttackerWin;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translatable;
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

		if (siege.getSiegeType() == SiegeType.CONQUEST && TownOccupationController.isTownOccupied(siege.getTown()))
			throw new TownyException(Translatable.of("msg_err_siege_occupied_towns_cannot_surrender_in_conquest_sieges").forLocale(player));

		Confirmation
			.runOnAccept(()-> surrenderDefence(siege))
			.runOnCancel(()-> Messaging.sendMsg(player, Translatable.of("msg_surrender_action_cancelled")))
			.sendTo(player);
	}

    public static void surrenderDefence(Siege siege) {
		//Send global message
		Messaging.sendGlobalMessage(getSurrenderMessage(siege));
		//Do surrender
		if(siege.getNumBattleSessionsCompleted() < SiegeWarSettings.getSiegeDurationBattleSessions()) {
			//Pending surrender
			siege.setStatus(SiegeStatus.PENDING_DEFENDER_SURRENDER);
			SiegeController.saveSiege(siege);
		} else {
			//Immediate surrender
			AttackerWin.attackerWin(siege, SiegeStatus.DEFENDER_SURRENDER);
		}
	}

	private static Translatable getSurrenderMessage(Siege siege) {
		Translatable message;
		String key;
		if (siege.getNumBattleSessionsCompleted() < SiegeWarSettings.getSiegeDurationBattleSessions()) {
			key = String.format("msg_%s_siege_defender_surrender", siege.getSiegeType().toLowerCase());
			message = Translatable.of(key, siege.getTown().getName(), siege.getAttacker().getName());
		} else {
			key = String.format("msg_%s_siege_defender_surrender_confirmed", siege.getSiegeType().toLowerCase());
			message = Translatable.of(key, siege.getTown().getName());
			key = String.format("msg_%s_siege_attacker_win_result", siege.getSiegeType().toLowerCase());
			message.append(Translatable.of(key));
		}
		return message;
	}
}
