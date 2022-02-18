package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
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

            //If Wall Breaching is active, increase Breach Points
            if(siege.isWallBreachingActive()) {
                double wallBreachPointsIncrease = 
                    SiegeWarSettings.getWallBreachingPointGenerationRate()
                    * siege.getTown().getTownBlocks().size();
                siege.setWallBreachPoints(siege.getWallBreachPoints() + wallBreachPointsIncrease);
            }

            //Award wall breach bonus to any who deserve it
            Player player;
            Set<Resident> newAwardees = new HashSet<>();
            Set<Resident> previousAwardees = new HashSet<>(siege.getWallBreachBonusAwardees());
            for(Resident candidate: siege.getBannerControllingResidents()) {
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

                //Candidate must not already have award
                if(previousAwardees.contains(candidate))
                    return;

                //Candidate must be at the homeblock of the besieged town
                player = TownyAPI.getInstance().getPlayer(candidate);
                TownBlock townblockWherePlayerIsLocated = TownyAPI.getInstance().getTownBlock(player);
                if(townblockWherePlayerIsLocated == null)
                    return;
                if(townblockWherePlayerIsLocated != siege.getTown().getHomeBlockOrNull())
                    return;                

                //Grant award!!!
                newAwardees.add(candidate);
                
                //Notify player
          		Messaging.sendMsg(player, Translation.of("msg_wall_breach_bonus_awarded"));
            }
         
            //Notify siege  of any awards       
            if(newAwardees.size() > 0) {
                String message = Translation.of("msg_wall_breach_bonus_awarded_to_attackers",siege.getTown().getName(), newAwardees.size(), SiegeWarSettings.getWallBreachBonusBattlePoints());
        		SiegeWarNotificationUtil.informSiegeParticipants(siege, message);
            }
    	}
    }
}
