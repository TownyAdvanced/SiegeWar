package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BannerControlSessionEndedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Siege siege;

    public BannerControlSessionEndedEvent(Siege siege) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.siege = siege;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Siege getSiege() {
        return siege;
    }
}