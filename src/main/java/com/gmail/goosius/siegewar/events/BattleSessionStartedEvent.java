package com.gmail.goosius.siegewar.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BattleSessionStartedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public BattleSessionStartedEvent() {
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
