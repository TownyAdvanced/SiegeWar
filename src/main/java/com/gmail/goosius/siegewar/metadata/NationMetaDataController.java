package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.DecimalDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

public class NationMetaDataController {
    @SuppressWarnings("unused")
    private SiegeWar plugin;

    private static String //IDF keys
        plunderGained = "siegewar_totalplundergained",
        plunderLost = "siegewar_totalplunderlost",
        townsGained = "siegewar_totaltownsgained",
        townsLost = "siegewar_totaltownslost";

    private static final LongDataField legacyFieldPendingSiegeImmunityMillis = new LongDataField("siegewar_pendingSiegeImmunityMillis");
    private static final IntegerDataField legacyFieldNationPeacefulOccupationTax = new IntegerDataField("siegeWar_nationPeacefulOccupationTax", 0);
    
    //Occupation tax per plot. A value of -1 causes the applied value to be the "max" set in the config file.
    private static final DecimalDataField nationOccupationTaxPerPlot = new DecimalDataField("siegeWar_nationOccupationTaxPerPlot", -1.0);
 
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

	public static void setNationOccupationTaxPerPlot(Nation nation, double tax) {
		MetaDataUtil.setDouble(nation, nationOccupationTaxPerPlot, tax, true);
	}

	public static double getNationOccupationTaxPerPlot(Nation nation) {
        if (!MetaDataUtil.hasMeta(nation, nationOccupationTaxPerPlot))
            return -1;
        return MetaDataUtil.getDouble(nation, nationOccupationTaxPerPlot);
	}

    public static void deleteLegacyMetadata(Nation nation) {
        LongDataField ldf = (LongDataField) legacyFieldPendingSiegeImmunityMillis.clone();
        if (nation.hasMeta(ldf.getKey())) {
            nation.removeMetaData(ldf);
        }
        IntegerDataField idf = (IntegerDataField) legacyFieldNationPeacefulOccupationTax.clone();
        if (nation.hasMeta(idf.getKey())) {
            nation.removeMetaData(idf);
        }
    }

}
