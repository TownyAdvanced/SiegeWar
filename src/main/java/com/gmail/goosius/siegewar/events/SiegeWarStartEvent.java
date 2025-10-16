package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SiegeWarStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String siegeType;
    private final Siege siege;
    private final Town townOfSiegeStarter;
    private final Player siegeStarter;
    private final Nation nation;
    private final Block flag;
    private final Town targetTown;
    private final String message;

    public SiegeWarStartEvent(Siege siege, Town townOfSiegeStarter, Player siegeStarter, String message) {
        this.siege = siege;
        this.siegeType = siege.getSiegeType().getName();
        this.targetTown = siege.getTown();
        this.townOfSiegeStarter = townOfSiegeStarter;
        this.siegeStarter = siegeStarter;
        this.nation = siege.isRevoltSiege() ? targetTown.getNationOrNull() : (Nation)siege.getAttackingNationIfPossibleElseTown();
        this.flag = siege.getFlagLocation().getBlock();
        this.message = message;
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

    public Player getSiegeStarter() {
        return siegeStarter;
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

    public String getMessage() {
        return PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(message));
    }
}
