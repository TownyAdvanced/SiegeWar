package com.gmail.goosius.siegewar.settings;

import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.gmail.goosius.siegewar.objects.HeldItemsCombination;
import org.jetbrains.annotations.Nullable;

public class SiegeWarSettings {
	
	private static List<HeldItemsCombination> mapSneakingItems = new ArrayList<>();
	private static List<String> worldsWithSiegeWarEnabled = new ArrayList<>();
	private static List<Material> battleSessionsForbiddenBlockMaterials = new ArrayList<>();
	private static List<Material> battleSessionsForbiddenBucketMaterials = new ArrayList<>();
	
	protected static void resetSpecialCaseVariables() {
		mapSneakingItems.clear();
		worldsWithSiegeWarEnabled.clear();
		battleSessionsForbiddenBlockMaterials.clear();
		battleSessionsForbiddenBucketMaterials.clear();
	}

	public static boolean getWarSiegeEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ENABLED);
	}

	public static List<String> getWarSiegeWorlds() {
		if (worldsWithSiegeWarEnabled.isEmpty()) {
			String[] worldNamesAsArray = Settings.getString(ConfigNodes.WAR_SIEGE_WORLDS).split(",");
			for (String worldName : worldNamesAsArray) {
				if (Bukkit.getServer().getWorld(worldName.trim()) != null)
					worldsWithSiegeWarEnabled.add(Bukkit.getServer().getWorld(worldName.trim()).getName());
			}
		}
		return worldsWithSiegeWarEnabled;
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
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_PLUNDER_ENABLED);
	}

	public static boolean getWarSiegeMilitarySalaryEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_MILITARY_SALARY_ENABLED);
	}

	public static boolean getConquestSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_CONQUEST_SIEGES_ENABLED);
	}

	public static boolean getLiberationSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_LIBERATION_SIEGES_ENABLED);
	}

	public static boolean getRevoltSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_REVOLT_SIEGES_ENABLED);
	}

	public static boolean getSuppressionSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_SUPPRESSION_SIEGES_ENABLED);
	}

	public static boolean getWarSiegePvpAlwaysOnInBesiegedTowns() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_PVP_ALWAYS_ON_IN_BESIEGED_TOWNS);
	}

	public static boolean getWarSiegeClaimingDisabledNearSiegeZones() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_CLAIMING_DISABLED_NEAR_SIEGE_ZONES);
	}

	public static int getWarSiegeMaxAllowedBannerToTownDownwardElevationDifference() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_MAX_ALLOWED_BANNER_TO_TOWN_DOWNWARD_ELEVATION_DIFFERENCE);
	}

	public static double getWarSiegeAttackerCostUpFrontPerPlot() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_ATTACKER_COST_UPFRONT_PER_PLOT);
	}

	public static double getWarSiegeSiegeImmunityTimeNewTownsHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_SIEGE_IMMUNITY_TIME_NEW_TOWN_HOURS);
	}

	public static double getWarSiegeSiegeImmunityTimeModifier() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_SIEGE_IMMUNITY_TIME_MODIFIER);
	}

	public static double getWarSiegeRevoltImmunityTimeModifier() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_REVOLT_IMMUNITY_TIME_MODIFIER);
	}

	public static double getWarSiegeAttackerPlunderAmountPerPlot() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_ATTACKER_PLUNDER_AMOUNT_PER_PLOT);
	}

	public static String getWarSiegePlunderDistributionRatio() {
		return Settings.getString(ConfigNodes.WAR_SIEGE_PLUNDER_DISTRIBUTION_RATIO);
	}

	public static double getWarSiegeMaxHoldoutTimeHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MAX_HOLDOUT_TIME_HOURS);
	}
	
	public static double getWarSiegeMinSiegeDurationBeforeSurrenderHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_SURRENDER_HOURS);
	}

	public static double getWarSiegeMinSiegeDurationBeforeAbandonHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_ABANDON_HOURS);
	}

	public static int getWarBattlePointsForAttackerOccupation() {
		return Settings.getInt(ConfigNodes.WAR_BATTLE_POINTS_FOR_ATTACKER_OCCUPATION);
	}

	public static int getWarBattlePointsForDefenderOccupation() {
		return Settings.getInt(ConfigNodes.WAR_BATTLE_POINTS_FOR_DEFENDER_OCCUPATION);
	}

	public static int getWarBattlePointsForAttackerDeath() {
		return Settings.getInt(ConfigNodes.WAR_BATTLE_POINTS_FOR_ATTACKER_DEATH);
	}

	public static int getWarBattlePointsForDefenderDeath() {
		return Settings.getInt(ConfigNodes.WAR_BATTLE_POINTS_FOR_DEFENDER_DEATH);
	}
	
	public static int getWarSiegeZoneRadiusBlocks() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_ZONE_RADIUS_BLOCKS);
	}

	public static boolean getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_NON_RESIDENT_SPAWN_INTO_SIEGE_ZONES_OR_BESIEGED_TOWNS_DISABLED);
	}

	public static double getWarSiegeNationCostRefundPercentageOnDelete() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_NATION_COST_REFUND_PERCENTAGE_ON_DELETE);
	}

	public static int getWarSiegeMaxActiveSiegeAttacksPerNation() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_MAX_ACTIVE_SIEGE_ATTACKS_PER_NATION);
	}

	public static boolean getWarSiegeRefundInitialNationCostOnDelete() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_REFUND_INITIAL_NATION_COST_ON_DELETE);
	}

	public static boolean getWarCommonPeacefulTownsEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_ENABLED);
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

	public static boolean getWarSiegeDeathPenaltyKeepInventoryEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_DEATH_PENALTY_KEEP_INVENTORY_ENABLED);
	}

	public static boolean getWarSiegeDeathPenaltyDegradeInventoryEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_DEATH_PENALTY_DEGRADE_INVENTORY_ENABLED);
	}

	public static double getWarSiegeDeathPenaltyDegradeInventoryPercentage() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_DEATH_PENALTY_DEGRADE_INVENTORY_PERCENTAGE);
	}

	public static int getWarSiegeExtraMoneyPercentagePerTownLevel() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_EXTRA_MONEY_PERCENTAGE_PER_TOWN_LEVEL);
	}

	public static boolean getWarSiegeMapSneakingEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_MAP_SNEAKING_ENABLED);
	}

	public static List<HeldItemsCombination> getWarSiegeMapSneakingItems() {
		try {
			if (mapSneakingItems.isEmpty()) {
				String itemsListAsString = Settings.getString(ConfigNodes.WAR_SIEGE_MAP_SNEAKING_ITEMS);
				String[] itemsListAsArray = itemsListAsString.split(",");
				String[] itemPair;
				boolean ignoreOffHand;
				boolean ignoreMainHand;
				Material offHandItem;
				Material mainHandItem;

				for (String itemAsString : itemsListAsArray) {
					itemPair = itemAsString.trim().split("\\|");

					if(itemPair[0].equalsIgnoreCase("any")) {
						ignoreOffHand = true;
						offHandItem = null;
					} else if (itemPair[0].equalsIgnoreCase("empty")){
						ignoreOffHand = false;
						offHandItem = Material.AIR;
					} else{
						ignoreOffHand = false;
						offHandItem = Material.matchMaterial(itemPair[0]);
					}

					if(itemPair[1].equalsIgnoreCase("any")) {
						ignoreMainHand = true;
						mainHandItem = null;
					} else if (itemPair[1].equalsIgnoreCase("empty")){
						ignoreMainHand = false;
						mainHandItem = Material.AIR;
					} else{
						ignoreMainHand = false;
						mainHandItem = Material.matchMaterial(itemPair[1]);
					}

					mapSneakingItems.add(
						new HeldItemsCombination(offHandItem,mainHandItem,ignoreOffHand,ignoreMainHand));
				}
			}
		} catch (Exception e) {
			System.out.println("Problem reading map sneaking items list. The list is config.yml may be misconfigured.");
			e.printStackTrace();
		}
		return mapSneakingItems;
	}

	public static int getWarSiegeBannerControlSessionDurationMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_SESSION_DURATION_MINUTES);
	}

	public static boolean getWarSiegePopulationBasedPointBoostsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_POPULATION_BASED_POINT_BOOSTS_ENABLED);
	}

	public static double getWarSiegePopulationQuotientForMaxPointsBoost() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_POPULATION_QUOTIENT_FOR_MAX_POINTS_BOOST);
	}

	public static double getWarSiegeMaxPopulationBasedPointBoost() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MAX_POPULATION_BASED_POINTS_BOOST);
	}

	public static boolean getWarCommonOccupiedTownUnClaimingDisabled() {
		return Settings.getBoolean(ConfigNodes.OCCUPIED_TOWN_UNCLAIMING_DISABLED);
	}

	public static boolean isWarSiegeCounterattackBoosterEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_COUNTERATTACK_BOOSTER_ENABLED);
	}

	public static double getWarSiegeCounterattackBoosterExtraDeathPointsPerPlayerPercentage() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_COUNTERATTACK_BOOSTER_EXTRA_DEATH_POINTS_PER_PLAYER_PERCENTAGE);
	}

	public static List<LocalTime> getAllBattleSessionStartTimesForTodayUtc() {
		LocalDate today = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
		return getAllBattleSessionStartTimesForDayUtc(today);
	}
	
	@Nullable
	public static LocalTime getFirstBattleSessionStartTimeForTomorrowUtc() {
		LocalDate tomorrow = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toLocalDate();
		List<LocalTime> allBattleSessionStartTimesForTomorrow = getAllBattleSessionStartTimesForDayUtc(tomorrow); 
		if(allBattleSessionStartTimesForTomorrow.size() != 0) {
			return allBattleSessionStartTimesForTomorrow.get(0);
		} else {
			return null;
		}
	}
	
	private static List<LocalTime> getAllBattleSessionStartTimesForDayUtc(LocalDate day) {
		//Determine if the given day is on the weekend
		boolean isWeekend = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;

		//Get the configured start times
		String timesAsString = isWeekend ? 
			getWarSiegeBattleSessionsWeekendStartTimesUtc() :
			getWarSiegeBattleSessionsWeekdayStartTimesUtc();

		//Transform the string times into a list of LocalTimes			
		List<LocalTime> timesAsList = new ArrayList<>();	
		String[] timeAsHourMinutePair;		
		LocalTime localTime;
		for(String timeAsString: timesAsString.split(",")) {
			if (timeAsString.contains(":")) {
				timeAsHourMinutePair = timeAsString.split(":");
				localTime = LocalTime.of(Integer.parseInt(timeAsHourMinutePair[0]), Integer.parseInt(timeAsHourMinutePair[1]));
			} else {
				localTime = LocalTime.of(Integer.parseInt(timeAsString), 0);
			}
			timesAsList.add(localTime);
		}
		return timesAsList;
	}

	public static String getWarSiegeBattleSessionsWeekdayStartTimesUtc() {
		return Settings.getString(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_WEEKDAY_START_TIMES_UTC);
	}

	public static String getWarSiegeBattleSessionsWeekendStartTimesUtc() {
		return Settings.getString(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_WEEKEND_START_TIMES_UTC);
	}

	public static int getWarSiegeBattleSessionsDurationMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_DURATION_MINUTES);
	}

	public static boolean isWarSiegeZoneBlockPlacementRestrictionsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ZONE_BLOCK_PLACEMENT_RESTRICTIONS_ENABLED);
	}

	public static List<Material> getWarSiegeZoneBlockPlacementRestrictionsMaterials() {
		if(battleSessionsForbiddenBlockMaterials.isEmpty()) {
			String listAsString = Settings.getString(ConfigNodes.WAR_SIEGE_ZONE_BLOCK_PLACEMENT_RESTRICTIONS_MATERIALS);
			String[] listAsStringArray = listAsString.split(",");
			for (String blockTypeAsString : listAsStringArray) {
				Material material = Material.matchMaterial(blockTypeAsString.trim());
				battleSessionsForbiddenBlockMaterials.add(material);
			}
		}
		return battleSessionsForbiddenBlockMaterials;
	}

	public static boolean isWarSiegeZoneBucketEmptyingRestrictionsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ZONE_BUCKET_EMPTYING_RESTRICTIONS_ENABLED);
	}

	public static List<Material> getWarSiegeZoneBucketEmptyingRestrictionsMaterials() {
		if(battleSessionsForbiddenBucketMaterials.isEmpty()) {
			String listAsString = Settings.getString(ConfigNodes.WAR_SIEGE_ZONE_BUCKET_EMPTYING_RESTRICTIONS_MATERIALS);
			String[] listAsStringArray = listAsString.split(",");
			for (String blockTypeAsString : listAsStringArray) {
				Material material = Material.matchMaterial(blockTypeAsString.trim());
				battleSessionsForbiddenBucketMaterials.add(material);
			}
		}
		return battleSessionsForbiddenBucketMaterials;
	}

	public static int getPeacefulTownsGuardianTownPlotsRequirement() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_GUARDIAN_TOWN_PLOTS_REQUIREMENT);
	}

	public static int getPeacefulTownsGuardianTownMinDistanceRequirement() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_GUARDIAN_TOWN_MIN_DISTANCE_REQUIREMENT_TOWNBLOCKS);
	}

	public static int getWarCommonPeacefulTownsNewTownConfirmationRequirementDays() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_NEW_TOWN_CONFIRMATION_REQUIREMENT_DAYS);
	}

	public static int getBannerControlVerticalDistanceBlocks() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_VERTICAL_DISTANCE_BLOCKS);
	}

	public static boolean getWarCommonPeacefulTownsAllowedToTogglePVP() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_ALLOWED_TO_TOGGLE_PVP);
	}

	public static boolean getPunishingNonSiegeParticipantsInSiegeZone() {
		return Settings.getBoolean(ConfigNodes.ENABLE_SICKNESS);
	}

	public static int getSicknessWarningTimeInTicks() {
		return Settings.getInt(ConfigNodes.SECONDS_BEFORE_SICKNESS) * 20;
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

	public static boolean isCannonsIntegrationEnabled() {
		return Settings.getBoolean(ConfigNodes.CANNONS_INTEGRATION_ENABLED);
	}

	public static int getMaxCannonSessionDuration() {
		return Settings.getInt(ConfigNodes.CANNONS_INTEGRATION_MAX_CANNON_SESSION_DURATION);
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
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_REVERSAL_BONUS_ENABLED);
	}

	public static double getWarSiegeBannerControlReversalBonusFactor() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_REVERSAL_BONUS_MULTIPLIER);
	}

	public static boolean isBannerXYZTextEnabled() {
		return Settings.getBoolean(ConfigNodes.BANNER_XYZ_TEXT_ENABLED);
	}

	public static boolean isTrapWarfareMitigationEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_SWITCHES_TRAP_WARFARE_MITIGATION_ENABLED);
	}

	public static boolean isNationSiegeImmunityEnabled() {
		return Settings.getBoolean(ConfigNodes.NATION_SIEGE_IMMUNITY_ENABLED);
	}

	public static double getNationSiegeImmunityDurationModifier() {
		return Settings.getDouble(ConfigNodes.NATION_SIEGE_IMMUNITY_DURATION_MODIFIER);
	}

	public static double getNationSiegeImmunityHomeTownContributionToAttackCost() {
		return Settings.getDouble(ConfigNodes.NATION_SIEGE_IMMUNITY_HOME_TOWN_CONTRIBUTION_TO_ATTACK_COST);
	}
}
