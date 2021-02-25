package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyObject;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains utility functions related to siege points
 * 
 * @author Goosius
 */
public class SiegeWarPointsUtil {

	/**
	 * This method calculates who has won a siege
	 * 
	 * Defending Town - The defending town has won the siege if all of the siege zones are in negative points.
	 * Attacking Nation - an attacking nation has won the siege if its siege points are positive,
	 *                    and higher than the siegepoints of any other attacker.
	 *
	 * @param siege the siege
	 * @return the winner of the siege
	 */
	public static TownyObject calculateSiegeWinner(Siege siege) {
		if(siege.getSiegePoints() > 0) {
			return siege.getAttackingNation();
		} else {
			return siege.getDefendingTown();
		}
    }

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
		return TownyAPI.getInstance().isWilderness(player.getLocation())
				&& SiegeWarDistanceUtil.isInTimedPointZone(player, siege);
	}

	/**
	 * This method applies penalty points to a player if they are in the given siegezone
	 * Offline players will also be punished
	 *
	 * @param residentIsAttacker is the resident an attacker or defender?
	 * @param player the player who the penalty relates to
	 * @param resident the resident who the penalty relates to
	 * @param siege the siege to apply the penalty to
	 * @param unformattedErrorMessage the error message to be shown if points are deducted
	 */
	public static void awardPenaltyPoints(boolean residentIsAttacker,
											 Player player,
											 Resident resident,
											 Siege siege,
											 String unformattedErrorMessage) {
		//No penalty points without an active battle session
		if(!BattleSession.getBattleSession().isActive())
			return;

		//Give battle score to opposing side
		int battleScore;
		if (residentIsAttacker) {
			battleScore = SiegeWarSettings.getWarSiegePointsForAttackerDeath();
			battleScore = adjustSiegePointPenaltyForBannerControl(true, battleScore, siege);
			battleScore = adjustSiegePointsForPopulationQuotient(false, battleScore, siege);
			siege.adjustAttackerBattleScore(battleScore);
		} else {
			battleScore = SiegeWarSettings.getWarSiegePointsForDefenderDeath();
			battleScore = adjustSiegePointPenaltyForBannerControl(false, battleScore, siege);
			battleScore = adjustSiegePointsForPopulationQuotient(true, battleScore, siege);
			siege.adjustDefenderBattleScore(battleScore);
		}

		//Send messages to siege participants
		String message = String.format(
			unformattedErrorMessage,
			siege.getDefendingTown().getName(),
			resident.getName(),
			Math.abs(battleScore));

		SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
	}

	public static void updatePopulationBasedSiegePointModifiers() {
		Map<Nation,Integer> nationSidePopulationsCache = new HashMap<>();
		for (Siege siege : SiegeController.getSieges()) {
			updateSiegePointPopulationModifier(siege, nationSidePopulationsCache);
		}
	}

	private static void updateSiegePointPopulationModifier(Siege siege, Map<Nation,Integer> nationSidePopulationsCache) {
		Nation nation = null;
		int attackerPopulation;
		int defenderPopulation;

		//Calculate defender population
		if(siege.getDefendingTown().hasNation()) {
			try {
				nation = siege.getDefendingTown().getNation();
			} catch (NotRegisteredException e) {
			}
			if(nationSidePopulationsCache != null && nationSidePopulationsCache.containsKey(nation)) {
				defenderPopulation = nationSidePopulationsCache.get(nation);
			} else {
				defenderPopulation = nation.getNumResidents();
				for(Nation alliedNation: nation.getMutualAllies()) {
					defenderPopulation += alliedNation.getNumResidents();
				}
				if(nationSidePopulationsCache != null) 
					nationSidePopulationsCache.put(nation, defenderPopulation);
			}
		} else {
			defenderPopulation = siege.getDefendingTown().getNumResidents();
		}

		//Calculate attacker population
		nation = siege.getAttackingNation();
		if(nationSidePopulationsCache != null && nationSidePopulationsCache.containsKey(nation)) {
			attackerPopulation = nationSidePopulationsCache.get(nation);
		} else {
			attackerPopulation = nation.getNumResidents();
			for (Nation alliedNation : nation.getMutualAllies()) {
				attackerPopulation += alliedNation.getNumResidents();
			}
			if (nationSidePopulationsCache != null)
				nationSidePopulationsCache.put(nation, attackerPopulation);
		}

		//Note which side has the lower population
		siege.setAttackerHasLowestPopulation(attackerPopulation < defenderPopulation);

		/*
		 * Calculate siege point modifier
		 * 
		 * Terminology: 
		 * The 'quotient' is the number of times the smaller population is contained in the larger one
		 */
		double maxPopulationQuotient = SiegeWarSettings.getWarSiegePopulationQuotientForMaxPointsBoost();
		double actualPopulationQuotient;
			if(siege.isAttackerHasLowestPopulation()) {
				actualPopulationQuotient = (double) defenderPopulation / attackerPopulation;
			} else {
				actualPopulationQuotient = (double) attackerPopulation / defenderPopulation;
			}
		double appliedPopulationQuotient;
			if(actualPopulationQuotient < maxPopulationQuotient) {
				appliedPopulationQuotient = actualPopulationQuotient;
			} else {
				appliedPopulationQuotient = maxPopulationQuotient;
			}
			
		//Normalized point boost
		//0 represents no boost
		//1 represents max boost
		double normalizedPointBoost = (appliedPopulationQuotient -1) / (maxPopulationQuotient -1);
	
		//Siege Point modifier
		//Lowest possible value should be 1.
		//Highest possible value should be the max boost value in the config
		double siegePointModifier = 1 + (normalizedPointBoost * (SiegeWarSettings.getWarSiegeMaxPopulationBasedPointBoost() -1));
		
		siege.setSiegePointModifierForSideWithLowestPopulation(siegePointModifier);
	}

	public static int adjustSiegePointsForPopulationQuotient(boolean attackerGain, int siegePoints, Siege siege) {
		if(!SiegeWarSettings.getWarSiegePopulationBasedPointBoostsEnabled()) {
			return siegePoints;
		}

		if (siege.getSiegePointModifierForSideWithLowestPopulation() == 0) {
			updateSiegePointPopulationModifier(siege, null); //Init values
		}

		if((attackerGain && !siege.isAttackerHasLowestPopulation())
			|| (!attackerGain && siege.isAttackerHasLowestPopulation())) {
			return siegePoints;
		}

		double modifier = siege.getSiegePointModifierForSideWithLowestPopulation();
		return (int) (siegePoints * modifier);
	}

	public static int adjustSiegePointPenaltyForBannerControl(boolean residentIsAttacker, int siegePoints, Siege siege) {
		if(SiegeWarSettings.isWarSiegeCounterattackBoosterDisabled())
			return siegePoints;

		if(
			(residentIsAttacker && siege.getBannerControllingSide() == SiegeSide.ATTACKERS)
			||
			(!residentIsAttacker && siege.getBannerControllingSide() == SiegeSide.DEFENDERS)
		) {
			return siegePoints + (int)((double)siegePoints * siege.getBannerControllingResidents().size() /100 * SiegeWarSettings.getWarSiegeCounterattackBoosterExtraDeathPointsPerPlayerPercentage());
		} else {
			return siegePoints;
		}
	}
}
