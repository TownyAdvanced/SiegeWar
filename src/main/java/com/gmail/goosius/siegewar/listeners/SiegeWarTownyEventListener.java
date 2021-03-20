package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.tasks.SiegeWarTimerTaskController;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.time.NewHourEvent;
import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarTownyEventListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarTownyEventListener(SiegeWar instance) {

		plugin = instance;
	}
	
	   /*
     * Siegewar has to be conscious of when Towny has loaded the Towny database.
     */
    @EventHandler
    public void onTownyDatabaseLoad(TownyLoadedDatabaseEvent event) {
    	System.out.println(SiegeWar.prefix + "Towny database reload detected, reloading sieges...");
        SiegeController.loadAll();
        TownOccupationController.loadAll();
    }
    
    /*
     * Update town peacefulness counters.
     */
    @EventHandler
    public void onNewDay(PreNewDayEvent event) {
        if (SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
            TownPeacefulnessUtil.updateTownPeacefulnessCounters();
            if(SiegeWarSettings.getWarSiegeEnabled())
                TownPeacefulnessUtil.evaluatePeacefulTownOccupationAssignments();
        }
    }
    
    /*
     * On NewHours SW makes some calculations.
     */
    @EventHandler
    public void onNewHour(NewHourEvent event) {
        if(SiegeWarSettings.getWarSiegeEnabled()) {
            SiegeWarTimerTaskController.updatePopulationBasedBattlePointModifiers();
        }
    }

    /*
     * On each ShortTime period, SW makes some calcuations.
     */
    @EventHandler
    public void onShortTime(NewShortTimeEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled()) {
            SiegeWarTimerTaskController.punishPeacefulPlayersInActiveSiegeZones();
            SiegeWarTimerTaskController.evaluateBattleSessions();
            SiegeWarTimerTaskController.evaluateBannerControl();
            SiegeWarTimerTaskController.evaluateMapSneaking();
            SiegeWarTimerTaskController.evaluateTimedSiegeOutcomes();
            SiegeWarTimerTaskController.punishNonSiegeParticipantsInSiegeZone();
            SiegeHUDManager.updateHUDs();
            SiegeWarTimerTaskController.evaluateCannonSessions();
            SiegeWarTimerTaskController.evaluateBeacons();
        }

    }
    
    /*
     * SiegeWar prevents people from spawning to siege areas if they are not peaceful and do not belong to the town in question.
     */
    @EventHandler
    public void onPlayerUsesTownySpawnCommand(SpawnEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled()) {
            Town destinationTown = TownyAPI.getInstance().getTown(event.getTo());
            Resident res = TownyUniverse.getInstance().getResident(event.getPlayer().getUniqueId());
            if (destinationTown == null || res == null)
                return;
            
            // Don't block spawning for residents which belong to the Town
            if (destinationTown.hasResident(res))
                return;

            //Block TP if the target town is besieged
            if (SiegeController.hasActiveSiege(destinationTown)) {
                event.setCancelled(true);
                event.setCancelMessage(Translation.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
                return;
            }

            //Block TP if the target spawn point is in a siege zone
            if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo())) {
                event.setCancelled(true);
                event.setCancelMessage(Translation.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));

            }
        }       
    }

    /**
     * Do not explode the siege banner or its supporting block
     *
     * If trap mitigation is active,
     *  do not explode blocks below the siege banner altitude
     *
     * If the cannons integration is active,
     *  override any Towny protections of blocks which are within towns with active cannon sessions,
     *  and allow their explosion.
     *
     * @param event the TownyExplodingBlocksEvent event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExploding(TownyExplodingBlocksEvent event) {
        List<Block> finalExplodeList = new ArrayList<>();

        //Do not add to final explode list: Blocks near banner or protected by trap mitigation
        List<Block> townyExplodeList = event.getTownyFilteredBlockList();
        if(townyExplodeList != null) {
            for(Block block: townyExplodeList) {
                if ((SiegeWarSettings.isTrapWarfareMitigationEnabled() && SiegeWarDistanceUtil.isLocationInActiveTimedPointZoneAndBelowSiegeBannerAltitude(block.getLocation()))
                    ||
                    SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block)) {
                    //Do not add block to final explode list
                } else {
                    //Add to final explode list
                    finalExplodeList.add(block);
                }
            }
        }

        //Add to final explode list: town blocks if there is a cannon session in progress
        if(SiegeWarSettings.isCannonsIntegrationEnabled() && SiegeWar.getCannonsPluginIntegrationEnabled()) {
            List<Block> vanillaExplodeList = event.getVanillaBlockList(); //original list of exploding blocks
            Town town;
            for (Block block : vanillaExplodeList) {
                if(!finalExplodeList.contains(block)) {
                    town = TownyAPI.getInstance().getTown(block.getLocation());
                    if (town != null
                        && SiegeController.hasActiveSiege(town)
                        && SiegeController.getSiege(town).getCannonSessionRemainingShortTicks() > 0) {
                        finalExplodeList.add(block);
                    }
                }
            }
        }

        event.setBlockList(finalExplodeList);
    }

    /**
     * If the cannons integration is active,
     *   SiegeWar will allow explosion damage,
     *   if the entity is in a town which has an active cannon session.
     *
     * @param event the TownyExplosionDamagesEntityEvent event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosionDamageEntity(TownyExplosionDamagesEntityEvent event) {
        if(SiegeWarSettings.isCannonsIntegrationEnabled() && SiegeWar.getCannonsPluginIntegrationEnabled()) {
            if (event.isCancelled()) {
                Town town = TownyAPI.getInstance().getTown(event.getLocation());
                if (town != null
                        && SiegeController.hasActiveSiege(town)
                        && SiegeController.getSiege(town).getCannonSessionRemainingShortTicks() > 0) {
                    event.setCancelled(false);
                }
            }
        }
    }
}
