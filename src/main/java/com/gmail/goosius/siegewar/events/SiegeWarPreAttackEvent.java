package com.gmail.goosius.siegewar.events;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

public class SiegeWarPreAttackEvent extends SiegeWarEvent {

    public SiegeWarPreAttackEvent(Nation attackerNation, Town attackerTown, Town defenderTown, TownBlock attackedTownBlock) {
        super(attackerNation, attackerTown, defenderTown, attackedTownBlock);
    }

}
