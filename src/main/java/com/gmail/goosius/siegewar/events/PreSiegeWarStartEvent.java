package com.gmail.goosius.siegewar.events;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreSiegeWarStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Town townOfAttackingPlayer;
    private final Nation nationOfAttackingPlayer;
    private final Block flag;
    private final TownBlock townBlock;
    private final Town defendingTown;
    private boolean isCancelled;
    private String cancellationMsg = "Siege prevented by another plugin.";

    public PreSiegeWarStartEvent(Town townOfAttackingPlayer, Nation nationOfAttackingPlayer, Block flag, TownBlock townBlock, Town defendingTown){

        this.townOfAttackingPlayer = townOfAttackingPlayer;
        this.nationOfAttackingPlayer = nationOfAttackingPlayer;
        this.flag = flag;
        this.townBlock = townBlock;
        this.defendingTown = defendingTown;

    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Town getTownOfAttackingPlayer() {
        return townOfAttackingPlayer;
    }

    public Nation getNationOfAttackingPlayer() {
        return nationOfAttackingPlayer;
    }

    public Block getFlag() {
        return flag;
    }

    public TownBlock getTownBlock() {
        return townBlock;
    }

    public Town getDefendingTown() {
        return defendingTown;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    public String getCancellationMsg() {
        return cancellationMsg;
    }

    public void setCancellationMsg(String cancellationMsg) {
        this.cancellationMsg = cancellationMsg;
    }
}
