package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyMessaging;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
				Map<Siege, Integer> battleResults = new HashMap<>();
				for(Siege siege: SiegeController.getSieges()) {

					if (siege.getStatus() == SiegeStatus.IN_PROGRESS
						&& (siege.getAttackerBattlePoints() > 0 || siege.getDefenderBattlePoints() > 0)) {
						//Calculate result
						int battleResult;
						if (siege.getAttackerBattlePoints() > siege.getDefenderBattlePoints()) {
							battleResult = siege.getAttackerBattlePoints();
						} else if (siege.getAttackerBattlePoints() < siege.getDefenderBattlePoints()) {
							battleResult = -siege.getDefenderBattlePoints();
						} else {
							battleResult = 0;
						}

						//Add to results
						battleResults.put(siege, battleResult);

						//Clear battle related stats from the siege
						siege.setBannerControllingSide(SiegeSide.NOBODY);
						siege.clearBannerControllingResidents();
						siege.clearBannerControlSessions();
						siege.setAttackerBattlePoints(0);
						siege.setDefenderBattlePoints(0);

						//Apply the result to the siege points
						siege.adjustSiegePoints(battleResult);
					}
				}

				//Send message
				sendBattleSessionEndedMessage(battleResults);
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
					Messaging.sendGlobalMessage(Translation.of("msg_war_siege_battle_session_started"));
				}
			}
		}
	}

	/**
	 * Send message to tell the server the current battle session has ended,
	 * with a brief summary of who won any battles which were fought
	 */
	private static void sendBattleSessionEndedMessage(Map<Siege, Integer> battleResults) {
		List<String> lines = new ArrayList<>();
		lines.add(Translation.of("msg_war_siege_battle_session_ended"));

		String resultLine;
		for(Map.Entry<Siege, Integer> battleResultEntry: battleResults.entrySet()) {
			if(battleResultEntry.getValue() > 0) {
				resultLine =
						String.format(Translation.of("msg_war_siege_battle_session_ended_attacker_result"),
								battleResultEntry.getKey().getDefendingTown().getName(),
								Math.abs(battleResultEntry.getValue()));
			} else if (battleResultEntry.getValue() < 0) {
				resultLine =
						String.format(Translation.of("msg_war_siege_battle_session_ended_defender_result"),
								battleResultEntry.getKey().getDefendingTown().getName(),
								Math.abs(battleResultEntry.getValue()));
			} else {
				resultLine =
						String.format(Translation.of("msg_war_siege_battle_session_ended_draw_result"),
								battleResultEntry.getKey().getDefendingTown().getName());
			}
			lines.add(resultLine);
		}
		TownyMessaging.sendGlobalMessage(lines);
	}
}
