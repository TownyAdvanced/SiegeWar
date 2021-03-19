package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SiegeWarStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Siege siege;
    private final Town townOfSiegeStarter;
    private final Block flag;

    public SiegeWarStartEvent(Siege siege, Town townOfSiegeStarter, Block flag) {
        this.siege = siege;
        this.townOfSiegeStarter = townOfSiegeStarter;
        this.flag = flag;
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

    public Town getTownOfSiegeStarter() {
        return townOfSiegeStarter;
    }

    public Block getFlag() {
        return flag;
    }
}
