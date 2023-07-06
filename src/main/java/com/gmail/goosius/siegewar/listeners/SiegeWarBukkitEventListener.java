package com.gmail.goosius.siegewar.listeners;

import java.util.List;

import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.utils.DataCleanupUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNotificationUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSpawnUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarWarningsUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerJoinEvent;
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
import com.palmergames.bukkit.towny.hooks.PluginIntegrations;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarBukkitEventListener implements Listener {
	
	public SiegeWarBukkitEventListener() {}

	/*
	 * SW will prevent someone in a banner area from curing their poisoning with milk.
	 *
	 * Also Artefacts fire events
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
							Messaging.sendErrorMsg(event.getPlayer(), Translatable.of("msg_war_siege_zone_milk_bucket_forbidden_while_attempting_banner_control"));
						}
					}
				}
			} catch (Exception e) {
				SiegeWar.severe("Problem evaluating siege player consume event");
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
	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		//Check for siege-war related death effects
		if(isSWEnabledAndIsThisAWarAllowedWorld(event.getEntity().getWorld())) {
			PlayerDeath.evaluateSiegePlayerDeath(event.getEntity(), event);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		// Check if SiegeWar is set to disallow non-residents teleporting into a siege
		// zone, limiting to only plugin and command TeleportCauses.
		if (!siegeWarStopsNonResidentsTeleporting(event))
			return;

		// Don't stop admins/ops. towny.admin.spawn is part of towny.admin.
		if (event.getPlayer().hasPermission("towny.admin.spawn") || event.getPlayer().isOp())
			return;
		
		// Let's ignore Citizens NPCs
		if (PluginIntegrations.getInstance().checkCitizens(event.getPlayer()))
			return;
		
		// Don't stop a player if they have a teleport pass
		if(SiegeWarSpawnUtil.doesPlayerHasTeleportPass(event.getPlayer())) {
			SiegeWarSpawnUtil.removePlayerTeleportPass(event.getPlayer());
			return;
		}
		
		// The teleport destination is in the wilderness.
		if (TownyAPI.getInstance().isWilderness(event.getTo())) {
			// A part of an active siege zone in the wilderness, we stop it.
			if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo())) {
				Messaging.sendErrorMsg(event.getPlayer(), Translatable.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
				event.setCancelled(true);
			}
			// An otherwise allowed teleport to this type of wilderness.
			return;
		}
		// The teleport destination is inside a town.

		// Don't stop a resident spawning into their own town.
		Town destinationTown = TownyAPI.getInstance().getTown(event.getTo());
		if (destinationTown.hasResident(event.getPlayer()))
			return;

		// Stop anyone else teleporting into a town with an active siege.
		if(SiegeController.hasActiveSiege(destinationTown)) {
			Messaging.sendErrorMsg(event.getPlayer(), Translatable.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
			event.setCancelled(true);
			return;
		}

		// Finally, stop a teleport into a town which is in another town's active siege zone.
		if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo())) {
			Messaging.sendErrorMsg(event.getPlayer(), Translatable.of("msg_err_siege_war_cannot_spawn_into_siegezone_or_besieged_town"));
			event.setCancelled(true);
		}
	}

	private boolean siegeWarStopsNonResidentsTeleporting(PlayerTeleportEvent event) {
		return SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled()
			&& (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND);
	}

	@EventHandler(ignoreCancelled = true)
	public void on(PlayerJoinEvent event) {
		if (isSWEnabledAndIsThisAWarAllowedWorld(event.getPlayer().getWorld())) {
			Siege activeSiegeAtPlayerLocation = SiegeController.getActiveSiegeAtLocation(event.getPlayer().getLocation());
			if(activeSiegeAtPlayerLocation != null) {
				//Register in active siege zone, for PVP calculations etc.
				SiegeWarDistanceUtil.registerPlayerToActiveSiegeZone(event.getPlayer(), activeSiegeAtPlayerLocation);
				/* 
				 * Send active siege warning.
				 * Note: The player object will be new, even if the player logged in recently.
				 * Thus, this line will always trigger a warning message.
				 */
				SiegeWarNotificationUtil.sendSiegeZoneProximityWarning(event.getPlayer(), activeSiegeAtPlayerLocation);
			}

			//Warn player if there are dodgy configs
			if(SiegeWarSettings.isBadConfigWarningsEnabled()) {
				SiegeWarWarningsUtil.sendWarningsIfConfigsBad(event.getPlayer());
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(!isSWEnabledAndIsThisAWarAllowedWorld(event.getPlayer().getWorld()))
			return;

		//Remove banner-control related glowing
		if(SiegeController.getPlayersInBannerControlSessions().contains(event.getPlayer()) 
		  && event.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
			SiegeWar.getSiegeWar().getScheduler().runLater(() -> event.getPlayer().removePotionEffect(PotionEffectType.GLOWING), 1l);
		}
	}

	//Stops TNT/Minecarts from destroying blocks in the siegezone wilderness
	@EventHandler(ignoreCancelled = true)
	public void on(EntityExplodeEvent event) {
		if(isSWEnabledAndIsThisAWarAllowedWorld(event.getEntity().getWorld())
				&& !event.isCancelled()
				&& SiegeWarSettings.getSiegeZoneWildernessForbiddenExplodeEntityTypes().contains(event.getEntityType())
				&& TownyAPI.getInstance().getTown(event.getLocation()) == null
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getLocation())) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * - Stop in-siegezone pvp events from being cancelled (e.g. by other plugins)
	 * - Stop TNT/Minecarts from injuring players in the siegezone wilderness
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(EntityDamageByEntityEvent event) {	
		if(!isSWEnabledAndIsThisAWarAllowedWorld(event.getEntity().getWorld()))
			return;

		//Return if the entity being damaged is not a player
		if(!(event.getEntity() instanceof Player))
			return;

		//Return if player being damaged is not in a SiegeZone
		if(!SiegeWarDistanceUtil.isPlayerRegisteredToActiveSiegeZone((Player)event.getEntity()))
			return;

		/*
		 * Catch-All to undo any remaining damage cancellation during battle sessions
		 */
		if(event.isCancelled() && SiegeWarSettings.isStopAllPvpProtection() && BattleSession.getBattleSession().isActive()) {
			event.setCancelled(false);
		}

		//EVP event ?:
		if(!(event.getDamager() instanceof Player)) {
			//Stop TNT/Minecarts from damaging players in SiegeZone wilderness
			if (SiegeWarSettings.getSiegeZoneWildernessForbiddenExplodeEntityTypes().contains(event.getDamager().getType())
					&& TownyAPI.getInstance().isWilderness(event.getEntity().getLocation())) {
				event.setCancelled(true);
				return;
			}
		}
	}

	private static boolean isSWEnabledAndIsThisAWarAllowedWorld(World world) {
		return SiegeWarSettings.getWarSiegeEnabled() && TownyAPI.getInstance().getTownyWorld(world).isWarAllowed();
	}


	@EventHandler (ignoreCancelled = true)
	public void on (PrepareItemCraftEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		if(event.isRepair()
				&& DataCleanupUtil.isLegacyArtefact(event.getInventory().getResult())) {
			event.getInventory().setResult(null); //Cannot repair artefact
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void on (PrepareAnvilEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		if(DataCleanupUtil.isLegacyArtefact(event.getResult())) {
			event.setResult(null); //Cannot repair artefact
		}
	}

	/**
	 * If toxicity reduction is enabled, the following effect applies:
	 * - No /tell if a battle session is active (and for 10 mins after)
	 * 
	 * This method will pick up any command where the first arg is "/<anything>tell"
	 * 
	 * @param event the player command preprocess event 
	 */
	@EventHandler (ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event){
		if(!SiegeWarSettings.getWarSiegeEnabled() || !SiegeWarSettings.isToxicityReductionEnabled())
			return;
		if(event.getMessage().split(" ")[0].endsWith("tell")) {
			if(BattleSession.getBattleSession().isChatDisabled()) {
				event.setCancelled(true);
				SiegeWarNotificationUtil.notifyPlayerOfBattleSessionChatRestriction(event.getPlayer(), "tell");
			}
		}
	}
}
