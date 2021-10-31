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
    private final String siegeType;
    private final Siege siege;
    private final Town townOfSiegeStarter;
    private final Nation nation;
    private final Block flag;
    private final Town targetTown;

    public SiegeWarStartEvent(
            Siege siege,
            String siegeType,
            Town targetTown,
            Nation nation,
            Town townOfSiegeStarter,
            Block flag) {
        this.siege = siege;
        this.siegeType = siegeType;
        this.targetTown = targetTown;
        this.townOfSiegeStarter = townOfSiegeStarter;
        this.nation = nation;
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

	public String getSiegeType() {
		return siegeType;
	}

	public Nation getNation() {
		return nation;
	}

	public Town getTargetTown() {
		return targetTown;
	}
}
