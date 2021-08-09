package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;

public class NationMetaDataController {
    @SuppressWarnings("unused")
    private SiegeWar plugin;

    private static String //Metadata Keys
        plunderGained = "siegewar_totalplundergained",
        plunderLost = "siegewar_totalplunderlost",
        townsGained = "siegewar_totaltownsgained",
        townsLost = "siegewar_totaltownslost",
        alignmentExpansionism = "siegewar_alignmentExpansionism", //e.g. "1"
        alignmentMilitarism = "siegewar_alignmentMilitarism",     //e.g. "-2"
        alignmentAggression = "siegewar_alignmentAggression",     //e.g. "0"
        alliance = "siegewar_alliance",   //e.g. "green"
        objectives = "siegewar_objectives",  //e.g. "{town_uuid_12345,win_bonus_+5str,loss_penalty_+5upkeep},{...},...",
        surveyData = "siegewar_surveyData"; //e.g.  "{army_size_12},{...},..."

    private static final LongDataField pendingSiegeImmunityMillis = new LongDataField("siegewar_pendingSiegeImmunityMillis");

    public NationMetaDataController(SiegeWar plugin) {
        this.plugin = plugin;
    }

    public static int getTotalPlunderGained(Nation nation) {
        return MetaDataUtil.getIdf(nation, plunderGained);
    }

    public static int getTotalPlunderLost(Nation nation) {
        return MetaDataUtil.getIdf(nation, plunderLost);
    }

    public static int getTotalTownsGained(Nation nation) {
        return MetaDataUtil.getIdf(nation, townsGained);
    }

    public static int getTotalTownsLost(Nation nation) {
        return MetaDataUtil.getIdf(nation, townsLost);
    }

    public static int getAlignmentExpansionism(Nation nation) {
        return MetaDataUtil.getIdf(nation, alignmentExpansionism);
    }

    public static int getAlignmentMilitarism(Nation nation) {
        return MetaDataUtil.getIdf(nation, alignmentMilitarism);
    }

    public static int getAlignmentAggression(Nation nation) {
        return MetaDataUtil.getIdf(nation, alignmentAggression);
    }

    public static String getAlliance (Nation nation) {
        return MetaDataUtil.getSdf(nation, alliance);
    }

    public static String getObjectives (Nation nation) {
        return MetaDataUtil.getSdf(nation, objectives);
    }

    public static String getSurveyData (Nation nation) {
        return MetaDataUtil.getSdf(nation, surveyData);
    }

    public static void setTotalPlunderGained(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, plunderGained, num);
    }

    public static void setTotalPlunderLost(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, plunderLost, num);
    }

    public static void setTotalTownsGained(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, townsGained, num);
    }

    public static void setTotalTownsLost(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, townsLost, num);
    }

    public static void setAlignmentExpansionism(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, alignmentExpansionism, num);
    }

    public static void setAlignmentMilitarism(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, alignmentMilitarism, num);
    }

    public static void setAlignmentAggression(Nation nation, int num) {
        MetaDataUtil.setIdf(nation, alignmentAggression, num);
    }

    public static void setAlliance(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, alliance, val);
    }

    public static void setObjectives(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, objectives, val);
    }

    public static void setSurveyData(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, surveyData, val);
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
            MetaDataUtil.setLong(nation, ldf, num);
        else
            nation.addMetaData(new LongDataField(pendingSiegeImmunityMillis.getKey(), num));
    }

    public static void removePendingSiegeImmunityMillis(Nation nation) {
        LongDataField ldf = (LongDataField) pendingSiegeImmunityMillis.clone();
        if (nation.hasMeta(ldf.getKey()))
            nation.removeMetaData(ldf);
    }

}
