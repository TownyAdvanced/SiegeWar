package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.integration.cannons.CannonsIntegration;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.tasks.SiegeWarTimerTaskController;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarImmunityUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNotificationUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.TranslationLoadEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.damage.TownyFriendlyFireTestEvent;
import com.palmergames.bukkit.towny.event.teleport.OutlawTeleportEvent;
import com.palmergames.bukkit.towny.event.time.NewHourEvent;
import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TranslationLoader;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * When Towny is reloading the languages, make sure we're re-injecting our language strings. 
	 */
	@EventHandler
	public void onTownyLoadLanguages(TranslationLoadEvent event) {
		Plugin plugin = SiegeWar.getSiegeWar();
		Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
		TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, SiegeWar.class);
		loader.load();
		Map<String, Map<String, String>> translations = loader.getTranslations();

		for (String language : translations.keySet())
			for (Map.Entry<String, String> map : translations.get(language).entrySet())
				event.addTranslation(language, map.getKey(), map.getValue());
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
            SiegeWarTimerTaskController.evaluateBattleSessions();
            SiegeWarDistanceUtil.recalculatePlayersRegisteredToActiveSiegeZones();
            SiegeWarTimerTaskController.evaluateWarSickness();
            SiegeWarTimerTaskController.evaluateBannerControl();
            SiegeWarTimerTaskController.evaluateWallBreaching();         
            SiegeWarTimerTaskController.evaluateTimedSiegeOutcomes();
            SiegeHUDManager.updateHUDs();
            SiegeWarTimerTaskController.evaluateBeacons();
            SiegeWarNotificationUtil.warnAllPlayersOfSiegeDanger();
        }
    }

    /**
     * Process block explosion events coming from Towny 
     *
     * @param event the TownyExplodingBlocksEvent event
     * @throws TownyException if something is misconfigured
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExploding(TownyExplodingBlocksEvent event) throws TownyException {
        if(!SiegeWarSettings.getWarSiegeEnabled())
            return;
        if (event.getEntity() != null && !TownyAPI.getInstance().getTownyWorld(event.getEntity().getWorld()).isWarAllowed())
            return;    
        List<Block> filteredExplodeList = event.getTownyFilteredBlockList();
        filteredExplodeList = CannonsIntegration.filterExplodeListByCannonEffects(filteredExplodeList, event);
        filteredExplodeList = filterExplodeListBySiegeBannerProtection(filteredExplodeList);
        filteredExplodeList = filterExplodeListByTrapWarfareMitigation(filteredExplodeList);
        event.setBlockList(filteredExplodeList);
    }

    /**
     * Filter a given explode list by siege banner protection
     * @param givenExplodeList given list of exploding blocks
     *
     * @return filtered list
     */
    private static List<Block> filterExplodeListBySiegeBannerProtection(List<Block> givenExplodeList) {       
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
    private static List<Block> filterExplodeListByTrapWarfareMitigation(List<Block> givenExplodeList) {
        if(!SiegeWarSettings.isTrapWarfareMitigationEnabled())
            return givenExplodeList;
        //For performance, just get the siege at the 1st block
        Siege siege;
        if(givenExplodeList.size() > 0) {
            siege = SiegeController.getActiveSiegeAtLocation(givenExplodeList.get(0).getLocation());
            if(siege == null)
               return givenExplodeList;
        } else {
            return givenExplodeList;
        }

        //Make convenience variables
        int protectionRadiusBlocks = SiegeWarSettings.getTrapWarfareMitigationRadiusBlocks();
        int upperAlterLimit = SiegeWarSettings.getTrapWarfareMitigationUpperHeightLimit();
        int lowerAlterLimit = SiegeWarSettings.getTrapWarfareMitigationLowerHeightLimit();
        Location siegeBannerLocation = siege.getFlagLocation();

        //Filter exploding blocks
        List<Block> finalExplodeList = new ArrayList<>(givenExplodeList);
        for(Block block: givenExplodeList) {
            if(SiegeWarDistanceUtil.isTargetLocationProtectedByTrapWarfareMitigation(
                    block.getLocation(),
                    siegeBannerLocation,
                    protectionRadiusBlocks,
                    upperAlterLimit,
                    lowerAlterLimit)) {
                finalExplodeList.remove(block);
            }
        }

        //Return final explode list
        return finalExplodeList;
    }

    /**
     * During a siege, players in the town are not protected from explosion damage.
     * (a related effect to how PVP protection is forced off)
     *
     * @param event the TownyExplosionDamagesEntityEvent event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosionDamageEntity(TownyExplosionDamagesEntityEvent event) {
        if (!event.isCancelled())
            return;
        if(!SiegeWarSettings.getWarSiegeEnabled())
            return;
        if (!TownyAPI.getInstance().getTownyWorld(event.getEntity().getWorld()).isWarAllowed())
            return;
        if(!(event.getEntity() instanceof Player))
            return;
        if(event.getTown() == null)
            return;
        if(SiegeController.hasActiveSiege(event.getTown()))
            event.setCancelled(false);
    }
    
    /**
     * Prevent an outlaw being teleported away if the town they are outlawed in has an active siege.
     * @param event OutlawTeleportEvent thrown by Towny.
     */
    @EventHandler
    public void onOutlawTeleportEvent(OutlawTeleportEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled() 
                && event.getOutlawLocation() != null
                && event.getOutlawLocation().getWorld() != null
                && TownyAPI.getInstance().getTownyWorld(event.getOutlawLocation().getWorld()).isWarAllowed()
                && SiegeController.hasActiveSiege(event.getTown())) {
    		event.setCancelled(true);
            }
    }

    /**
     * If friendly fire has cancelled damage in a SiegeZone,
     * undo the cancellation
     * @param event the event
     */
    @EventHandler
    public void on (TownyFriendlyFireTestEvent event) {
    	if (!event.isPVP()
    	        && SiegeWarSettings.getWarSiegeEnabled() 
                && TownyAPI.getInstance().getTownyWorld(event.getAttacker().getWorld()).isWarAllowed()
                && SiegeWarSettings.isStopTownyFriendlyFireProtection()
                && SiegeWarDistanceUtil.isPlayerRegisteredToActiveSiegeZone(event.getAttacker())) {
            event.setPVP(true);
        }
    }
}
