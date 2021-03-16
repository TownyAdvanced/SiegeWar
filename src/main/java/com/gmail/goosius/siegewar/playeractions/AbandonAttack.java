package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
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

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, siege.getSiegeType().getPermissionNodeToAbandonAttack().getNode()))
			throw new TownyException(Translation.of("msg_err_action_disable"));

		abandonAttack(siege);
	}

    private static void abandonAttack(Siege siege) {
		long timeUntilOfficialAbandon = siege.getTimeUntilAbandonConfirmationMillis();

		if(timeUntilOfficialAbandon > 0) {
			//Pending abandon
			siege.setStatus(SiegeStatus.PENDING_ATTACKER_ABANDON);
			SiegeController.saveSiege(siege);
			Messaging.sendGlobalMessage(getAbandonMessage(siege, timeUntilOfficialAbandon));
		} else {
			//Immediate abandon
			SiegeWarMoneyUtil.giveWarChestToDefendingTown(siege);
			SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.ATTACKER_ABANDON);
			Messaging.sendGlobalMessage(getAbandonMessage(siege, 0));
		}
	}

	private static String getAbandonMessage(Siege siege, long timeUntilAbandonConfirmation) {
		String key = String.format("msg_%s_siege_attacker_abandon", siege.getSiegeType().toString().toLowerCase());
		String message = "";
		switch (siege.getSiegeType()) {
			case CONQUEST:
			case SUPPRESSION:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getAttacker().getFormattedName());
				break;
			case LIBERATION:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getAttacker().getFormattedName(),
						siege.getDefender().getFormattedName());
				break;
			case REVOLT:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getDefender().getFormattedName());
				break;
		}

		if (timeUntilAbandonConfirmation == 0) {
			message += Translation.of("msg_immediate_defender_victory");
		} else {
			message += Translation.of("msg_pending_defender_victory", timeUntilAbandonConfirmation);
		}

		return message;
	}
}
