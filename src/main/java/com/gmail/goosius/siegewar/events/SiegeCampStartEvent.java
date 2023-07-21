package com.gmail.goosius.siegewar.events;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event that marks the start of a {@link SiegeCamp}.
 *
 * SiegeCamps precede Sieges, acting as a minigame of sorts, in which the
 * Siege-starter must gain an amount of points:
 * {@link SiegeWarSettings#getSiegeCampPointsForSuccess()}, in order for the
 * Siege to begin in earnest. This gives towns an opportunity to disrupt two
 * friendly towns from acting out a mock-siege, in order to get around being
 * sieged by actual enemies.
 *
 * @author ewof
 * @since 2.8.0
 */
public class SiegeCampStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final SiegeCamp siegeCamp;
    private final String message;

    public SiegeCampStartEvent(SiegeCamp camp, String message){
        this.siegeCamp = camp;
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

    public Town getTownOfSiegeStarter() {
        return siegeCamp.getTownOfSiegeStarter();
    }

    public Nation getNation() {
        return siegeCamp.getSiegeType().equals(SiegeType.REVOLT) ? (Nation) siegeCamp.getDefender() : (Nation) siegeCamp.getAttacker();
    }

    public Block getFlag() {
        return siegeCamp.getBannerBlock();
    }

    public TownBlock getTownBlock() {
        return siegeCamp.getTownBlock();
    }

    public Town getTargetTown() {
        return siegeCamp.getTargetTown();
    }

    public SiegeType getSiegeType() {
        return siegeCamp.getSiegeType();
    }

    public String getMessage() {
        return message;
    }
}