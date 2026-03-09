package com.gmail.goosius.siegewar.hud;

import java.util.HashMap;
import java.util.Map;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.huds.HUDManager;
import com.palmergames.bukkit.towny.huds.providers.FoliaHUD;
import com.palmergames.bukkit.towny.huds.providers.HUD;
import com.palmergames.bukkit.towny.huds.providers.PaperHUD;
import com.palmergames.bukkit.towny.huds.providers.ServerHUD;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class SiegeHUDManager implements Listener {

	private static final String SIEGE_WAR_HUD_NAME = "siegeWarHUD";
	private static final String SIEGE_WAR_HUD_OBJ = "SIEGE_HUD_OBJ";
	static Map<Player, Siege> warHudUsers;

    public SiegeHUDManager() {
		boolean isFolia = Towny.getPlugin().isFolia();

		HUD siegeWarHUD = new HUD(SIEGE_WAR_HUD_NAME, SIEGE_WAR_HUD_OBJ, (p) -> SiegeWarHud.updateHUD(p), (p, siege) -> SiegeWarHud.updateHUD(p, (Siege) siege));
		SiegeWarHud siegeHUD = new SiegeWarHud(siegeWarHUD);
		HUDManager.addHUD(SIEGE_WAR_HUD_NAME, isFolia ? new FoliaHUD(siegeHUD) : new PaperHUD(siegeHUD));
        warHudUsers = new HashMap<>();
    }

    public void toggleWarHud(Player player, Siege siege) {
		ServerHUD hud = HUDManager.getHUD(SIEGE_WAR_HUD_NAME);
		if (hud == null)
			return;

        if (!warHudUsers.containsKey(player)) {
            warHudUsers.put(player, siege);
			hud.toggleOn(player);
			SiegeWarHud.updateHUD(player, siege);
        } else if (warHudUsers.get(player) != siege) {
            warHudUsers.replace(player, siege);
			hud.toggleOn(player);
			SiegeWarHud.updateHUD(player, siege);
        } else
            toggleOff(player);
    }

    public static void toggleOff(Player player) {
        warHudUsers.remove(player);
        HUDManager.toggleAllOff(player);
    }

	public static void updateHUDs() {
		ServerHUD hud = HUDManager.getHUD(SIEGE_WAR_HUD_NAME);
		if (hud == null)
			return;

		for (Player player : hud.getPlayers()) {
			if (!hud.isActive(player)) {
				hud.removePlayer(player);
				warHudUsers.remove(player);
			} else
				SiegeWarHud.updateHUD(player, warHudUsers.get(player));
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
