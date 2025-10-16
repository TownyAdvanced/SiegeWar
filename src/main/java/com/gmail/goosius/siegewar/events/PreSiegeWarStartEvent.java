package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.enums.SiegeType;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreSiegeWarStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final SiegeType siegeType;
    private final Town townOfSiegeStarter;
    private final Player siegeStarter;
    private final Nation nation;
    private final Block flag;
    private final TownBlock townBlock;
    private final Town targetTown;
    private boolean isCancelled;
    private String cancellationMsg = "Siege prevented by another plugin.";

    public PreSiegeWarStartEvent(SiegeType siegeType,
                                 Town targetTown,
                                 Nation nation,
                                 Town townOfSiegeStarter, 
                                 Player siegeStarter,
                                 Block flag,
                                 TownBlock townBlock){
        this.siegeType = siegeType;
        this.targetTown = targetTown;
        this.townOfSiegeStarter = townOfSiegeStarter;
        this.siegeStarter = siegeStarter;
        this.nation = nation;
        this.flag = flag;
        this.townBlock = townBlock;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Town getTownOfSiegeStarter() {
        return townOfSiegeStarter;
    }

    public Nation getNation() {
        return nation;
    }

    public Block getFlag() {
        return flag;
    }

    public Player getSiegeStarter() {
        return siegeStarter;
    }

    public TownBlock getTownBlock() {
        return townBlock;
    }

    public Town getTargetTown() {
        return targetTown;
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

    public SiegeType getSiegeType() {
        return siegeType;
    }
}
