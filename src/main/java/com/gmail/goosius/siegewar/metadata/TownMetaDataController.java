package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.metadata.*;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 
 * @author LlmDl
 *
 */
public class TownMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;
	private static IntegerDataField peacefulnessChangeConfirmationCounterDays = new IntegerDataField("siegewar_peacefuldays", 0, Translation.of("status_town_days_to_peacefulness_status_change"));
	private static BooleanDataField desiredPeacefulness = new BooleanDataField("siegewar_desiredPeaceSetting", false);
	private static LongDataField revoltImmunityEndTime = new LongDataField("siegewar_revoltImmunityEndTime", 0l);
	private static LongDataField siegeImmunityEndTime = new LongDataField("siegewar_siegeImmunityEndTime", 0l);
	private static StringDataField failedCampList = new StringDataField("siegewar_failedCampList", "");
	private static IntegerDataField plunderDebtDays = new IntegerDataField("siegewar_plunderDays", 0);
	private static DecimalDataField dailyPlunderCost = new DecimalDataField("siegewar_dailyPlunderCost", 0.0);

	//Legacy Metadata
	private static StringDataField legacyDataOccupyingNationUUID = new StringDataField("siegewar_occupyingNationUUID", "");
	private static StringDataField legacyDataPrePeacefulOccupierUUID = new StringDataField("siegewar_prePeacefulOccupierUUID", "");

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
	
	public static int getPeacefulnessChangeConfirmationCounterDays(Town town) {
		IntegerDataField idf = (IntegerDataField) peacefulnessChangeConfirmationCounterDays.clone();
		if (town.hasMeta(idf.getKey())) {
			return MetaDataUtil.getInt(town, idf);
		}
		return 0;
	}

	public static void setPeacefulnessChangeDays(Town town, int days) {
		IntegerDataField idf = (IntegerDataField) peacefulnessChangeConfirmationCounterDays.clone();
		if (town.hasMeta(idf.getKey())) {
			if (days == 0) {
				town.removeMetaData(idf);
				return;
			}
			MetaDataUtil.setInt(town, idf, days, true);
		} else if (days != 0) {
			town.addMetaData(new IntegerDataField("siegewar_peacefuldays", days, Translation.of("status_town_days_to_peacefulness_status_change")));
		}
	}
	
	public static boolean getDesiredPeacefulnessSetting(Town town) {
		BooleanDataField bdf = (BooleanDataField) desiredPeacefulness.clone();
		if (town.hasMeta(bdf.getKey())) {
			return MetaDataUtil.getBoolean(town, bdf);
		}
		return false;
	}
	
	public static void setDesiredPeacefulnessSetting(Town town, boolean bool) {
		BooleanDataField bdf = (BooleanDataField) desiredPeacefulness.clone();
		if (town.hasMeta(bdf.getKey())) {
			MetaDataUtil.setBoolean(town, bdf, bool, true);
		} else {
			town.addMetaData(new BooleanDataField("siegewar_desiredPeaceSetting", bool));
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

	public static boolean hasLegacyPrePeacefulOccupierUUID(Town town) {
		return MetaDataUtil.hasMeta(town, legacyDataPrePeacefulOccupierUUID);
	}

	public static String getLegacyOccupierUUID(Town town) {
		return MetaDataUtil.getString(town, legacyDataOccupyingNationUUID);
	}

	public static void removeLegacyOccupierUUID(Town town) {
		town.removeMetaData(legacyDataOccupyingNationUUID.getKey());
		town.save();
	}

	public static void removeLegacyPrePeacefulOccupierUUID(Town town) {
		town.removeMetaData(legacyDataPrePeacefulOccupierUUID.getKey());
		town.save();
	}
}
