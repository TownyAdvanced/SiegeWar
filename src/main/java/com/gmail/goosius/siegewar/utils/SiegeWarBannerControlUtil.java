package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BannerControlSession;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.TimeMgmt;
import com.palmergames.util.TimeTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains utility functions related to banner control
 *
 * @author Goosius
 */
public class SiegeWarBannerControlUtil {

	public static void evaluateBannerControl(Siege siege) {
		try {
			if(siege.getStatus() == SiegeStatus.IN_PROGRESS) {
				evaluateBannerControlPoints(siege);
				evaluateExistingBannerControlSessions(siege);
				evaluateNewBannerControlSessions(siege);
				evaluatePlayerGlowing(siege);
			}
		} catch (Exception e) {
			try {
				SiegeWar.severe("Problem evaluating banner control for siege on town: " + siege.getTown().getName());
			} catch (Exception e2) {
				SiegeWar.severe("Problem evaluating banner control for siege: (could not read town name)");
			}
			e.printStackTrace();
		}
	}

	private static void evaluateNewBannerControlSessions(Siege siege) {
		try {
			TownyUniverse universe = TownyUniverse.getInstance();
			Resident resident;

			for(Player player: Bukkit.getOnlinePlayers()) {

				resident = universe.getResident(player.getUniqueId());
	            if (resident == null)
	            	throw new TownyException(Translation.of("msg_err_not_registered_1", player.getName()));

				if(!doesPlayerMeetBasicSessionRequirements(siege, player, resident))
					continue;

				if(!BattleSession.getBattleSession().isActive()) {
					Translatable message = Translatable.of("msg_war_siege_battle_session_break_cannot_get_banner_control",
													SiegeWarBattleSessionUtil.getFormattedTimeUntilNextBattleSessionStarts());
					Messaging.sendErrorMsg(player, message);
					continue;
				}

				if(siege.getBannerControlSessions().containsKey(player))
					continue; // Player already has a control session

				if(siege.getBannerControllingResidents().contains(resident))
					continue;  // Player already on the BC list

				if(SiegeWarBattleSessionUtil.hasResidentExceededTheirSiegeAttendanceLimit(resident)) {
					Translatable message = Translatable.of(
						"msg_capping_limiter_limit_exceeded_warning",
						SiegeWarSettings.getCappingLimiterBattleSessions(),
						SiegeWarBattleSessionUtil.getFormattedTimeUntilPlayerCappingLimitExpires(resident));
					Messaging.sendErrorMsg(player, message);
					continue; // Capping limit reached
				}

				SiegeSide siegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(player, resident.getTown(), siege);

				switch(siegeSide) {
					case ATTACKERS:
						addNewBannerControlSession(siege, player, resident, SiegeSide.ATTACKERS);
						break;
					case DEFENDERS:
						addNewBannerControlSession(siege, player, resident, SiegeSide.DEFENDERS);
						break;
					case NOBODY:
						continue;
				}
			}
		} catch (Exception e) {
			SiegeWar.severe("Problem evaluating new banner control sessions");
			e.printStackTrace();
		}
	}

