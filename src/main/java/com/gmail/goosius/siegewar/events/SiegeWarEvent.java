package com.gmail.goosius.siegewar.events;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class SiegeWarEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Nation attackerNation;
    private final Town attackerTown;
    private final Town defenderTown;
    private final TownBlock attackedTownBlock;
    private boolean isCancelled;
    private String cancellationMsg;

    public SiegeWarEvent(Nation attackerNation, Town attackerTown, Town defenderTown, TownBlock attackedTownBlock){

        this.attackerNation = attackerNation;
        this.attackerTown = attackerTown;
        this.defenderTown = defenderTown;
        this.attackedTownBlock = attackedTownBlock;

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

    public TownBlock getAttackedTownBlock() {
        return attackedTownBlock;
    }

    public String getCancellationMsg() {
        return cancellationMsg;
    }

    public void setCancellationMsg(String cancellationMsg) {
        this.cancellationMsg = cancellationMsg;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

}
