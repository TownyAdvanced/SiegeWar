package com.gmail.goosius.siegewar.metadata;

import org.jetbrains.annotations.Nullable;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.DecimalDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

/**
 * 
 * 
 * @author LlmDl
 *
 */
public class SiegeMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;
	private static BooleanDataField hasSiege = new BooleanDataField("siegewar_hasSiege", false);

	private static StringDataField siegeName = new StringDataField("siegewar_name", "");
	private static StringDataField siegeNationUUID = new StringDataField("siegewar_nationUUID", "");

	private static StringDataField siegeTownUUID = new StringDataField("siegewar_townUUID", "");
	private static StringDataField siegeAttackerUUID = new StringDataField("siegewar_attackerUUID", "");
	private static StringDataField siegeDefenderUUID = new StringDataField("siegewar_defenderUUID", "");
	private static StringDataField siegeAttackerName = new StringDataField("siegewar_attackerName", "");
	private static StringDataField siegeDefenderName = new StringDataField("siegewar_defenderName", "");
	
	private static StringDataField siegeFlagLocation = new StringDataField("siegewar_flagLocation", "");
	private static StringDataField siegeStatus = new StringDataField("siegewar_status", "");
	private static StringDataField siegeType = new StringDataField("siegewar_type", "");
	//In metadata, siegeBalance still uses the old name of points
	private static IntegerDataField siegeBalance = new IntegerDataField("siegewar_points", 0);
	private static IntegerDataField attackerBattlePoints = new IntegerDataField("siegewar_attackerBattlePoints", 0);
	private static IntegerDataField defenderBattlePoints = new IntegerDataField("siegewar_defenderBattlePoints", 0);
	
	private static DecimalDataField siegeWarChestAmount = new DecimalDataField("siegewar_warChestAmount", 0.0);
	private static BooleanDataField townPlundered = new BooleanDataField("siegewar_townPlundered", false);
	private static BooleanDataField townInvaded = new BooleanDataField("siegewar_townInvaded", false);
	private static LongDataField startTime = new LongDataField("siegewar_startTime", 0l);
	private static LongDataField endTime = new LongDataField("siegewar_endTime", 0l);
	private static LongDataField actualEndTime = new LongDataField("siegewar_actualEndTime", 0l);
	
	public SiegeMetaDataController(SiegeWar plugin) {
		this.plugin = plugin;
	}

	public static boolean hasSiege(Town town) {
		BooleanDataField bdf = (BooleanDataField) hasSiege.clone();
		if (town.hasMeta(bdf.getKey())) {
			return MetaDataUtil.getBoolean(town, bdf);
		}
		return false;
	}
	
	public static void setSiege(Town town, boolean bool) {
		BooleanDataField bdf = (BooleanDataField) hasSiege.clone();
		if (town.hasMeta(bdf.getKey()))
			MetaDataUtil.setBoolean(town, bdf, bool, true);
		else
			town.addMetaData(new BooleanDataField("siegewar_hasSiege", bool));
	}

	@Nullable
	public static String getAttackerUUID(Town town) {
		StringDataField sdf = (StringDataField) siegeAttackerUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setAttackerUUID(Town town, String uuid) {
		StringDataField sdf = (StringDataField) siegeAttackerUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, uuid, true);
		else
			town.addMetaData(new StringDataField("siegewar_attackerUUID", uuid));
	}

	@Nullable
	public static String getAttackerName(Town town) {
		StringDataField sdf = (StringDataField) siegeAttackerName.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setAttackerName(Town town, String name) {
		StringDataField sdf = (StringDataField) siegeAttackerName.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, name, true);
		else
			town.addMetaData(new StringDataField("siegewar_attackerName", name));
	}

	@Nullable
	public static String getDefenderUUID(Town town) {
		StringDataField sdf = (StringDataField) siegeDefenderUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setDefenderUUID(Town town, String uuid) {
		StringDataField sdf = (StringDataField) siegeDefenderUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, uuid, true);
		else
			town.addMetaData(new StringDataField("siegewar_defenderUUID", uuid));
	}

	@Nullable
	public static String getDefenderName(Town town) {
		StringDataField sdf = (StringDataField) siegeDefenderName.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setDefenderName(Town town, String name) {
		StringDataField sdf = (StringDataField) siegeDefenderName.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, name, true);
		else
			town.addMetaData(new StringDataField("siegewar_defenderName", name));
	}
	
	public static void setTownUUID(Town town, String uuid) {
		StringDataField sdf = (StringDataField) siegeTownUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, uuid, true);
		else
			town.addMetaData(new StringDataField("siegewar_townUUID", uuid));
	}

	@Nullable
	public static String getFlagLocation(Town town) {
		StringDataField sdf = (StringDataField) siegeFlagLocation.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}
	
	public static void setFlagLocation(Town town, String loc) {
		StringDataField sdf = (StringDataField) siegeFlagLocation.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, loc, true);
		else
			town.addMetaData(new StringDataField("siegewar_flagLocation", loc));
	}
	
	@Nullable
	public static String getSiegeStatus(Town town) {
		StringDataField sdf = (StringDataField) siegeStatus.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}
	
	public static void setSiegeStatus(Town town, String status) {
		StringDataField sdf = (StringDataField) siegeStatus.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, status, true);
		else
			town.addMetaData(new StringDataField("siegewar_status", status));
	}

	@Nullable
	public static String getSiegeType(Town town) {
		StringDataField sdf = (StringDataField) siegeType.clone();
		if (town.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(town, sdf);
		return null;
	}

	public static void setSiegeType(Town town, String status) {
		StringDataField sdf = (StringDataField) siegeType.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, status, true);
		else
			town.addMetaData(new StringDataField("siegewar_type", status));
	}

	public static int getSiegeBalance(Town town) {
		IntegerDataField idf = (IntegerDataField) siegeBalance.clone();
		if (town.hasMeta(idf.getKey()))
			return MetaDataUtil.getInt(town, idf);
		return 0;
	}

	public static int getAttackerBattlePoints(Town town) {
		IntegerDataField idf = (IntegerDataField) attackerBattlePoints.clone();
		if (town.hasMeta(idf.getKey()))
			return MetaDataUtil.getInt(town, idf);
		return 0;		
	}

	public static int getDefenderBattlePoints(Town town) {
		IntegerDataField idf = (IntegerDataField) defenderBattlePoints.clone();
		if (town.hasMeta(idf.getKey()))
			return MetaDataUtil.getInt(town, idf);
		return 0;		
	}

	public static void setSiegeBalance(Town town, int num) {
		IntegerDataField idf = (IntegerDataField) siegeBalance.clone();
		if (town.hasMeta(idf.getKey()))
			MetaDataUtil.setInt(town, idf, num, true);
		else
			town.addMetaData(new IntegerDataField("siegewar_points", num));
	}

	public static void setAttackerBattlePoints(Town town, int num) {
		IntegerDataField idf = (IntegerDataField) attackerBattlePoints.clone();
		if (town.hasMeta(idf.getKey()))
			MetaDataUtil.setInt(town, idf, num, true);
		else
			town.addMetaData(new IntegerDataField("siegewar_attackerBattlePoints", num));
	}

	public static void setDefenderBattlePoints(Town town, int num) {
		IntegerDataField idf = (IntegerDataField) defenderBattlePoints.clone();
		if (town.hasMeta(idf.getKey()))
			MetaDataUtil.setInt(town, idf, num, true);
		else
			town.addMetaData(new IntegerDataField("siegewar_defenderBattlePoints", num));
	}

	public static double getWarChestAmount(Town town) {
		DecimalDataField ddf = (DecimalDataField) siegeWarChestAmount.clone();
		if (town.hasMeta(ddf.getKey()))
			return MetaDataUtil.getDouble(town, ddf);
		return 0.0;
	}
	
	public static void setWarChestAmount(Town town, double num) {
		DecimalDataField ddf = (DecimalDataField) siegeWarChestAmount.clone();
		if (town.hasMeta(ddf.getKey()))
			MetaDataUtil.setDouble(town, ddf, num, true);
		else
			town.addMetaData(new DecimalDataField("siegewar_warChestAmount", num));
	}
	
	public static boolean townPlundered(Town town) {
		BooleanDataField bdf = (BooleanDataField) townPlundered.clone();
		if (town.hasMeta(bdf.getKey()))
			return MetaDataUtil.getBoolean(town, bdf);
		return false;
	}
	
	public static void setTownPlundered(Town town, boolean bool) {
		BooleanDataField bdf = (BooleanDataField) townPlundered.clone();
		if (town.hasMeta(bdf.getKey()))
			MetaDataUtil.setBoolean(town, bdf, bool, true);
		else
			town.addMetaData(new BooleanDataField("siegewar_townPlundered", bool));
	}
	
	public static boolean townInvaded(Town town) {
		BooleanDataField bdf = (BooleanDataField) townInvaded.clone();
		if (town.hasMeta(bdf.getKey()))
			return MetaDataUtil.getBoolean(town, bdf);
		return false;
	}
	
	public static void setTownInvaded(Town town, boolean bool) {
		BooleanDataField bdf = (BooleanDataField) townInvaded.clone();
		if (town.hasMeta(bdf.getKey()))
			MetaDataUtil.setBoolean(town, bdf, bool, true);
		else
			town.addMetaData(new BooleanDataField("siegewar_townInvaded", bool));
	}
	
	public static long getStartTime(Town town) {
		LongDataField ldf = (LongDataField) startTime.clone();
		if (town.hasMeta(ldf.getKey()))
			return MetaDataUtil.getLong(town, ldf);
		return 0l;
	}

	public static void setStartTime(Town town, long num) {
		LongDataField ldf = (LongDataField) startTime.clone();
		if (town.hasMeta(ldf.getKey()))
			MetaDataUtil.setLong(town, ldf, num, true);
		else
			town.addMetaData(new LongDataField("siegewar_startTime", num));
	}
	
	public static long getEndTime(Town town) {
		LongDataField ldf = (LongDataField) endTime.clone();
		if (town.hasMeta(ldf.getKey()))
			return MetaDataUtil.getLong(town, ldf);
		return 0l;
	}

	public static void setEndTime(Town town, long num) {
		LongDataField ldf = (LongDataField) endTime.clone();
		if (town.hasMeta(ldf.getKey()))
			MetaDataUtil.setLong(town, ldf, num, true);
		else
			town.addMetaData(new LongDataField("siegewar_endTime", num));
	}
	
	public static long getActualEndTime(Town town) {
		LongDataField ldf = (LongDataField) actualEndTime.clone();
		if (town.hasMeta(ldf.getKey()))
			return MetaDataUtil.getLong(town, ldf);
		return 0l;
	}

	public static void setActualEndTime(Town town, long num) {
		LongDataField ldf = (LongDataField) actualEndTime.clone();
		if (town.hasMeta(ldf.getKey()))
			MetaDataUtil.setLong(town, ldf, num, true);
		else
			town.addMetaData(new LongDataField("siegewar_actualEndTime", num));
	}

	public static void removeSiegeMeta (Town town) {
		StringDataField sdf = (StringDataField) siegeName.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);

		sdf = (StringDataField) siegeType.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeAttackerUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeDefenderUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeAttackerName.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeDefenderName.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);

		sdf = (StringDataField) siegeNationUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeTownUUID.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeFlagLocation.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);
		sdf = (StringDataField) siegeStatus.clone();
		if (town.hasMeta(sdf.getKey()))
			town.removeMetaData(sdf);

		IntegerDataField idf = (IntegerDataField) siegeBalance.clone();
		if (town.hasMeta(idf.getKey()))
			town.removeMetaData(idf);
		idf = (IntegerDataField) attackerBattlePoints.clone();
		if (town.hasMeta(idf.getKey()))
			town.removeMetaData(idf);
		idf = (IntegerDataField) defenderBattlePoints.clone();
		if (town.hasMeta(idf.getKey()))
			town.removeMetaData(idf);

		DecimalDataField ddf = (DecimalDataField) siegeWarChestAmount.clone();
		if (town.hasMeta(ddf.getKey()))
			town.removeMetaData(ddf);

		BooleanDataField bdf = (BooleanDataField) townPlundered.clone();
		if (town.hasMeta(bdf.getKey()))
			town.removeMetaData(bdf);
		bdf = (BooleanDataField) townInvaded.clone();
		if (town.hasMeta(bdf.getKey()))
			town.removeMetaData(bdf);
		bdf = (BooleanDataField) hasSiege.clone();
		if (town.hasMeta(bdf.getKey()))
			town.removeMetaData(bdf);

		LongDataField ldf = (LongDataField) startTime.clone();
		if (town.hasMeta(ldf.getKey()))
			town.removeMetaData(ldf);
		ldf = (LongDataField) endTime.clone();
		if (town.hasMeta(ldf.getKey()))
			town.removeMetaData(ldf);
		ldf = (LongDataField) actualEndTime.clone();
		if (town.hasMeta(ldf.getKey()))
			town.removeMetaData(ldf);
	}
	
}
