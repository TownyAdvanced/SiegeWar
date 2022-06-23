package com.gmail.goosius.siegewar.listeners;

import java.util.List;

import com.gmail.goosius.siegewar.events.ArtefactConsumeItemEvent;
import com.gmail.goosius.siegewar.events.ArtefactDamageEntityEvent;
import com.gmail.goosius.siegewar.events.ArtefactThrownPotionEvent;
import com.gmail.goosius.siegewar.utils.SiegeWarDominationAwardsUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNotificationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.playeractions.PlayerDeath;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.projectiles.ProjectileSource;

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
				//Artefact Potions
				if(SiegeWarDominationAwardsUtil.isArtefact(event.getItem())) {
					ArtefactConsumeItemEvent artefactEvent = new ArtefactConsumeItemEvent(event.getPlayer(), event.getItem());
					Bukkit.getPluginManager().callEvent(artefactEvent);
					return;
				}
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
	@EventHandler
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
		if (Towny.getPlugin().isCitizens2() && CitizensAPI.getNPCRegistry().isNPC(event.getPlayer()))
			return;
		
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

	@EventHandler
	public void on(PlayerJoinEvent event) {
		if(isSWEnabledAndIsThisAWarAllowedWorld(event.getPlayer().getWorld())) {
		    Siege siegeAtPlayerLocation = SiegeController.getActiveSiegeAtLocation(event.getPlayer().getLocation());
		    if(siegeAtPlayerLocation != null) {
		    	SiegeWarDistanceUtil.registerPlayerToActiveSiegeZone(event.getPlayer(), siegeAtPlayerLocation);
		    	SiegeWarNotificationUtil.warnPlayerOfActiveSiegeDanger(event.getPlayer(), siegeAtPlayerLocation);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(!isSWEnabledAndIsThisAWarAllowedWorld(event.getPlayer().getWorld()))
			return;

		//Remove banner-control related glowing
		if(SiegeController.getPlayersInBannerControlSessions().contains(event.getPlayer()) 
		  && event.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SiegeWar.getSiegeWar(), () -> event.getPlayer().removePotionEffect(PotionEffectType.GLOWING));
		}

		//Kill players in Siege-Zones
		if(event.getPlayer().getHealth() > 0
				&& SiegeWarSettings.getKillPlayersWhoLogoutInSiegeZones()
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getPlayer().getLocation())) {
			event.setQuitMessage(Translatable.of("msg_player_killed_for_logging_out_in_siege_zone", event.getPlayer().getName()).translate());
			event.getPlayer().setHealth(0);
		}
	}

	//Stops TNT/Minecarts from destroying blocks in the siegezone wilderness
	@EventHandler
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
	 * - Stop battlefield observers from hitting players in siegezones
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(EntityDamageByEntityEvent event) {	
		if(!isSWEnabledAndIsThisAWarAllowedWorld(event.getEntity().getWorld()))
			return;

		//Check for Artefact Usage
		if(event.getDamager() instanceof Player) {
			//Check if melee weapon is an artefact
			ItemStack itemInMainHand = ((Player) event.getDamager()).getInventory().getItemInMainHand();
			if(SiegeWarDominationAwardsUtil.isArtefact(itemInMainHand)) {
				ArtefactDamageEntityEvent artefactDamageEntityEvent = new ArtefactDamageEntityEvent(event.getDamager(), event.getEntity(), itemInMainHand);
				Bukkit.getPluginManager().callEvent(artefactDamageEntityEvent);
			}
		} else if (event.getDamager() instanceof Projectile) {
			//Check if missile is an artefact
			ProjectileSource shooter = ((Projectile)event.getDamager()).getShooter();
			if(shooter instanceof Player && SiegeWarDominationAwardsUtil.isArtefact(event.getDamager())) {
				ArtefactDamageEntityEvent artefactDamageEntityEvent = new ArtefactDamageEntityEvent((Player)shooter, event.getEntity(), event.getDamager());
				Bukkit.getPluginManager().callEvent(artefactDamageEntityEvent);
			}
		}

		//Return if the entity being damaged is not a player
		if(!(event.getEntity() instanceof Player))
			return;

		//Return if player being damaged is not in a SiegeZone
		if(!SiegeWarDistanceUtil.isPlayerRegisteredToActiveSiegeZone((Player)event.getEntity()))
			return;

		/*
		 * Catch-All to undo any remaining damage cancellation
		 */
		if(event.isCancelled() && SiegeWarSettings.isStopAllPvpProtection()) {
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

	@EventHandler (ignoreCancelled = true)
	public void on (PrepareItemCraftEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		if (!SiegeWarSettings.isDominationAwardsGlobalEnabled())
			return;
		if(event.isRepair()
				&& SiegeWarDominationAwardsUtil.isArtefact(event.getInventory().getResult())) {
			event.getInventory().setResult(null); //Cannot repair artefact
		}
	}

    @EventHandler (ignoreCancelled = true)
	public void on (PrepareAnvilEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		if (!SiegeWarSettings.isDominationAwardsGlobalEnabled())
			return;
		if(SiegeWarDominationAwardsUtil.isArtefact(event.getResult())) {
			event.setResult(null); //Cannot repair artefact
		}
	}

	/**
	 * If a bow/crossbow is fired, transfer any artefact tags to the projectile
	 *
	 * @param event the event
	 */
	@EventHandler (ignoreCancelled = true)
	public void on (EntityShootBowEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		if(!(event.getEntity() instanceof Player))
			return;
		boolean arrowIsArtefact = SiegeWarDominationAwardsUtil.isArtefact(event.getConsumable());
		boolean bowIsArtefact = SiegeWarDominationAwardsUtil.isArtefact(event.getBow());
		if(arrowIsArtefact || bowIsArtefact)
			setupArtefactIdentificationTag(event.getProjectile());  //Setup projectile to be artefact
		if(arrowIsArtefact)
			transferCustomEffectTags(event.getConsumable(), event.getProjectile()); //Transfer custom effect tags
		if(bowIsArtefact)
			transferCustomEffectTags(event.getBow(), event.getProjectile()); //Transfer custom effect tags
	}

	/**
	 * If a trident is launched, transfer any artefact tags to the projectile
	 *
	 * @param event the event
	 */
	@EventHandler (ignoreCancelled = true)
	public void on(ProjectileLaunchEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;
		if(!(event.getEntity().getShooter() instanceof Player))
			return;
		if(event.getEntityType() != EntityType.TRIDENT)
			return;
		ItemStack itemInMainHand = ((Player) event.getEntity().getShooter()).getInventory().getItemInMainHand();
		if(SiegeWarDominationAwardsUtil.isArtefact(itemInMainHand)) {
			setupArtefactIdentificationTag(event.getEntity());
			transferCustomEffectTags(itemInMainHand, event.getEntity());
		}
	}

	/**
	 * Setup an artefact tag, to allow the given projectile to be identified later as an artefact
	 * To do this,we add a dummy expiration time.
	 *
	 * @param projectile the projectile
	 */
	private void setupArtefactIdentificationTag(Entity projectile) {
		PersistentDataContainer projectileDataContainer = projectile.getPersistentDataContainer();
		projectileDataContainer.set(SiegeWarDominationAwardsUtil.EXPIRATION_TIME_KEY, SiegeWarDominationAwardsUtil.EXPIRATION_TIME_KEY_TYPE, 1L);
	}

	/**
	 * Transfer custom effect tags from itemstack to projectile
	 *
	 * @param itemStack the itemstack
	 * @param projectile the projectile
	 */
	private void transferCustomEffectTags(ItemStack itemStack, Entity projectile) {
		PersistentDataContainer itemStackDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
		PersistentDataContainer projectileDataContainer = projectile.getPersistentDataContainer();
		//Transfer custom effects
		if(itemStackDataContainer.has(SiegeWarDominationAwardsUtil.CUSTOM_EFFECTS_KEY, SiegeWarDominationAwardsUtil.CUSTOM_EFFECTS_KEY_TYPE)) {
			String customEffects = itemStackDataContainer.get(SiegeWarDominationAwardsUtil.CUSTOM_EFFECTS_KEY, SiegeWarDominationAwardsUtil.CUSTOM_EFFECTS_KEY_TYPE);
			projectileDataContainer.set(SiegeWarDominationAwardsUtil.CUSTOM_EFFECTS_KEY, SiegeWarDominationAwardsUtil.CUSTOM_EFFECTS_KEY_TYPE, customEffects);
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void on (PotionSplashEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()) {
			//Artefact Potions
			if(SiegeWarDominationAwardsUtil.isArtefact(event.getPotion().getItem())) {
				ArtefactThrownPotionEvent artefactEvent = new ArtefactThrownPotionEvent(event.getPotion(), event.getAffectedEntities());
				Bukkit.getPluginManager().callEvent(artefactEvent);
			}
		}
	}
	
	private static boolean isSWEnabledAndIsThisAWarAllowedWorld(World world) {
		return SiegeWarSettings.getWarSiegeEnabled() && TownyAPI.getInstance().getTownyWorld(world).isWarAllowed();
	}
}
