package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

public class SiegeWarBattleCommanderUtil {
    
    /**
     * Assign the siege commanders for this battle session
     * To qualify, a player must be:
     * - Online
     * - In the siege zone
     * - Nation king or general
     * - An official siege participant
     */
    public static void assignBattleCommanders() {
        if(!SiegeWarSettings.isBattleCommandersEnabled()) {
            return;
        }
        Player attackingCommander;
        Player defendingCommander;
        String attackingCommanderName;
        String defendingCommanderName;
        for(Siege siege: SiegeController.getSieges()) {
            if(siege.getStatus().isActive()) {
                //Wipe the current commanders
                siege.setAttackingCommander(null);
                siege.setDefendingCommander(null);
                //Reset local vars
                attackingCommander = null;
                defendingCommander = null;
                //Find the commanders for the siege
                for(Player player: Bukkit.getOnlinePlayers()) {
                    if (SiegeWarDistanceUtil.isPlayerRegisteredToActiveSiegeZone(player)
                            && player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_STARTCONQUESTSIEGE.getNode()))
                    {
                        if(SiegeSide.getPlayerSiegeSide(siege,player) == SiegeSide.ATTACKERS) {
                            if(attackingCommander == null) {
                                attackingCommander = player;
                                siege.setAttackingCommander(TownyAPI.getInstance().getResident(player));
                            }
                        } else if (SiegeSide.getPlayerSiegeSide(siege,player) == SiegeSide.DEFENDERS) {
                            if(defendingCommander == null) {
                                defendingCommander = player;
                                siege.setDefendingCommander(TownyAPI.getInstance().getResident(player));
                            }
                        }
                    }
                }

                //Display the commanders to all participants
                attackingCommanderName = attackingCommander == null ? Translation.of("no_battle_commander") : attackingCommander.getName();
                defendingCommanderName = defendingCommander == null ? Translation.of("no_battle_commander") : defendingCommander.getName();
                SiegeWarNotificationUtil.informSiegeParticipants(siege, Translatable.of("msg_battle_commanders_assigned", siege.getTown().getName(), attackingCommanderName, defendingCommanderName));
            }
        }
    }
}
