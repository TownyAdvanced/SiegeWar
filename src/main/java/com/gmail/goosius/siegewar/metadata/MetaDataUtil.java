package com.gmail.goosius.siegewar.metadata;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.DecimalDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

/**
 * 
 * @author LlmDl
 *
 */
class MetaDataUtil {

    public static int getIdf(Nation nation, String key) {
        if (nation.hasMeta(key)) {
            CustomDataField<?> cdf = nation.getMetadata(key);
            if (cdf instanceof IntegerDataField)
                return ((IntegerDataField) cdf).getValue();
        }
        return 0;
    }

    public static void setIdf(Nation nation, String key, int num) {
        if (nation.hasMeta(key)) {
            if (num == 0)
                nation.removeMetaData(nation.getMetadata(key));
            else {
                CustomDataField<?> cdf = nation.getMetadata(key);
                if (cdf instanceof IntegerDataField) {
                    ((IntegerDataField) cdf).setValue(num);
                    nation.save();
                }
                return;
            }
        } else if (num != 0)
            nation.addMetaData(new IntegerDataField(key, num));
    }

    public static String getSdf(Nation nation, String key) {
        if (nation.hasMeta(key)) {
            CustomDataField<?> cdf = nation.getMetadata(key);
            if (cdf instanceof StringDataField)
                return ((StringDataField) cdf).getValue();
        }
        return "";
    }

    public static void setSdf(Nation nation, String key, String newValue) {
        if (nation.hasMeta(key)) {
            if (newValue.isEmpty())
                nation.removeMetaData(nation.getMetadata(key));
            else {
                CustomDataField<?> cdf = nation.getMetadata(key);
                if (cdf instanceof StringDataField) {
                    ((StringDataField) cdf).setValue(newValue);
                    nation.save();
                }
            }
        } else if (!newValue.isEmpty())
            nation.addMetaData(new StringDataField(key, newValue));
    }

	static String getString(Town town, StringDataField sdf) {
		CustomDataField<?> cdf = town.getMetadata(sdf.getKey());
		if (cdf instanceof StringDataField) {
			return ((StringDataField) cdf).getValue();
		}
		return "";
	}

	static boolean getBoolean(Town town, BooleanDataField bdf) {
		CustomDataField<?> cdf = town.getMetadata(bdf.getKey());
		if (cdf instanceof BooleanDataField)
			return ((BooleanDataField) cdf).getValue();
		return false;
	}

	static long getLong(TownyObject townyObject, LongDataField ldf) {
		CustomDataField<?> cdf = townyObject.getMetadata(ldf.getKey());
		if (cdf instanceof LongDataField)
			return ((LongDataField) cdf).getValue();
		return 0l;
	}
	
	static int getInt(TownyObject townyObject, IntegerDataField idf) {
		CustomDataField<?> cdf = townyObject.getMetadata(idf.getKey());
		if (cdf instanceof IntegerDataField) 
			return ((IntegerDataField) cdf).getValue();
		return 0;				
	}
	
	static double getDouble(Town town, DecimalDataField ddf) {
		CustomDataField<?> cdf = town.getMetadata(ddf.getKey());
		if (cdf instanceof DecimalDataField)
			return ((DecimalDataField) cdf).getValue();
		return 0.0;
	}

	static void setString(Town town, StringDataField sdf, String string) {
		CustomDataField<?> cdf = town.getMetadata(sdf.getKey());
		if (cdf instanceof StringDataField) {
			StringDataField value = (StringDataField) cdf;
			value.setValue(string);
			town.save();
		}
	}

	static void setBoolean(Town town, BooleanDataField bdf, boolean bool) {
		CustomDataField<?> cdf = town.getMetadata(bdf.getKey());
		if (cdf instanceof BooleanDataField) {
			BooleanDataField value = (BooleanDataField) cdf;
			value.setValue(bool);
			town.save();
		}
	}

	static void setLong(TownyObject townyObject, LongDataField ldf, long num) {
		CustomDataField<?> cdf = townyObject.getMetadata(ldf.getKey());
		if (cdf instanceof LongDataField) {
			LongDataField value = (LongDataField) cdf;
			value.setValue(num);
			townyObject.save();
		}
	}

	static void setInt(TownyObject townyObject, IntegerDataField idf, int num) {
		CustomDataField<?> cdf = townyObject.getMetadata(idf.getKey());
		if (cdf instanceof IntegerDataField) {
			IntegerDataField value = (IntegerDataField) cdf;
			value.setValue(num);
			townyObject.save();
		}
	}
	
	static void setDouble(Town town, DecimalDataField ddf, double num) {
		CustomDataField<?> cdf = town.getMetadata(ddf.getKey());
		if (cdf instanceof DecimalDataField) {
			DecimalDataField value = (DecimalDataField) cdf;
			value.setValue(num);
			town.save();
		}
	}
}
