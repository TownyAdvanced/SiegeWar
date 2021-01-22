package com.gmail.goosius.siegewar.settings;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import com.gmail.goosius.siegewar.objects.HeldItemsCombination;

public class SiegeWarSettings {
	
	private static final List<HeldItemsCombination> mapSneakingItems = new ArrayList<>();
	private static List<Material> battleSessionsForbiddenBlockMaterials = null;
	private static List<Material> battleSessionsForbiddenBucketMaterials = null;
	
	public static boolean getWarSiegeEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ENABLED);
	}

	public static String getWarSiegeWorlds() {
		return Settings.getString(ConfigNodes.WAR_SIEGE_WORLDS);
	}

	public static boolean getWarSiegeAttackEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ATTACK_ENABLED);
	}

	public static boolean getWarSiegeAbandonEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ABANDON_ENABLED);
	}

	public static boolean getWarSiegeSurrenderEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_TOWN_SURRENDER_ENABLED);
	}

	public static boolean getWarSiegeInvadeEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_INVADE_ENABLED);
	}

	public static boolean getWarSiegePlunderEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_PLUNDER_ENABLED);
	}

	public static boolean getWarSiegeRevoltEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_REVOLT_ENABLED);
	}

	public static boolean getWarSiegeTownLeaveDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_TOWN_LEAVE_DISABLED);
	}

	public static boolean getWarSiegePvpAlwaysOnInBesiegedTowns() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_PVP_ALWAYS_ON_IN_BESIEGED_TOWNS);
	}

	public static boolean getWarSiegeExplosionsAlwaysOnInBesiegedTowns() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_EXPLOSIONS_ALWAYS_ON_IN_BESIEGED_TOWNS);
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

	public static double getWarSiegeRevoltImmunityTimeHours() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_REVOLT_IMMUNITY_TIME_HOURS);
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

	public static int getWarSiegePointsForAttackerOccupation() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_ATTACKER_OCCUPATION);
	}

	public static int getWarSiegePointsForDefenderOccupation() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_DEFENDER_OCCUPATION);
	}

	public static int getWarSiegePointsForAttackerDeath() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_ATTACKER_DEATH);
	}

	public static int getWarSiegePointsForDefenderDeath() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_DEFENDER_DEATH);
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

	public static boolean isWarSiegeCounterattackBoosterDisabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_COUNTERATTACK_BOOSTER_DISABLED);
	}

	public static double getWarSiegeCounterattackBoosterExtraDeathPointsPerPlayerPercentage() {
		return Settings.getDouble(ConfigNodes.WAR_SIEGE_COUNTERATTACK_BOOSTER_EXTRA_DEATH_POINTS_PER_PLAYER_PERCENTAGE);
	}

	public static boolean isWarSiegeBattleSessionsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_ENABLED);
	}

	public static int getWarSiegeBattleSessionsActivePhaseDurationMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_ACTIVE_PHASE_DURATION_MINUTES);
	}

	public static int getWarSiegeBattleSessionsFirstWarningMinutesToExpiry() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_FIRST_WARNING_MINUTES_TO_EXPIRY);
	}

	public static int getWarSiegeBattleSessionsSecondWarningMinutesToExpiry() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_SECOND_WARNING_MINUTES_TO_EXPIRY);
	}

	public static int getWarSiegeBattleSessionsExpiredPhaseDurationMinutes() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BATTLE_SESSIONS_EXPIRED_PHASE_DURATION_MINUTES);
	}

	public static boolean isWarSiegeZoneBlockPlacementRestrictionsEnabled() {
		return Settings.getBoolean(ConfigNodes.WAR_SIEGE_ZONE_BLOCK_PLACEMENT_RESTRICTIONS_ENABLED);
	}

	public static List<Material> getWarSiegeZoneBlockPlacementRestrictionsMaterials() {
		if(battleSessionsForbiddenBlockMaterials == null) {
			battleSessionsForbiddenBlockMaterials = new ArrayList<>();
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
		if(battleSessionsForbiddenBucketMaterials == null) {
			battleSessionsForbiddenBucketMaterials = new ArrayList<>();
			String listAsString = Settings.getString(ConfigNodes.WAR_SIEGE_ZONE_BUCKET_EMPTYING_RESTRICTIONS_MATERIALS);
			String[] listAsStringArray = listAsString.split(",");
			for (String blockTypeAsString : listAsStringArray) {
				Material material = Material.matchMaterial(blockTypeAsString.trim());
				battleSessionsForbiddenBucketMaterials.add(material);
			}
		}
		return battleSessionsForbiddenBucketMaterials;
	}

	public static int getWarSiegePeacefulTownsGuardianTownPlotsRequirement() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_PEACEFUL_TOWNS_GUARDIAN_TOWN_PLOTS_REQUIREMENT);
	}

	public static int getWarSiegePeacefulTownsGuardianTownMinDistanceRequirement() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_PEACEFUL_TOWNS_GUARDIAN_TOWN_MIN_DISTANCE_REQUIREMENT_TOWNBLOCKS);
	}

	public static boolean getWarCommonPeacefulTownsAllowedToMakeNation() {
		return Settings.getBoolean(ConfigNodes.PEACEFUL_TOWNS_ALLOWED_TO_MAKE_NATION);
	}

	public static int getWarCommonPeacefulTownsNewTownConfirmationRequirementDays() {
		return Settings.getInt(ConfigNodes.PEACEFUL_TOWNS_NEW_TOWN_CONFIRMATION_REQUIREMENT_DAYS);
	}

	public static int getBannerControlHorizontalDistanceBlocks() {
		return Settings.getInt(ConfigNodes.WAR_SIEGE_BANNER_CONTROL_HORIZONTAL_DISTANCE_BLOCKS);
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

}
