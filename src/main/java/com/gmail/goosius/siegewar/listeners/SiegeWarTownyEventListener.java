package com.gmail.goosius.siegewar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.time.NewHourEvent;
import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.tasks.SiegeWarTimerTaskController;

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
    }
    
    /*
     * Update town peacefulness counters.
     */
    @EventHandler
    public void onNewDay(PreNewDayEvent event) {
        if (SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
            TownPeacefulnessUtil.updateTownPeacefulnessCounters();
            if(SiegeWarSettings.getWarSiegeEnabled())
                TownPeacefulnessUtil.evaluatePeacefulTownNationAssignments();
        }
    }
    
    /*
     * On NewHours SW makes some calculations.
     */
    @EventHandler
    public void onNewHour(NewHourEvent event) {
        if(SiegeWarSettings.getWarSiegeEnabled()) {
            SiegeWarTimerTaskController.updatePopulationBasedSiegePointModifiers();
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
            SiegeWarTimerTaskController.evaluateCannonSessions();
            SiegeWarTimerTaskController.punishNonSiegeParticipantsInSiegeZone();
            SiegeHUDManager.updateHUDs();
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
            
            // Don't block spawning for residents which belong to the Town, or to neutral towns.
            if (destinationTown.hasResident(res) || (destinationTown.isNeutral() && SiegeWarSettings.getWarCommonPeacefulTownsPublicSpawning()))
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
}
