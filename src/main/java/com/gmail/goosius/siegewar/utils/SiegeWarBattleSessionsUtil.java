package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiegeWarBattleSessionsUtil {

	//Schedule first battle session
	private static long scheduledStartTimeOfNextBattleSession = System.currentTimeMillis() + getTimeUntilNextBattleSessionMillis();

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
						&& (siege.getAttackerBattleScore() > 0 || siege.getDefenderBattleScore() > 0)) {
						//Calculate result
						int battleResult;
						if (siege.getAttackerBattleScore() > siege.getDefenderBattleScore()) {
							battleResult = siege.getAttackerBattleScore();
						} else if (siege.getAttackerBattleScore() < siege.getDefenderBattleScore()) {
							battleResult = -siege.getDefenderBattleScore();
						} else {
							battleResult = 0;
						}

						//Add to results
						battleResults.put(siege, battleResult);

						//Clear battle related stats from the siege
						siege.setBannerControllingSide(SiegeSide.NOBODY);
						siege.clearBannerControllingResidents();
						siege.clearBannerControlSessions();
						siege.setAttackerBattleScore(0);
						siege.setDefenderBattleScore(0);

						//Apply the result to the siege points
						siege.adjustSiegePoints(battleResult);
						SiegeController.saveSiege(siege);
					}
				}

				//Send message
				sendBattleSessionEndedMessage(battleResults);

				//Schedule next session
				scheduledStartTimeOfNextBattleSession = System.currentTimeMillis() + getTimeUntilNextBattleSessionMillis();
			}

		} else {
			/*
			 * Battle session is inactive. Check to see if it starts
			 * If the time remaining is less than a minute, start it
			 */
			if (System.currentTimeMillis() > scheduledStartTimeOfNextBattleSession) {
				//Start battle session
				battleSession.setActive(true);
				battleSession.setScheduledEndTime(System.currentTimeMillis() + (SiegeWarSettings.getWarSiegeBattleSessionsDurationMinutes() * 60000));
				//Send global message to let the server know that "it is on"
				Messaging.sendGlobalMessage(Translation.of("msg_war_siege_battle_session_started"));
			}
		}
	}

	/**
	 * Send message to tell the server the current battle session has ended,
	 * with a brief summary of who won any battles which were fought
	 */
	private static void sendBattleSessionEndedMessage(Map<Siege, Integer> battleResults) {
		String header;
		List<String> lines = new ArrayList<>();

		//Compile message
		if(battleResults.size() == 0) {
			header = Translation.of("msg_war_siege_battle_session_ended_without_battles");
		} else {
			header = Translation.of("msg_war_siege_battle_session_ended_with_battles");

			String resultLine;
			for (Map.Entry<Siege, Integer> battleResultEntry : battleResults.entrySet()) {
				if (battleResultEntry.getValue() > 0) {
					resultLine =
							Translation.of("msg_war_siege_battle_session_ended_attacker_result",
									battleResultEntry.getKey().getDefendingTown().getName(),
									"+" + battleResultEntry.getValue());
				} else if (battleResultEntry.getValue() < 0) {
					resultLine =
							Translation.of("msg_war_siege_battle_session_ended_defender_result",
									battleResultEntry.getKey().getDefendingTown().getName(),
									"-" + Math.abs(battleResultEntry.getValue()));
				} else {
					resultLine =
							Translation.of("msg_war_siege_battle_session_ended_draw_result",
									battleResultEntry.getKey().getDefendingTown().getName());
				}
				lines.add(resultLine);
			}
		}
		//Send message
		Messaging.sendGlobalMessage(header, lines);
	}

	public static String getFormattedTimeUntilNextBattleSessionStarts() {
		long timeRemaining = scheduledStartTimeOfNextBattleSession - System.currentTimeMillis();
		if(timeRemaining > 0) {
			return TimeMgmt.getFormattedTimeValue(timeRemaining);
		} else {
			return "0";
		}
	}

	private static long getTimeUntilNextBattleSessionMillis() {
		LocalDateTime currentDateTime = LocalDateTime.now(Clock.systemUTC());
		Duration closestDuration = null;
		Duration candidateDuration;
		LocalTime candidateTime;
		LocalDate candidateDate;
		LocalDateTime candidateDateTime;
		String[] startTimeHourMinutePair;
		for (String startTime : SiegeWarSettings.getWarSiegeBattleSessionsStartTimesUtc()) {
			if (startTime.contains(":")) {
				startTimeHourMinutePair = startTime.split(":");
				candidateTime = LocalTime.of(Integer.parseInt(startTimeHourMinutePair[0]), Integer.parseInt(startTimeHourMinutePair[1]));
			} else {
				candidateTime = LocalTime.of(Integer.parseInt(startTime), 0);
			}

			//Convert candidate to local date time
			if (candidateTime.isAfter(currentDateTime.toLocalTime())) {
				candidateDate = LocalDate.now(Clock.systemUTC());
			} else {
				candidateDate = LocalDate.now(Clock.systemUTC()).plusDays(1);
			}
			candidateDateTime = LocalDateTime.of(candidateDate, candidateTime);

			//Make this candidate our favourite if it is closer
			candidateDuration = Duration.between(currentDateTime, candidateDateTime);
			if (closestDuration == null) {
				closestDuration = candidateDuration;
			} else if (candidateDuration.getSeconds() < closestDuration.getSeconds())
				closestDuration = candidateDuration;
		}

		//Return closest duration
		return closestDuration.getSeconds() * 1000;
	}
}
