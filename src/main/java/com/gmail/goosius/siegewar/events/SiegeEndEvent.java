package com.gmail.goosius.siegewar.events;

import com.gmail.goosius.siegewar.objects.Siege;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is triggered immediately after a siege ends
 *
 * Note: If end-users want names of the town/attacker/defender,
 *       they should be careful about getting the name via town/nation objects,
 *       as these towns/nations may disappear after the siege.
 *
 *       Rather they should use:
 *       event.getSiege().getAttackerName()
 *       siege.getSiege().getDefenderName()
 *       event.getBesiegedTownName()
 */
public class SiegeEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Siege siege;
    private final String besiegedTownName;

    public SiegeEndEvent(Siege siege,
                         String besiegedTownName) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.siege = siege;
        this.besiegedTownName = besiegedTownName;
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

    public String getBesiegedTownName() {
        return besiegedTownName;
    }
}
