package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.integration.cannons.CannonsIntegration;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.tasks.SiegeWarTimerTaskController;
import com.gmail.goosius.siegewar.utils.*;
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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
            SiegeWarTimerTaskController.evaluateWallBreaching();         
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
     * Process block explosion events coming from Towny 
     *
     * @param event the TownyExplodingBlocksEvent event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExploding(TownyExplodingBlocksEvent event) {
        List<Block> currentExplodeList = event.getTownyFilteredBlockList();
        List<Block> filteredExplodeList = filterExplodeListByCannonEffects(currentExplodeList, event);
        filteredExplodeList = filterExplodeListBySiegeBannerProtection(filteredExplodeList);
        filteredExplodeList = filterExplodeListByTrapWarfareMitigation(filteredExplodeList);
        event.setBlockList(filteredExplodeList);
    }

    /**
     * Filter a given explode list by cannon effects
     * @param givenExplodeList given list of exploding blocks
     * @param event the explosion event
     *
     * @return filtered list
     */
    private List<Block> filterExplodeListByCannonEffects(List<Block> givenExplodeList, TownyExplodingBlocksEvent event) {       
        if(SiegeWar.getCannonsPluginIntegrationEnabled()
            && BattleSession.getBattleSession().isActive()
            && event.getEntity() != null
            && event.getEntity() instanceof Projectile
            && ((Projectile) event.getEntity()).getShooter() instanceof Player) {

            //Prepare filtered list
            List<Block> filteredList = new ArrayList<>(givenExplodeList);

            //Prepare some cache sets, to optimize processing
            Set<Town> cachedSafeTownSet = new HashSet<>();
            Set<Town> cachedUnsafeTownSet = new HashSet<>();
 
            //Find the blocks explosions which were removed by Towny, and see if they should be re-added.
            Player player = (Player)(((Projectile) event.getEntity()).getShooter());
            Town town;
            List<Block> vanillaExplodeList = event.getVanillaBlockList(); //The pre-towny-protection list
            for (Block block : vanillaExplodeList) {
                if(givenExplodeList.contains(block))
                    continue;   //Block is unprotected & will explode. No breach points needed
                town = TownyAPI.getInstance().getTown(block.getLocation());
                if(town == null)
                    continue; 
                if(cachedSafeTownSet.contains(town))
                    continue;
                if(cachedUnsafeTownSet.contains(town)) {
                    filteredList.add(block);
                    continue;
                }                
                Siege siege = SiegeController.getSiege(town);
                if(siege == null || !siege.getStatus().isActive()) {
                    cachedSafeTownSet.add(town); //No siege or inactive siege. Town is safe
                    continue;
                }
                if(!SiegeWarWallBreachUtil.canPlayerUseBreachPointsByCannon(player, siege))
                    continue;
                cachedUnsafeTownSet.add(town);  //Player can breach at the siege. Town is unsafe
                if(!SiegeWarWallBreachUtil.payBreachPoints(SiegeWarSettings.getWallBreachingCannonExplosionCostPerBlock(), siege))
                    continue;   //Insufficient breach points to explode this block
                /*
                 * Player has now paid the required breach points.
                 * Allow block to explode
                 */
                 filteredList.add(block);
            }

            //Return filtered list
            return filteredList;
        } else {
            //Return given list
            return givenExplodeList;
        }
    }

    /**
     * Filter a given explode list by siege banner protection
     * @param givenExplodeList given list of exploding blocks
     *
     * @return filtered list
     */
    private List<Block> filterExplodeListBySiegeBannerProtection(List<Block> givenExplodeList) {       
        List<Block> filteredList = new ArrayList<>(givenExplodeList);
        for(Block block: givenExplodeList) {
            if (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block)) {
                //Remove block from final explode list
                filteredList.remove(block);
            } 
        }
        return filteredList;
    }

    /**
     * Filter a given explode list by trap warfare mitigation
     * @param givenExplodeList given list of exploding blocks
     *
     * @return filtered list
     */
    private List<Block> filterExplodeListByTrapWarfareMitigation(List<Block> givenExplodeList) {       
        List<Block> filteredList = new ArrayList<>(givenExplodeList);
        if(!SiegeWarSettings.isTrapWarfareMitigationEnabled())
            return filteredList;

        for(Block block: givenExplodeList) {
            if (SiegeWarDistanceUtil.isLocationInActiveTimedPointZoneAndBelowSiegeBannerAltitude(block.getLocation())) {
                //Remove block from final explode list
                filteredList.remove(block);
            } 
        }
        return filteredList;
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
