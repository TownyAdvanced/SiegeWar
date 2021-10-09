package com.gmail.goosius.siegewar.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BattleSessionEndedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public BattleSessionEndedEvent() {
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
