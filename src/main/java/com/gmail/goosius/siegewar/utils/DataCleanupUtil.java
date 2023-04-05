package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.metadata.SiegeMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class deals with everything related to data cleanup, including:
 * - Migration of legacy data structure to new data structure
 * - Deletion of legacy data
 */
public class DataCleanupUtil {

    public static void cleanupData(boolean siegeWarPluginError, boolean listenersRegistered) {
        if(siegeWarPluginError) {
            SiegeWar.severe("SiegeWar is in safe mode. Data cleanup not attempted.");
            return;
        }
        if(!listenersRegistered) {
            SiegeWar.severe("Listeners are not registered. Ensure listeners are registered before cleanup up data (e.g. to ensure nation refunds).");
            return;
        }
        //Cleanup battle session data
        cleanupBattleSession();
        
        //Migrate Data
        migrateTownNeutralityData();
        migrateTownOccupationData();
        
        //Only after all migration is complete, delete legacy data.
        deleteLegacyResidentMetadata();
        deleteLegacyTownMetadata();
        deleteLegacyNationMetadata();
    }

    /**
     * Cleans up the battle session, if it did not exit properly when the plugin shut down.
     */
    private static void cleanupBattleSession() {
        //Find any sieges with unresolved battles
        List<Siege> siegesWithUnresolvedBattles = new ArrayList<>();
        for(Siege siege: SiegeController.getSieges()) {
            if(siege.getStatus() == SiegeStatus.IN_PROGRESS
                    && (siege.getAttackerBattlePoints() > 0 || siege.getDefenderBattlePoints() > 0)) {
                siegesWithUnresolvedBattles.add(siege);
            }
        }
        //Resolve battles
        if(siegesWithUnresolvedBattles.size() > 0) {
            SiegeWar.info(Translation.of("msg.battle.session.cleanup.starting"));
            int numBattlesUpdated = 0;
            for(Siege siege: siegesWithUnresolvedBattles) {
                siege.adjustSiegeBalance(siege.getAttackerBattlePoints() - siege.getDefenderBattlePoints());
                siege.setAttackerBattlePoints(0);
                siege.setDefenderBattlePoints(0);
                SiegeController.saveSiege(siege);
                numBattlesUpdated++;
            }

            SiegeWar.info(Translation.of("msg.battle.session.cleanup.complete", numBattlesUpdated));
        }
    }

    /**
     * In some previous SW versions,
     * a town could have both a regular nation, and a different occupying nation.
     * -
     * This method migrates the old data,
     * so that if the older data schema is detected,
     * and a town has an occupying nation,
     * that town will be transferred to that occupying nation.
     * - 
     * FYI the metadata is deleted later, in deleteLegacyMetaData()
     */
    private static void migrateTownOccupationData() {
        boolean success = false;
        for(Town town: new ArrayList<>(TownyAPI.getInstance().getTowns())) {
            if(TownMetaDataController.hasLegacyOccupierUUID(town)) {
                Nation occupyingNation = TownyAPI.getInstance().getNation(UUID.fromString(TownMetaDataController.getLegacyOccupierUUID(town)));
                if(occupyingNation != null) {
                    SiegeWarTownOccupationUtil.setTownOccupation(town, occupyingNation);
                    success = true;
                }
            }
        }
        if (success)
            SiegeWar.info("Old Town Occupation Data migrated...");
    }

    /**
     * In some previous SW versions,
     * the towny "neutral" flag was being re-used/hijacked to indicate peacefulness
     * -
     * However this was not suitable for either SW or Towny, because:
     * 1. The indicated Towny flag comes with an extra monetary cost which interfered with SW's system balance.
     * 2. The hijacking forced towny to support duplicate commands like /t toggle neutral, and /t toggle peaceful
     * -
     * This method migrates the old data,
     * so that if a town is found with neutral=true,
     * that town will be set to neutral=false, and the appropriate SW peaceful metadata flag will be set.
     * -
     */
    private static void migrateTownNeutralityData() {
        boolean success = false;
        for(Town town: new ArrayList<>(TownyAPI.getInstance().getTowns())) {
            if(town.isNeutral()) {
                TownMetaDataController.setPeacefulness(town, true);
                town.setNeutral(false);
                town.save();
                success = true;
            }
        }
        if (success)
            SiegeWar.info("Old Town Neutrality Data migrated...");
    }

    private static void deleteLegacyResidentMetadata() {
        for(Resident resident: TownyUniverse.getInstance().getResidents()) {
            ResidentMetaDataController.deleteLegacyMetadata(resident);
        }
    }

    private static void deleteLegacyTownMetadata() {
        for(Town town: TownyUniverse.getInstance().getTowns()) {
            TownMetaDataController.deleteLegacyMetadata(town);
        }
    }

    private static void deleteLegacyNationMetadata() {
        for(Nation nation: TownyUniverse.getInstance().getNations()) {
            NationMetaDataController.deleteLegacyMetadata(nation);
        }
    }

    /**
     * Handle legacy siege data
     * This method keeps legacy conquest siege data, and deletes any other legacy siege data
     * 
     * @param town the town which we know has a siege
     *             
     * @return true if we should load the given siege
     */
    public static boolean handleLegacySiegeDataAndCheckForLoad(Town town) {
        try {
            String siegeType = SiegeMetaDataController.getSiegeType(town);
            UUID attackerUUID;
            switch (siegeType.toLowerCase()) {
                case "conquest": 
                    return true;

                case "revolt":
                    attackerUUID = UUID.fromString(SiegeMetaDataController.getAttackerUUID(town));
                    if(attackerUUID.equals(town.getUUID())) {
                        SiegeWar.info("Data Migration: Deleting siege on " + town.getName() + ", because its format was a legacy revolt siege.");
                        SiegeMetaDataController.removeSiegeMeta(town);
                        town.save();
                        return false;
                    } else {
                        return true;
                    }
                
                default:    
                    if (TownyEconomyHandler.isActive()) {
                        attackerUUID = UUID.fromString(SiegeMetaDataController.getAttackerUUID(town));
                        Nation attacker = TownyAPI.getInstance().getNation(attackerUUID);
                        double warChestAmount = SiegeMetaDataController.getWarChestAmount(town);
                        attacker.getAccount().deposit(warChestAmount, "Warchest Returned by data migration");
                        SiegeWar.info("Data Migration: Siege on " + town.getName() + " had warchest returned to attacker, because the siege will not be loaded.");
                    }
                    SiegeWar.info("Data Migration: Deleting siege on " + town.getName() + ", because its type was legacy: " + siegeType + ".");
                    SiegeMetaDataController.removeSiegeMeta(town);
                    town.save();
                    return false;
            }
        } catch (Exception e) {
            SiegeWar.info("Problem Migrating Siege on " + town.getName());
            SiegeWar.info("Now deleting Siege on " + town.getName());
            SiegeMetaDataController.removeSiegeMeta(town);
            return false;
        }
    }
}
