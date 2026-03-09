package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.SiegeWarAPI;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.huds.HUDImplementer;
import com.palmergames.bukkit.towny.huds.HUDManager;
import com.palmergames.bukkit.towny.huds.providers.HUD;
import com.palmergames.bukkit.towny.huds.providers.ServerHUD;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.towny.utils.TownyComponents;
import com.palmergames.bukkit.util.Colors;

import net.kyori.adventure.text.Component;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.entity.Player;

public class SiegeWarHud implements HUDImplementer {
	private static final String GRAY = Colors.GRAY;
	final HUD hud;

	public SiegeWarHud(HUD hud) {
		this.hud = hud;
	}

	@Override
	public HUD getHUD() {
		return hud;
	}

	public static void updateHUD(Player player) {
		updateHUD(player, SiegeWarAPI.getSiegeOrNull(player));
	}

	public static void updateHUD(Player player, Siege siege) {
		if (siege == null)
			return;

		ServerHUD hud = HUDManager.getHUD("siegeWarHUD");
		if (hud == null) {
			SiegeWar.getSiegeWar().getLogger().warning("SiegeHUD could not find siegeHUD from HUDManager for player: " + player.getName());
			return;
		}
		final Translator translator = Translator.locale(player);

		UUID uuid = player.getUniqueId();
		hud.setTitle(uuid, miniMessage(translator.of("hud_title", SiegeHUDManager.checkLength(siege.getTown().getName()))));

		LinkedList<Component> sbComponents = new LinkedList<>();
		sbComponents.add(miniMessage(translator.of("hud_siege_type"), SiegeHUDManager.checkLength(siege.getSiegeType().getTranslatedName().forLocale(player))));
		sbComponents.add(miniMessage(translator.of("hud_attackers"), SiegeHUDManager.checkLength(siege.getAttackerNameForDisplay())));
		sbComponents.add(miniMessage(translator.of("hud_defenders"), SiegeHUDManager.checkLength(siege.getDefenderNameForDisplay())));
		sbComponents.add(miniMessage(translator.of("hud_siege_balance"), siege.getSiegeBalance().toString()));
		sbComponents.add(miniMessage(translator.of("hud_siege_progress"), siege.getNumBattleSessionsCompleted() + "/" + SiegeWarSettings.getSiegeDurationBattleSessions()));
		sbComponents.add(miniMessage(translator.of("hud_siege_status"), siege.getStatus().getName()));
		sbComponents.add(miniMessage(translator.of("hud_warchest"), (TownyEconomyHandler.isActive() ? TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()) : "-")));
		sbComponents.add(miniMessage(translator.of("hud_banner_control"), (siege.getBannerControllingSide().getFormattedName().forLocale(player)
				+ (siege.getBannerControllingSide() == SiegeSide.NOBODY ? "" : " (" + siege.getBannerControllingResidents().size() + ")"))));
		sbComponents.add(miniMessage(translator.of("hud_battle_attacker_points"), siege.getFormattedAttackerBattlePoints()));
		sbComponents.add(miniMessage(translator.of("hud_battle_defender_points"), siege.getFormattedDefenderBattlePoints()));
		sbComponents.add(miniMessage(translator.of("hud_battle_time_remaining"), siege.getFormattedBattleTimeRemaining(translator)));
		hud.setLines(uuid, sbComponents);
	}

	private static Component miniMessage(String string) {
		return TownyComponents.miniMessage(string);
	}

	private static Component miniMessage(String string1, String string2) {
		return TownyComponents.miniMessage(GRAY + string1 + " " + string2);
	}
}