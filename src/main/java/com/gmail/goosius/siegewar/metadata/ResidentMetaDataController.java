package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;

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
}
