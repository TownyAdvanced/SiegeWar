package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarPointsUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPermissionSource;
import com.gmail.goosius.siegewar.settings.Translation;

import org.bukkit.Color;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * This class intercepts 'player death' events coming from the TownyPlayerListener class.
 *
 * This class evaluates the death, and determines if the player is involved in any nearby sieges.
 * If so, their opponents gain siege points, and the player keeps inventory.
 *
 * @author Goosius
 */
public class PlayerDeath {

	/**
	 * Evaluates a siege death event.
	 *
	 * If the dead player is officially involved in a nearby siege, 
	 * - Their side loses siege points 
	 * - Their inventory items degrade a little (e.g. 10%)
	 *
	 * This mechanic allows for a wide range of siege-kill-tactics.
	 * Examples:
	 * - Players without towns can contribute to siege points
	 * - Players from non-nation towns can contribute to siege points
	 * - Players from secretly-allied nations can contribute to siege points
	 * - Devices (cannons, traps, bombs etc.) can be used to gain siege points
	 *
	 * @param deadPlayer The player who died
	 * @param playerDeathEvent The player death event
	 *
	 */
	public static void evaluateSiegePlayerDeath(Player deadPlayer, PlayerDeathEvent playerDeathEvent)  {
		try {
			if (!SiegeWarSettings.getWarSiegeWorlds().contains(playerDeathEvent.getEntity().getWorld().getName()))
				return;

			TownyPermissionSource tps = TownyUniverse.getInstance().getPermissionSource();
			Resident deadResident = TownyUniverse.getInstance().getResident(deadPlayer.getUniqueId());

			if (deadResident == null || !deadResident.hasTown())
				return;
			
			/*
			 * Do an early permission test to avoid hitting the sieges list if
			 * it could never return a proper SiegeSide.
			 */			
			if (!tps.testPermission(deadPlayer, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_POINTS.getNode())
				&& !PermissionUtil.hasTownMilitaryRank(deadResident)
				&& !tps.testPermission(deadPlayer, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_POINTS.getNode())
				&& !PermissionUtil.hasNationMilitaryRank(deadResident))
				return;

			Town deadResidentTown = deadResident.getTown();

			//Residents of occupied towns do not give siege points if killed
			if (deadResidentTown.isConquered())
				return;

			//Declare local variables
			Siege confirmedCandidateSiege = null;
			SiegeSide confirmedCandidateSiegePlayerSide = SiegeSide.NOBODY;
			double confirmedCandidateDistanceToPlayer = 0;
			double candidateSiegeDistanceToPlayer;
			SiegeSide candidateSiegePlayerSide;

			//Find nearest eligible siege
			for (Siege candidateSiege : SiegeController.getSieges()) {

				//Skip if player is not is siege-zone
				if(!SiegeWarDistanceUtil.isInSiegeZone(deadPlayer, candidateSiege))
					continue;

				//Is player eligible ?
				if (SiegeController.hasActiveSiege(deadResidentTown)
					&& SiegeController.getSiege(deadResidentTown) == candidateSiege
					&& (tps.testPermission(deadPlayer, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_POINTS.getNode())
						|| PermissionUtil.hasTownMilitaryRank(deadResident))
				) {
					candidateSiegePlayerSide = SiegeSide.DEFENDERS; //Candidate siege has player defending own-town

				} else if (deadResidentTown.hasNation()
					&& candidateSiege.getDefendingTown().hasNation()
					&& candidateSiege.getStatus().isActive()
					&& (tps.testPermission(deadPlayer, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_POINTS.getNode())
						|| PermissionUtil.hasNationMilitaryRank(deadResident))
					&& (deadResidentTown.getNation() == candidateSiege.getDefendingTown().getNation()
						|| deadResidentTown.getNation().hasMutualAlly(candidateSiege.getDefendingTown().getNation()))) {

					candidateSiegePlayerSide = SiegeSide.DEFENDERS; //Candidate siege has player defending another town

				} else if (deadResidentTown.hasNation()
					&& candidateSiege.getStatus().isActive()
					&& (tps.testPermission(deadPlayer, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_POINTS.getNode())
						|| PermissionUtil.hasNationMilitaryRank(deadResident))
					&& (deadResidentTown.getNation() == candidateSiege.getAttackingNation() 
						|| deadResidentTown.getNation().hasMutualAlly(candidateSiege.getAttackingNation()))) {

					candidateSiegePlayerSide = SiegeSide.ATTACKERS; //Candidate siege has player attacking

				} else {
					continue;
				}

				//Confirm candidate siege if it is 1st viable one OR closer than confirmed candidate
				candidateSiegeDistanceToPlayer = deadPlayer.getLocation().distance(candidateSiege.getFlagLocation());
				if(confirmedCandidateSiege == null || candidateSiegeDistanceToPlayer < confirmedCandidateDistanceToPlayer) {
					confirmedCandidateSiege = candidateSiege;
					confirmedCandidateSiegePlayerSide = candidateSiegePlayerSide;
					confirmedCandidateDistanceToPlayer = candidateSiegeDistanceToPlayer;
				}
			}

			//If player is confirmed as close to one or more sieges in which they are eligible to be involved, 
			// apply siege point penalty for the nearest one, and keep inventory
			if(confirmedCandidateSiege != null) {

				//Award penalty points w/ notification if siege is in progress
				if(confirmedCandidateSiege.getStatus() == SiegeStatus.IN_PROGRESS) {
					if (SiegeWarSettings.getWarSiegeDeathSpawnFireworkEnabled()) {
						Color bannerColor = ((Banner) confirmedCandidateSiege.getFlagLocation().getBlock().getState()).getBaseColor().getColor();
						CosmeticUtil.spawnFirework(deadPlayer.getLocation().add(0, 2, 0), Color.RED, bannerColor, true);
					}

					if (confirmedCandidateSiegePlayerSide == SiegeSide.DEFENDERS) {
						SiegeWarPointsUtil.awardPenaltyPoints(
								false,
								deadPlayer,
								deadResident,
								confirmedCandidateSiege,
								Translation.of("msg_siege_war_defender_death"));
					} else {
						SiegeWarPointsUtil.awardPenaltyPoints(
								true,
								deadPlayer,
								deadResident,
								confirmedCandidateSiege,
								Translation.of("msg_siege_war_attacker_death"));
					}
				}

				//Keep and degrade inventory
				degradeInventory(playerDeathEvent);
				keepInventory(playerDeathEvent);
				SiegeHUDManager.updateHUDs();

				if (confirmedCandidateSiege.getBannerControlSessions().containsKey(deadPlayer)) { //If the player that died had an ongoing session, remove it.
					confirmedCandidateSiege.removeBannerControlSession(confirmedCandidateSiege.getBannerControlSessions().get(deadPlayer));
					Messaging.sendMsg(deadPlayer, Translation.of("msg_siege_war_banner_control_session_failure"));
				}
			}
		} catch (Exception e) {
			try {
				System.out.println("Error evaluating siege death for player " + deadPlayer.getName());
			} catch (Exception e2) {
				System.out.println("Error evaluating siege death (could not read player name)");
			}
			e.printStackTrace();
		}
	}

