package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SiegeWarStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Siege siege;
    private final Nation attackerNation;
    private final Town attackerTown;
    private final Town defenderTown;
    private final Block flag;

    public SiegeWarStartEvent(Siege siege, Town town, Block flag) {
        this.siege = siege;
        this.attackerNation = siege.getAttackingNation();
        this.attackerTown = town;
        this.defenderTown = siege.getDefendingTown();
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
