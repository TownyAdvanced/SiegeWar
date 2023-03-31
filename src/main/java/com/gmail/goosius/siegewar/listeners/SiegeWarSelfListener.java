package com.gmail.goosius.siegewar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.goosius.siegewar.utils.SiegeWarSiegeUtil;
import com.gmail.goosius.siegewar.events.BattleSessionPreStartEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Translatable;

public class SiegeWarSelfListener implements Listener {
	
	@EventHandler
	public void onBattleSessionPreStart(BattleSessionPreStartEvent event) {
		if (SiegeWarSettings.cancelBattleSessionWhenNoActiveSieges()
		&& (SiegeWarSiegeUtil.getSieges().isEmpty() || SiegeWarSiegeUtil.getSieges().stream().noneMatch(siege -> siege.getStatus().isActive()))) {
			event.setCancelled(true);
			event.setCancellationMsg(Translatable.of("battle_session_cancelled_no_sieges").defaultLocale());
		}
	}
}
