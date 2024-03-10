package com.gmail.goosius.siegewar.settings;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

import com.gmail.goosius.siegewar.SiegeController;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Material;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class SiegeWarSettings {
	
	private static List<DayOfWeek> allowedDaysList = null;
	private static List<Material> siegeZoneWildernessForbiddenBlockMaterials = null;
	private static List<Material> siegeZoneWildernessForbiddenBucketMaterials = null;
	private static List<EntityType> siegeZoneWildernessForbiddenExplodeEntityTypes = null;
	protected static void resetCachedSettings() {
		allowedDaysList = null;
		siegeZoneWildernessForbiddenBlockMaterials = null;
		siegeZoneWildernessForbiddenBucketMaterials = null;
		siegeZoneWildernessForbiddenExplodeEntityTypes = null;
	}

	public static boolean getWarSiegeEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ENABLED);
	}

	public static boolean getWarSiegeAbandonEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ABANDON_ENABLED);
	}

	public static boolean getWarSiegeSurrenderEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_SURRENDER_ENABLED);
	}

	public static boolean getWarSiegeInvadeEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_INVADE_ENABLED);
	}

	public static boolean getWarSiegePlunderEnabled() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_PLUNDER_AMOUNT_PER_PLOT) > 0;
	}

	public static boolean isPlunderPaidOutOverDays() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_PLUNDER_PAID_OVER_TIME);
	}

	public static int plunderDays() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_PLUNDER_DAYS);
	}

	public static boolean getWarSiegeMilitarySalaryEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_MILITARY_SALARY_ENABLED);
	}

	public static boolean getConquestSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_CONQUEST_SIEGES_ENABLED);
	}

	public static boolean getRevoltSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_REVOLT_SIEGES_ENABLED);
	}

	public static boolean getWarSiegeClaimingDisabledNearSiegeZones() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_CLAIMING_DISABLED_NEAR_SIEGE_ZONES);
	}

	public static double getWarSiegeUpfrontCostPerPlot() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_UPFRONT_COST_PER_PLOT);
	}

	public static double getWarSiegeWarchestCostPerPlot() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_WARCHEST_COST_PER_PLOT);
	}

	public static double getSiegeImmunityNewTownsHours() {
		return Settings.getDouble(ConfigNodes.SIEGE_IMMUNITY_TIME_NEW_TOWN_HOURS);
	}

	public static double getSiegeImmunityPostSiegeHours() {
		return Settings.getDouble(ConfigNodes.SIEGE_IMMUNITY_POST_SIEGE_HOURS);
	}

	public static double getRevoltImmunityPostSiegeHours() {
		return Settings.getDouble(ConfigNodes.REVOLT_IMMUNITY_POST_SIEGE_HOURS);
	}

	public static double getWarSiegePlunderAmountPerPlot() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_PLUNDER_AMOUNT_PER_PLOT);
	}

	public static int getSiegeDurationBattleSessions() {
		return Settings.getInt(ConfigNodes.SIEGE_DURATION_BATTLE_SESSIONS);
	}

	public static boolean areBattlePointsWinnerTakesAll() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_END_OF_BATTLE_POINTS_DISTRIBUTION_WINNER_TAKES_ALL);
	}

	public static int getWarBattlePointsForAttackerOccupation() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_BASE_POINTS_BANNER_CONTROL_ATTACKER);
	}

	public static int getWarBattlePointsForDefenderOccupation() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_BASE_POINTS_BANNER_CONTROL_DEFENDER);
	}

	public static int getWarBattlePointsForAttackerDeath() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_BASE_POINTS_BANNER_CONTROL_DEATHS_ATTACKER);
	}

	public static int getWarBattlePointsForDefenderDeath() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_BASE_POINTS_BANNER_CONTROL_DEATHS_DEFENDER);
	}
	
	public static int getWarSiegeZoneRadiusBlocks() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_ZONE_RADIUS_BLOCKS);
	}

	public static boolean getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_NON_RESIDENT_SPAWN_INTO_SIEGE_ZONES_OR_BESIEGED_TOWNS_DISABLED);
	}

	public static int getWarSiegeMaxActiveSiegeAttacksPerNation() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_MAX_ACTIVE_SIEGE_ATTACKS_PER_NATION);
	}

	public static boolean doesThisNationHaveTooManyActiveSieges(Nation nation) {
		return SiegeController.getNumActiveConquestAttackSieges(nation) >= getWarSiegeMaxActiveSiegeAttacksPerNation();
	}

	public static boolean getWarCommonPeacefulTownsEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_ENABLED);
	}
	
	public static boolean capitalsAllowedTownPeacefulness() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_CAPITALS_ENABLED);
	}

	public static boolean getNewTownPeacefulness() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_NEW_TOWN_PEACEFULNESS);
	}

	public static int getWarCommonPeacefulTownsConfirmationRequirementDays() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_CONFIRMATION_REQUIREMENT_DAYS);
	}

	public static boolean getWarSiegeBesiegedTownRecruitmentDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_BESIEGED_TOWN_RECRUITMENT_DISABLED);
	}

	public static boolean getWarSiegeBesiegedTownClaimingDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_BESIEGED_TOWN_CLAIMING_DISABLED);
	}

	public static boolean getWarSiegeBesiegedTownUnClaimingDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_BESIEGED_TOWN_UNCLAIMING_DISABLED);
	}
	
	public static boolean getWarSiegeBesiegedCapitalsCannotChangeKing() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_BESIEGED_TOWN_CANNOT_CHANGE_KING);
	}

	public static int getWarSiegeExtraMoneyPercentagePerTownLevel() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_EXTRA_MONEY_PERCENTAGE_PER_TOWN_LEVEL);
	}

	public static double getMaxOccupationTaxPerPlot() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MAX_OCCUPATION_TAX_PER_PLOT);
	}

	public static int getWarSiegeBannerControlSessionDurationMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_SESSION_DURATION_MINUTES);
	}

	public static boolean getWarCommonOccupiedTownUnClaimingDisabled() {
		return Settings.getBoolean(ConfigNodes.OCCUPIED_TOWN_UNCLAIMING_DISABLED);
	}

	public static boolean isWarSiegeCounterattackBoosterEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_COUNTERATTACK_BOOSTER_ENABLED);
	}

	public static double getWarSiegeCounterattackBoosterExtraDeathPointsPerPlayerPercentage() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_COUNTERATTACK_BOOSTER_EXTRA_DEATH_POINTS_PER_PLAYER_PERCENTAGE);
	}

	public static List<Material> getSiegeZoneWildernessForbiddenBlockMaterials() {
		if(siegeZoneWildernessForbiddenBlockMaterials == null) {
			siegeZoneWildernessForbiddenBlockMaterials = new ArrayList<>();
			String listAsString = Settings.getString(ConfigNodes.SIEGE_MATERIAL_RESTRICTIONS_WILDERNESS_BLOCK_PLACEMENT_PREVENTION_MATERIALS);
			String[] listAsStringArray = listAsString.split(",");
			for (String blockTypeAsString : listAsStringArray) {
				Material material = Material.matchMaterial(blockTypeAsString.trim());
				siegeZoneWildernessForbiddenBlockMaterials.add(material);
			}
		}
		return siegeZoneWildernessForbiddenBlockMaterials;
	}

	public static List<Material> getSiegeZoneWildernessForbiddenBucketMaterials() {
		if(siegeZoneWildernessForbiddenBucketMaterials == null) {
			siegeZoneWildernessForbiddenBucketMaterials = new ArrayList<>();
			String listAsString = Settings.getString(ConfigNodes.SIEGE_MATERIAL_RESTRICTIONS_WILDERNESS_BUCKET_EMPTYING_PREVENTION_MATERIALS);
			String[] listAsStringArray = listAsString.split(",");
			for (String blockTypeAsString : listAsStringArray) {
				Material material = Material.matchMaterial(blockTypeAsString.trim());
				siegeZoneWildernessForbiddenBucketMaterials.add(material);
			}
		}
		return siegeZoneWildernessForbiddenBucketMaterials;
	}

	public static List<EntityType> getSiegeZoneWildernessForbiddenExplodeEntityTypes() {
		if(siegeZoneWildernessForbiddenExplodeEntityTypes == null) {
			siegeZoneWildernessForbiddenExplodeEntityTypes = new ArrayList<>();
			String listAsString = Settings.getString(ConfigNodes.SIEGE_MATERIAL_RESTRICTIONS_WILDERNESS_EXPLOSION_PREVENTION_ENTITY_TYPES);
			String[] listAsStringArray = listAsString.split(",");
			for (String entityTypeAsString : listAsStringArray) {
				EntityType entityType = EntityType.valueOf(entityTypeAsString.trim().toUpperCase(Locale.ROOT));
				siegeZoneWildernessForbiddenExplodeEntityTypes.add(entityType);
			}
		}
		return siegeZoneWildernessForbiddenExplodeEntityTypes;
	}
	
	public static boolean isPeacefulTownPublicSpawnEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_PUBLIC_SPAWN_ENABLED);
	}

	public static boolean isPeacefulTownsSubvertEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_SUBVERT_ENABLED);
	}

	public static int getPeacefulTownsGuardianTownSearchRadius() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_GUARDIAN_TOWN_SEARCH_RADIUS);
	}

	public static int getWarCommonPeacefulTownsNewTownConfirmationRequirementDays() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_NEW_TOWN_CONFIRMATION_REQUIREMENT_DAYS);
	}

	public static boolean getPunishingNonSiegeParticipantsInSiegeZone() {
		return Settings.getBoolean(ConfigNodes.ENABLE_SICKNESS);
	}

	public static int getNonResidentSicknessWarningTimeSeconds() {
		return Settings.getInt(ConfigNodes.SECONDS_BEFORE_SICKNESS);
	}

	public static double getWarSiegeCapitalCostIncreasePercentage() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_CAPITAL_SIEGE_COST_INCREASE_PERCENTAGE);
	}

	public static boolean getWarSiegeNationStatisticsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_NATION_STATISTICS_ENABLED);
	}

	public static boolean getWarSiegeDeathSpawnFireworkEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_DEATH_SPAWN_FIREWORK);
	}

	public static boolean getBeaconsEnabled() {
		return Settings.getBoolean(ConfigNodes.BEACON_MARKERS_ENABLED);
	}

	public static String getBeaconCaptureColor() {
		return Settings.getString(ConfigNodes.BEACON_MARKERS_CAPTURE_COLOR);
	}

	public static String getBeaconCapturedColor() {
		return Settings.getString(ConfigNodes.BEACON_MARKERS_CAPTURED_COLOR);
	}

	public static String getBeaconEnemyColor() {
		return Settings.getString(ConfigNodes.BEACON_MARKERS_ENEMY_COLOR);
	}

	public static boolean isWarSiegeBannerControlReversalBonusEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_BANNER_CONTROL_REVERSAL_BONUS_ENABLED);
	}

	public static double getWarSiegeBannerControlReversalBonusFactor() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_BANNER_CONTROL_REVERSAL_BONUS_MULTIPLIER_VALUE);
	}

	public static boolean isBannerXYZTextEnabled() {
		return Settings.getBoolean(ConfigNodes.BANNER_XYZ_TEXT_ENABLED);
	}

	public static boolean isBesiegedTownTownTrapWarfareMitigationEnabled() {
		return Settings.getBoolean(ConfigNodes.TRAP_WARFARE_MITIGATION_BESIEGED_TOWN_ENABLED);
	}

	public static int getBesiegedTownTrapWarfareMitigationRadius() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_BESIEGED_TOWN_RADIUS);
	}

	public static boolean isBannerAtTownBorderEnabled() {
		return Settings.getBoolean(ConfigNodes.TRAP_WARFARE_MITIGATION_BESIEGED_TOWN_BANNER_AT_BORDER_ENABLED);
	}

	public static boolean isWildernessTrapWarfareMitigationEnabled() {
		return Settings.getBoolean(ConfigNodes.TRAP_WARFARE_MITIGATION_WILDERNESS_ENABLED);
	}

	public static int getWildernessTrapWarfareMitigationRadiusBlocks() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_WILDERNESS_RADIUS);
	}

	public static int getWildernessTrapWarfareMitigationUpperHeightLimit() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_WILDERNESS_UPPER_HEIGHT_LIMIT);
	}

	public static int getWildernessTrapWarfareMitigationLowerHeightLimit() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_WILDERNESS_LOWER_HEIGHT_LIMIT);
	}

	public static String getBannerControlCaptureMessageColor() {
		return Settings.getString(ConfigNodes.BANNER_CONTROL_CAPTURE_MESSAGE_COLOR);
	}

	public static boolean areSiegeCampsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_SIEGECAMPS_ENABLED);
	}
	
	public static long getFailedSiegeCampCooldown() {
		return Settings.getSeconds(ConfigNodes.WAR_SIEGE_SIEGECAMPS_COOLDOWN);
	}
	
	public static int getSiegeCampPointsForSuccess() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_SIEGECAMPS_POINTS);
	}
	
	public static int getSiegeCampPointsPerMinute() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_SIEGECAMPS_POINTS_PER_MINUTE);
	}
	
	public static int getSiegeCampDurationInMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_SIEGECAMPS_DURATION_IN_MINUTES);
	}

	public static List<DayOfWeek> getSiegeStartDayLimiterAllowedDays() {
		List<DayOfWeek> allowedDaysList = new ArrayList<>();
		String[] allowedDaysStringArray = Settings.getString(ConfigNodes.SIEGE_START_DAY_LIMITER_ALLOWED_DAYS).toUpperCase(Locale.ROOT).replaceAll(" ", "").split(",");

			DayOfWeek allowedDay;
			for(String allowedDayString: allowedDaysStringArray) {
				allowedDay = DayOfWeek.valueOf(allowedDayString);
				allowedDaysList.add(allowedDay);
			}

		return  allowedDaysList;
	}

	public static boolean doesTodayAllowASiegeToStart() {
		if (allowedDaysList == null)
			allowedDaysList = getSiegeStartDayLimiterAllowedDays();
		return allowedDaysList.contains(LocalDate.now().getDayOfWeek());
	}

	public static int getSiegeBalanceCapValue() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_SIEGE_BALANCE_CAP_VALUE);
	}

	public static boolean isStopTownyPlotPvpProtection() {
		return Settings.getBoolean(ConfigNodes.PVP_PROTECTION_OVERRIDES_STOP_TOWNY_PLOT_PVP_PROTECTION);
	}

	public static boolean isStopTownyFriendlyFireProtection() {
		return Settings.getBoolean(ConfigNodes.PVP_PROTECTION_OVERRIDES_STOP_TOWNY_FRIENDLY_FIRE_PROTECTION);
	}

	public static boolean isStopAllPvpProtection() {
		return Settings.getBoolean(ConfigNodes.PVP_PROTECTION_OVERRIDES_STOP_ALL_PVP_PROTECTION);
	}

	public static List<LocalDateTime> getAllBattleSessionStartTimesForToday() {
		return getAllBattleSessionStartTimesForDay(LocalDate.now());
	}

	@Nullable
	public static LocalDateTime getNextBattleSessionDaysInAdvance() {
		LocalDateTime nextSession = null;
		// Check the next 1-6 days for battle session start times. 
		for (int i = 1 ; i < 7 ; i++) {
			List<LocalDateTime> allBattleSessionStartTimesForDate = getAllBattleSessionStartTimesForDay(LocalDate.now().plusDays(i));
			if (allBattleSessionStartTimesForDate.size() != 0) {
				nextSession = allBattleSessionStartTimesForDate.get(0);
				break;
			}
		}
		return nextSession;
	}

	private static List<LocalDateTime> getAllBattleSessionStartTimesForDay(LocalDate day) {
		//Get Start times for the given day
		String startTimesAsString = "";
		switch (day.getDayOfWeek()) {
			case MONDAY:
				startTimesAsString = getBattleSessionStartTimesMonday();
				break;
			case TUESDAY:
				startTimesAsString = getBattleSessionStartTimesTuesday();
				break;
			case WEDNESDAY:
				startTimesAsString = getBattleSessionStartTimesWednesday();
				break;
			case THURSDAY:
				startTimesAsString = getBattleSessionStartTimesThursday();
				break;
			case FRIDAY:
				startTimesAsString = getBattleSessionStartTimesFriday();
				break;
			case SATURDAY:
				startTimesAsString = getBattleSessionStartTimesSaturday();
				break;
			case SUNDAY:
				startTimesAsString = getBattleSessionStartTimesSunday();
				break;
		}

		//Transform the config file strings into a list of LocalDateTime objects
		List<LocalDateTime> startTimesAsList = new ArrayList<>();	
		if(startTimesAsString.length() > 0) {		
			String[] startTimeAsHourMinutePair;		
			LocalDateTime startTime;
			for(String startTimeAsString: startTimesAsString.split(",")) {
				if (startTimeAsString.contains(":")) {
					startTimeAsHourMinutePair = startTimeAsString.split(":");
					startTime = LocalDateTime.of(day, LocalTime.of(Integer.parseInt(startTimeAsHourMinutePair[0]), Integer.parseInt(startTimeAsHourMinutePair[1])));
				} else {
					startTime = LocalDateTime.of(day, LocalTime.of(Integer.parseInt(startTimeAsString), 0));
				}
				startTimesAsList.add(startTime);	
			}
		}
		return startTimesAsList;
	}

	private static String getBattleSessionStartTimesMonday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_MONDAY);
	}

	private static String getBattleSessionStartTimesTuesday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_TUESDAY);
	}

	private static String getBattleSessionStartTimesWednesday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_WEDNESDAY);
	}

	private static String getBattleSessionStartTimesThursday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_THURSDAY);
	}

	private static String getBattleSessionStartTimesFriday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_FRIDAY);
	}

	private static String getBattleSessionStartTimesSaturday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_SATURDAY);
	}

	private static String getBattleSessionStartTimesSunday() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_SUNDAY);
	}

	public static int getWarSiegeBattleSessionsDurationMinutes() {
		return Settings.getInt(ConfigNodes.BATTLE_SESSION_SCHEDULER_DURATION_MINUTES);
	}

	public static boolean cancelBattleSessionWhenNoActiveSieges() {
		return Settings.getBoolean(ConfigNodes.BATTLE_SESSION_SCHEDULER_CANCEL_SESSION_WHEN_NO_SIEGES);
	}

	public static boolean isUnjailingAttackerResidents() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_UNJAIL_RESIDENTS_WHEN_ATTACKERS_WIN_SESSION);
	}

	public static boolean isGlowingEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_GLOWING);
	}
	public static double getWarSiegeNationCostRefundPercentageOnDelete() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_NATION_COST_REFUND_PERCENTAGE_ON_DELETE);
	}

	public static boolean isKeepInventoryOnSiegeZoneDeathEnabled() {
		return Settings.getBoolean(ConfigNodes.KEEP_INVENTORY_ON_SIEGEZONE_DEATH_ENABLED);
	}

	public static double getKeepInventoryOnSiegeZoneDeathToolsDegradePercentage() {
		return Settings.getDouble(ConfigNodes.KEEP_INVENTORY_ON_SIEGEZONE_DEATH_TOOL_DEGRADE_PERCENTAGE);
	}

	public static int getSpecialVictoryEffectsDecisiveVictoryThreshold() {
		return Settings.getInt(ConfigNodes.SPECIAL_VICTORY_EFFECTS_DECISIVE_VICTORY_THRESHOLD);
	}

	public static int getSpecialVictoryEffectsWarchestReductionPercentageOnCloseVictory() {
		return Settings.getInt(ConfigNodes.SPECIAL_VICTORY_EFFECTS_WARCHEST_REDUCTION_PERCENTAGE_ON_CLOSE_VICTORY);
	}

	public static int getSpecialVictoryEffectsPlunderReductionPercentageOnCloseVictory() {
		return Settings.getInt(ConfigNodes.SPECIAL_VICTORY_EFFECTS_PLUNDER_REDUCTION_PERCENTAGE_ON_CLOSE_VICTORY);
	}

	public static int getSpecialVictoryEffectsSiegeBalancePenaltyOnDecisiveRebelVictory() {
		return Settings.getInt(ConfigNodes.SPECIAL_VICTORY_EFFECTS_SIEGE_BALANCE_PENALTY_ON_DECISIVE_REBEL_VICTORY);
	}

	public static int getSpecialVictoryEffectsSiegeBalancePenaltyDurationDays() {
		return Settings.getInt(ConfigNodes.SPECIAL_VICTORY_EFFECTS_SIEGE_BALANCE_PENALTY_DURATION_DAYS);
	}

	public static boolean isToxicityReductionEnabled() {
		return Settings.getBoolean(ConfigNodes.TOXICITY_REDUCTION_ENABLED);
	}

	public static double getToxicityReductionGeneralChatRestorationAfterBattleSessionMinutes() {
		return Settings.getDouble(ConfigNodes.TOXICITY_REDUCTION_GENERAL_CHAT_RESTORATION_AFTER_BATTLE_SESSION_MINUTES);
	}

	//Convenience Method
	public static double getToxicityReductionChatRestorationAfterBattleSessionMillis() {
		return getToxicityReductionGeneralChatRestorationAfterBattleSessionMinutes() * TimeMgmt.ONE_MINUTE_IN_MILLIS;
	}

	public static String getToxicityReductionServerDiscordLink() {
		return Settings.getString(ConfigNodes.TOXICITY_REDUCTION_DISCORD_LINK);
	}

	public static boolean isDiscordWebhookEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_ENABLED);
	}

	public static String getDiscordWebhookUrl() {
		return Settings.getString(ConfigNodes.DISCORD_WEBHOOK_URL);
	}

	public static Color getFallbackColor() {
		return Color.decode(Settings.getString(ConfigNodes.DISCORD_WEBHOOK_FALLBACK_COLOR));
	}

	public static boolean isSessionStartNotificationEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_NOTIFICATION_SESSION_START);
	}

	public static boolean isSessionEndNotificationEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_NOTIFICATION_SESSION_END);
	}

	public static boolean isSiegeCampStartNotificationEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_NOTIFICATION_SIEGECAMP_START);
	}

	public static boolean isSiegeStartNotificationEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_NOTIFICATION_SIEGE_START);
	}

	public static boolean isSiegeEndNotificationEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_NOTIFICATION_SIEGE_END);
	}

	public static boolean isSiegeRemoveNotificationEnabled() {
		return Settings.getBoolean(ConfigNodes.DISCORD_WEBHOOK_NOTIFICATION_SIEGE_REMOVE);
	}

	public static boolean isBadConfigWarningsEnabled() {
		return Settings.getBoolean(ConfigNodes.BAD_CONFIG_WARNINGS_ENABLED);
	}
	public static double getBadConfigWarningsTolerancePercentage() {
		return Settings.getDouble(ConfigNodes.BAD_CONFIG_WARNINGS_TOLERANCE_PERCENTAGE);
	}
	public static double getBadConfigWarningsIdealPlunderPercentage() {
		return Settings.getDouble(ConfigNodes.BAD_CONFIG_WARNINGS_IDEAL_PLUNDER_PERCENTAGE);
	}
	public static double getBadConfigWarningsIdealWarchestPercentage() {
		return Settings.getDouble(ConfigNodes.BAD_CONFIG_WARNINGS_IDEAL_WARCHEST_PERCENTAGE);
	}	
	public static double getBadConfigWarningsIdealUpfrontCostPercentage() {
		return Settings.getDouble(ConfigNodes.BAD_CONFIG_WARNINGS_IDEAL_UPFRONTCOST_PERCENTAGE);
	}	
	public static double getBadConfigWarningsIdealOccupationTaxPercentage() {
		return Settings.getDouble(ConfigNodes.BAD_CONFIG_WARNINGS_IDEAL_OCCUPATIONTAX_PERCENTAGE);
	}
	
	public static boolean isBattleCommandersEnabled() {
		return Settings.getBoolean(ConfigNodes.BATTLE_COMMANDERS_ENABLED);
	}

	public static String getDynmapLayerName() {
		return Settings.getString(ConfigNodes.WAR_SIEGE_DYNMAP_LAYER_NAME);
	}
}
