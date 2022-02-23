package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SiegeWarWallBreachUtil {

    /**
     * Evaluate wall breaching
     * 
     * For each siege:
     * - Increase the Breach Points
     * - Award Wall Breach bonuses
     */
    public static void evaluateWallBreaching() {
        //Return if battle session is inactive
        if(!BattleSession.getBattleSession().isActive())
            return;

        //Cycle all sieges
        for (Siege siege : SiegeController.getSieges()) {
            increaseBreachPoints(siege);
            awardWallBreachBonuses(siege);            
        }
    }

    /**
     * Increase breach points if conditions are met.
     * 
     * Conditions: Town-Friendly side has banner control.
     * 
     * @param siege the siege
     */
    private static void increaseBreachPoints(Siege siege) {
        if(siege.getWallBreachPoints() >= SiegeWarSettings.getWallBreachingMaxPoolSize())
            return; //Already at max
                    
        if(SiegeWarSettings.getWallBreachingPointGenerationRate() == 0)
            return;

        switch(siege.getBannerControllingSide()) {
            case NOBODY:
                return;
            case ATTACKERS:
                if(siege.getSiegeType() == SiegeType.CONQUEST || siege.getSiegeType() == SiegeType.SUPPRESSION) 
                    return;
                break;
            case DEFENDERS:
                if(siege.getSiegeType() == SiegeType.REVOLT || siege.getSiegeType() == SiegeType.LIBERATION) 
                    return;                                               
                break;
        }

        double wallBreachPointsIncrease = 
        SiegeWarSettings.getWallBreachingPointGenerationRate()
        * siege.getBannerControllingResidents().size()
        * siege.getTown().getTownBlocks().size();

        siege.setWallBreachPoints(            
            Math.min(
                SiegeWarSettings.getWallBreachingMaxPoolSize(), 
                siege.getWallBreachPoints() + wallBreachPointsIncrease));
    }

    /**
     * Award wall-breach bonuses if conditions are met.
     * 
     * Conditions:
     * - Town hostile team has Banner Control.
     * - Player is the BC list.
     * - Player is at the homeblock.
     * - Player did not already get the award in this Battle Session.
     *
     * @param siege the siege
     */
    private static void awardWallBreachBonuses(Siege siege) {
        if(SiegeWarSettings.getWallBreachBonusBattlePoints() == 0)
            return;
        
        //Town-hostile team must have banner control                
        switch(siege.getBannerControllingSide()) {
            case NOBODY:
                return;
            case ATTACKERS:
                if(siege.getSiegeType() == SiegeType.REVOLT || siege.getSiegeType() == SiegeType.LIBERATION) 
                    return;    
                break;                                           
            case DEFENDERS:
                if(siege.getSiegeType() == SiegeType.CONQUEST || siege.getSiegeType() == SiegeType.SUPPRESSION) 
                    return;
                break;                                           
        }

        //Cycle banner controlling residents
        Player player;
        Set<Resident> newAwardees = new HashSet<>();
        Set<Resident> previousAwardees = new HashSet<>(siege.getWallBreachBonusAwardees());
        for(Resident candidate: siege.getBannerControllingResidents()) {
            //Candidate must be at the homeblock of the besieged town
            player = TownyAPI.getInstance().getPlayer(candidate);
            TownBlock townblockWherePlayerIsLocated = TownyAPI.getInstance().getTownBlock(player);
            if(townblockWherePlayerIsLocated == null)
                continue;
            if(townblockWherePlayerIsLocated != siege.getTown().getHomeBlockOrNull())
                continue;

            //Candidate must not already have award
            if(previousAwardees.contains(candidate)) {
                Messaging.sendErrorMsg(player, Translation.of("msg_err_already_received_wall_breach_bonus"));
                continue;                                       
            }
            
            //Mark candidate to receive bonus
            newAwardees.add(candidate);
            
            //Notify player
            Messaging.sendMsg(player, Translation.of("msg_wall_breach_bonus_awarded"));
        }

        //Grant bonuses!
        if(newAwardees.size() > 0) {         
            //Adjust Battle Points
            int battlePointsBonus = SiegeWarSettings.getWallBreachBonusBattlePoints() * newAwardees.size();
            if(siege.getBannerControllingSide() == SiegeSide.ATTACKERS) {
                siege.adjustAttackerBattlePoints(battlePointsBonus);                
            } else {
                siege.adjustDefenderBattlePoints(battlePointsBonus);
            }
            
            //Register new awardees with Siege
            siege.getWallBreachBonusAwardees().addAll(newAwardees);
         
            //Notify siege stakeholders       
            if(newAwardees.size() > 0) {
                String message = Translation.of("msg_wall_breach_bonus_awarded_to_attackers",siege.getTown().getName(), newAwardees.size(), SiegeWarSettings.getWallBreachBonusBattlePoints());
                SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
            }
        }
    }

    /**
     * Determine if player can use breach points by cannon
     * 
     * @param player player
     * @param siege siege
     * 
     * @return true if the player can use breach points by cannon
     */
    public static boolean canPlayerUseBreachPointsByCannon(Player player, Siege siege) {
			//Validate that player has town
			Resident gunnerResident = TownyAPI.getInstance().getResident(player);
            if(!gunnerResident.hasTown())
                return false;

			//Validate player perms
			if(
				! ( 
                    (gunnerResident.getTownOrNull() == siege.getTown()
					&& TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_FIRE_CANNON_IN_SIEGEZONE.getNode()))
					|| 
					(gunnerResident.hasNation()
					&& TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_FIRE_CANNONS_IN_SIEGEZONE.getNode()))
				 )
			) {
    			return false;
			}
			
			//Validate that player is on the town-hostile side of the given siege
            Town gunnerResidentTown = gunnerResident.getTownOrNull();
			SiegeSide playerSiegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(player, gunnerResidentTown, siege);
            switch(playerSiegeSide) {
            case ATTACKERS:
                return siege.getSiegeType() == SiegeType.CONQUEST || siege.getSiegeType() == SiegeType.SUPPRESSION; 
            case DEFENDERS:
               return siege.getSiegeType() == SiegeType.REVOLT || siege.getSiegeType() == SiegeType.LIBERATION;
            default:
                return false;
        }
    }

    /**
     * Pay the given breach point cost
     * 
     * @param breachPointCost the cost
     * @param siege the siege
     * @return true if the breach is allow, false if not (then no payment is made)
     */
    public static boolean payBreachPoints(int breachPointCost, Siege siege) {
        if(breachPointCost > siege.getWallBreachPoints()) {
            return false;
        } else {
            siege.setWallBreachPoints(siege.getWallBreachPoints() - breachPointCost);
            return true;
        }
    }
}
