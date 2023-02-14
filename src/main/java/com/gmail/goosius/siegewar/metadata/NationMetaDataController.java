package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NationMetaDataController {
    @SuppressWarnings("unused")
    private SiegeWar plugin;

    private static String //IDF keys
        plunderGained = "siegewar_totalplundergained",
        plunderLost = "siegewar_totalplunderlost",
        townsGained = "siegewar_totaltownsgained",
        townsLost = "siegewar_totaltownslost",
        dominationRecordKey = "siegewar_dominationrecord";

    private static final LongDataField pendingSiegeImmunityMillis = new LongDataField("siegewar_pendingSiegeImmunityMillis");
    private static final IntegerDataField nationPeacefulOccupationTax = new IntegerDataField("siegeWar_nationPeacefulOccupationTax", 0);

    public NationMetaDataController(SiegeWar plugin) {
        this.plugin = plugin;
    }

    public static int getIdf(Nation nation, String key) {
        if (nation.hasMeta(key)) {
            CustomDataField<?> cdf = nation.getMetadata(key);
            if (cdf instanceof IntegerDataField)
                return ((IntegerDataField) cdf).getValue();
        }
        return 0;
    }

    public static String getSdf(Nation nation, String key) {
        if (nation.hasMeta(key)) {
            CustomDataField<?> cdf = nation.getMetadata(key);
            if (cdf instanceof StringDataField)
                return ((StringDataField) cdf).getValue();
        }
        return "";
    }

    private static void setIdf(Nation nation, String key, int num) {
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

    private static void setSdf(Nation nation, String key, String value) {
        if (nation.hasMeta(key)) {
            if (value == null || value.length() == 0) {
                nation.removeMetaData(nation.getMetadata(key));
            } else {
                CustomDataField<?> cdf = nation.getMetadata(key);
                if (cdf instanceof StringDataField) {
                    ((StringDataField) cdf).setValue(value);
                }
            }
        } else if (value != null && value.length() > 0) {
            nation.addMetaData(new StringDataField(key, value));
        }
        nation.save();
    }

    public static int getTotalPlunderGained(Nation nation) {
        return getIdf(nation, plunderGained);
    }

    public static int getTotalPlunderLost(Nation nation) {
        return getIdf(nation, plunderLost);
    }

    public static int getTotalTownsGained(Nation nation) {
        return getIdf(nation, townsGained);
    }

    public static int getTotalTownsLost(Nation nation) {
        return getIdf(nation, townsLost);
    }

    public static void setTotalPlunderGained(Nation nation, int num) {
        setIdf(nation, plunderGained, num);
    }

    public static void setTotalPlunderLost(Nation nation, int num) {
        setIdf(nation, plunderLost, num);
    }

    public static void setTotalTownsGained(Nation nation, int num) {
        setIdf(nation, townsGained, num);
    }

    public static void setTotalTownsLost(Nation nation, int num) {
        setIdf(nation, townsLost, num);
    }

    public static long getPendingSiegeImmunityMillis(Nation nation) {
        LongDataField ldf = (LongDataField) pendingSiegeImmunityMillis.clone();
        if (nation.hasMeta(ldf.getKey()))
            return MetaDataUtil.getLong(nation, ldf);
        return 0L;
    }

    public static void setPendingSiegeImmunityMillis(Nation nation, long num) {
        LongDataField ldf = (LongDataField) pendingSiegeImmunityMillis.clone();
        if (nation.hasMeta(ldf.getKey()))
            MetaDataUtil.setLong(nation, ldf, num, true);
        else
            nation.addMetaData(new LongDataField(pendingSiegeImmunityMillis.getKey(), num));
    }

    public static void removePendingSiegeImmunityMillis(Nation nation) {
        LongDataField ldf = (LongDataField) pendingSiegeImmunityMillis.clone();
        if (nation.hasMeta(ldf.getKey()))
            nation.removeMetaData(ldf);
    }

	public static List<String> getDominationRecord(Nation nation) {
        String recordString = getSdf(nation, dominationRecordKey).replaceAll(" ", "");
        if(recordString.length() > 0) {
            return new ArrayList<>(Arrays.asList(recordString.split(",")));
        } else {
            return new ArrayList<>();
        }
	}

	public static void setDominationRecord(Nation nation, List<String> dominationRecord) {
        String string = dominationRecord.size() > 0 ? dominationRecord.toString().replace("[", "").replace("]","") : "";
        setSdf(nation, dominationRecordKey, string);
    }

	public static void setNationPeacefulOccupationTax(Nation nation, int tax) {
		MetaDataUtil.setInt(nation, nationPeacefulOccupationTax, tax, true);
	}

	public static int getNationPeacefulOccupationTax(Nation nation) {
		return MetaDataUtil.getInt(nation, nationPeacefulOccupationTax);
	}
}
