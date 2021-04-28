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
import java.util.UUID;

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
		 * If this was a town defence siege,
		 * Grant siege immunity to any nations who were the home nation of the town during the siege
		 */
		if(SiegeWarSettings.isNationSiegeImmunityEnabled()) {
			int totalBattles = siege.getTotalTownDefenceBattleSessions();
			double immunityRewardDurationPerBattleInMillis = siegeDurationMillis / totalBattles * SiegeWarSettings.getNationSiegeImmunityDurationModifier();
			int numBattlesFoughtByNation;
			double siegeImmunityRewardInMillis;

			System.out.println("xxxTotal Battles" + totalBattles);
			System.out.println("xxxReward Per battle" + immunityRewardDurationPerBattleInMillis);


			for(Map.Entry<UUID,Integer> townDefenceGovernmentEntry: siege.getTownDefenceGovernments().entrySet()) {
				if(!townDefenceGovernmentEntry.getKey().equals(town.getUUID())) {
					Nation nation = TownyUniverse.getInstance().getNation(townDefenceGovernmentEntry.getKey());
					if(nation != null) {

						System.out.println("Granting Immunity now");

						numBattlesFoughtByNation = townDefenceGovernmentEntry.getValue();
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
		long pendingSiegeImmunityMillis = NationMetaDataController.getPendingSiegeImmunityMillis(nation);

		if(SiegeController.doesNationHaveAnyHomeDefenceContributionsInActiveSieges(nation)) {
			//Make immunity pending
			pendingSiegeImmunityMillis += siegeImmunityMillis;
			NationMetaDataController.setPendingSiegeImmunityMillis(nation, pendingSiegeImmunityMillis);
			nation.save();
		} else {
			//Grant immunity immediately
			long siegeImmunityToGrantMillis = pendingSiegeImmunityMillis + (long)siegeImmunityMillis;
			double currentSiegeImmunityMillis;

			for(Town nationTown: nation.getTowns()) {
				currentSiegeImmunityMillis = TownMetaDataController.getSiegeImmunityEndTime(nationTown)- System.currentTimeMillis();
				if(currentSiegeImmunityMillis < siegeImmunityToGrantMillis) {
					TownMetaDataController.setSiegeImmunityEndTime(nationTown, siegeImmunityToGrantMillis);
					nationTown.save();
				}
			}

			NationMetaDataController.setPendingSiegeImmunityMillis(nation, 0);
			nation.save();
		}
	}
}