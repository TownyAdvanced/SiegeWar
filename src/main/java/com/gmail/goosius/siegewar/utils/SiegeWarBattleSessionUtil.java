package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.events.BattleSessionEndedEvent;
import com.gmail.goosius.siegewar.events.BattleSessionPreStartEvent;
import com.gmail.goosius.siegewar.events.BattleSessionStartedEvent;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class SiegeWarBattleSessionUtil {
	
	private static Map<Siege, Integer> battleResults = new HashMap<>();
	private static final int ONE_DAY_IN_MILLIS = 86400000;
	private static final int ONE_MINUTE_IN_MILLIS = 60000;

	/**
	 * Schedule the next battle session
	 */
	public static void scheduleNextBattleSession() {
		long startTimeOfNextSession;
		int minutesUntilStart;
		int millisUntilStart;
		int startTimeClip = SiegeWarSettings.getWarSiegeBattleSessionsStartTimeClip();
		int currentMinutes = LocalDateTime.now().getMinute();
		if(currentMinutes <= startTimeClip) {
			//The next session will occur this hour
			minutesUntilStart = startTimeClip - currentMinutes -1;
		} else {
			//This next session will occur next hour
			minutesUntilStart = (60 - currentMinutes) + startTimeClip;
		}
		millisUntilStart = minutesUntilStart * ONE_MINUTE_IN_MILLIS;
		startTimeOfNextSession = System.currentTimeMillis() + millisUntilStart;
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
				//Record primary government of besieged town
				if(SiegeWarSettings.isNationSiegeImmunityEnabled())
					siege.recordPrimaryTownGovernment();

				//If any battle points were gained, calculate a result
				if(siege.getAttackerBattlePoints() > 0 || siege.getDefenderBattlePoints() > 0) {
					//Adjust the siege balance
					int siegeBalanceAdjustment = calculateSiegeBalanceAdjustment(siege);

					//Apply the battle points of the winner to the siege balance
					siege.adjustSiegeBalance(siegeBalanceAdjustment);

					//Prepare result for messaging
					battleResults.put(siege, siegeBalanceAdjustment);
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
				siege.setWallBreachPoints(0);
				siege.getWallBreachBonusAwardees().clear();
				siege.setNumberOfBannerControlReversals(0);
				siege.clearPlayersWhoWereInTheSiegeZone();
				siege.clearPlayersWhoWereInTheBesiegedTown();

				//Save siege to database
				SiegeController.saveSiege(siege);
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
				scheduleNextBattleSession();
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
	 * Checks if the battle session capping limiter is active for the given resident
	 * 
	 * @return true if the limiter is active, false if the limit is inactive
	 */
	public static boolean isBattleSessionCappingLimiterActiveForResident(Resident resident)  {
		//Return false if the limit is disabled
		int maxDailyPlayerBattleSessions = SiegeWarSettings.getBattleSessionCappingLimiter();
		if(maxDailyPlayerBattleSessions == -1)
			return false;

		//Return true if the limit is set to 0
		if(maxDailyPlayerBattleSessions == 0)
			return true;

		//Return false if current battle session is inactive
		if(!BattleSession.getBattleSession().isActive())
			return false;

		//If player's recent-sessions list is null, initialize it
		String recentBattleSessionsString = ResidentMetaDataController.getRecentBattleSessions(resident);
		if(recentBattleSessionsString == null) {
			ResidentMetaDataController.setRecentBattleSessions(resident,"");
			resident.save();
			recentBattleSessionsString = "";
		}
		
		//Transform recent-sessions string into List
		List<String> recentBattleSessionsList;
		if(recentBattleSessionsString.length() == 0) {
			recentBattleSessionsList = new ArrayList<>();		
		} else {
			String[] recentBattleSessionsArray = recentBattleSessionsString.replaceAll(" ","").split(",");
			recentBattleSessionsList = Arrays.asList(recentBattleSessionsArray);
		}
		
		//Return false if current session is in the player's recent-sessions list
		String startTimeOfCurrentBattleSessionAsString = BattleSession.getBattleSession().getStartTime().toString();
		if(recentBattleSessionsList.contains(startTimeOfCurrentBattleSessionAsString)) 
			return false;			

		//Recalculate recent-sessions list, keeping only entries which are newer then 24 hours old
		List<String> recalculatedRecentBattleSessionsList = new ArrayList<>();
		for(String battleSessionStartTime: recentBattleSessionsList) {
			if(System.currentTimeMillis() - Long.parseLong(battleSessionStartTime) < ONE_DAY_IN_MILLIS) {
				recalculatedRecentBattleSessionsList.add(battleSessionStartTime);
			}
		}
		
		//Save recent-session list if it has changed (will happen if one or more entries have dropped off)
		if(recentBattleSessionsList.size() != recalculatedRecentBattleSessionsList.size()) {
			ResidentMetaDataController.setRecentBattleSessions(resident, recalculatedRecentBattleSessionsList);
			resident.save();
		}

		//Check if player is at their daily limit
		if(recalculatedRecentBattleSessionsList.size() >= maxDailyPlayerBattleSessions) {
			//Player at or over the limit. Return true
			return true;
		} else {
			//Player not at the limit. Add the current session to the recent-sessions list, then return false 
			recalculatedRecentBattleSessionsList.add(BattleSession.getBattleSession().getStartTime().toString());
			ResidentMetaDataController.setRecentBattleSessions(resident, recalculatedRecentBattleSessionsList);
			resident.save();
			return false;
		}
	}

	/**
	 * Gets the formatted time until a player is no longer limited by the max-daily-battle-sessions feature
	 *
	 * @param resident the resident
	 * @return formatted time e.g.  11.2 minutes, 12.8 hours,
	 */
	public static String getFormattedTimeUntilPlayerBattleSessionLimitExpires(Resident resident) {
		//Get list of recent sessions
		String recentBattleSessionsString = ResidentMetaDataController.getRecentBattleSessions(resident);

		//If player's recent-sessions list is null, initialize it
		if(recentBattleSessionsString == null) {
			ResidentMetaDataController.setRecentBattleSessions(resident,"");
			resident.save();
			recentBattleSessionsString = "";
		}

		/*
		 * If the list is blank at this point, it means that the server has limited player sessions to 0
		 * This should be ideally be done via the scheduler
		 * But in any case, lets return something
		*/
		if(recentBattleSessionsString.length()== 0)
			return "?";

		//The 1st entry will be the oldest one. Find out when it will drop off the list
		String stringStartTimeOfOldestSession = recentBattleSessionsString.replace(" ", "").split(",")[0];
		long longStartTimeOfOldestSession = Long.parseLong(stringStartTimeOfOldestSession);
		long millisSinceOldestSessionStarted = System.currentTimeMillis() - longStartTimeOfOldestSession; //will be less than 24 hrs
		long millisUntilOldestSessionDropsOffList = ONE_DAY_IN_MILLIS - millisSinceOldestSessionStarted;

		return TimeMgmt.getFormattedTimeValue(millisUntilOldestSessionDropsOffList);
	}
}
