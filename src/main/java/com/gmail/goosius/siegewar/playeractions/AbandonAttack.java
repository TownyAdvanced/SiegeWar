package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.timeractions.DefenderWin;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.util.TimeMgmt;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to Abandon siege attacks
 *
 * @author Goosius
 */
public class AbandonAttack {

	public static void processAbandonAttackRequest(Player player, Siege siege) throws TownyException {
		if(!SiegeWarSettings.getWarSiegeAbandonEnabled())
			throw new TownyException(Translation.of("msg_err_action_disable"));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToAbandonAttack(siege.getSiegeType())))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		abandonAttack(siege, siege.getTimeUntilAbandonConfirmationMillis());
	}

    public static void abandonAttack(Siege siege, long timeUntilOfficialAbandon) {
		if(timeUntilOfficialAbandon > 0) {
			//Pending abandon
			siege.setStatus(SiegeStatus.PENDING_ATTACKER_ABANDON);
			SiegeController.saveSiege(siege);
		} else {
			//Immediate abandon
			DefenderWin.defenderWin(siege, SiegeStatus.ATTACKER_ABANDON);
		}

		//Send global message
		Messaging.sendGlobalMessage(getAbandonMessage(siege, timeUntilOfficialAbandon));
	}

	private static String getAbandonMessage(Siege siege, long timeUntilAbandonConfirmation) {
		String key = String.format("msg_%s_siege_attacker_abandon", siege.getSiegeType().toString().toLowerCase());
		String message = "";
		switch (siege.getSiegeType()) {
			case CONQUEST:
			case SUPPRESSION:
				message = Translation.of(key,
						siege.getTown().getName(),
						siege.getAttacker().getName());
				break;
			case LIBERATION:
				message = Translation.of(key,
						siege.getTown().getName(),
						siege.getAttacker().getName(),
						siege.getDefender().getName());
				break;
			case REVOLT:
				message = Translation.of(key,
						siege.getTown().getName(),
						siege.getDefender().getName());
				break;
		}

		if (timeUntilAbandonConfirmation > 0) {
			message += Translation.of("msg_pending_defender_victory", TimeMgmt.getFormattedTimeValue(timeUntilAbandonConfirmation));
		} else {
			message += Translation.of("msg_immediate_defender_victory");
		}

		return message;
	}
}
