package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.BannerControlSession;
import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BannerControlSessionPreStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Siege siege;
    private final BannerControlSession bannerControlSession;
    private boolean cancelled;

    public BannerControlSessionPreStartEvent(Siege siege, BannerControlSession bannerControlSession) {
        this.siege = siege;
        this.bannerControlSession = bannerControlSession;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Siege getSiege() {
        return siege;
    }

    public BannerControlSession getBannerControlSession() {
        return bannerControlSession;
    }
}
