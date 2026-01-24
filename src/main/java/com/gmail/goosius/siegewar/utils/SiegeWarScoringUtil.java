package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.events.BattleSessionPenaltyPointsEvent;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * This class contains utility functions related to battle scoring - i.e. battle points and siege balance
 * 
 * @author Goosius
 */
public class SiegeWarScoringUtil {

	/**
	 * This method determines if a players is in the 'timed point zone' of a siege
	 * 
	 * - Must be in same world as flag
	 * - Must be in wilderness  (This is important, otherwise the defender could create a 'safe space' 
	 *                           inside a perm-protected town block, and gain points there with no threat.)
	 * - Must be within 1 townblock length of the flag
	 *
	 * @param player the player
	 * @param siege the siege
	 * @return true if a player in in the timed point zone
	 */
	public static boolean isPlayerInTimedPointZone(Player player, Siege siege) {
		return (!SiegeWarSettings.getWarSiegeBannerControlSessionCheckWilderness() || TownyAPI.getInstance().isWilderness(player.getLocation()))
				&& SiegeWarDistanceUtil.isInTimedPointZone(player.getLocation(), siege);
	}

	/**
	 * This method applies penalty battle points to a player if they are in the given siegezone
	 * Offline players will also be punished
	 *
	 * @param residentIsAttacker is the resident an attacker or defender?
	 * @param player the player who the penalty relates to
	 * @param siege the siege to apply the penalty to
	 */
	public static void awardPenaltyPoints(boolean residentIsAttacker,
										  Player player,
										  Siege siege) {

		//Give battle points to opposing side
		int battlePoints; 
		if (residentIsAttacker) {
			battlePoints = SiegeWarSettings.getWarBattlePointsForAttackerDeath();
			battlePoints = applyBattlePointsPenaltyForBannerControl(true, battlePoints, siege);
			siege.adjustDefenderBattlePoints(battlePoints);
		} else {
			battlePoints = SiegeWarSettings.getWarBattlePointsForDefenderDeath();
			battlePoints = applyBattlePointsPenaltyForBannerControl(false, battlePoints, siege);
			siege.adjustAttackerBattlePoints(battlePoints);
		}

		//Save siege to db
		SiegeController.saveSiege(siege);

		//Generate message
		String langKey;
		Translatable message;
		final BattleSessionPenaltyPointsEvent.Reason reason;
		Player killer = getPlayerKiller(player);
		if(killer != null) {
			reason = residentIsAttacker ? BattleSessionPenaltyPointsEvent.Reason.KILLED_BY_DEFENDER : BattleSessionPenaltyPointsEvent.Reason.KILLED_BY_ATTACKER;
			langKey = residentIsAttacker ? 	"msg_siege_war_attacker_killed_by_player" : "msg_siege_war_defender_killed_by_player";
			message = Translatable.of(
				langKey,
				siege.getTown().getName(),
				player.getName(),
				killer.getName(),
				Math.abs(battlePoints));
		} else {
			reason = BattleSessionPenaltyPointsEvent.Reason.DEATH;
			langKey = residentIsAttacker ? 	"msg_siege_war_attacker_death" : "msg_siege_war_defender_death";
			message = Translatable.of(
				langKey,
				siege.getTown().getName(),
				player.getName(),
				Math.abs(battlePoints));
		}

		Bukkit.getPluginManager().callEvent( new BattleSessionPenaltyPointsEvent(siege, Math.abs(battlePoints), reason, residentIsAttacker, player, killer));


		//Send messages to siege participants
		SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
	}

	public static int applyBattlePointsPenaltyForBannerControl(boolean residentIsAttacker, int battlePoints, Siege siege) {
		if(!SiegeWarSettings.isWarSiegeCounterattackBoosterEnabled())
			return battlePoints;

		if(
			(residentIsAttacker && siege.getBannerControllingSide() == SiegeSide.ATTACKERS)
			||
			(!residentIsAttacker && siege.getBannerControllingSide() == SiegeSide.DEFENDERS)
		) {
			return battlePoints + (int)((double)battlePoints * siege.getBannerControllingResidents().size() /100 * SiegeWarSettings.getWarSiegeCounterattackBoosterExtraDeathPointsPerPlayerPercentage());
		} else {
			return battlePoints;
		}
	}

	/**
	 * If the given victim player was killed by another player, return the killer player.
	 * Otherwise return null.
     *
	 * @return the player killer, if there was one
	 */
	public static Player getPlayerKiller(Player victim) {
		if(victim.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) victim.getLastDamageCause();
			Entity attackerEntity = damageEvent.getDamager();

			if (attackerEntity instanceof Projectile) { // Killed by projectile, try to narrow the true source of the kill.
				Projectile projectile = (Projectile) attackerEntity;
				if (projectile.getShooter() instanceof Player) { // Player shot a projectile.
					return (Player) projectile.getShooter();
				}
			} else if (attackerEntity instanceof Player) {
				// This was a player kill
				return (Player) attackerEntity;
			}
		}
		return null; //This was not a PVP death
	}
}
