package com.gmail.goosius.siegewar.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.*;
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
	
	private static String   //Metadata keys    
		 unrest_percentage = "siegewar_unrest_percentage",  //e.g. 57
		 objectiveStatus = "siegewar_objectiveStatus",  //e.g. "Not Started"
		 objectiveAttackerAlliance = "siegewar_objectiveAttackerAlliance",  //e.g. "white"
		 objectiveDefenderAlliance = "siegewar_objectiveDefenderAlliance",  //e.g. "gold"       
		 surveyData = "siegewar_surveyData";   //e.g.  {town_size_12,nation_uuid_765},{...},...

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

	public static int getUnrestPercentage(Nation nation) {
        return MetaDataUtil.getIdf(nation, unrest_percentage);
    }
	
	public static String getObjectiveStatus(Nation nation) {
        return MetaDataUtil.getSdf(nation, objectiveStatus);
    }

    public static String getObjectiveAttackerAlliance(Nation nation) {
        return MetaDataUtil.getSdf(nation, objectiveAttackerAlliance);
    }

    public static String getObjectiveDefenderAlliance(Nation nation) {
        return MetaDataUtil.getSdf(nation, objectiveDefenderAlliance);
    }

    public static String getSurveyData (Nation nation) {
        return MetaDataUtil.getSdf(nation, surveyData);
    }

	public static void setUnrest_percentage(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, unrest_percentage, val);
    }

	public static void setObjectiveStatus(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, objectiveStatus, val);
    }

	public static void setObjectiveAttackerAlliance(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, objectiveAttackerAlliance, val);
    }

	public static void setObjectiveDefenderAlliance(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, objectiveDefenderAlliance, val);
    }

	public static void setSurveyData(Nation nation, String val) {
        MetaDataUtil.setSdf(nation, surveyData, val);
    }
}
