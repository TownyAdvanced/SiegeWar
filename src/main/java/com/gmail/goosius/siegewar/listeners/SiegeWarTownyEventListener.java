package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.integration.cannons.CannonsIntegration;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.tasks.SiegeWarTimerTaskController;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarImmunityUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.teleport.OutlawTeleportEvent;
import com.palmergames.bukkit.towny.event.time.NewHourEvent;
import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
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
    	SiegeWar.info("Towny database reload detected, reloading sieges...");
        SiegeController.loadAll();
        TownOccupationController.loadAll();
    }
    
    /*
     * Update town peacefulness counters.
     */
    @EventHandler
    public void onNewDay(PreNewDayEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
            TownPeacefulnessUtil.updateTownPeacefulnessCounters();
        }
    }
    
    /*
     * On NewHours SW makes some calculations.
     */
    @EventHandler
    public void onNewHour(NewHourEvent event) {
        if(SiegeWarSettings.getWarSiegeEnabled()) {
            SiegeWarImmunityUtil.evaluateExpiredImmunities();
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
            SiegeWarTimerTaskController.evaluateMapHiding();
            SiegeWarTimerTaskController.evaluateTimedSiegeOutcomes();
            SiegeWarTimerTaskController.punishNonSiegeParticipantsInSiegeZones();
            SiegeHUDManager.updateHUDs();
            SiegeWarTimerTaskController.evaluateCannonSessions();
            SiegeWarTimerTaskController.evaluateBeacons();
            SiegeWarTimerTaskController.evaluateBattlefieldReporters();
        }

    }

    /**
     * Do not explode the siege banner or its supporting block
     *
     * If trap mitigation is active,
     *  do not explode blocks below the siege banner altitude
     *
     * If the Cannons integration is active,
     *  override any Towny protections of blocks within towns where cannon sessions are in progress.
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

        //Add to final explode list: town blocks if town has a cannon session in progress
        if(SiegeWarSettings.isCannonsIntegrationEnabled() && SiegeWar.getCannonsPluginIntegrationEnabled()) {
            List<Block> vanillaExplodeList = event.getVanillaBlockList(); //original list of exploding blocks
            Town town;
            for (Block block : vanillaExplodeList) {
                if(!finalExplodeList.contains(block)) {
                    town = TownyAPI.getInstance().getTown(block.getLocation());
                    if (town != null && CannonsIntegration.doesTownHaveCannonSession(town)) {
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
     *   if the entity is in a town which has a cannon session
     *
     * @param event the TownyExplosionDamagesEntityEvent event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosionDamageEntity(TownyExplosionDamagesEntityEvent event) {
        if(SiegeWarSettings.isCannonsIntegrationEnabled() && SiegeWar.getCannonsPluginIntegrationEnabled()) {
            if (event.isCancelled()) {
                Town town = TownyAPI.getInstance().getTown(event.getLocation());
                if (town != null && CannonsIntegration.doesTownHaveCannonSession(town)) {
                    event.setCancelled(false);
                }
            }
        }
    }
    
    /**
     * Prevent an outlaw being teleported away if the town they are outlawed in has an active siege.
     * @param event OutlawTeleportEvent thrown by Towny.
     */
    @EventHandler
    public void onOutlawTeleportEvent(OutlawTeleportEvent event) {
    	if (SiegeWarSettings.getWarSiegeEnabled() && SiegeController.hasActiveSiege(event.getTown())) 
    		event.setCancelled(true);
    }
}
