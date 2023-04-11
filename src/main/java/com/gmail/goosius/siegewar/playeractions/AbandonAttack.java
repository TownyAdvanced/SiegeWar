package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.timeractions.DefenderWin;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to Abandon siege attacks
 *
 * @author Goosius
 */
public class AbandonAttack {

	/**
	 * At this point we know the player is a member of the attacking army
	 *
	 * @param player player
	 * @param siege siege
	 * @throws TownyException
	 */
	public static void processAbandonAttackRequest(Player player, Siege siege) throws TownyException {
		if(!SiegeWarSettings.getWarSiegeAbandonEnabled())
			throw new TownyException(Translatable.of("msg_err_action_disable"));

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_ABANDON.getNode()))
			throw new TownyException(Translatable.of("msg_err_action_disable"));

		Confirmation
			.runOnAccept(()-> abandonAttack(siege))
			.runOnCancel(()-> Messaging.sendMsg(player, Translatable.of("msg_abandon_action_cancelled")))
			.sendTo(player);
	}

    public static void abandonAttack(Siege siege) {
		//Send global message
		Messaging.sendGlobalMessage(getAbandonMessage(siege));
		//Do abandon
		if(siege.getNumBattleSessionsCompleted() < SiegeWarSettings.getSiegeDurationBattleSessions()) {
			//Pending abandon
			siege.setStatus(SiegeStatus.PENDING_ATTACKER_ABANDON);
			SiegeController.saveSiege(siege);
		} else {
			//Immediate abandon
			siege.setStatus(SiegeStatus.ATTACKER_ABANDON);
			DefenderWin.defenderWin(siege);
		}
	}

	private static Translatable getAbandonMessage(Siege siege) {
		Translatable message;
		String key;
		if (siege.getNumBattleSessionsCompleted() < SiegeWarSettings.getSiegeDurationBattleSessions()) {
			//Pending
			key = String.format("msg_%s_siege_attacker_abandon", siege.getSiegeType().toLowerCase());
			message = Translatable.of(key, siege.getTown().getName(), siege.getAttacker().getName());
		} else {
			//Base message
			key = String.format("msg_%s_siege_attacker_abandon_confirmed", siege.getSiegeType().toLowerCase());
			message = Translatable.of(key, siege.getTown().getName());
			//Standard effects
			key= String.format("msg_%s_siege_defender_win_result", siege.getSiegeType().toLowerCase());
			message.append(Translatable.of(key));
			//Special effects
			if(siege.getSiegeType() == SiegeType.REVOLT) {
				message.append(Translatable.of("msg_revolt_siege_defender_decisive_win_demoralization", 
						siege.getAttacker().getName(),
						SiegeWarSettings.getRevoltSiegeDecisiveDefenderVictoryWeaknessAmount(),
						SiegeWarSettings.getRevoltSiegeDecisiveDefenderVictoryWeaknessDurationDays()));
			}
		}

		return message;
	}
}
