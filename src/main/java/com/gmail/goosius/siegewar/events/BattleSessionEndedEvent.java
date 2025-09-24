package com.gmail.goosius.siegewar.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class BattleSessionEndedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final String message;

	public BattleSessionEndedEvent(String message) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.message = message;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public String getMessage() {
		return PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(message));
	}
}
