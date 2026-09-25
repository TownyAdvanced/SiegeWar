package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BannerControlPointsEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Siege siege;
    private final SiegeSide awardingSide;
    private final int controllersCount;
    private final int battlePoints;
    private final int sessionNumber;

    public BannerControlPointsEvent(Siege siege, SiegeSide awardingSide, int controllersCount, int battlePoints, int sessionNumber) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.siege = siege;
        this.awardingSide = awardingSide;
        this.controllersCount = controllersCount;
        this.battlePoints = battlePoints;
        this.sessionNumber = sessionNumber;
    }

    public Siege getSiege() {
        return siege;
    }

    public SiegeSide getAwardingSide() {
        return awardingSide;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public int getControllersCount() {
        return controllersCount;
    }

    public int getBattlePoints() {
        return battlePoints;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
