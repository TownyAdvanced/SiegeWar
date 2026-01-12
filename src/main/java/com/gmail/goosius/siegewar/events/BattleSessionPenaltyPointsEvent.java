package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BattleSessionPenaltyPointsEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Reason {
        DEATH,
        KILLED_BY_DEFENDER,
        KILLED_BY_ATTACKER
    }

    private final @NotNull Siege siege;
    private final int pointsPenalized;
    private final @NotNull Reason reason;

    private final boolean victimWasAttacker;
    private final @NotNull Player victim;
    private final @Nullable Player killer;

    public BattleSessionPenaltyPointsEvent(@NotNull Siege siege, int pointsPenalized, @NotNull Reason reason, boolean victimWasAttacker, @NotNull Player victim, @Nullable Player killer) {
        this.siege = siege;
        this.pointsPenalized = pointsPenalized;
        this.reason = reason;
        this.victimWasAttacker = victimWasAttacker;
        this.victim = victim;
        this.killer = killer;
    }

    public @NotNull Siege getSiege() {
        return siege;
    }

    public int getPointsPenalized() {
        return pointsPenalized;
    }

    public @NotNull Reason getReason() {
        return reason;
    }

    public boolean isVictimWasAttacker() {
        return victimWasAttacker;
    }

    public @NotNull Player getVictim() {
        return victim;
    }

    public @Nullable Player getKiller() {
        return killer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