	private static void addNewBannerControlSession(Siege siege, final Player player, Resident resident, SiegeSide siegeSide) {
		//Add session
		int sessionDurationMillis = (int)(SiegeWarSettings.getWarSiegeBannerControlSessionDurationMinutes() * TimeMgmt.ONE_MINUTE_IN_MILLIS);
		long sessionEndTime = System.currentTimeMillis() + sessionDurationMillis;
		BannerControlSession bannerControlSession =
			new BannerControlSession(resident, player, siegeSide, sessionEndTime);
		siege.addBannerControlSession(player, bannerControlSession);

		//Notify Player in console
		String messageKey = SiegeWarSettings.isTrapWarfareMitigationEnabled() ? "msg_siege_war_banner_control_session_started_with_altitude" : "msg_siege_war_banner_control_session_started";
		String sessionDurationText = TimeMgmt.getFormattedTimeValue(sessionDurationMillis);
		Messaging.sendMsg(player,
			Translatable.of(messageKey,
			TownySettings.getTownBlockSize(),
			SiegeWarSettings.getBannerControlVerticalDistanceUpBlocks(),
			SiegeWarSettings.getBannerControlVerticalDistanceDownBlocks(),
			sessionDurationText));

		//Notify player in action bar
		ChatColor bossBarMessageColor = ChatColor.valueOf(SiegeWarSettings.getBannerControlCaptureMessageColor().toUpperCase());
		String actionBarMessage = bossBarMessageColor + Translatable.of("msg_siege_war_banner_control_remaining_session_time", sessionDurationText).forLocale(bannerControlSession.getPlayer());
		BossBarUtil.updateBannerCapBossBar(bannerControlSession.getPlayer(), actionBarMessage, bannerControlSession);

		CosmeticUtil.evaluateBeacon(player, siege);

		//If this is a switching session, notify participating nations/towns
		if(siegeSide != siege.getBannerControllingSide()) {

			boolean firstControlSwitchingSession = true;
			for (BannerControlSession otherSession : siege.getBannerControlSessions().values()) {
				if (otherSession != bannerControlSession
					&& otherSession.getSiegeSide() != siege.getBannerControllingSide()) {
					firstControlSwitchingSession = false;
					break;
				}
			}

			if(firstControlSwitchingSession) {
				Translatable message = siegeSide == SiegeSide.ATTACKERS 
					? Translatable.of("msg_siege_war_attacking_troops_at_siege_banner", siege.getTown().getFormattedName())
					: Translatable.of("msg_siege_war_defending_troops_at_siege_banner", siege.getTown().getFormattedName());

				SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
			}
		}
	}

	private static boolean doesPlayerMeetBasicSessionRequirements(Siege siege, Player player, Resident resident) throws Exception {
		if(!player.isOnline())
			return false; // Player offline

		if(player.isDead())
			return false; // Player is dead

		if(player.getWorld() != siege.getFlagLocation().getWorld())
			return false; //Player not in same world as siege

		if (!resident.hasTown())
			return false; //Player is a nomad

		if(resident.getTownOrNull().isNeutral())
			return false; //Player if from a neutral town

		if(player.isFlying() || player.isGliding())
			return false;   // Player is flying

		if (player.getGameMode() == GameMode.SPECTATOR)
			return false; // Player is spectating

		if(!SiegeWarScoringUtil.isPlayerInTimedPointZone(player, siege))
			return false; //player is not in the timed point zone

		if(SiegeWarSettings.isTrapWarfareMitigationEnabled()
			&& player.getLocation().getY() < siege.getFlagLocation().getY())
			return false; //Player is below the siege banner

		return true;
	}

