package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.utils.SiegeWarDominationAwardsUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ArtefactThrownPotionEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final ThrownPotion artefact;
	private final Collection<LivingEntity> affectedEntities;
	private final List<String> customEffects;

	public ArtefactThrownPotionEvent(ThrownPotion artefact, Collection<LivingEntity> affectedEntities) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.artefact = artefact;
		this.affectedEntities = affectedEntities;
		this.customEffects = SiegeWarDominationAwardsUtil.getCustomEffects(artefact.getItem());
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ThrownPotion getArtefact() {
		return artefact;
	}

	public List<String> getCustomEffects() {
		return customEffects;
	}

	public Collection<LivingEntity> getAffectedEntities() {
		return affectedEntities;
	}
}
