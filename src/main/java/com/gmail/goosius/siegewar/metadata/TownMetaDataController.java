package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.DecimalDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @author LlmDl
 *
 */
public class TownMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;
	private static IntegerDataField peacefulnessChangeConfirmationCounterDays = new IntegerDataField("siegewar_peacefuldays", 0);
	private static BooleanDataField desiredPeacefulness = new BooleanDataField("siegewar_desiredPeaceSetting", false);
	private static BooleanDataField peacefulness = new BooleanDataField("siegewar_peaceSetting", false);
	private static LongDataField revoltImmunityEndTime = new LongDataField("siegewar_revoltImmunityEndTime", 0l);
	private static LongDataField siegeImmunityEndTime = new LongDataField("siegewar_siegeImmunityEndTime", 0l);
	private static StringDataField failedCampList = new StringDataField("siegewar_failedCampList", "");
	private static IntegerDataField plunderDebtDays = new IntegerDataField("siegewar_plunderDays", 0);
	private static DecimalDataField dailyPlunderCost = new DecimalDataField("siegewar_dailyPlunderCost", 0.0);
	
	//Legacy Metadata
	private static StringDataField legacyDataOccupyingNationUUID = new StringDataField("siegewar_occupyingNationUUID", "");
	private static StringDataField legacyDataPrePeacefulOccupierUUID = new StringDataField("siegewar_prePeacefulOccupierUUID", "");
	private static StringDataField legacyAttackerSiegeContributors = new StringDataField("siegewar_attackerSiegeContributors", "");
	private static StringDataField legacyPrimaryTownGovernments = new StringDataField("siegewar_primaryTownGovernments", "");

	public TownMetaDataController(SiegeWar plugin) {
		this.plugin = plugin;
	}
	
	@Nullable
	public static String getFailedSiegeCampList(Town town) {
		StringDataField sdf = (StringDataField) failedCampList.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}
	
	public static void setFailedCampSiegeList(Town town, String campList) {
		StringDataField sdf = (StringDataField) failedCampList.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, campList, true);
		else
			town.addMetaData(new StringDataField("siegewar_failedCampList", campList));
	}
	
	public static void removeFailedCampSiegeList(Town town) {
		town.removeMetaData((StringDataField) failedCampList.clone());
	}
	
	public static int getPeacefulnessChangeCountdownDays(Town town) {
		IntegerDataField idf = (IntegerDataField) peacefulnessChangeConfirmationCounterDays.clone();
		if (town.hasMeta(idf.getKey())) {
			return MetaDataUtil.getInt(town, idf);
		}
		return 0;
	}

	public static void setPeacefulnessChangeCountdownDays(Town town, int days) {
		IntegerDataField idf = (IntegerDataField) peacefulnessChangeConfirmationCounterDays.clone();
		if (town.hasMeta(idf.getKey())) {
			if (days == 0) {
				town.removeMetaData(idf);
				return;
			}
			MetaDataUtil.setInt(town, idf, days, true);
		} else if (days != 0) {
			town.addMetaData(new IntegerDataField("siegewar_peacefuldays", days));
		}
	}
	
	public static boolean getDesiredPeacefulness(Town town) {
		BooleanDataField bdf = (BooleanDataField) desiredPeacefulness.clone();
		if (town.hasMeta(bdf.getKey())) {
			return MetaDataUtil.getBoolean(town, bdf);
		}
		return false;
	}
	
	public static void setDesiredPeacefulness(Town town, boolean bool) {
		BooleanDataField bdf = (BooleanDataField) desiredPeacefulness.clone();
		if (town.hasMeta(bdf.getKey())) {
			MetaDataUtil.setBoolean(town, bdf, bool, true);
		} else {
			town.addMetaData(new BooleanDataField("siegewar_desiredPeaceSetting", bool));
		}
	}
	
	public static boolean getPeacefulness(Town town) {
		BooleanDataField bdf = (BooleanDataField) peacefulness.clone();
		if (town.hasMeta(bdf.getKey())) {
			return MetaDataUtil.getBoolean(town, bdf);
		}
		return false;
	}

	public static void setPeacefulness(Town town, boolean bool) {
		BooleanDataField bdf = (BooleanDataField) peacefulness.clone();
		if (town.hasMeta(bdf.getKey())) {
			MetaDataUtil.setBoolean(town, bdf, bool, true);
		} else {
			town.addMetaData(new BooleanDataField("siegewar_peaceSetting", bool));
		}
	}
	
	public static long getRevoltImmunityEndTime(Town town) {
		LongDataField ldf = (LongDataField) revoltImmunityEndTime.clone();
		if (town.hasMeta(ldf.getKey())) {
			return MetaDataUtil.getLong(town, ldf);
		}
		return 0l;
	}
	
	public static void setRevoltImmunityEndTime(Town town, long time) {
		LongDataField ldf = (LongDataField) revoltImmunityEndTime.clone();
		if (time == 0) {
			town.removeMetaData(ldf);
			return;
		}
		if (town.hasMeta(ldf.getKey())) {
			MetaDataUtil.setLong(town, ldf, time, true);
		} else {
			town.addMetaData(new LongDataField("siegewar_revoltImmunityEndTime", time));
		}
	}
	
	public static long getSiegeImmunityEndTime(Town town) {
		LongDataField ldf = (LongDataField) siegeImmunityEndTime.clone();
		if (town.hasMeta(ldf.getKey())) {
			return MetaDataUtil.getLong(town, ldf);
		}
		return 0l;
	}
	
	public static void setSiegeImmunityEndTime(Town town, long time) {
		LongDataField ldf = (LongDataField) siegeImmunityEndTime.clone();
		if (time == 0) {
			town.removeMetaData(ldf);
			return;
		}
		if (town.hasMeta(ldf.getKey())) {
			MetaDataUtil.setLong(town, ldf, time, true);
		} else {
			town.addMetaData(new LongDataField("siegewar_siegeImmunityEndTime", time));
		}
	}

	public static boolean hasPlunderDebt(Town town) {
		return MetaDataUtil.hasMeta(town, plunderDebtDays);
	}

	public static void removePlunderDebt(Town town) {
		town.removeMetaData(plunderDebtDays.getKey());
		town.removeMetaData(dailyPlunderCost.getKey());
		town.save();
	}

	public static void setPlunderDebtDays(Town town, int days) {
		MetaDataUtil.setInt(town, plunderDebtDays, days, true);
	}

	public static int getPlunderDebtDays(Town town) {
		return MetaDataUtil.getInt(town, plunderDebtDays);
	}

	public static void setDailyPlunderDebt(Town town, double amount) {
		MetaDataUtil.setDouble(town, dailyPlunderCost, amount, true);
	}

	public static double getDailyPlunderDebt(Town town) {
		return MetaDataUtil.getDouble(town, dailyPlunderCost);
	}

	public static boolean hasLegacyOccupierUUID(Town town) {
		return MetaDataUtil.hasMeta(town, legacyDataOccupyingNationUUID);
	}

	public static String getLegacyOccupierUUID(Town town) {
		return MetaDataUtil.getString(town, legacyDataOccupyingNationUUID);
	}

	public static void deleteLegacyMetadata(Town town) {
		StringDataField sdf = (StringDataField) legacyDataOccupyingNationUUID.clone();
		if (town.hasMeta(sdf.getKey())) {
			town.removeMetaData(sdf);
		}
		sdf = (StringDataField) legacyDataPrePeacefulOccupierUUID.clone();
		if (town.hasMeta(sdf.getKey())) {
			town.removeMetaData(sdf);
		}
		sdf = (StringDataField) legacyAttackerSiegeContributors.clone();
		if (town.hasMeta(sdf.getKey())) {
			town.removeMetaData(sdf);
		}
		sdf = (StringDataField) legacyPrimaryTownGovernments.clone();
		if (town.hasMeta(sdf.getKey())) {
			town.removeMetaData(sdf);
		}
		//Completely delete sieges of types suppression, liberation, and revolt
		SiegeMetaDataController.removeSiegeMeta(town);
	}
}
