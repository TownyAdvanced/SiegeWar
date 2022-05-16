package com.gmail.goosius.siegewar.events;

import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GlobalDominationAwardsEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private List<Nation> awardees; //First to last
	private Map<Nation, Integer> moneyAwards;
	private Map<Nation, List<ItemStack>> artefactAwards;

	public GlobalDominationAwardsEvent(List<Nation> awardees, Map<Nation, Integer> moneyAwards, Map<Nation, List<ItemStack>> artefactAwards) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.awardees = awardees;
		this.moneyAwards = moneyAwards;
		this.artefactAwards = artefactAwards;
	}

	public List<Nation> getAwardees() {
		return awardees;
	}

	public Map<Nation, Integer> getMoneyAwards() {
		return moneyAwards;
	}

	public Map<Nation, List<ItemStack>> getArtefactAwards() {
		return artefactAwards;
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
