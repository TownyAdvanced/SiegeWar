package com.gmail.goosius.siegewar.events;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A cancellable event that is called right before a Nation subverts a peaceful town.
 * 
 * When cancelled, SiegeWar will not occupy the town and your plugin should do
 * something else.
 * 
 * If the cancellation message is not set to empty then it will be shown to the
 * player who tried to invade.
 *
 * @author LlmDl
 * @since 1.1.0
 */
public class PreSubvertTownEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final Nation nation;
	private final Town town;

	private boolean isCancelled;
	private String cancellationMsg = "Subversion prevented by another plugin.";

	public PreSubvertTownEvent(Player player, Nation nation, Town town) {
		this.player = player;
		this.nation = nation;
		this.town = town;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public Town getPeacfulTown() {
		return town;
	}

	public Nation getSubvertingNation() {
		return nation;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		isCancelled = b;
	}

	public String getCancellationMsg() {
		return cancellationMsg;
	}

	public void setCancellationMsg(String cancellationMsg) {
		this.cancellationMsg = cancellationMsg;
	}
}
