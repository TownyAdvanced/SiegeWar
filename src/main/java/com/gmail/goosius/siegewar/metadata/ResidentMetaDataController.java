package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

/**
 * 
 * @author LlmDl
 *
 */
public class ResidentMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;
	private static IntegerDataField refundAmount = new IntegerDataField("siegewar_nationrefund", 0, "Nation Refund");
	private static IntegerDataField plunderAmount = new IntegerDataField("siegewar_plunder", 0, "Plunder");
	private static IntegerDataField militarySalaryAmount = new IntegerDataField("siegewar_militarysalary", 0, "Military Salary");

	static String
		captureColorPreference = "siegewar_capturecolor",
		allyColorPreference = "siegewar_allycolor",
		enemyColorPreference = "siegewar_enemycolor";


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
				resident.addMetaData(new IntegerDataField("siegewar_nationrefund", updatedRefundAmount, "Nation Refund"));
			} else {
				resident.addMetaData(new IntegerDataField("siegewar_nationrefund", nationRefundAmount, "Nation Refund"));
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
				resident.addMetaData(new IntegerDataField("siegewar_plunder", updatedAmount, "Plunder"));
			} else {
				resident.addMetaData(new IntegerDataField("siegewar_plunder", amountToAdd, "Plunder"));
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
				resident.addMetaData(new IntegerDataField("siegewar_militarysalary", updatedAmount, "Military Salary"));
			} else {
				resident.addMetaData(new IntegerDataField("siegewar_militarysalary", amountToAdd, "Military Salary"));
			}
		}
	}

	public static void setString(Resident resident, String key, String string) {
		if (resident.hasMeta(key)) {
			if (string.equals(""))
				resident.removeMetaData(resident.getMetadata(key));
			else {
				CustomDataField<?> cdf = resident.getMetadata(key);
				if (cdf instanceof StringDataField) {
					((StringDataField) cdf).setValue(string);
					resident.save();
				}
				return;
			}
		} else if (!string.equals(""))
			resident.addMetaData(new StringDataField(key, string));
	}

	public static String getString(Resident resident, String key) {
		if (resident.hasMeta(key)) {
			CustomDataField<?> cdf = resident.getMetadata(key);
			if (cdf instanceof StringDataField)
				return ((StringDataField) cdf).getValue();
		}
		return "";
	}

	public static void setCaptureColorPreference(Resident resident, String materialName) {
		setString(resident, captureColorPreference, materialName);
	}

	public static void setAllyColorPreference(Resident resident, String materialName) {
		setString(resident, allyColorPreference, materialName);
	}

	public static void setEnemyColorPreference(Resident resident, String materialName) {
		setString(resident, enemyColorPreference, materialName);
	}

	public static String getCaptureColorPreference(Resident resident) {
		return getString(resident, captureColorPreference);
	}

	public static String getAllyColorPreference(Resident resident) {
		return getString(resident, allyColorPreference);
	}

	public static String getEnemyColorPreference(Resident resident) {
		return getString(resident, enemyColorPreference);
	}
}
