package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.integration.cannons.CannonsIntegration;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SiegeWarWallBreachUtil {

    /**
     * Evaluate wall breaching
     */
    public static void evaluateWallBreaching() {
        //Return if battle session is inactive
        if(!BattleSession.getBattleSession().isActive())
            return;

        //Cycle all sieges
        for (Siege siege : SiegeController.getSieges()) {
            increaseBreachPointsFromBannerControl(siege);
            awardWallBreachBonuses(siege);
            CannonsIntegration.clearRecentTownFriendlycannonFirers(siege);   
        }
    }

    /**
     * Increase breach points from the town-friendly side having banner-control.
     * 
     * @param siege the siege
     */
    private static void increaseBreachPointsFromBannerControl(Siege siege) {
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

        //Increase wall breach points
        siege.increaseWallBreachPointsToCap(wallBreachPointsIncrease);
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
                Messaging.sendErrorMsg(player, Translatable.of("msg_err_already_received_wall_breach_bonus"));
                continue;                                       
            }
            
            //Mark candidate to receive bonus
            newAwardees.add(candidate);
            
            //Notify player
            Messaging.sendMsg(player, Translatable.of("msg_wall_breach_bonus_awarded"));
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

    /**
     * Ensure the height is ok for a breach attempt
     * 
     * @param block the block
     * @param town the town
     * @param siege the siege
     * @return true if its ok to breach at this height
     */
    public static boolean validateBreachHeight(Block block, Town town, Siege siege) {
        if(SiegeWarDistanceUtil.isBlockCloseToTownBlock(block, town.getHomeBlockOrNull(), 2)) {					
            int heightOfBlockRelativeToSiegeBanner = block.getY() - siege.getFlagLocation().getBlockY();
            if(heightOfBlockRelativeToSiegeBanner < SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMin()) {
                return false;
            }
            if(heightOfBlockRelativeToSiegeBanner > SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMax()) {
                return false;
            }
        }	
        return true;
    }

    /**
     * Ensure a block is ok to destroy in a breach 
     * 
     * @param block the block (if entity, this will be a dummy one! e.g AIR
     * @param location the location
     * 
     * @return true if its ok to breach-destroy this material
     * @throws TownyException if something is misconfigured
     */
    public static boolean validateDestroyMaterial(Block block, Location location) throws TownyException {
        if(SiegeWarSettings.isWallBreachingDestroyEntityBlacklist()
            && isEntityAtLocation(location)) {
            return false;
        }
        if(SiegeWarSettings.getWallBreachingDestroyBlocksBlacklist()
            .contains(block.getType())) {
            return false;
        }
        return true;
    }

    /**
	 * Determine if an entity is at the location
	 *
	 * We can do this because blocks have an integers only location (e.g. -20,60,140),
	 * but entities have doubles (e.g. -20.445,60.444,140.999)
	 *
	 * @param location the given location
	 * @return true if an entity is at the given location
	 */
	private static boolean isEntityAtLocation(Location location) {
		if(location.getX() % 1 == 0
			&& location.getY() % 1 == 0
			&& location.getZ() % 1 == 0) {
			return false;
		} else {
			return true;
		}
	}
}
