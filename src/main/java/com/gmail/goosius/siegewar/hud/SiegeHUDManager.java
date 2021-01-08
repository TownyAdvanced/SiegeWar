package com.gmail.goosius.siegewar.hud;

import java.util.ArrayList;

import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.hud.SiegeWarHud;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class SiegeHUDManager {

    ArrayList<Player> warHudUsers;

    public SiegeHUDManager(SiegeWar plugin) {
        warHudUsers = new ArrayList<Player>();
    }

    public void toggleWarHud(Player player, Siege siege) {
        System.out.println(warHudUsers);
        if (!warHudUsers.contains(player)) {
            warHudUsers.add(player);
            SiegeWarHud.toggleOn(player, siege);
        } else {
            toggleOff(player);
        }
    }

    public void toggleOff(Player player) {
        warHudUsers.remove(player);
        if (player.isOnline())
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        warHudUsers.remove(event.getPlayer());
    }
}
