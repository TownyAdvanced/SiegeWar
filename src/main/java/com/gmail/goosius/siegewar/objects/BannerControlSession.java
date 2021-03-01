package com.gmail.goosius.siegewar.objects;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.palmergames.bukkit.towny.object.Resident;

import org.bukkit.entity.Player;

public class BannerControlSession {
	private Player player;
	private Resident resident;
	private SiegeSide siegeSide;
	private long sessionEndTime;
	int shortTicksUntilNextPauseWarning;

	public BannerControlSession(Resident resident, Player player, SiegeSide siegeSide, long sessionEndTime) {
		this.resident = resident;
		this.player = player;
		this.siegeSide = siegeSide;
		this.sessionEndTime = sessionEndTime;
		this.shortTicksUntilNextPauseWarning = 0;
	}

	public Player getPlayer() {
		return player;
	}

	public SiegeSide getSiegeSide() {
		return siegeSide;
	}

	public long getSessionEndTime() {
		return sessionEndTime;
	}

	public Resident getResident() {
		return resident;
	}

	public int getShortTicksUntilNextPauseWarning() {
		return shortTicksUntilNextPauseWarning;
	}

	public void setShortTicksUntilNextPauseWarning(int i) {
		shortTicksUntilNextPauseWarning = i;
	}

	public void decrementShortTicksUntilNextPauseWarning() {
		shortTicksUntilNextPauseWarning--;
	}
}
