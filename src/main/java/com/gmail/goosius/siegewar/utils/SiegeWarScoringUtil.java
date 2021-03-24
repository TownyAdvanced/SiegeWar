package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
		return TownyAPI.getInstance().isWilderness(player.getLocation())
				&& SiegeWarDistanceUtil.isInTimedPointZone(player.getLocation(), siege);
	}

	/**
	 * This method applies penalty battle points to a player if they are in the given siegezone
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

		//Give battle points to opposing side
		int battlePoints;
		if (residentIsAttacker) {
			battlePoints = SiegeWarSettings.getWarBattlePointsForAttackerDeath();
			battlePoints = applyBattlePointsPenaltyForBannerControl(true, battlePoints, siege);
			battlePoints = applyBattlePointsAdjustmentForPopulationQuotient(false, battlePoints, siege);
			siege.adjustDefenderBattlePoints(battlePoints);
		} else {
			battlePoints = SiegeWarSettings.getWarBattlePointsForDefenderDeath();
			battlePoints = applyBattlePointsPenaltyForBannerControl(false, battlePoints, siege);
			battlePoints = applyBattlePointsAdjustmentForPopulationQuotient(true, battlePoints, siege);
			siege.adjustAttackerBattlePoints(battlePoints);
		}

		//Send messages to siege participants
		String message = String.format(
			unformattedErrorMessage,
			siege.getTown().getName(),
			resident.getName(),
			Math.abs(battlePoints));

		SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
	}

	public static void updatePopulationBasedBattlePointModifiers() {
		Map<Nation,Integer> nationSidePopulationsCache = new HashMap<>();
		for (Siege siege : SiegeController.getSieges()) {
			updateBattlePointPopulationModifier(siege, nationSidePopulationsCache);
		}
	}

	private static void updateBattlePointPopulationModifier(Siege siege, Map<Nation,Integer> nationSidePopulationsCache) {
		int attackerPopulation;
		int defenderPopulation;

		//Calculate defender population
		Nation nation = null;
		if(siege.getDefender() instanceof Nation) {
			nation = (Nation)siege.getDefender();
		} else if (((Town)siege.getDefender()).hasNation()) {
			nation = (Nation)siege.getDefender();
		}

		if(nation != null) {
			nation = TownyAPI.getInstance().getTownNationOrNull(siege.getTown());

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
			defenderPopulation = ((Town)siege.getDefender()).getNumResidents();
		}

		//Calculate attacker population
		nation = null;
		if(siege.getAttacker() instanceof Nation) {
			nation = (Nation)siege.getAttacker();
		} else if (((Town)siege.getAttacker()).hasNation()) {
			nation = (Nation)siege.getAttacker();
		}

		if(nation != null) {
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
		} else {
			attackerPopulation = ((Town)siege.getAttacker()).getNumResidents();
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
	
		//Battle Points modifier
		//Lowest possible value should be 1.
		//Highest possible value should be the max boost value in the config
		double battlePointsModifier = 1 + (normalizedPointBoost * (SiegeWarSettings.getWarSiegeMaxPopulationBasedPointBoost() -1));
		
		siege.setBattlePointsModifierForSideWithLowestPopulation(battlePointsModifier);
	}

	public static int applyBattlePointsAdjustmentForPopulationQuotient(boolean attackerGain, int battlePoints, Siege siege) {
		if(!SiegeWarSettings.getWarSiegePopulationBasedPointBoostsEnabled()) {
			return battlePoints;
		}

		if (siege.getBattlePointsModifierForSideWithLowestPopulation() == 0) {
			updateBattlePointPopulationModifier(siege, null); //Init values
		}

		if((attackerGain && !siege.isAttackerHasLowestPopulation())
			|| (!attackerGain && siege.isAttackerHasLowestPopulation())) {
			return battlePoints;
		}

		double modifier = siege.getBattlePointsModifierForSideWithLowestPopulation();
		return (int) (battlePoints * modifier);
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
}
