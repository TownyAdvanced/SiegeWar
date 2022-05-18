package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.utils.SiegeWarDominationAwardsUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArtefactConsumeItemEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Player consumer;
	private final ItemStack artefact;
	private final List<String> customEffects;

	public ArtefactConsumeItemEvent(Player consumer, ItemStack artefact) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.consumer = consumer;
		this.artefact = artefact;
		this.customEffects = SiegeWarDominationAwardsUtil.getCustomEffects(artefact);
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getConsumer() {
		return consumer;
	}

	public ItemStack getArtefact() {
		return artefact;
	}

	public List<String> getCustomEffects() {
		return customEffects;
	}
}