	private static void evaluateExistingBannerControlSessions(Siege siege) {
		String inProgressMessage;
		String remainingSessionTime;
		ChatColor bossBarMessageColor = ChatColor.valueOf(SiegeWarSettings.getBannerControlCaptureMessageColor().toUpperCase());

		if(!BattleSession.getBattleSession().isActive())
			return;
		
		for(BannerControlSession bannerControlSession: siege.getBannerControlSessions().values()) {
			try {
				//Check if session failed
				if (!doesPlayerMeetBasicSessionRequirements(siege, bannerControlSession.getPlayer(), bannerControlSession.getResident())) {
					siege.removeBannerControlSession(bannerControlSession);
					Translatable errorMessage = SiegeWarSettings.isTrapWarfareMitigationEnabled() ? Translatable.of("msg_siege_war_banner_control_session_failure_with_altitude") : Translatable.of("msg_siege_war_banner_control_session_failure");
					BossBarUtil.removeBannerCapBossBar(bannerControlSession.getPlayer());
					Messaging.sendMsg(bannerControlSession.getPlayer(), errorMessage);
					//Update beacon
					CosmeticUtil.evaluateBeacon(bannerControlSession.getPlayer(), siege);
					//Remove glowing effect
					if(bannerControlSession.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
							@Override
							public void run() {
								bannerControlSession.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
							}
						});
					}
					continue;
				}

				//Check if session is in progress or succeeded. Countdown accurate to 1 second, not less
				if((System.currentTimeMillis() / 1000) < (bannerControlSession.getSessionEndTime() / 1000)) {
					//Session still in progress
					remainingSessionTime = TimeMgmt.getFormattedTimeValue(bannerControlSession.getSessionEndTime() - System.currentTimeMillis());
					inProgressMessage = bossBarMessageColor + Translatable.of("msg_siege_war_banner_control_remaining_session_time", remainingSessionTime).forLocale(bannerControlSession.getPlayer());
					BossBarUtil.updateBannerCapBossBar(bannerControlSession.getPlayer(), inProgressMessage, bannerControlSession);
				} else {
					//Session success
					siege.removeBannerControlSession(bannerControlSession);
					//Update beacon
					CosmeticUtil.evaluateBeacon(bannerControlSession.getPlayer(), siege);
					//Remove bossbar
					BossBarUtil.removeBannerCapBossBar(bannerControlSession.getPlayer());
					//Remove glowing effect
					if(bannerControlSession.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
							@Override
							public void run() {
								bannerControlSession.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
							}
						});
					}

					//Mark the player as having capped (info is used by capping limiter)
					SiegeWarBattleSessionUtil.markResidentAsHavingCappedAtCurrentBattleSession(bannerControlSession.getResident());

