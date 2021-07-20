package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @author LlmDl
 *
 */
public class TownMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;
	private static IntegerDataField peacefulnessChangeConfirmationCounterDays = new IntegerDataField("siegewar_peacefuldays", 0, "Days To Peacefulness Status Change");
	private static BooleanDataField desiredPeacefulness = new BooleanDataField("siegewar_desiredPeaceSetting", false);
	private static LongDataField revoltImmunityEndTime = new LongDataField("siegewar_revoltImmunityEndTime", 0l);
	private static LongDataField siegeImmunityEndTime = new LongDataField("siegewar_siegeImmunityEndTime", 0l);
	private static StringDataField occupyingNationUUID = new StringDataField("siegewar_occupyingNationUUID", "");
	//The nation who was the occupier prior to peacefulness confirmation
	private static StringDataField prePeacefulOccupierUUID = new StringDataField("siegewar_prePeacefulOccupierUUID", "");

	public TownMetaDataController(SiegeWar plugin) {
		this.plugin = plugin;
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
			MetaDataUtil.setInt(town, idf, days);
		} else if (days != 0) {
			town.addMetaData(new IntegerDataField("siegewar_peacefuldays", days, "Days To Peacefulness Status Change"));			
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
			MetaDataUtil.setBoolean(town, bdf, bool);
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
			MetaDataUtil.setLong(town, ldf, time);
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
			MetaDataUtil.setLong(town, ldf, time);
		} else {
			town.addMetaData(new LongDataField("siegewar_siegeImmunityEndTime", time));
		}
	}

	@Nullable
	public static String getOccupyingNationUUID(Town town) {
		StringDataField sdf = (StringDataField) occupyingNationUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setOccupyingNationUUID(Town town, String uuid) {
		StringDataField sdf = (StringDataField) occupyingNationUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, uuid);
		else
			town.addMetaData(new StringDataField("siegewar_occupyingNationUUID", uuid));
	}

	public static void removeOccupationMetadata(Town town) {
		StringDataField sdf = (StringDataField) occupyingNationUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
	}

	@Nullable
	public static String getPrePeacefulOccupierUUID(Town town) {
		StringDataField sdf = (StringDataField) prePeacefulOccupierUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setPrePeacefulOccupierUUID(Town town, String uuid) {
		StringDataField sdf = (StringDataField) prePeacefulOccupierUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, uuid);
		else
			town.addMetaData(new StringDataField("siegewar_prePeacefulOccupierUUID", uuid));
	}

	public static void removePrePeacefulOccupierUUID(Town town) {
		StringDataField sdf = (StringDataField) prePeacefulOccupierUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
	}
}
