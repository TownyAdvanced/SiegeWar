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

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiegeWarBattleSessionUtil {

	/**
	 * Attempt to schedule the next battle session
	 * 1. If there is a battle session configured to start later today, or tomorrow,
	 *     this method will successfully set the battleSessions.scheduledStartTime variable.
	 * 2. If there are no battle sessions configured to start later today, or tomorrow,
	 *     this method will set the battleSessions.scheduledStartTime variable to null.
	 */
   	public static void attemptToScheduleNextBattleSession() {
		Long startTimeOfNextSession = getConfiguredStartTimeOfNextBattleSession();
		BattleSession.getBattleSession().setScheduledStartTime(startTimeOfNextSession);
   	}
       
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
								int battlePointsOfWinner = calcPointsForBattleSession(siege);
	
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
		    //Battle session is inactive.

			//If there is no battle session scheduled, attempt to schedule session now.
			if(battleSession.getScheduledStartTime() == null) {
				attemptToScheduleNextBattleSession();
			}

			//If a battle session is scheduled, start it if we hit the scheduled time
			if(battleSession.getScheduledStartTime() != null) {
				if (System.currentTimeMillis() > battleSession.getScheduledStartTime()) {
					//Activate the session
					battleSession.setActive(true);
					//Set the scheduled end time
					battleSession.setScheduledEndTime(System.currentTimeMillis() + (SiegeWarSettings.getWarSiegeBattleSessionDurationMinutes() * 60000));
					//Clear the scheduled start time
					battleSession.setScheduledStartTime(null);
					//Send global message to let the server know that the battle session started
					Messaging.sendGlobalMessage(Translation.of("msg_war_siege_battle_session_started"));
				}
			}
		}
	}

	/** 
	 * Determines how many points are awarded to the winner of a Battle Session.
	 *
	 * Affected by the areBattlePointsWinnerTakesAll config setting.
	 * 
	 * @param siege the Siege to gather BattlePoints from.
	 * @return points awarded to the winner.
	 */
	public static int calcPointsForBattleSession(Siege siege) {
		int battlePointsOfWinner;
		
		// Attackers won the session.
		if (siege.getAttackerBattlePoints() > siege.getDefenderBattlePoints()) {
			
			if (SiegeWarSettings.areBattlePointsWinnerTakesAll())
				battlePointsOfWinner = siege.getAttackerBattlePoints();
			else 
				battlePointsOfWinner = siege.getAttackerBattlePoints() - siege.getDefenderBattlePoints();

		// Defenders won the session.
		} else if (siege.getAttackerBattlePoints() < siege.getDefenderBattlePoints()) {

			if (SiegeWarSettings.areBattlePointsWinnerTakesAll())
				battlePointsOfWinner = -siege.getDefenderBattlePoints();
			else 
				battlePointsOfWinner = -(siege.getDefenderBattlePoints() - siege.getAttackerBattlePoints());

		// Session was a draw.
		} else {
			return 0;
		}
		return battlePointsOfWinner;
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
		Long startTimeOfTodaysNextBattleSession = BattleSession.getBattleSession().getScheduledStartTime();
		if(startTimeOfTodaysNextBattleSession == null) {
			return "?"; // Rarely needed but can happen if a server configures only weekday/weekend sessions
		} else {
			long timeRemaining = startTimeOfTodaysNextBattleSession - System.currentTimeMillis();
			if(timeRemaining > 0) {
				return TimeMgmt.getFormattedTimeValue(timeRemaining);
			} else {
				return "0";
			}		
		}	
	}

	/**
	 * Get the configured start time, in millis, of the next battle session.
	 * 
	 * This method will only find the start time if it is later today or tomorrow.
	 * If there are no start times later today or tomorrow, then this method will return null
	 * 
	 * @return configured start time, in millis.
	 */
	private static Long getConfiguredStartTimeOfNextBattleSession() {
		LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
		LocalDateTime nextStartDateTime = null;

		//Look for next configured-start-time for today
		for (LocalDateTime candidateStartTimeUtc : SiegeWarSettings.getAllBattleSessionStartTimesForTodayUtc()) {
			if(candidateStartTimeUtc.isAfter(currentTime)) {
				nextStartDateTime = candidateStartTimeUtc;
				break;
			}
		}

		//If no configured-start-time was found, look for the first configured time for tomorrow
		if(nextStartDateTime == null) {
			nextStartDateTime = SiegeWarSettings.getFirstBattleSessionStartTimeForTomorrowUtc();
		}
		
		//If nextStartTime is still null, return null, else transform it to millis and return
		if(nextStartDateTime != null) {
			ZonedDateTime zdt = ZonedDateTime.of(nextStartDateTime, ZoneOffset.UTC);
			return zdt.toInstant().toEpochMilli();
		} else {
			return null;
		}
	}
}
