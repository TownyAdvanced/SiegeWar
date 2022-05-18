package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.utils.SiegeWarDominationAwardsUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArtefactDamageEntityEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Entity attacker;
	private final Entity victim;
	private Object artefact;  //Will either be Projectile or ItemStack
	private List<String> customEffects;

	public ArtefactDamageEntityEvent(Entity attacker, Entity victim, Object artefact) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.attacker = attacker;
		this.victim = victim;
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
}
