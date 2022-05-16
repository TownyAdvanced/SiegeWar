package com.gmail.goosius.siegewar.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ArtefactHitEntityEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public ArtefactHitEntityEvent() {
		super(!Bukkit.getServer().isPrimaryThread());
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
