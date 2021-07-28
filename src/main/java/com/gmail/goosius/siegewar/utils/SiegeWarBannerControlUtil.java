package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.objects.BannerControlSession;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;
import com.palmergames.util.TimeTools;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

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
			}
		} catch (Exception e) {
			try {
				System.err.println("Problem evaluating banner control for siege on town: " + siege.getTown().getName());
			} catch (Exception e2) {
				System.err.println("Problem evaluating banner control for siege: (could not read town name)");
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
					String message = Translation.of("msg_war_siege_battle_session_break_cannot_get_banner_control",
													SiegeWarBattleSessionUtil.getFormattedTimeUntilNextBattleSessionStarts());
					Messaging.sendErrorMsg(player, message);
					continue;
				}

				if(siege.getBannerControlSessions().containsKey(player))
					continue; // Player already has a control session

				if(siege.getBannerControllingResidents().contains(resident))
					continue;  // Player already on the BC list

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
			System.err.println("Problem evaluating new banner control sessions");
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

		//Notify Player
		String messageKey = SiegeWarSettings.isTrapWarfareMitigationEnabled() ? "msg_siege_war_banner_control_session_started_with_altitude" : "msg_siege_war_banner_control_session_started";
		Messaging.sendMsg(player, String.format(
			Translation.of(messageKey),
			TownySettings.getTownBlockSize(),
			SiegeWarSettings.getBannerControlVerticalDistanceBlocks(),
			TimeMgmt.getFormattedTimeValue(sessionDurationMillis)));

		CosmeticUtil.evaluateBeacon(player, siege);

		//Make player glow (which also shows them a timer)
		if (SiegeWarSettings.getWarSiegeEnablePlayerGlowing()) {
			int effectDurationSeconds = (SiegeWarSettings.getWarSiegeBannerControlSessionDurationMinutes() * 60) + (int) TownySettings.getShortInterval();
			final int effectDurationTicks = (int) (TimeTools.convertToTicks(effectDurationSeconds));
			Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
				public void run() {
					List<PotionEffect> potionEffects = new ArrayList<>();
					potionEffects.add(new PotionEffect(PotionEffectType.GLOWING, effectDurationTicks, 0));
					player.addPotionEffects(potionEffects);
				}
			});
		}

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
				String message;
				if (siegeSide == SiegeSide.ATTACKERS) {
					message = Translation.of("msg_siege_war_attacking_troops_at_siege_banner", siege.getTown().getFormattedName());
				} else {
					message = Translation.of("msg_siege_war_defending_troops_at_siege_banner", siege.getTown().getFormattedName());
				}

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

		if(player.isFlying() || player.isGliding())
			return false;   // Player is flying

		if (player.getGameMode() == GameMode.SPECTATOR)
			return false; // Player is spectating

		if(!SiegeWarScoringUtil.isPlayerInTimedPointZone(player, siege))
			return false; //player is not in the timed point zone

		if(SiegeWarSettings.isTrapWarfareMitigationEnabled()
			&& SiegeWarDistanceUtil.isBelowSiegeBannerAltitude(player.getLocation(), siege))
			return false; //Player is below the siege banner

		return true;
	}

	private static void evaluateExistingBannerControlSessions(Siege siege) {
		if(!BattleSession.getBattleSession().isActive())
			return;

		for(BannerControlSession bannerControlSession: siege.getBannerControlSessions().values()) {
			try {
				//Check if session failed
				if (!doesPlayerMeetBasicSessionRequirements(siege, bannerControlSession.getPlayer(), bannerControlSession.getResident())) {
					siege.removeBannerControlSession(bannerControlSession);

					String errorMessage = SiegeWarSettings.isTrapWarfareMitigationEnabled() ? Translation.of("msg_siege_war_banner_control_session_failure_with_altitude") : Translation.of("msg_siege_war_banner_control_session_failure");
					Messaging.sendMsg(bannerControlSession.getPlayer(), errorMessage);
					CosmeticUtil.evaluateBeacon(bannerControlSession.getPlayer(), siege);

					if (SiegeWarSettings.getWarSiegeEnablePlayerGlowing()) {
						if (bannerControlSession.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
							Towny.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Towny.getPlugin(), new Runnable() {
								public void run() {
									bannerControlSession.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
								}
							});
						}
						continue;
					}
				}

				//Check if session succeeded
				if(System.currentTimeMillis() > bannerControlSession.getSessionEndTime()) {

					//Session success
					siege.removeBannerControlSession(bannerControlSession);

					if(bannerControlSession.getSiegeSide() == siege.getBannerControllingSide()) {
						//The player contributes to ongoing banner control
						siege.addBannerControllingResident(bannerControlSession.getResident());
						Messaging.sendMsg(bannerControlSession.getPlayer(), Translation.of("msg_siege_war_banner_control_session_success"));
					} else {
						//The player gains banner control for their side
						boolean reversal = false;
						int reversalBonusScore = 0;
						if(siege.getBannerControllingSide() != SiegeSide.NOBODY
							&& bannerControlSession.getSiegeSide() != siege.getBannerControllingSide()) {
							reversal = true;
							//Apply reversal bonus if required setting is enabled
							if(SiegeWarSettings.isWarSiegeBannerControlReversalBonusEnabled()) {
								reversalBonusScore = (int)((siege.getTimedBattlePointsEarnedFromCurrentBannerControl() * SiegeWarSettings.getWarSiegeBannerControlReversalBonusFactor()) + 0.5);
								if (bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS) {
									siege.adjustAttackerBattlePoints(reversalBonusScore);
								} else {
									siege.adjustDefenderBattlePoints(reversalBonusScore);
								}
							}
						}
						siege.clearBannerControllingResidents();
						siege.setTimedBattlePointsEarnedFromCurrentBannerControl(0);
						siege.setBannerControllingSide(bannerControlSession.getSiegeSide());
						siege.addBannerControllingResident(bannerControlSession.getResident());

						//Inform player
						Messaging.sendMsg(bannerControlSession.getPlayer(), Translation.of("msg_siege_war_banner_control_session_success"));
						//Inform town/nation participants
						String message;
						if(reversal) {
							if (bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS) {
								message = Translation.of("msg_siege_war_banner_control_reversed_by_attacker", siege.getTown().getFormattedName());
							} else {
								message = Translation.of("msg_siege_war_banner_control_reversed_by_defender", siege.getTown().getFormattedName());
							}
							if(SiegeWarSettings.isWarSiegeBannerControlReversalBonusEnabled()) {
								String sign = bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS ? "+" : "-";
								message += Translation.of("msg_siege_war_banner_control_reversal_bonus", sign, reversalBonusScore);
							}
						} else {
							if (bannerControlSession.getSiegeSide() == SiegeSide.ATTACKERS) {
								message = Translation.of("msg_siege_war_banner_control_gained_by_attacker", siege.getTown().getFormattedName());
							} else {
								message = Translation.of("msg_siege_war_banner_control_gained_by_defender", siege.getTown().getFormattedName());
							}
						}
						SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
						CosmeticUtil.evaluateBeacon(bannerControlSession.getPlayer(), siege);
					}
				}
			} catch (Exception e) {
				System.err.println("Problem evaluating banner control session for player " + bannerControlSession.getPlayer().getName());
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
				battlePoints = SiegeWarScoringUtil.applyBattlePointsAdjustmentForPopulationQuotient(true, battlePoints, siege);
				siege.adjustAttackerBattlePoints(battlePoints);
				if (!siege.getSiegeType().equals(SiegeType.REVOLT))
					siege.registerSuccessfulBattleContributorsFromBannerControl();
			break;
			case DEFENDERS:
				battlePoints = siege.getBannerControllingResidents().size() * SiegeWarSettings.getWarBattlePointsForDefenderOccupation();
				battlePoints = SiegeWarScoringUtil.applyBattlePointsAdjustmentForPopulationQuotient(false, battlePoints, siege);
				siege.adjustDefenderBattlePoints(battlePoints);
				if (siege.getSiegeType().equals(SiegeType.REVOLT))
					siege.registerSuccessfulBattleContributorsFromBannerControl();
			break;
			default:
		}

		//Record gained battle points for use by the 'Banner Control Reversal Bonus' feature
		siege.adjustBattlePointsEarnedFromCurrentBannerControl(battlePoints);
	}
}
