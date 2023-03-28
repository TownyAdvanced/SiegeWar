package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SiegeHUDManager {

    static Map<Player, Siege> warHudUsers;

    public SiegeHUDManager() {
        warHudUsers = new HashMap<>();
    }

    public void toggleWarHud(Player player, Siege siege) {
        if (!warHudUsers.containsKey(player)) {
            warHudUsers.put(player, siege);
            SiegeWarHud.toggleOn(player, siege);
        } else if (warHudUsers.get(player) != siege) {
            warHudUsers.replace(player, siege);
            SiegeWarHud.updateInfo(player, siege);
        } else
            toggleOff(player);
    }

    public static void toggleOff(Player player) {
        warHudUsers.remove(player);
        if (player.isOnline())
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public static void updateHUDs() {
        for (Entry<Player, Siege> entry : new ArrayList<>(warHudUsers.entrySet())) {
            if (entry.getKey().getScoreboard().getTeam("balance") == null) {
                warHudUsers.remove(entry.getKey());
                continue;
            } else
                SiegeWarHud.updateInfo(entry.getKey(), entry.getValue());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        warHudUsers.remove(event.getPlayer());
    }

    public static String checkLength(String string) {
        return string.length() > 32 ? string.substring(0, 32) + "..." : string;
    }
}
