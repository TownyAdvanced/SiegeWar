package com.gmail.goosius.siegewar.listeners;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.SiegeWarTimerTaskController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.time.NewHourEvent;
import com.palmergames.bukkit.towny.event.time.NewShortTimeEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarEventListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarEventListener(SiegeWar instance) {

		plugin = instance;
	}

	/*
	 * SW will prevent someone in a banner area from curing their poisoning with milk.
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerConsume(PlayerItemConsumeEvent event) {

		if(SiegeWarSettings.getWarSiegeEnabled()) {
			try {
				//Prevent milk bucket usage while attempting to gain banner control
				if(event.getItem().getType() == Material.MILK_BUCKET) {
					for(Siege siege: SiegeController.getSieges()) {
						if(siege.getBannerControlSessions().containsKey(event.getPlayer())) {
							event.setCancelled(true);
							TownyMessaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_war_siege_zone_milk_bucket_forbidden_while_attempting_banner_control"));
						}
					}
				}
		
			} catch (Exception e) {
				System.out.println("Problem evaluating siege player consume event");
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Duplicates what exists in the TownyBlockListener but on a higher priority.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {

		if (testBlockMove(event.getBlock(), event.isSticky() ? event.getDirection().getOppositeFace() : event.getDirection()))
			event.setCancelled(true);

		List<Block> blocks = event.getBlocks();
		
		if (!blocks.isEmpty()) {
			//check each block to see if it's going to pass a plot boundary
			for (Block block : blocks) {
				if (testBlockMove(block, event.getDirection()))
					event.setCancelled(true);
			}
		}
	}

	/*
	 * Duplicates what exists in the TownyBlockListener but on a higher priority.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {

		if (testBlockMove(event.getBlock(), event.getDirection()))
			event.setCancelled(true);
		
		List<Block> blocks = event.getBlocks();

		if (!blocks.isEmpty()) {
			//check each block to see if it's going to pass a plot boundary
			for (Block block : blocks) {
				if (testBlockMove(block, event.getDirection()))
					event.setCancelled(true);
			}
		}
	}

	/**
	 * Decides whether blocks moved by pistons follow the rules.
	 * 
	 * @param block - block that is being moved.
	 * @param direction - direction the piston is facing.
	 * 
	 * @return true if block is able to be moved according to siege war rules. 
	 */
	private boolean testBlockMove(Block block, BlockFace direction) {

		Block blockTo = block.getRelative(direction);

		if(SiegeWarSettings.getWarSiegeEnabled()) {
			if(SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block) || SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(blockTo)) {
				return true;
			}
		}

		return false;
	}
	
	/*
	 * Siegewar has to be conscious of when Towny has loaded the Towny database.
	 */
	@EventHandler
	public void onTownyDatabaseLoad(TownyLoadedDatabaseEvent event) {
		plugin.loadSieges();
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
			SiegeWarTimerTaskController.evaluateTacticalVisibility();
			SiegeWarTimerTaskController.evaluateTimedSiegeOutcomes();
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
			if (destinationTown.hasResident(res) || destinationTown.isNeutral())
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
