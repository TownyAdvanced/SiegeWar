package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.objects.BannerControlSession;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.adventure.bossbar.BossBar;
import com.palmergames.adventure.bossbar.BossBar.Color;
import com.palmergames.adventure.bossbar.BossBar.Overlay;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.TextComponent;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossBarUtil {

	private static Map<Player, BossBar> bossBarBannerCapMap = new HashMap<>(); 
	private static Map<Player, BossBar> bossBarBattleSessionMap = new HashMap<>();
	
	public static void removeBattleSessionBossBars() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (bossBarBattleSessionMap.containsKey(player)) {
				Towny.getAdventure().player(player).hideBossBar(bossBarBattleSessionMap.get(player));
			}
		}
		bossBarBattleSessionMap.clear();
	}
	
	public static void updateBattleSessionBossBar() {
		BattleSession session = BattleSession.getBattleSession();
		TextComponent comp = Component.text(Translation.of("bossbar_msg_battle_time_remaining", session.getFormattedTimeRemainingUntilBattleSessionEnds()));
		float remaining = getRemainder(session.getScheduledEndTime(), SiegeWarSettings.getWarSiegeBattleSessionsDurationMinutes() * 60000);
		for (Player player : Bukkit.getOnlinePlayers()) {
			Resident resident = TownyAPI.getInstance().getResident(player);
			if (resident == null || ResidentMetaDataController.getBossBarsDisabled(resident))
				continue;
			BossBar bossBar = bossBarBattleSessionMap.getOrDefault(player, BossBar.bossBar(comp, 0, Color.WHITE, Overlay.PROGRESS));
			bossBar.progress((float) (remaining/100.0));
			bossBar.name(comp);
			if (!bossBarBattleSessionMap.containsKey(player)) {
				bossBarBattleSessionMap.put(player, bossBar);
				Towny.getAdventure().player(player).showBossBar(bossBar);
			}
		}
		
	}

	public static void removeBannerCapBossBar(Player player) {
		if (player.isOnline()) {
			Towny.getAdventure().player(player).hideBossBar(bossBarBannerCapMap.get(player));
		}
		bossBarBannerCapMap.remove(player);
	}

	public static void updateBannerCapBossBar(Player player, String msg, BannerControlSession bannerControlSession) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		if (resident == null || ResidentMetaDataController.getBossBarsDisabled(resident))
			return;
		TextComponent comp = Component.text(msg);
		float remaining = getRemainder(bannerControlSession.getSessionEndTime(), SiegeWarSettings.getWarSiegeBannerControlSessionDurationMinutes());
		BossBar bossBar = bossBarBannerCapMap.getOrDefault(player, BossBar.bossBar(comp, 0, Color.WHITE, Overlay.PROGRESS));
		bossBar.progress(remaining);
		bossBar.name(comp);
		if (!bossBarBannerCapMap.containsKey(player)) {
			bossBarBannerCapMap.put(player, bossBar);
			Towny.getAdventure().player(player).showBossBar(bossBar);
		}
	}
	
	public static void removeBossBars(Player player) {
		if (bossBarBattleSessionMap.containsKey(player)) {
			Towny.getAdventure().player(player).hideBossBar(bossBarBattleSessionMap.get(player));
			bossBarBattleSessionMap.remove(player);
		}
		removeBannerCapBossBar(player);
	}
	
	private static float getRemainder(long endTime, long duration) {
		duration = duration * 60000;
		double remaining = 100.0 * Math.abs(endTime - System.currentTimeMillis() - duration) / duration;
		return (float) (remaining/100.0);
		
	}
}
