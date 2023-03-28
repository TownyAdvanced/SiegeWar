package com.gmail.goosius.siegewar.settings;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.utils.SiegeWarDominationAwardsUtil;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SiegeWarSettings {
	
	private static List<DayOfWeek> allowedDaysList = null;
	private static List<Material> siegeZoneWildernessForbiddenBlockMaterials = null;
	private static List<Material> siegeZoneWildernessForbiddenBucketMaterials = null;
	private static List<EntityType> siegeZoneWildernessForbiddenExplodeEntityTypes = null;
	@SuppressWarnings("unused")
    private static EnumSet<Material> cachedWallBreachingPlaceBlocksWhitelist = null;
	@SuppressWarnings("unused")
    private static EnumSet<Material> cachedWallBreachingDestroyBlocksBlacklist = null;
	@SuppressWarnings("unused")
    private static Boolean cachedWallBreachingDestroyEntityBlacklist = null;
    private static Map<Integer, List<ItemStack>> cachedDominationAwardsArtefactOffers = null;

	protected static void resetCachedSettings() {
		allowedDaysList = null;
		siegeZoneWildernessForbiddenBlockMaterials = null;
		siegeZoneWildernessForbiddenBucketMaterials = null;
		siegeZoneWildernessForbiddenExplodeEntityTypes = null;
		cachedWallBreachingPlaceBlocksWhitelist = null;
		cachedWallBreachingDestroyBlocksBlacklist = null;
		cachedWallBreachingDestroyEntityBlacklist = null;
		cachedDominationAwardsArtefactOffers = null;
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
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_PLUNDER_ENABLED);
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

	public static boolean getLiberationSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_LIBERATION_SIEGES_ENABLED);
	}

	public static boolean getRevoltSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_REVOLT_SIEGES_ENABLED);
	}

	public static boolean getSuppressionSiegesEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_SUPPRESSION_SIEGES_ENABLED);
	}

	public static boolean getWarSiegeClaimingDisabledNearSiegeZones() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_CLAIMING_DISABLED_NEAR_SIEGE_ZONES);
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

	public static double getWarSiegeMaxHoldoutTimeHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MAX_HOLDOUT_TIME_HOURS);
	}
	
	public static double getWarSiegeMinSiegeDurationBeforeSurrenderHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_SURRENDER_HOURS);
	}

	public static double getWarSiegeMinSiegeDurationBeforeAbandonHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_ABANDON_HOURS);
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
		return SiegeController.getActiveOffensiveSieges(nation).size() >= getWarSiegeMaxActiveSiegeAttacksPerNation();
	}

	public static boolean getWarCommonPeacefulTownsEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_ENABLED);
	}
	
	public static boolean capitalsAllowedTownPeacefulness() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_CAPITALS_ENABLED);
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

	public static int maxNationPeacefulOccupationTax() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_MAX_PEACEFUL_OCCUPATION_TAX);
	}

	public static int getWarSiegeBannerControlSessionDurationMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_SESSION_DURATION_MINUTES);
	}

	public static boolean getWarCommonOccupiedTownUnClaimingDisabled() {
		return Settings.getBoolean(ConfigNodes.OCCUPIED_TOWN_UNCLAIMING_DISABLED);
	}
	
	public static boolean getWarCommonOccupiedTownCorrectConquerStatus() {
		return Settings.getBoolean(ConfigNodes.OCCUPIED_TOWN_CORRECT_CONQUERED_STATUS);
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

	public static boolean isPeacefulTownsSubvertEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_SUBVERT_ENABLED);
	}

	public static boolean isPeacefulTownsRevoltEnabled() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_PEACEFUL_REVOLT_ENABLED);
	}

	public static int getPeacefulTownsTownyInfluenceRadius() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_TOWNY_INFLUENCE_RADIUS);
	}

	public static int getPeacefulTownsSicknessWarningDurationSeconds() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_SICKNESS_WARNING_DURATION_SECONDS);
	}

	public static int getWarCommonPeacefulTownsNewTownConfirmationRequirementDays() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_NEW_TOWN_CONFIRMATION_REQUIREMENT_DAYS);
	}

	public static int getBannerControlVerticalDistanceUpBlocks() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_VERTICAL_DISTANCE_UP_BLOCKS);
	}
	
	public static int getBannerControlVerticalDistanceDownBlocks() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_VERTICAL_DISTANCE_DOWN_BLOCKS);
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

	public static boolean isTrapWarfareMitigationEnabled() {
		return Settings.getBoolean(ConfigNodes.TRAP_WARFARE_MITIGATION_ENABLED);
	}

	public static int getTrapWarfareMitigationRadiusBlocks() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_RADIUS_BLOCKS);
	}

	public static int getTrapWarfareMitigationUpperHeightLimit() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_UPPER_HEIGHT_LIMIT);
	}

	public static int getTrapWarfareMitigationLowerHeightLimit() {
		return Settings.getInt(ConfigNodes.TRAP_WARFARE_MITIGATION_LOWER_HEIGHT_LIMIT);
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

	public static int getCappingLimiterBattleSessions() {
		DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
		boolean weekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

		if(weekend) {
			return getCappingLimiterWeekendDayBattleSessions();
		} else {
			return getCappingLimiterWeekdayBattleSessions();
		}
	}

	private static int getCappingLimiterWeekdayBattleSessions() {
		return Settings.getInt(ConfigNodes.CAPPING_LIMITER_WEEKDAY_BATTLE_SESSIONS);
	}

	private static int getCappingLimiterWeekendDayBattleSessions() {
		return Settings.getInt(ConfigNodes.CAPPING_LIMITER_WEEKEND_DAY_BATTLE_SESSIONS);
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

	public static boolean isWallBreachingEnabled() {
		return false;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_WALL_BREACHING_ENABLED);
		 */
	}
	
	public static double getWallBreachingPointGenerationRate() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_WALL_BREACHING_BREACH_POINT_GENERATION_RATE);
		 */
	}

	public static int getWallBreachingMaxPoolSize() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_WALL_BREACHING_BREACH_POINT_GENERATION_MAX_POOL_SIZE);
		 */
	}

	public static int getWallBreachBonusBattlePoints() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_BALANCING_WALL_BREACH_BONUS_BATTLE_POINTS);
		 */
	}

	public static int getWallBreachingBlockPlacementCost() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_WALL_BREACHING_PLACING_BLOCKS_COST_PER_BLOCK);
		 */
	}

	public static int getWallBreachingBlockDestructionCost() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_WALL_BREACHING_DESTROYING_BLOCKS_COST_PER_BLOCK);
		 */
	}

	public static Set<Material> getWallBreachingPlaceBlocksWhitelist() throws TownyException
	{
		return null;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		if(cachedWallBreachingPlaceBlocksWhitelist == null) {			
    		cachedWallBreachingPlaceBlocksWhitelist = EnumSet.noneOf(Material.class);
			String configuredListUppercase = Settings.getString(ConfigNodes.WAR_SIEGE_WALL_BREACHING_PLACING_BLOCKS_WHITELIST).toUpperCase(Locale.ROOT);
			for(String configuredItemUppercase: configuredListUppercase.replaceAll(" ","").split(",")) {
				if(configuredItemUppercase.startsWith("ENDSWITH=")) {
					String partialName = configuredItemUppercase.replace("ENDSWITH=","");
					for(Material material: Material.values()) {
						if(material.name().toUpperCase().endsWith(partialName))
							cachedWallBreachingPlaceBlocksWhitelist.add(material);
					}
				} else {
					Material material = Material.matchMaterial(configuredItemUppercase);
					if(material == null) {
						throw new TownyException(Translation.of("msg_error_misconfigured_place_blocks_whitelist", configuredItemUppercase));
					} else {
						cachedWallBreachingPlaceBlocksWhitelist.add(material);
					}
				}
			}
		}
		return cachedWallBreachingPlaceBlocksWhitelist;
		 */
	}

    public static boolean isWallBreachingDestroyEntityBlacklist() {
		return false;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.

    	if(cachedWallBreachingDestroyEntityBlacklist == null) {
    		String configuredListLowercase = Settings.getString(ConfigNodes.WAR_SIEGE_WALL_BREACHING_DESTROYING_BLOCKS_BLACKLIST).toLowerCase(Locale.ROOT);
			cachedWallBreachingDestroyEntityBlacklist = configuredListLowercase.contains("is=entity"); 				
		}
		return cachedWallBreachingDestroyEntityBlacklist;
		 */
	}

	public static Set<Material> getWallBreachingDestroyBlocksBlacklist() throws TownyException {
		return null;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		if(cachedWallBreachingDestroyBlocksBlacklist == null) {			
    		cachedWallBreachingDestroyBlocksBlacklist = EnumSet.noneOf(Material.class);
			String configuredListUppercase = Settings.getString(ConfigNodes.WAR_SIEGE_WALL_BREACHING_DESTROYING_BLOCKS_BLACKLIST).toUpperCase(Locale.ROOT);
			for(String configuredItemUppercase: configuredListUppercase.replaceAll(" ","").split(",")) {
				if(!configuredItemUppercase.equals("IS=ENTITY")
					&& !configuredItemUppercase.equals("IS=CONTAINER")) {
					Material material = Material.matchMaterial(configuredItemUppercase);
					if(material == null) {
						throw new TownyException(Translation.of("msg_error_misconfigured_destroy_blocks_blacklist", configuredItemUppercase));
					} else {
						cachedWallBreachingDestroyBlocksBlacklist.add(material);
					}
				}							
			}
		}
		return cachedWallBreachingDestroyBlocksBlacklist;
		 */
	}

	public static int getWallBreachingHomeblockBreachHeightLimitMin() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_WALL_BREACHING_HOMEBLOCK_BREACH_HEIGHT_LIMITS_MIN);
		 */
	}
	
	public static int getWallBreachingHomeblockBreachHeightLimitMax() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_WALL_BREACHING_HOMEBLOCK_BREACH_HEIGHT_LIMITS_MAX);
		 */
	}

	public static boolean isWallBreachingCannonsIntegrationEnabled() {
		return false;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_WALL_BREACHING_CANNONS_INTEGRATION_ENABLED);
		 */
	}

	public static double getWallBreachingCannonFirePointGenerationRate() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_WALL_BREACHING_CANNONS_INTEGRATION_BREACH_POINT_GENERATION_RATE_FROM_CANNON_FIRE);
		 */
	}

	public static int getWallBreachingCannonExplosionCostPerBlock() {
		return 0;
		/*
		TODO - Re-enable when end-of-session rollbacks are implemented.
		return Settings.getInt(ConfigNodes.WAR_SIEGE_WALL_BREACHING_CANNONS_INTEGRATION_EXPLODING_BLOCKS_COST_PER_BLOCK);
		 */
	}

	public static int getSiegeBalanceCapValue() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_SIEGE_BALANCE_CAP_VALUE);
	}

	public static boolean getKillPlayersWhoLogoutInSiegeZones() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_KILL_PLAYERS_WHO_LOG_OUT_IN_SIEGE_ZONES);
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
	public static LocalDateTime getFirstBattleSessionStartTimeForTomorrowUtc() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		List<LocalDateTime> allBattleSessionStartTimesForTomorrow = getAllBattleSessionStartTimesForDay(tomorrow); 
		if(allBattleSessionStartTimesForTomorrow.size() != 0) {
			return allBattleSessionStartTimesForTomorrow.get(0);
		} else {
			return null;
		}
	}

	private static List<LocalDateTime> getAllBattleSessionStartTimesForDay(LocalDate day) {
		//Determine if the given day is on the weekend
		boolean isWeekend = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;

		//Get the start times from the config file, in the form of a single string.
		String startTimesAsString = isWeekend ? 
			getWarSiegeBattleSessionWeekendStartTimes() :
			getWarSiegeBattleSessionWeekdayStartTimes();

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

	public static String getWarSiegeBattleSessionWeekdayStartTimes() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_WEEKDAYS);
	}

	public static String getWarSiegeBattleSessionWeekendStartTimes() {
		return Settings.getString(ConfigNodes.BATTLE_SESSION_SCHEDULER_START_TIMES_WEEKEND_DAYS);
	}

	public static int getWarSiegeBattleSessionsDurationMinutes() {
		return Settings.getInt(ConfigNodes.BATTLE_SESSION_SCHEDULER_DURATION_MINUTES);
	}

	public static boolean cancelBattleSessionWhenNoActiveSieges() {
		return Settings.getBoolean(ConfigNodes.BATTLE_SESSION_SCHEDULER_CANCEL_SESSION_WHEN_NO_SIEGES);
	}

	public static boolean isDominationAwardsGlobalEnabled() {
		return Settings.getBoolean(ConfigNodes.DOMINATION_AWARDS_GLOBAL_ENABLED);
	}

	public static int getDominationAwardsGlobalMinimumAssessmentPeriodHours() {
		return Settings.getInt(ConfigNodes.DOMINATION_AWARDS_GLOBAL_MINIMUM_ASSESSMENT_PERIOD_HOURS);
	}

	public static String getDominationAwardsGlobalAssessmentCriterion() {
		return Settings.getString(ConfigNodes.DOMINATION_AWARDS_GLOBAL_MINIMUM_ASSESSMENT_CRITERION);
	}

	public static DayOfWeek getDominationAwardsGlobalGrantDayOfWeek() {
		return DayOfWeek.valueOf(Settings.getString(ConfigNodes.DOMINATION_AWARDS_GLOBAL_GRANT_DAY_OF_WEEK).toUpperCase());
	}

	public static List<Integer> getDominationAwardsGlobalGrantedMoney() {
		List<Integer> result = new ArrayList<>();
		for(String entry: Settings.getString(ConfigNodes.DOMINATION_AWARDS_GLOBAL_GRANTED_MONEY).replaceAll(" ","").split(",")) {
			result.add(Integer.parseInt(entry));
		}
		return result;
	}

	public static List<List<Integer>> getDominationAwardsGlobalGrantedOffers() {
		List<List<Integer>> result = new ArrayList<>();
		List<String> listOfOffersPerPosition = Settings.getListOfCurlyBracketedItems(ConfigNodes.DOMINATION_AWARDS_GLOBAL_GRANTED_ARTEFACT_OFFERS);
		for(String listOfOffersAsString: listOfOffersPerPosition) {
			List<Integer> listOfOffersAsIntegers = new ArrayList<>();
			for(String numOffers: listOfOffersAsString.replaceAll(" ","").split(",")) {
				listOfOffersAsIntegers.add(Integer.parseInt(numOffers));
			}
			result.add(listOfOffersAsIntegers);
		}
		return result;
	}

	/**
	 * Get the artefact offers specified in a particular config node
	 * 
	 * @param configNode the config node
	 * @param tier the tier (this is used for lore)
	 * 
	 * @return the artefact offers
	 */
	private static List<ItemStack> getDominationAwardsArtefactOffers(ConfigNodes configNode, int tier) {
		List<ItemStack> result = new ArrayList<>();

		for(String offerAsString: Settings.getListOfCurlyBracketedItems(configNode)) {
			//Create convenience variables
			String[] specificationFields = offerAsString.toLowerCase().replaceAll(" ","").split(",");
			SiegeWar.info("Loading Domination Awards Artefact Offer: " +  specificationFields[0]);
			String name = Translatable.of("artefact_name_" + specificationFields[0]).translate();
			int quantity = Integer.parseInt(specificationFields[1]);
			Material material = Material.matchMaterial("minecraft:" + specificationFields[2]);
			//Create artefact
			ItemStack artefact = new ItemStack(material);
			//Set Amount
			artefact.setAmount(quantity);
			//Set name
			ItemMeta itemMeta = artefact.getItemMeta();
			itemMeta.setDisplayName(name);
			//Add special effects
			if (!addSpecialEffects(artefact, itemMeta, specificationFields)) {
			    SiegeWar.severe("Domination Awards Artefact Offer: " +  specificationFields[0] + " cannot be loaded, check the " + (isPotionBased(material) ? "PotionEffectType" : "EnchantmentType") + ". Skipping...");
			    continue;
			}
			//Add non-effect lore
			List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
			lore.add(ChatColor.translateAlternateColorCodes('&', Translatable.of("artefact_lore_summary_line",tier+1).translate()));
			lore.add(ChatColor.translateAlternateColorCodes('&', Translatable.of("artefact_lore_warning_line",(int)SiegeWarSettings.getDominationAwardsArtefactExpiryLifetimeDays()).translate()));
			itemMeta.setLore(lore);
			//Set item meta
			artefact.setItemMeta(itemMeta);
			//Add artefact to result
			result.add(artefact);
		}
		return result;
	}

	/**
	 * Get artefacts offers available for domination rewards.
	 *
	 * @return map of artefact offers
	 * The map is in the form of`:   tier -> List of offers
	 *
	 * WARNING:
	 * The returned offers should considered as "templates"
	 * Thus make sure to clone any of the items before granting.
	 */
	public static Map<Integer, List<ItemStack>> getDominationAwardsArtefactOffers() {
		return cachedDominationAwardsArtefactOffers;
	}

	/**
	 * Loads the indicated list into cache
	 */
	public static void loadDominationAwardsArtefactOffers() {
		Map<Integer, List<ItemStack>> result = new HashMap<>();

		List<ItemStack> offersInTier = new ArrayList<>();
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_CUSTOM_TIER1,0));
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_DEFAULT_TIER1,0));
		result.put(0, offersInTier);

		offersInTier = new ArrayList<>();
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_CUSTOM_TIER2,1));
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_DEFAULT_TIER2,1));
		result.put(1, offersInTier);

		offersInTier = new ArrayList<>();
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_CUSTOM_TIER3,2));
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_DEFAULT_TIER3,2));
		result.put(2, offersInTier);

		offersInTier = new ArrayList<>();
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_CUSTOM_TIER4,3));
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_DEFAULT_TIER4,3));
		result.put(3, offersInTier);

		offersInTier = new ArrayList<>();
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_CUSTOM_TIER5,4));
		offersInTier.addAll(getDominationAwardsArtefactOffers(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_OFFERS_DEFAULT_TIER5,4));
		result.put(4, offersInTier);

		cachedDominationAwardsArtefactOffers = result;
	}

	private static boolean addSpecialEffects(ItemStack artefact, ItemMeta itemMeta, String[] specificationFields) {
		//Create convenience variables
		Material material = artefact.getType();
        List<String[]> effectSpecs = new ArrayList<>();
        for(int i = 3; i < specificationFields.length; i++) {
            effectSpecs.add(specificationFields[i].split(":"));
        }
		//Add special effects
		for(String[] effectSpec: effectSpecs) {
		    if (!validEffect(material, effectSpec[0]))
		        return false;
			addSpecialEffect(material, itemMeta, effectSpec);
		}
		//Set updated item meta
		artefact.setItemMeta(itemMeta);
		return true;
	}

	// Make sure that the config has been given a valid PotionEffectType or Enchantment name.
    private static boolean validEffect(Material material, String name) {
        return !name.isEmpty() && (name.equalsIgnoreCase("custom_effect") || isValid(material, name));
    }

    private static boolean isValid(Material material, String name) {
        return (isPotionBased(material) && validatePotionEffectType(name))
            || (!isPotionBased(material) && validateEnchantmentType(name));
    }

    private static boolean validateEnchantmentType(String enchantSpec) {
        return Enchantment.getByKey(NamespacedKey.fromString("minecraft:"+ enchantSpec)) != null;
    }

    private static boolean validatePotionEffectType(String effectSpec) {
        return PotionEffectType.getByName(effectSpec) != null;
    }

    private static boolean isPotionBased(Material material) {
        return material == Material.POTION
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION
                || material == Material.TIPPED_ARROW;
    }

    private static void addSpecialEffect(Material material, ItemMeta itemMeta, String[] effectSpec) {
		if(effectSpec[0].equalsIgnoreCase("custom_effect")) {
			addCustomEffect(itemMeta, effectSpec);
		} else if (isPotionBased(material)) {
			PotionEffect potionEffect = generatePotionEffect(effectSpec);
			((PotionMeta)itemMeta).addCustomEffect(potionEffect, true);
		} else {
			Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString("minecraft:"+ effectSpec[0]));
			int power = Integer.parseInt(effectSpec[1]);
			itemMeta.addEnchant(enchantment, power, true);
		}
	}

	private static void addCustomEffect(ItemMeta itemMeta, String[] enchantSpec) {
		//Add tag for easy artefact recognition
		List<String> customEffects = SiegeWarDominationAwardsUtil.getCustomEffects(itemMeta);
		customEffects.add(enchantSpec[1]);
		SiegeWarDominationAwardsUtil.setCustomEffects(itemMeta, customEffects);
		//Add lore line
		List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
		lore.add(ChatColor.translateAlternateColorCodes('&', Translatable.of("artefact_custom_effect_lore_" + enchantSpec[1]).translate()));
		itemMeta.setLore(lore);
	}

	private static PotionEffect generatePotionEffect(String[] effectSpec) {
		PotionEffectType potionEffectType = PotionEffectType.getByName(effectSpec[0]);
		int amplifier = Integer.parseInt(effectSpec[1]);
		int duration = Integer.parseInt(effectSpec[2]) * 20;  //Multiply by 20 to convert seconds to ticks
		boolean particles = Boolean.parseBoolean(effectSpec[3]);
		boolean ambient = Boolean.parseBoolean(effectSpec[4]);
		boolean icon = Boolean.parseBoolean(effectSpec[5]);
		return new PotionEffect(potionEffectType, duration, amplifier, particles, ambient, icon);
	}

	public static List<String> getDominationAwardsArtefactChestSignsLowercase() {
		String listAsString = Settings.getString(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_CHEST_SIGNS);
		String[] list = listAsString.toLowerCase().replace(" ","").split(",");
		return Arrays.asList(list);
	}

	public static double getDominationAwardsArtefactExpiryLifetimeDays() {
		return Settings.getDouble(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_EXPIRY_LIFETIME_DAYS);
	}

	public static double getDominationAwardsArtefactExpiryPercentageChancePerShortTick() {
		return Settings.getDouble(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_EXPIRY_PERCENTAGE_CHANCE_PER_SHORT_TICK);
	}

	public static boolean getDominationAwardsArtefactExpiryExplosionsEnabled() {
		return Settings.getBoolean(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_EXPIRY_EXPLOSIONS_ENABLED);
	}

	public static int getDominationAwardsArtefactExpiryExplosionsBasePower() {
		return Settings.getInt(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_EXPIRY_EXPLOSIONS_BASE_POWER);
	}

	public static int getDominationAwardsArtefactExpiryExplosionsExtraPowerPerExpiredArtefact() {
		return Settings.getInt(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_EXPIRY_EXPLOSIONS_EXTRA_POWER_PER_EXPIRED_ARTEFACT);
	}

	public static int getDominationAwardsArtefactExpiryExplosionsMaxPower() {
		return Settings.getInt(ConfigNodes.DOMINATION_AWARDS_ARTEFACT_EXPIRY_EXPLOSIONS_MAX_POWER);
	}

	public static boolean isUnjailingAttackerResidents() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_UNJAIL_RESIDENTS_WHEN_ATTACKERS_WIN_SESSION);
	}

	public static boolean isGlowingEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_GLOWING);
	}
}
