package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SiegeWarSpawnUtil {
    
    private static final Set<Player> playersWithTeleportPasses = new HashSet<>();
    
    public static boolean doesPlayerHasTeleportPass(Player player) {
        return playersWithTeleportPasses.contains(player);
    }
    
    private static void grandTeleportPassToPlayer(Player player) {
        playersWithTeleportPasses.add(player);
    }
    
    public static void removePlayerTeleportPass(Player player) {
        playersWithTeleportPasses.remove(player);
    }
    
    /**
     * Evaluate a spawn to siege request
     *
     * - If the primary nation/town has just 1 general/mayor in the siegezone, spawn to them.
     * - If any allied nation has just 1 general in the siegezone, spawn to them.
     * - Otherwise spawn is unavailable.
     * 
     * @param player the player trying to spawn
     * @param besiegedTown the besieged town
     */
    public static void evaluateSpawnToSiegeRequest(Player player, Town besiegedTown) throws TownyException {
        if(!SiegeWarSettings.isBattleCommandersEnabled()) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_feature_not_active"));
        }
        Siege siege = SiegeController.getSiege(besiegedTown);
        if(siege == null) {
            throw new TownyException(Translatable.of("msg_err_not_being_sieged", besiegedTown.getName()));
        }
        if(!siege.getStatus().isActive()) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_siege_not_active"));
        }
        if(!BattleSession.getBattleSession().isActive()) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_battle_session_not_active"));
        }
        SiegeSide playerSide = SiegeSide.getPlayerSiegeSide(siege, player);
        Resident battleCommanderResident;
        switch (playerSide) {
            case ATTACKERS:
                battleCommanderResident = siege.getAttackingCommander();
                break;
            case DEFENDERS:
                battleCommanderResident = siege.getDefendingCommander();
                break;
            default:
                throw new TownyException(Translatable.of("msg_err_cannot_spawn_not_participant"));
        }
        if(battleCommanderResident == null) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_no_battle_commander"));
        }
        Player battleCommander = TownyAPI.getInstance().getPlayer(battleCommanderResident);
        if(battleCommander == null || !battleCommander.isOnline()) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_no_battle_commander_offline"));
        }
        if(battleCommander.isDead()) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_battle_commander_dead"));
        }
        if(!SiegeWarDistanceUtil.isPlayerRegisteredToActiveSiegeZone(battleCommander)) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_battle_commander_not_in_siegezone"));
        }
        if(SiegeWarDistanceUtil.getActiveSiegeZonePlayerIsRegisteredTo(battleCommander) != siege) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_battle_commander_not_in_siegezone"));
        }
        Resident spawnerResident = TownyAPI.getInstance().getResident(player);
        if(!WorldCoord.parseWorldCoord(player).equals(spawnerResident.getTown().getHomeBlock().getWorldCoord())) {
            throw new TownyException(Translatable.of("msg_err_cannot_spawn_not_in_homeblock"));
        }
        //Grant teleport pass in order to bypass SW's teleport blocker
        grandTeleportPassToPlayer(player);
        //SPAWN!
        player.teleport(battleCommander);
    }
}