	private static void degradeInventory(PlayerDeathEvent playerDeathEvent) {
		Damageable damageable;
		double maxDurability;
		int currentDurability;
		int damageToInflict;
		int newDurability;
		Boolean closeToBreaking = false;
		if (SiegeWarSettings.getWarSiegeDeathPenaltyDegradeInventoryEnabled()) {
			for (ItemStack itemStack : playerDeathEvent.getEntity().getInventory().getContents()) {
				if (itemStack != null && itemStack.getType().getMaxDurability() != 0 && !itemStack.getItemMeta().isUnbreakable()) {
					damageable = ((Damageable) itemStack.getItemMeta());
					maxDurability = itemStack.getType().getMaxDurability();
					currentDurability = damageable.getDamage();
					damageToInflict = (int)(maxDurability / 100 * SiegeWarSettings.getWarSiegeDeathPenaltyDegradeInventoryPercentage());
					newDurability = currentDurability + damageToInflict;
					if (newDurability >= maxDurability) {
						damageable.setDamage(Math.max((int)maxDurability-10, currentDurability));
						closeToBreaking = true;
					}
					else {
						damageable.setDamage(newDurability);
					}
					itemStack.setItemMeta((ItemMeta)damageable);
				}
			}
			if (closeToBreaking) //One or more items are close to breaking, send warning.
				Messaging.sendMsg(playerDeathEvent.getEntity(), Translation.of("msg_inventory_degrade_warning"));
		}
	}

	private static void keepInventory(PlayerDeathEvent playerDeathEvent) {
		if(SiegeWarSettings.getWarSiegeDeathPenaltyKeepInventoryEnabled() && !playerDeathEvent.getKeepInventory()) {
			playerDeathEvent.setKeepInventory(true);
			playerDeathEvent.getDrops().clear();
		}
	}
}
