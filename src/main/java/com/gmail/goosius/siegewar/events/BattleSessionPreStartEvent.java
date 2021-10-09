package com.gmail.goosius.siegewar.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BattleSessionPreStartEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private String cancellationMsg = "Battle Session prevented by another plugin.";

	public BattleSessionPreStartEvent() {
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public String getCancellationMsg() {
		return cancellationMsg;
	}

	public void setCancellationMsg(String cancellationMsg) {
		this.cancellationMsg = cancellationMsg;
	}
}
