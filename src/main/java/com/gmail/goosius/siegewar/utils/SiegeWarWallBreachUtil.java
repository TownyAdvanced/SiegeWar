package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SiegeWarWallBreachUtil {

    /**
     * Evaluate wall breaching
     * 
     * For each siege:
     * 1. Increase the Breach Points if Wall-Breaching is active
     * 2. Evaluate if any players are eligible for the Wall Breach bonus
     */
    public static void evaluateWallBreaching() {
        for (Siege siege : SiegeController.getSieges()) {
            /*
             * Increase Breach Points
             */
            double wallBreachPointsIncrease = 
                SiegeWarSettings.getWallBreachingPointGenerationRate()
                * siege.getTown().getTownBlocks().size();
            siege.setWallBreachPoints(siege.getWallBreachPoints() + wallBreachPointsIncrease);
            
            /* 
             * Award Wall Breach Bonuses
             */
            //The town-hostile side must be controlling the banner                
            switch(siege.getSiegeType()) {
                case CONQUEST:
                case SUPPRESSION:
                    if(siege.getBannerControllingSide() == SiegeSide.DEFENDERS)
                        continue; 
                    break;
                case REVOLT:
                case LIBERATION:
                    if(siege.getBannerControllingSide() == SiegeSide.ATTACKERS)
                        continue;
                    break;
            }
                
            //For a resident to get it, they must be on the bc list, at the homeblock, and they must not already have the award
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
                
                //List candidate for award
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
                
                //Register new awardees
                siege.getWallBreachBonusAwardees().addAll(newAwardees);
             
                //Notify siege stakeholders       
                if(newAwardees.size() > 0) {
                    String message = Translation.of("msg_wall_breach_bonus_awarded_to_attackers",siege.getTown().getName(), newAwardees.size(), SiegeWarSettings.getWallBreachBonusBattlePoints());
                    SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
                }
            }
    	}
    }
}
