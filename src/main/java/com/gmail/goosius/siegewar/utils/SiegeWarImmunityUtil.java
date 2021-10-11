package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * This class contains utility functions related to siege/revolt immunity
 * 
 * @author Goosius
 */
public class SiegeWarImmunityUtil {

	/**
	 * Activate the revolt immunity timer for a town
	 *
	 * While this timer is active, the town cannot start a revolt siege.
     * When this timer hits 0, the town can start a revolt siege.
	 *
	 * Note:
	 * Siege immunity does not block revolts;
	 * only revolt immunity blocks revolts.
	 *
	 * @param town the town
	 */
	public static void activateRevoltImmunityTimer(Town town) {
		long siegeImmunityDurationMillis = TownMetaDataController.getSiegeImmunityEndTime(town) - System.currentTimeMillis();
		long revoltImmunityDurationMillis = (long)(siegeImmunityDurationMillis * SiegeWarSettings.getWarSiegeRevoltImmunityTimeModifier());
		long revoltImmunityEndTime = System.currentTimeMillis() + revoltImmunityDurationMillis;
		TownMetaDataController.setRevoltImmunityEndTime(town, revoltImmunityEndTime);
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
         * Grant siege immunity to any nations who were the home nation of the town during the siege
         */
        if(SiegeWarSettings.isNationSiegeImmunityEnabled()) {
            int totalBattles = siege.getTotalBattleSessions();
            double immunityRewardDurationPerBattleInMillis = siegeDurationMillis / totalBattles * SiegeWarSettings.getNationSiegeImmunityDurationModifier();
            int numBattleSessionsForNation;
            double siegeImmunityRewardInMillis;

            for(Map.Entry<UUID,Integer> primaryTownGovernmentEntry: siege.getPrimaryTownGovernments().entrySet()) {
                if(!primaryTownGovernmentEntry.getKey().equals(town.getUUID())) {
                    Nation nation = TownyUniverse.getInstance().getNation(primaryTownGovernmentEntry.getKey());
                    if(nation != null) {
                        numBattleSessionsForNation = primaryTownGovernmentEntry.getValue();
                        siegeImmunityRewardInMillis = immunityRewardDurationPerBattleInMillis * numBattleSessionsForNation;
                        grantSiegeImmunityToNation(nation, siegeImmunityRewardInMillis);
                    }
                }
            }
        }
    }

    private static void grantSiegeImmunityToNation(Nation nation, double siegeImmunityDurationMillis) {
        /*
         * If the nation has contributions in active sieges, make the immunity pending.
         * (i.e. wait until those sieges end until it is granted)
         *
         * Otherwise, grant the immunity immediately.
         */
        long pendingSiegeImmunityDurationMillis = NationMetaDataController.getPendingSiegeImmunityMillis(nation);

        if(SiegeController.doesNationHaveAnyHomeDefenceContributionsInActiveSieges(nation)) {
            //Make immunity pending
            pendingSiegeImmunityDurationMillis += siegeImmunityDurationMillis;
            NationMetaDataController.setPendingSiegeImmunityMillis(nation, pendingSiegeImmunityDurationMillis);
            nation.save();
        } else {
            //Grant immunity immediately
            long totalSiegeImmunityDurationMillis = pendingSiegeImmunityDurationMillis + (long)siegeImmunityDurationMillis;
            long siegeImmunityEndTime = System.currentTimeMillis() + totalSiegeImmunityDurationMillis;

            for(Town nationTown: nation.getTowns()) {
                if(siegeImmunityEndTime > TownMetaDataController.getSiegeImmunityEndTime(nationTown)) {
                    TownMetaDataController.setSiegeImmunityEndTime(nationTown, siegeImmunityEndTime);
                    nationTown.save();
                }
            }

            NationMetaDataController.removePendingSiegeImmunityMillis(nation);
            nation.save();
        }
    }
    
    public static void evaluateExpiredImmunities() {
    	if (!SiegeWarSettings.getWarSiegeEnabled())
    		return;
		final long olderThanAnHour = System.currentTimeMillis() - 3600000;
    	for (Town town : new ArrayList<>(TownyUniverse.getInstance().getTowns())) {
			long expirationTime = TownMetaDataController.getSiegeImmunityEndTime(town);
			// Expiration happened longer than an hour ago or MetaData returned 0l.
			if (expirationTime < olderThanAnHour)
				continue;
			// Expired in the last hour.
			if (expirationTime < System.currentTimeMillis())
				TownyMessaging.sendPrefixedTownMessage(town, Translation.of("msg_town_immunity_expired"));
		}
    }
}
