package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.BattleResult;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleHistory;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import java.util.ArrayList;
import java.util.List;

public class SiegeWarBattleSessionsUtil {
	
	public static void evaluateBattleSessions() {
		BattleSession battleSession = BattleSession.getBattleSession();

		if (battleSession.isActive()) {
			//A Battle session is active. Check to see if it finishes.

			if(System.currentTimeMillis() > battleSession.getScheduledEndTime()) {
				//Finish battle session
				battleSession.setActive(false);

				//Gather the results of all active battles, then end them
				List<BattleHistory> battleHistories = new ArrayList<>();
				for(Siege siege: SiegeController.getSieges()) {

					if (siege.getStatus() == SiegeStatus.IN_PROGRESS
						&& (siege.getSiegePoints() > 0 || siege.getBannerControllingAttackersHistory().size() > 0)) {
						//Calculate result
						BattleResult battleResult;
						if (siege.getSiegePoints() > 0) {
							battleResult = BattleResult.ATTACKER_WIN;
						} else if (siege.getSiegePoints() < 0) {
							battleResult = BattleResult.DEFENDER_WIN;
						} else {
							battleResult = BattleResult.DRAW;
						}
						//Distribute plunder to nation/town, plus any soldiers who got BC during the sesh
						int totalPlunder = 999;
						if(battleResult == BattleResult.ATTACKER_WIN) {
							SiegeWarPlunderUtil.attackerPlundersTown(siege, totalPlunder);
						} else if (battleResult == BattleResult.DEFENDER_WIN) {
							SiegeWarPlunderUtil.defenderPlundersWarchest(siege, totalPlunder);
						}

						//Add to history
						battleHistories.add(new BattleHistory(siege, battleResult, totalPlunder));
						//Clear battle related stats from the siege
						siege.setBannerControllingSide(SiegeSide.NOBODY);
						siege.clearBannerControllingResidents();
						siege.clearBannerControllingAttackersHistory();
						siege.clearBannerControllingDefendersHistory();
						siege.clearBannerControlSessions();
						siege.setSiegePoints(0);
					}
				}

				/*
				 * Send global message
				 * with a brief summary of who won the active battles in the last session
				 */
				Messaging.sendGlobalMessage("People won people lost get over it.");
			}

		} else {
			//Battle session is inactive. Check to see if it starts
			long currentSystemClockMinutes = System.currentTimeMillis() % 60000;
			if(currentSystemClockMinutes == SiegeWarSettings.getWarSiegeBattleSessionStartClip()) {
				//Start battle session
				battleSession.setActive(false);
				battleSession.setScheduledEndTime(System.currentTimeMillis() + (SiegeWarSettings.getWarSiegeBattleSessionDurationMinutes() * 60000));
				//Send global message to let the server know that "it is on"
				Messaging.sendGlobalMessage("Battle Session started.");
			}
		}
	}
}
