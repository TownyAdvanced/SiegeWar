package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiegeWarBattleSessionUtil {

	public static void evaluateBattleSessions() {
		BattleSession battleSession = BattleSession.getBattleSession();

		if (battleSession.isActive()) {
			//A Battle session is active. Check to see if it finishes.

			if(System.currentTimeMillis() > battleSession.getScheduledEndTime()) {
				//Finish battle session
				battleSession.setActive(false);

				/*
				 * Gather the results of all battles
				 * End any active battles
				 */
				Map<Siege, Integer> battleResults = new HashMap<>();
				for (Siege siege : SiegeController.getSieges()) {
					try {
						if (siege.getStatus() == SiegeStatus.IN_PROGRESS) {
							//Record primary government of besieged town
							if(SiegeWarSettings.isNationSiegeImmunityEnabled())
								siege.recordPrimaryTownGovernment();

							//If any battle points were gained, calculate a result
							if(siege.getAttackerBattlePoints() > 0 || siege.getDefenderBattlePoints() > 0) {
								//Calculate result
								int battlePointsOfWinner;
								if (siege.getAttackerBattlePoints() > siege.getDefenderBattlePoints()) {
									battlePointsOfWinner = siege.getAttackerBattlePoints();
								} else if (siege.getAttackerBattlePoints() < siege.getDefenderBattlePoints()) {
									battlePointsOfWinner = -siege.getDefenderBattlePoints();
								} else {
									battlePointsOfWinner = 0;
								}
	
								//Apply the battle points of the winner to the siege balance
								siege.adjustSiegeBalance(battlePointsOfWinner);
	
								//Propagate attacker battle contributions to siege history
								siege.propagateSuccessfulBattleContributorsToResidentTimedPointContributors();
	
								//Prepare result for messaging
								battleResults.put(siege, battlePointsOfWinner);

								//Save siege
								SiegeController.saveSiege(siege);							
							}
							
							//Remove glowing effects from players in bc sessions
							for (Player player : siege.getBannerControlSessions().keySet()) {
								if (player.isOnline() && player.hasPotionEffect(PotionEffectType.GLOWING)) {
									Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
										@Override
										public void run() {
											player.removePotionEffect(PotionEffectType.GLOWING);
										}
									});
								}
							}

							//Clear battle related stats from the siege
							siege.setBannerControllingSide(SiegeSide.NOBODY);
							siege.clearBannerControllingResidents();
							siege.clearBannerControlSessions();
							siege.setAttackerBattlePoints(0);
							siege.setDefenderBattlePoints(0);
							siege.clearSuccessfulBattleContributors();
						}
					} catch (Throwable t) {
						try {
							System.err.println("Problem ending battle for siege: " + siege.getTown().getName());
						} catch (Throwable t2) {
							System.err.println("Problem ending battle for siege: (could not read town name)");
						}
						t.printStackTrace();
					}
				}

				//Send message
				sendBattleSessionEndedMessage(battleResults);
			}

		} else {
			/* 
			 * Battle session is inactive. 
			 * Determine whether to activate it
			 */
			 
			if(battleSession.getScheduledStartTime() == null) {
				/*
				 * There is no battle session scheduled.
				 * Attempt to schedule session now.
				 */
				battleSession.setScheduledStartTime(getStartTimeOfNextBattleSessionForToday());
			}

			if(battleSession.getScheduledStartTime() != null) {
				/* 
				 * A battle session is scheduled.
				 * Start session if we hit the scheduled time.
				 */
				if (System.currentTimeMillis() > battleSession.getScheduledStartTime()) {
					//Activate the session
					battleSession.setActive(true);
					//Set the end time
					battleSession.setScheduledEndTime(System.currentTimeMillis() + (SiegeWarSettings.getWarSiegeBattleSessionsDurationMinutes() * 60000));
					//Clear the start time
					battleSession.setScheduledStartTime(null);
					//Send global message to let the server know that the battle session started
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
									battleResultEntry.getKey().getTown().getName(),
									"+" + battleResultEntry.getValue());
				} else if (battleResultEntry.getValue() < 0) {
					resultLine =
							Translation.of("msg_war_siege_battle_session_ended_defender_result",
									battleResultEntry.getKey().getTown().getName(),
									"-" + Math.abs(battleResultEntry.getValue()));
				} else {
					resultLine =
							Translation.of("msg_war_siege_battle_session_ended_draw_result",
									battleResultEntry.getKey().getTown().getName());
				}
				lines.add(resultLine);
			}
		}
		//Send message
		Messaging.sendGlobalMessage(header, lines);
	}

	public static String getFormattedTimeUntilNextBattleSessionStarts() {
		Long startTimeOfNextBattleSessionToday = getStartTimeOfNextBattleSessionForToday();
		if(startTimeOfNextBattleSessionToday == null) {
			return "N/A";  //No more sessions today
		} else {
			long timeRemaining = startTimeOfNextBattleSessionToday - System.currentTimeMillis();
			if(timeRemaining > 0) {
				return TimeMgmt.getFormattedTimeValue(timeRemaining);
			} else {
				return "0";
			}			
		}
	}

	@Nullable
	private static Long getStartTimeOfNextBattleSessionForToday() {
		LocalTime currentTime = LocalTime.now(Clock.systemUTC());
		LocalDate currentDate = LocalDate.now(Clock.systemUTC());
		LocalTime candidateTime;
		String[] startTimeHourMinutePair;

		for (String configuredStartTime : SiegeWarSettings.getBattleSessionStartTimesForTodayUtc()) {
			//Parse configured time into LocalTime object
			if (configuredStartTime.contains(":")) {
				startTimeHourMinutePair = configuredStartTime.split(":");
				candidateTime = LocalTime.of(Integer.parseInt(startTimeHourMinutePair[0]), Integer.parseInt(startTimeHourMinutePair[1]));
			} else {
				candidateTime = LocalTime.of(Integer.parseInt(configuredStartTime), 0);
			}
			
			//If the candidate is a future time today, pick it
			if(candidateTime.isAfter(currentTime)) {
				LocalDateTime resultLocalDateTime = LocalDateTime.of(currentDate, currentTime);
				long resultLong = resultLocalDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
				return resultLong; 
			}
		}
		
		//At this point, no future start time was found for today
		return null;
	}

	private static long getTimeUntilNextBattleSessionMillis() {
		LocalDateTime currentDateTime = LocalDateTime.now(Clock.systemUTC());
		Duration closestDuration = null;
		Duration candidateDuration;
		LocalTime candidateTime;
		LocalDate candidateDate;
		LocalDateTime candidateDateTime;
		String[] startTimeHourMinutePair;
		for (String startTime : SiegeWarSettings.getBattleSessionStartTimesForTodayUtc()) {
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
