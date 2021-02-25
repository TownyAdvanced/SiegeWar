package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.BattleResult;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Resident;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SiegeWarBattleSessionsUtil {
	
	public static void evaluateBattleSessions() {
		BattleSession battleSession = BattleSession.getBattleSession();

		if (battleSession.isActive()) {
			//A Battle session is active. Check to see if it finishes.

			if(System.currentTimeMillis() > battleSession.getScheduledEndTime()) {
				//Finish battle session
				battleSession.setActive(false);

				//Gather the results of all active battles, then end them
				Map<Siege, BattleResult> battleResults = new HashMap<>();
				for(Siege siege: SiegeController.getSieges()) {

					if (siege.getStatus() == SiegeStatus.IN_PROGRESS
						&& (siege.getSiegePoints() > 0 || siege.getAttackerSiegeContributionHistory().size() > 0)) {
						//Calculate result
						BattleResult battleResult;
						if (siege.getSiegePoints() > 0) {
							battleResult = BattleResult.ATTACKER_WIN;
						} else if (siege.getSiegePoints() < 0) {
							battleResult = BattleResult.DEFENDER_WIN;
						} else {
							battleResult = BattleResult.DRAW;
						}

						//Add to results
						battleResults.put(siege, battleResult);

						//Add resident BC contributions to siege history
						if(battleResult == BattleResult.ATTACKER_WIN) {
							for(Resident resident: siege.getAttackerBattleContributionHistory()) {
								siege.addContributionToAttackerBannerControlSiegeHistory(resident);
							}
						} else if (battleResult == BattleResult.DEFENDER_WIN) {
							for(Resident resident: siege.getDefenderBattleContributionHistory()) {
								siege.addContributionToDefenderBannerControlSiegeHistory(resident);
							}
						} else {
							//Draw - count all contributions:
							for(Resident resident: siege.getAttackerBattleContributionHistory()) {
								siege.addContributionToAttackerBannerControlSiegeHistory(resident);
							}
							for(Resident resident: siege.getDefenderBattleContributionHistory()) {
								siege.addContributionToDefenderBannerControlSiegeHistory(resident);
							}
						}

						//Clear battle related stats from the siege
						siege.setBannerControllingSide(SiegeSide.NOBODY);
						siege.clearBannerControllingResidents();
						siege.clearAttackerBannerControlBattleHistory();
						siege.clearDefenderBannerControlBattleHistory();
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
			long currentHourOfDay = LocalDateTime.now(Clock.systemUTC()).getHour();
			long currentMinuteOfHour = LocalDateTime.now(Clock.systemUTC()).getMinute();
			String currentTimeAsString;
			if(currentMinuteOfHour == 0)
				currentTimeAsString = "" + currentHourOfDay;
			else
				currentTimeAsString = "" + currentHourOfDay + ":" + currentMinuteOfHour;

			for(String startTime: SiegeWarSettings.getWarSiegeBattleSessionsStartTimesUtc()) {
				if(startTime.equals(currentTimeAsString)) {
					//Start battle session
					battleSession.setActive(false);
					battleSession.setScheduledEndTime(System.currentTimeMillis() + (SiegeWarSettings.getWarSiegeBattleSessionsDurationMinutes() * 60000));
					//Send global message to let the server know that "it is on"
					Messaging.sendGlobalMessage("Battle Session started.");
				}
			}
		}
	}
}
