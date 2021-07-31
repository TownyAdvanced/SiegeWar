package com.gmail.goosius.siegewar.listeners;

import java.util.List;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffectType;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.playeractions.PlayerDeath;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarBukkitEventListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarBukkitEventListener(SiegeWar instance) {

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
							Messaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_war_siege_zone_milk_bucket_forbidden_while_attempting_banner_control"));
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
	 * SW can affect whether an inventory is dropped and also can degrade an inventory.
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		//Check for siege-war related death effects
		if(SiegeWarSettings.getWarSiegeEnabled()) {
			PlayerDeath.evaluateSiegePlayerDeath(event.getEntity(), event);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		
		// Don't stop admins/ops. towny.admin.spawn is part of towny.admin.
		if (event.getPlayer().hasPermission("towny.admin.spawn") || event.getPlayer().isOp())
			return;
		
		if (SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled()
			&& (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND)) {
			if (TownyAPI.getInstance().isWilderness(event.getTo())) { // The teleport destination is in the wilderness.
				if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo())) {
					Messaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
					event.setCancelled(true);
				}
			} else { // The teleport destination is inside a town.
				Town destinationTown = TownyAPI.getInstance().getTown(event.getTo());
				Resident resident = TownyUniverse.getInstance().getResident(event.getPlayer().getUniqueId());

				if (destinationTown.hasResident(resident))
					return;

				//Check IF TP destination is a besieged town
				if(SiegeController.hasActiveSiege(destinationTown)) {
					Messaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
					event.setCancelled(true);
					return;
				}

				//Check if the destination is inside a siege zone
				if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo())) {
					Messaging.sendErrorMsg(event.getPlayer(), Translation.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeController.getPlayersInBannerControlSessions().contains(event.getPlayer()) 
				&& event.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), new Runnable() {
				@Override
				public void run() {
					event.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
				}
			});
		}
	}

	//Stops TNT/Minecarts from destroying blocks in the siegezone wilderness
	@EventHandler
	public void on(EntityExplodeEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& !event.isCancelled()
				&& SiegeWarSettings.getSiegeZoneWildernessForbiddenExplodeEntityTypes().contains(event.getEntityType())
				&& TownyAPI.getInstance().getTown(event.getLocation()) == null
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getLocation())) {
			event.setCancelled(true);
		}
	}
	
	//Stops TNT/Minecarts from injuring players in the siegezone wilderness
	@EventHandler
	public void on(EntityDamageByEntityEvent event) {	
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& !event.isCancelled()
				&& SiegeWarSettings.getSiegeZoneWildernessForbiddenExplodeEntityTypes().contains(event.getDamager().getType())
				&& TownyAPI.getInstance().getTown(event.getDamager().getLocation()) == null
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getDamager().getLocation())) {
			event.setCancelled(true);
		}
	}

	//Stops battlefield reporters from taking damage in siegezones
	@EventHandler
	public void on(EntityDamageEvent event) {	
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& !event.isCancelled()
				&& event.getEntity() instanceof Player
				&& event.getEntity().hasPermission(SiegeWarPermissionNodes.SIEGEWAR_BATTLEFIELD_REPORTER.getNode())
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getEntity().getLocation())) {
			event.setCancelled(true);
		}
	}				
}
