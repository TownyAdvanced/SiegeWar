package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.events.*;
import com.gmail.goosius.siegewar.utils.DiscordWebhook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Translatable;

import java.awt.*;
import java.util.Arrays;

public class SiegeWarSelfListener implements Listener {
	
	@EventHandler(ignoreCancelled = true)
	public void onBattleSessionPreStart(BattleSessionPreStartEvent event) {
		if (SiegeWarSettings.cancelBattleSessionWhenNoActiveSieges()
		&& (SiegeController.getSieges().isEmpty() || SiegeController.getSieges().stream().noneMatch(siege -> siege.getStatus().isActive()))) {
			event.setCancelled(true);
			event.setCancellationMsg(Translatable.of("battle_session_cancelled_no_sieges").defaultLocale());
		}
	}

	@EventHandler
	public void onBattleSessionStarted(BattleSessionStartedEvent event) {
		if (!SiegeWarSettings.isDiscordWebhookEnabled())
			return;

		DiscordWebhook webhook = new DiscordWebhook(SiegeWarSettings.getDiscordWebhookUrl());

		webhook.addEmbed(new DiscordWebhook.EmbedObject()
				.setColor(new Color(255, 157, 0))
				.setDescription(event.getMessage().substring(2))
		);
		try {
			webhook.execute();
		} catch (java.io.IOException e) {
			SiegeWar.getSiegeWar().getLogger().severe(Arrays.toString(e.getStackTrace()));
		}
	}

	@EventHandler
	public void onBattleSessionEnded(BattleSessionEndedEvent event) {
		if (!SiegeWarSettings.isDiscordWebhookEnabled())
			return;

		DiscordWebhook webhook = new DiscordWebhook(SiegeWarSettings.getDiscordWebhookUrl());

		webhook.addEmbed(new DiscordWebhook.EmbedObject()
				.setColor(new Color(255, 157, 0))
				.setDescription(event.getMessage().substring(2))
		);
		try {
			webhook.execute();
		} catch (java.io.IOException e) {
			SiegeWar.getSiegeWar().getLogger().severe(Arrays.toString(e.getStackTrace()));
		}
	}

	@EventHandler
	public void onSiegeCampStart(SiegeCampStartEvent event) {
		if (!SiegeWarSettings.isDiscordWebhookEnabled())
			return;

		DiscordWebhook webhook = new DiscordWebhook(SiegeWarSettings.getDiscordWebhookUrl());

		webhook.addEmbed(new DiscordWebhook.EmbedObject()
				.setColor(Color.decode(event.getNation().getMapColorHexCode()))
				.setDescription(event.getMessage())
		);
		try {
			webhook.execute();
		} catch (java.io.IOException e) {
			SiegeWar.getSiegeWar().getLogger().severe(Arrays.toString(e.getStackTrace()));
		}
	}

	@EventHandler
	public void onSiegeWarStart(SiegeWarStartEvent event) {
		if (!SiegeWarSettings.isDiscordWebhookEnabled())
			return;

		DiscordWebhook webhook = new DiscordWebhook(SiegeWarSettings.getDiscordWebhookUrl());
		webhook.addEmbed(new DiscordWebhook.EmbedObject()
				.setColor(Color.decode(event.getNation().getMapColorHexCode()))
				.setDescription(SiegeController.getGlobalSiegeStartMessage(event.getSiege()).defaultLocale().substring(2))
		);
		try {
			webhook.execute();
		} catch (java.io.IOException e) {
			SiegeWar.getSiegeWar().getLogger().severe(Arrays.toString(e.getStackTrace()));
		}
	}

	@EventHandler
	public void onSiegeEnd(SiegeEndEvent event) {
		if (!SiegeWarSettings.isDiscordWebhookEnabled())
			return;

		DiscordWebhook webhook = new DiscordWebhook(SiegeWarSettings.getDiscordWebhookUrl());

		webhook.addEmbed(new DiscordWebhook.EmbedObject()
				.setColor(Color.decode(event.getNation().getMapColorHexCode()))
				.setDescription(event.getSiege().getEndMessage().substring(2))
		);
		try {
			webhook.execute();
		} catch (java.io.IOException e) {
			SiegeWar.getSiegeWar().getLogger().severe(Arrays.toString(e.getStackTrace()));
		}
	}
}
