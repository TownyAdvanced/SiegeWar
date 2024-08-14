package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TownPlunderedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Siege siege;
    private final Player player;

    public TownPlunderedEvent(Siege siege, Player player) {
        this.siege = siege;
        this.player = player;
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

    @Nullable
    public Town getTown() {
        return siege.getTown();
    }

    public Player getPlayer() {
        return player;
    }
}
