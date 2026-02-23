package com.gmail.goosius.siegewar.integration;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.SiegeWarAPI;
import com.gmail.goosius.siegewar.listeners.SiegeWarStatusScreenListener;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarImmunityUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.util.TimeMgmt;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

public class PAPIPlaceholderExpansion extends PlaceholderExpansion {

	@Override
	public @NotNull String getIdentifier() {
		return "siegewar";
	}

	@Override
	public @NotNull String getAuthor() {
		return "TownyAdvanced Team and Contributors";
	}

	@Override
	public @NotNull String getVersion() {
		return SiegeWar.getSiegeWar().getVersion();
	}

	@Override
	public String onRequest(OfflinePlayer player, String identifier) {
		return ChatColor.translateAlternateColorCodes('&', getOfflinePlayerPlaceholder(player, identifier));
	}

	private String getOfflinePlayerPlaceholder(OfflinePlayer player, String identifier) {
		if (identifier.startsWith("player_") && (player == null || !player.isOnline())) {
			return "";
		}

		// All player-oriented placeholders pass through here. 
		if (identifier.startsWith("player_"))
			return getPlayerFocusedPlaceholder(player.getPlayer(), identifier);

		switch (identifier) {
		case "siege_battlesession_time_remaining" -> { // %siegewar_siege_battlesession_time_remaining%
			if (!SiegeWarAPI.isBattleSessionActive())
				return "";
			return BattleSession.getBattleSession().getFormattedTimeRemainingUntilBattleSessionEnds();
		}
		};
		return "";
	}

	private String getPlayerFocusedPlaceholder(Player player, String identifier) {
		Town town = TownyAPI.getInstance().getTown(player);
		Siege siege = SiegeWarAPI.getSiegeOrNull(player);
		Translator translator = Translator.locale(player);
		String value = switch(identifier) {
		case "player_is_sieged" -> String.valueOf(SiegeWarAPI.hasSiege(player)); // %siegewar_player_is_sieged%
		case "player_immunity_timer" -> { // %siegewar_player_immunity_timer%
			if (town == null)
				yield "";
			if (!SiegeWarImmunityUtil.isTownSiegeImmune(town))
				yield "";
			long immunity = TownMetaDataController.getSiegeImmunityEndTime(town);
			if (System.currentTimeMillis() < immunity || immunity == -1l) {
				String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
				yield translator.of("status_town_siege_immunity_timer", time);
			}
			yield "";
		}
		case "player_siege_attackers" -> { // %siegewar_player_siege_attackers%
			if (siege == null)
				yield "";
			yield translator.of("status_town_siege_attacker", siege.getAttackerNameForDisplay());
		}
		case "player_siege_defenders" -> { // %siegewar_player_siege_defenders%
			if (siege == null)
				yield "";
			yield translator.of("status_town_siege_defender", siege.getDefenderNameForDisplay());
		}
		case "player_siege_progress" -> { // %siegewar_player_siege_progress%
			if (siege == null)
				yield "";
			yield translator.of("status_town_siege_status", SiegeWarStatusScreenListener.getStatusTownSiegeSummary(siege, translator));
		}
		case "player_siege_battle_sessions_remaining" -> { // %siegewar_player_siege_battle_sessions_remaining%
			if (siege == null)
				yield "";
			yield switch (siege.getStatus()) {
			case IN_PROGRESS, PENDING_ATTACKER_ABANDON, PENDING_DEFENDER_SURRENDER ->
				translator.of("status_town_siege_progress", siege.getNumBattleSessionsCompleted(), SiegeWarSettings.getSiegeDurationBattleSessions());
			default -> "";
			};
		}

		default -> "";
		};
		return value;
	}

}