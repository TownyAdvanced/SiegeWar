package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.BannerControlSession;
import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BannerControlSessionStartedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Siege siege;
    private final BannerControlSession bannerControlSession;

    public BannerControlSessionStartedEvent(Siege siege, BannerControlSession bannerControlSession) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.siege = siege;
        this.bannerControlSession = bannerControlSession;
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

    public BannerControlSession getBannerControlSession() {
        return bannerControlSession;
    }
}
