package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.events.BattleSessionEndedEvent;
import com.gmail.goosius.siegewar.events.BattleSessionPreStartEvent;
import com.gmail.goosius.siegewar.events.BattleSessionStartedEvent;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.jail.UnJailReason;
import com.palmergames.bukkit.towny.utils.JailUtil;
import com.palmergames.util.StringMgmt;
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
	
	private static Map<Siege, Integer> battleResults = new HashMap<>();

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

	public static void startBattleSession() {
		BattleSession battleSession = BattleSession.getBattleSession();
		battleSession.setActive(true);
		//Set the start time
		battleSession.setStartTime(System.currentTimeMillis());
		//Set the scheduled end time
		battleSession.setScheduledEndTime(System.currentTimeMillis() + (SiegeWarSettings.getWarSiegeBattleSessionsDurationMinutes() * 60000));
		//Clear the scheduled start time
		battleSession.setScheduledStartTime(null);
		//Send up the Bukkit event for other plugins to listen for.
		Bukkit.getPluginManager().callEvent(new BattleSessionStartedEvent());	
		//Recalculate recent battle sessions of players
		try { Thread.sleep(5000); //Sleep to ensure recalculation is good
		} catch (InterruptedException e) { e.printStackTrace();}
		//Send global message to let the server know that the battle session started
		Messaging.sendGlobalMessage(Translatable.of("msg_war_siege_battle_session_started"));
		//Start the bossbar for the Battle Session
		BossBarUtil.updateBattleSessionBossBar();
	}

	public static void endBattleSession() {
		BattleSession battleSession = BattleSession.getBattleSession();
		battleSession.setActive(false);
		battleResults.clear();
		/*
		 * Gather the results of all battles
		 * End any active battles
		 */
		for (Siege siege : SiegeController.getSieges())
			endBattleSessionForSiege(siege);
		
		Bukkit.getPluginManager().callEvent(new BattleSessionEndedEvent());
		
		//Send message
		sendBattleSessionEndedMessage(battleResults);
		
		//Remove Battle Session Boss-Bars
		BossBarUtil.removeBattleSessionBossBars();
	}

	public static void endBattleSessionForSiege(Siege siege) {
		try {
			if (siege.getStatus() == SiegeStatus.IN_PROGRESS) {
				//Adjust numBattleSessionsCompleted
				siege.setNumBattleSessionsCompleted(siege.getNumBattleSessionsCompleted()+1);

				//If any battle points were gained, calculate a result
				if(siege.getAttackerBattlePoints() > 0 || siege.getDefenderBattlePoints() > 0) {
					//Adjust the siege balance
					int siegeBalanceAdjustment = calculateSiegeBalanceAdjustment(siege);

					//Apply the battle points of the winner to the siege balance
					siege.adjustSiegeBalance(siegeBalanceAdjustment);

					//Prepare result for messaging
					battleResults.put(siege, siegeBalanceAdjustment);

					// Potentially unjail the siegedTowns' prisoners if they attackers lost the battlesession.
					if (siege.getAttackerBattlePoints() > siege.getDefenderBattlePoints()
							&& SiegeWarSettings.isUnjailingAttackerResidents()
							&& siege.getDefender() instanceof Town
							&& siege.getAttacker() instanceof Nation) {
						unjailPlayers((Nation) siege.getAttacker(), (Town) siege.getDefender());
					}

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

				//Remove banner cap boss bars
				for(Player player: siege.getBannerControlSessions().keySet()) {
					BossBarUtil.removeBannerCapBossBar(player);
				}

				//Clear battle related stats from the siege
				siege.setBannerControllingSide(SiegeSide.NOBODY);
				siege.clearBannerControllingResidents();
				siege.clearBannerControlSessions();
				siege.setAttackerBattlePoints(0);
				siege.setDefenderBattlePoints(0);
				siege.setNumberOfBannerControlReversals(0);

				//Save siege to database
				SiegeController.saveSiege(siege);

			} else if (siege.getStatus() == SiegeStatus.PENDING_ATTACKER_ABANDON
				|| siege.getStatus() == SiegeStatus.PENDING_DEFENDER_SURRENDER) {
				//Adjust numBattleSessionsCompleted
				siege.setNumBattleSessionsCompleted(siege.getNumBattleSessionsCompleted()+1);
			}
		} catch (Throwable t) {
			try {
				SiegeWar.severe("Problem ending battle for siege: " + siege.getTown().getName());
			} catch (Throwable t2) {
				SiegeWar.severe("Problem ending battle for siege: (could not read town name)");
			}
			t.printStackTrace();
		}
	}

	private static void unjailPlayers(Nation attacker, Town defender) {
		if (!defender.hasJails())
			return;
		List<String> freedNames = new ArrayList<>();
		defender.getJailedResidents().stream()
			.filter(r -> attacker.hasResident(r))
			.forEach(r -> {
				JailUtil.unJailResident(r, UnJailReason.JAILBREAK);
				freedNames.add(r.getName());
			});
		if (freedNames.isEmpty())
			return;
		Messaging.sendGlobalMessage(Translatable.of("msg_attackers_have_triggered_a_jail_break_freeing", StringMgmt.join(freedNames, ", ")));
	}

	public static void evaluateBattleSessions() {
		BattleSession battleSession = BattleSession.getBattleSession();

		if (battleSession.isActive()) {
			//A Battle session is active. Check to see if it finishes.

			if(System.currentTimeMillis() > battleSession.getScheduledEndTime()) {
				//Finish battle session
				endBattleSession();
			} else {
				//Update battle session boss bars.
				BossBarUtil.updateBattleSessionBossBar();
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
					
					//Send up the Bukkit event for other plugins to listen for and potentially cancel.
					BattleSessionPreStartEvent event = new BattleSessionPreStartEvent();
					Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						//Null the next scheduled time, so it can be reset on the next ShortTime.
						battleSession.setScheduledStartTime(null);
						//Broadcast a cancelled BatterlSession message.
						Messaging.sendGlobalMessage(event.getCancellationMsg());
						return;
					}
					
					//Activate the session
					startBattleSession();
				}
			}
		}
	}

	/** 
	 * Determines the amount the siege points will be adjusted by.
	 *
	 * Overridden by the areBattlePointsWinnerTakesAll config setting.
	 * 
	 * @param siege the Siege to gather BattlePoints from.
	 * @return points awarded to the winner.
	 */
	public static int calculateSiegeBalanceAdjustment(Siege siege) {
		
		// If Winner-Takes-All points are disabled return attacker points - defender points.
		if (!SiegeWarSettings.areBattlePointsWinnerTakesAll()) 
			return siege.getAttackerBattlePoints() - siege.getDefenderBattlePoints();
		
		// Attackers won the session.
		if (siege.getAttackerBattlePoints() > siege.getDefenderBattlePoints())
			return siege.getAttackerBattlePoints();

		// Defenders won the session.
		if (siege.getAttackerBattlePoints() < siege.getDefenderBattlePoints())
			return -siege.getDefenderBattlePoints();

		// Session was a draw.
		return 0;
	}

	/**
	 * Send message to tell the server the current battle session has ended,
	 * with a brief summary of who won any battles which were fought
	 */
	private static void sendBattleSessionEndedMessage(Map<Siege, Integer> battleResults) {
		Translatable header;
		List<Translatable> lines = new ArrayList<>();

		//Compile message
		if(battleResults.size() == 0) {
			header = Translatable.of("msg_war_siege_battle_session_ended_without_battles");
		} else {
			header = Translatable.of("msg_war_siege_battle_session_ended_with_battles");

			Translatable resultLine;
			for (Map.Entry<Siege, Integer> battleResultEntry : battleResults.entrySet()) {
				if (battleResultEntry.getValue() > 0) {
					resultLine =
							Translatable.of("msg_war_siege_battle_session_ended_attacker_result",
									battleResultEntry.getKey().getTown().getName(),
									"+" + battleResultEntry.getValue());
				} else if (battleResultEntry.getValue() < 0) {
					resultLine =
							Translatable.of("msg_war_siege_battle_session_ended_defender_result",
									battleResultEntry.getKey().getTown().getName(),
									"-" + Math.abs(battleResultEntry.getValue()));
				} else {
					resultLine =
							Translatable.of("msg_war_siege_battle_session_ended_draw_result",
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
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime nextStartDateTime = null;

		//Look for next configured-start-time for today
		for (LocalDateTime candidateStartTime : SiegeWarSettings.getAllBattleSessionStartTimesForToday()) {
			if(candidateStartTime.isAfter(currentTime)) {
				nextStartDateTime = candidateStartTime;
				break;
			}
		}

		//If no configured-start-time was found, look for the first configured time for tomorrow
		if(nextStartDateTime == null) {
			nextStartDateTime = SiegeWarSettings.getFirstBattleSessionStartTimeForTomorrowUtc();
		}

		//If nextStartTime is still null, return null, else return the UTC time in millis of the given value.
		if(nextStartDateTime != null) {
			ZonedDateTime nextStartTimeInServerTimeZone = ZonedDateTime.of(nextStartDateTime, ZoneId.systemDefault());
			ZonedDateTime nextStartTimeInUtcTimeZone = nextStartTimeInServerTimeZone.withZoneSameInstant(ZoneId.of("UTC"));
			return nextStartTimeInUtcTimeZone.toInstant().toEpochMilli();
		} else {
			return null;
		}
	}
}
