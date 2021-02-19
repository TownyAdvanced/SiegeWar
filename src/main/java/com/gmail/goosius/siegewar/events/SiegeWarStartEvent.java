package com.gmail.goosius.siegewar.events;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SiegeWarStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Nation attackerNation;
    private final Town attackerTown;
    private final Town defenderTown;
    private final Block flag;

    public SiegeWarStartEvent(Block flag, Nation attackerNation, Town attackerTown, Town defenderTown){

        this.flag = flag;
        this.attackerNation = attackerNation;
        this.attackerTown = attackerTown;
        this.defenderTown = defenderTown;

    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Nation getAttackerNation() {
        return attackerNation;
    }

    public Town getAttackerTown() {
        return attackerTown;
    }

    public Town getDefenderTown() {
        return defenderTown;
    }

    public Block getFlag() {
        return flag;
    }
}
