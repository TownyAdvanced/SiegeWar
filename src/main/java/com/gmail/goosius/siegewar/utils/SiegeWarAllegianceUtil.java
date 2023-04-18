package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

@Deprecated
public class SiegeWarAllegianceUtil {

	/**
	 * Deprecated in 2.0.0, use {@link SiegeSide#getPlayerSiegeSide(Siege, Player)} instead.
	 * 
	 * @param player Player to check.
	 * @param playerTown Town the player belongs to.
	 * @param siege Siege you're checking.
	 * @return SiegeSide
	 */
	@Deprecated
    public static SiegeSide calculateSiegePlayerSide(Player player, Town playerTown, Siege siege) {
		return SiegeSide.getPlayerSiegeSide(siege, player);
	}
}
