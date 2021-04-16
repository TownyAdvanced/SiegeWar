package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;

import java.util.Map;

/**
 * Util class containing methods related to town flags/permssions.
 */
public class SiegeWarTownUtil {
    public static void disableTownPVP(Town town) {
		if (town.isPVP())
				town.setPVP(false);

		for (TownBlock plot : town.getTownBlocks()) {
			if (plot.getPermissions().pvp) {
				if (plot.getType() == TownBlockType.ARENA)
					plot.setType(TownBlockType.RESIDENTIAL);
			
				plot.getPermissions().pvp = false;
				plot.save();
			}
		}
		town.save();
    }

    /**
	 * Sets pvp flag in a town to the desired setting.
	 * 
	 * @param town The town to set the flag for.
	 * @param desiredSetting The value to set pvp to.
	 */
	public static void setPvpFlag(Town town, boolean desiredSetting) {
		
		if (town.getPermissions().pvp != desiredSetting && SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns()) {
			town.getPermissions().pvp = desiredSetting;
			town.save();
		}
	}

	/**
	 * The siege ended.
	 *
	 * 1. Grant siege immunity to the town which was besieged.
	 * 2. If the town was the defender,
	 *    grant siege immunity to any nations who were the home nation of the town during the siege
	 *
	 * @param town the town which was besieged
	 * @param siege the siege
	 */
	public static void grantSiegeImmunityAfterEndedSiege(Town town, Siege siege) {
		//Grant siege immunity to town
		double siegeDurationMillis = siege.getActualEndTime() - siege.getStartTime();
		double immunityDurationMillisDouble = siegeDurationMillis * SiegeWarSettings.getWarSiegeSiegeImmunityTimeModifier();
		long immunityDurationMillis = (long)(immunityDurationMillisDouble + 0.5);
		TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + immunityDurationMillis);
		town.save();

		/*
		 * If the town was the defender,
		 * Grant siege immunity to any nations who were the home nation of the town during the siege
		 */
		if(SiegeWarSettings.isPostWarNationImmunityEnabled()
			&& (siege.getSiegeType() == SiegeType.CONQUEST || siege.getSiegeType() == SiegeType.SUPPRESSION)) {
			int totalBattles = siege.getTotalBattles();
			double immunityRewardDurationPerBattleInMillis = siegeDurationMillis / totalBattles * SiegeWarSettings.getPostWarNationImmunityDurationModifier();
			int numBattlesFoughtByNation;
			double siegeImmunityRewardInMillis;
			for(Map.Entry<String,Integer> nationEntry: siege.getHomeDefenceSiegeContributors().entrySet()) {
				if(!nationEntry.getKey().equals("TOWN") && !nationEntry.getKey().equals("NOBODY")) {
					Nation nation = TownyUniverse.getInstance().getNation(nationEntry.getKey());
					if(nation != null) {
						numBattlesFoughtByNation = nationEntry.getValue();
						siegeImmunityRewardInMillis = immunityRewardDurationPerBattleInMillis * numBattlesFoughtByNation;
						grantSiegeImmunityToNation(nation, siegeImmunityRewardInMillis);
					}
				}
			}
		}
	}

	private static void grantSiegeImmunityToNation(Nation nation, double siegeImmunityMillis) {
		/*
		 * If the nation has contributions in active sieges, make the immunity pending.
		 * (i.e. wait until those sieges end until it is granted)
		 *
		 * Otherwise, grant the immunity immediately.
		 */
		if(SiegeController.doesNationHaveAnyHomeDefenceContributionsInActiveSieges(nation)) {
			long pendingSiegeImmunityMillis = NationMetaDataController.getPendingSiegeImmunityMillis(nation);
			pendingSiegeImmunityMillis += siegeImmunityMillis;
			NationMetaDataController.setPendingSiegeImmunityMillis(nation, pendingSiegeImmunityMillis);
			nation.save();
		} else {
			long pendingSiegeImmunityMillis = NationMetaDataController.getPendingSiegeImmunityMillis(nation);
			long totalSiegeImmunityMillis = pendingSiegeImmunityMillis + (long)siegeImmunityMillis;

			for(Town nationTown: nation.getTowns()) {
				if(TownMetaDataController.getSiegeImmunityEndTime(nationTown) < siegeImmunityMillis) {
					TownMetaDataController.setSiegeImmunityEndTime(nationTown, totalSiegeImmunityMillis);
					nationTown.save();
				}
			}
		}
	}
}