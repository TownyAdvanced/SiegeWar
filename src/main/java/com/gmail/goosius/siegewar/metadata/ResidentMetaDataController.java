package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @author LlmDl
 *
 */
public class ResidentMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;
	private static IntegerDataField refundAmount = new IntegerDataField("siegewar_nationrefund", 0);
	private static IntegerDataField plunderAmount = new IntegerDataField("siegewar_plunder", 0);
	private static IntegerDataField militarySalaryAmount = new IntegerDataField("siegewar_militarysalary", 0);
	//A list of battle sessions the player was recently involved in
	private static StringDataField recentBattleSessions = new StringDataField("siegewar_recentbattlesessions", "");

	static String beaconsDisabled = "siegewar_beaconsdisabled";
	static String bossBarsDisabled = "siegewar_bossBarsdisabled";


	public ResidentMetaDataController(SiegeWar plugin) {
		this.plugin = plugin;
	}
	
	public static int getNationRefundAmount(Resident resident) {
		IntegerDataField idf = (IntegerDataField) refundAmount.clone();
		if (resident.hasMeta(idf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(idf.getKey());
			if (cdf instanceof IntegerDataField) {
				IntegerDataField amount = (IntegerDataField) cdf; 
				return amount.getValue();
			}
		}
		return 0;
	}

	public static void clearNationRefund(Resident resident) {
		IntegerDataField idf = (IntegerDataField) refundAmount.clone();
		if (resident.hasMeta(idf.getKey())) {
			resident.removeMetaData(idf);
		}
	}

	public static void addNationRefundAmount(Resident resident, int nationRefundAmount) {
		IntegerDataField idf = (IntegerDataField) refundAmount.clone();
		if (nationRefundAmount > 0) {
			if (resident.hasMeta(idf.getKey())) {
				int updatedRefundAmount = getNationRefundAmount(resident) + nationRefundAmount;
				resident.removeMetaData(idf);
				resident.addMetaData(new IntegerDataField("siegewar_nationrefund", updatedRefundAmount));
			} else {
				resident.addMetaData(new IntegerDataField("siegewar_nationrefund", nationRefundAmount));
			}
		}
	}

	public static int getPlunderAmount(Resident resident) {
		IntegerDataField idf = (IntegerDataField) plunderAmount.clone();
		if (resident.hasMeta(idf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(idf.getKey());
			if (cdf instanceof IntegerDataField) {
				IntegerDataField amount = (IntegerDataField) cdf;
				return amount.getValue();
			}
		}
		return 0;
	}

	public static void clearPlunder(Resident resident) {
		IntegerDataField idf = (IntegerDataField) plunderAmount.clone();
		if (resident.hasMeta(idf.getKey())) {
			resident.removeMetaData(idf);
		}
	}

	public static void addPlunderAmount(Resident resident, int amountToAdd) {
		IntegerDataField idf = (IntegerDataField) plunderAmount.clone();
		if (amountToAdd > 0) {
			if (resident.hasMeta(idf.getKey())) {
				int updatedAmount = getPlunderAmount(resident) + amountToAdd;
				resident.removeMetaData(idf);
				resident.addMetaData(new IntegerDataField("siegewar_plunder", updatedAmount));
			} else {
				resident.addMetaData(new IntegerDataField("siegewar_plunder", amountToAdd));
			}
		}
	}

	public static int getMilitarySalaryAmount(Resident resident) {
		IntegerDataField idf = (IntegerDataField) militarySalaryAmount.clone();
		if (resident.hasMeta(idf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(idf.getKey());
			if (cdf instanceof IntegerDataField) {
				IntegerDataField amount = (IntegerDataField) cdf;
				return amount.getValue();
			}
		}
		return 0;
	}

	public static void clearMilitarySalary(Resident resident) {
		IntegerDataField idf = (IntegerDataField) militarySalaryAmount.clone();
		if (resident.hasMeta(idf.getKey())) {
			resident.removeMetaData(idf);
		}
	}

	public static void addMilitarySalaryAmount(Resident resident, int amountToAdd) {
		IntegerDataField idf = (IntegerDataField) militarySalaryAmount.clone();
		if (amountToAdd > 0) {
			if (resident.hasMeta(idf.getKey())) {
				int updatedAmount = getMilitarySalaryAmount(resident) + amountToAdd;
				resident.removeMetaData(idf);
				resident.addMetaData(new IntegerDataField("siegewar_militarysalary", updatedAmount));
			} else {
				resident.addMetaData(new IntegerDataField("siegewar_militarysalary", amountToAdd));
			}
		}
	}

	public static void setBoolean(Resident resident, String key, boolean bool) {
		if (resident.hasMeta(key)) {
			if (bool == false)
				resident.removeMetaData(resident.getMetadata(key));
			else {
				CustomDataField<?> cdf = resident.getMetadata(key);
				if (cdf instanceof BooleanDataField) {
					((BooleanDataField) cdf).setValue(bool);
					resident.save();
				}
				return;
			}
		} else if (bool)
			resident.addMetaData(new BooleanDataField(key, bool));
	}

	public static boolean getBoolean(Resident resident, String key) {
		if (resident.hasMeta(key)) {
			CustomDataField<?> cdf = resident.getMetadata(key);
			if (cdf instanceof BooleanDataField)
				return ((BooleanDataField) cdf).getValue();
		}
		return false;
	}

	public static void setBeaconsDisabled(Resident resident, boolean disabled) {
		setBoolean(resident, beaconsDisabled, disabled);
	}

	public static boolean getBeaconsDisabled(Resident resident) {
		return getBoolean(resident, beaconsDisabled);
	}
	
	public static void setBossBarsDisabled(Resident resident, boolean disabled) {
		setBoolean(resident, bossBarsDisabled, disabled);
	}

	public static boolean getBossBarsDisabled(Resident resident) {
		return getBoolean(resident, bossBarsDisabled);
	}
	
	@Nullable
	public static String getRecentBattleSessions(Resident resident) {
		StringDataField sdf = (StringDataField) recentBattleSessions.clone();
		if (resident.hasMeta(sdf.getKey()))
			return MetaDataUtil.getString(resident, sdf);
		return null;
	}
	
	public static void setRecentBattleSessions(Resident resident, String value) {
		StringDataField sdf = (StringDataField) recentBattleSessions.clone();
		if (resident.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(resident, sdf, value, true);
		else
			resident.addMetaData(new StringDataField("siegewar_recentbattlesessions", value));
	}
}