					//Update siege
					if(bannerControlSession.getSiegeSide() == siege.getBannerControllingSide()) {
						//Player contributes to ongoing banner control
						siege.addBannerControllingResident(bannerControlSession.getResident());
						Messaging.sendMsg(bannerControlSession.getPlayer(), Translatable.of("msg_siege_war_banner_control_session_success"));
					} else {
						//Player gains banner control for their side
						boolean reversal = false;
						if(siege.getBannerControllingSide() != SiegeSide.NOBODY
							&& bannerControlSession.getSiegeSide() != siege.getBannerControllingSide()) {
							reversal = true;
							//Apply reversal bonus if required setting is enabled
							if(SiegeWarSettings.isWarSiegeBannerControlReversalBonusEnabled()) {
								siege.setNumberOfBannerControlReversals(siege.getNumberOfBannerControlReversals()+1);
							}
						}
						siege.clearBannerControllingResidents();
						siege.setBannerControllingSide(bannerControlSession.getSiegeSide());
						siege.addBannerControllingResident(bannerControlSession.getResident());

						//Inform player
						Messaging.sendMsg(bannerControlSession.getPlayer(), Translatable.of("msg_siege_war_banner_control_session_success"));
						//Inform town/nation participants
						Translatable[] message = new Translatable[2];
						if(reversal) {
							if (bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS) {
								message[0] = Translatable.of("msg_siege_war_banner_control_reversed_by_attacker", siege.getTown().getFormattedName());
							} else {
								message[0] = Translatable.of("msg_siege_war_banner_control_reversed_by_defender", siege.getTown().getFormattedName());
							}
							if(SiegeWarSettings.isWarSiegeBannerControlReversalBonusEnabled()) {
								double battlePointMultiplierDouble = siege.getNumberOfBannerControlReversals() * SiegeWarSettings.getWarSiegeBannerControlReversalBonusFactor();
								DecimalFormat decimalFormat = new DecimalFormat("#.##");
								message[1] = Translatable.of("msg_siege_war_banner_control_reversal_bonus", decimalFormat.format(battlePointMultiplierDouble));
							}
						} else {
							if (bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS) {
								message[0] = Translatable.of("msg_siege_war_banner_control_gained_by_attacker", siege.getTown().getFormattedName());
							} else {
								message[0] = Translatable.of("msg_siege_war_banner_control_gained_by_defender", siege.getTown().getFormattedName());
							}
						}
						SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				SiegeWar.severe("Problem evaluating banner control session for player " + bannerControlSession.getPlayer().getName());
			}
		}
	}

	private static void evaluateBannerControlPoints(Siege siege) {
		if(!BattleSession.getBattleSession().isActive())
			return;

		//Evaluate the siege zone only if the siege is 'in progress'.
		if(siege.getStatus() != SiegeStatus.IN_PROGRESS)
			return;

		//Award battle points
		int battlePoints = 0;
		switch(siege.getBannerControllingSide()) {
			case ATTACKERS:
				battlePoints = siege.getBannerControllingResidents().size() * SiegeWarSettings.getWarBattlePointsForAttackerOccupation();
				if(siege.getNumberOfBannerControlReversals() > 0)
					battlePoints *= siege.getNumberOfBannerControlReversals() * SiegeWarSettings.getWarSiegeBannerControlReversalBonusFactor();
				siege.adjustAttackerBattlePoints(battlePoints);
			break;
			case DEFENDERS:
				battlePoints = siege.getBannerControllingResidents().size() * SiegeWarSettings.getWarBattlePointsForDefenderOccupation();
				if(siege.getNumberOfBannerControlReversals() > 0)
					battlePoints *= siege.getNumberOfBannerControlReversals() * SiegeWarSettings.getWarSiegeBannerControlReversalBonusFactor();
				siege.adjustDefenderBattlePoints(battlePoints);
			break;
			default:
		}

		//Save siege to db
		SiegeController.saveSiege(siege);
	}

	/**
	 * This method evaluates whether players should be made to glow
	 * 
	 * Glowing Rules:
	 * - The capper with the lowest time-to-cap on each side, glows.
	 * 
	 * @param siege the siege
	 */
	private static void evaluatePlayerGlowing(Siege siege) throws Exception {
		if (!SiegeWarSettings.isGlowingEnabled())
			return;

		//Create attacker and defender capper maps
		Set<BannerControlSession> attackerSessions = new HashSet<>();
		Set<BannerControlSession> defenderSessions = new HashSet<>();
		for(BannerControlSession bannerControlSession: siege.getBannerControlSessions().values()) {
			if(bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS) {
				attackerSessions.add(bannerControlSession);
			} else {
				defenderSessions.add(bannerControlSession);
			}
		}

		//Ensure glowing
		grantGlowingToSessionWithLowestTimer(attackerSessions);
		grantGlowingToSessionWithLowestTimer(defenderSessions);
	}

	/**
	 * Given a list of sessions, grant glowing to the session with the lowest timer.
	 * Ensure all others do NOT glow.
	 *
	 * @param sessions given list of sessions.
	 */
	private static void grantGlowingToSessionWithLowestTimer(Set<BannerControlSession> sessions) {
		//Calculate glow duration
		int effectDurationSeconds = SiegeWarSettings.getWarSiegeBannerControlSessionDurationMinutes() * 60; 
		final int effectDurationTicks = (int)(TimeTools.convertToTicks(effectDurationSeconds));

		if(sessions.size() > 0) {
			//Find player with lowest timer
			BannerControlSession glowingSession = null;
			for(BannerControlSession bannerControlSession: sessions) {
				if(glowingSession == null
					|| bannerControlSession.getSessionEndTime() < glowingSession.getSessionEndTime()) {
					glowingSession = bannerControlSession;
				}
			}
			//Ensure the player glows
			BannerControlSession glowingSessionFinal = glowingSession;
			Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
			public void run() {
					List<PotionEffect> potionEffects = new ArrayList<>();
					potionEffects.add(new PotionEffect(PotionEffectType.GLOWING, effectDurationTicks, 0));
					glowingSessionFinal.getPlayer().addPotionEffects(potionEffects);
				}
			});
			//Ensure nobody else glows
			for(BannerControlSession bannerControlSession: sessions) {
				if(bannerControlSession == glowingSession)
					continue;
				if(bannerControlSession.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
						public void run() {
							bannerControlSession.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
						}
					});
				}
			}
		}
	}

}
