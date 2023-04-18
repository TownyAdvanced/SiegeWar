package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarInventoryUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarScoringUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * This class intercepts 'player death' events coming from the SiegeWarBukkitEventListener class.
 *
 * This class evaluates the death, and determines if the player is involved in any nearby sieges.
 * If so, the opposing team gains battle points, and the player keeps inventory.
 *
 * @author Goosius
 */
public class PlayerDeath {

	private static final String NATION_POINTS_NODE = SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS.getNode();
	private static final String TOWN_POINTS_NODE = SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS.getNode();

	/**
	 * Evaluates a siege death event.
	 * <p>
	 * Any player who dies in an active SiegeZone keeps inventory (with degrade)
	 * <p>
	 * If the dead player is officially involved in a nearby siege,
	 * - The opposing team gains battle points
	 * - Their inventory items degrade a little (e.g. 20%)
	 * <p>
	 * The allegiance of the killer is not considered,
	 * in order to allows for a wider range of siege-kill-tactics.
	 * Examples:
	 * - Players without towns can contribute to battle points
	 * - Players from non-nation towns can contribute to battle points
	 * - Players from secretly-allied nations can contribute to battle points
	 * - Devices (cannons, traps, bombs etc.) can be used to gain battle points
	 *
	 * @param deadPlayer The player who died
	 * @param playerDeathEvent The player death event
	 */
	public static void evaluateSiegePlayerDeath(Player deadPlayer, PlayerDeathEvent playerDeathEvent) {
		//Keep inventory
		if (SiegeWarSettings.isKeepInventoryOnSiegeZoneDeathEnabled() && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(deadPlayer.getLocation())) {
			SiegeWarInventoryUtil.degradeInventory(playerDeathEvent);
			SiegeWarInventoryUtil.keepInventory(playerDeathEvent);
		}
		//Battle-points & banner-control-sessions
		try {
			Resident deadResident = TownyUniverse.getInstance().getResident(deadPlayer.getUniqueId());
			// Weed out invalid residents, residents without a town, and players who cannot collect Points in a Siege.
			if (deadResident == null || !deadResident.hasTown() || playerIsMissingSiegePointsNodes(deadPlayer))
				return;

			Town deadResidentTown = deadResident.getTownOrNull();
			Siege siege = findAValidSiege(deadPlayer, deadResidentTown);

			// If player is confirmed as close to one or more sieges in which they are
			// eligible to be involved, apply siege point penalty for the nearest one, and
			// keep inventory
			if (siege != null) {
				//Award penalty points w/ notification if siege is in progress
				if(siege.getStatus() == SiegeStatus.IN_PROGRESS) {
					spawnFireWork(deadPlayer, siege);
					tryAwardingPoints(deadPlayer, deadResidentTown, siege);
				}
				// This might be outside of the IN_PROGRESS if statement because players could
				// be in a BCS and the SiegeStatus changes.
				tryRemovingPlayerFromBannerControlSession(deadPlayer, siege);
			}
		} catch (Exception e) {
			try {
				SiegeWar.severe("Error evaluating siege death for player " + deadPlayer.getName());
			} catch (Exception e2) {
				SiegeWar.severe("Error evaluating siege death (could not read player name)");
			}
			e.printStackTrace();
		}
	}

	private static boolean playerIsMissingSiegePointsNodes(Player deadPlayer) {
		return !TownyUniverse.getInstance().getPermissionSource().testPermission(deadPlayer, TOWN_POINTS_NODE)
			&& !TownyUniverse.getInstance().getPermissionSource().testPermission(deadPlayer, NATION_POINTS_NODE);
	}

	private static Siege findAValidSiege(Player deadPlayer, Town deadResidentTown) {
		Siege nearestSiege = null;
		double smallestDistanceToSiege = 0;

		//Find nearest eligible siege
		for (Siege candidateSiege : SiegeController.getSieges()) {

			//Skip if siege is not active
			if (!candidateSiege.getStatus().isActive())
				continue;

			//Skip if player is not is siege-zone
			if(!SiegeWarDistanceUtil.isInSiegeZone(deadPlayer, candidateSiege))
				continue;

			//Is player an attacker or defender in this siege?
			if(SiegeSide.getPlayerSiegeSide(nearestSiege, deadPlayer) == SiegeSide.NOBODY)
				continue;

			//Set nearestSiege if it is 1st viable one OR closer than smallestDistanceToSiege.
			double candidateSiegeDistanceToPlayer = deadPlayer.getLocation().distance(candidateSiege.getFlagLocation());
			if (nearestSiege == null || candidateSiegeDistanceToPlayer < smallestDistanceToSiege) {
				nearestSiege = candidateSiege;
				smallestDistanceToSiege = candidateSiegeDistanceToPlayer;
			}
		}
		return nearestSiege;
	}

	private static void spawnFireWork(Player deadPlayer, Siege siege) {
		if (SiegeWarSettings.getWarSiegeDeathSpawnFireworkEnabled()) {
			if (isBannerMissing(siege.getFlagBlock()))
				replaceMissingBanner(siege.getFlagBlock());
			Color bannerColor = ((Banner) siege.getFlagBlock().getState()).getBaseColor().getColor();
			CosmeticUtil.spawnFirework(deadPlayer.getLocation().add(0, 2, 0), Color.RED, bannerColor, true);
		}
	}

	private static boolean isBannerMissing(Block block) {
		return !Tag.BANNERS.isTagged(block.getType());
	}

	private static void replaceMissingBanner(Block block) {
		if (SiegeWarBlockUtil.isSupportBlockUnstable(block))
			block.getRelative(BlockFace.DOWN).setType(Material.STONE);
		
		block.setType(Material.BLACK_BANNER);
	}

	private static void tryAwardingPoints(Player deadPlayer, Town deadResidentTown, Siege siege) {
		//No penalty points without an active battle session
		if (BattleSession.getBattleSession().isActive()) {
			SiegeWarScoringUtil.awardPenaltyPoints(
					SiegeSide.getPlayerSiegeSide(siege, deadPlayer) == SiegeSide.ATTACKERS,
					deadPlayer,
					siege);
		}
	}

	private static void tryRemovingPlayerFromBannerControlSession(Player deadPlayer, Siege siege) {
		//If the player that died had an ongoing session, remove it.
		if(siege.getBannerControlSessions().containsKey(deadPlayer)) {
			siege.removeBannerControlSession(siege.getBannerControlSessions().get(deadPlayer));
			Messaging.sendMsg(deadPlayer, SiegeWarSettings.isWildernessTrapWarfareMitigationEnabled() 
				? Translatable.of("msg_siege_war_banner_control_session_failure_with_altitude")
				: Translatable.of("msg_siege_war_banner_control_session_failure"));
		}
	}

}
