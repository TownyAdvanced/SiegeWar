package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;

/**
 * Util class containing methods related to town flags/permssions.
 */
public class SiegeWarTownUtil {
    public static void disableTownPVP(Town town) {
		if (town.isPVP())
				town.setPVP(false);

		for (TownBlock plot : town.getTownBlocks()) {
			if (plot.getPermissions().pvp) {
				if (plot.getType() == TownBlockType.ARENA)
					plot.setType(TownBlockType.RESIDENTIAL);

				plot.getPermissions().pvp = false;
				plot.save();
			}
		}
		town.save();
    }

    /**
	 * Sets pvp flags in a town to the desired setting.
	 *
	 * @param town The town to set the flags for.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	public static void setTownPvpFlags(Town town, boolean desiredSetting) {
		if (town.getPermissions().pvp != desiredSetting && SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns()) {
			town.getPermissions().pvp = desiredSetting;
			town.save();
		}
	}

	/**
	 * Sets pvp flags of all the towns in a nation to the desired setting.
	 *
	 * @param nationTown A town belonging to the nation.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	public static void setPvpFlagsOfAllNationTowns(Town nationTown, boolean desiredSetting) {
		try {
			Nation nation = nationTown.getNation();
			for(Town town: nation.getTowns()) {
				if (town.getPermissions().pvp != desiredSetting && SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns()) {
					town.getPermissions().pvp = desiredSetting;
					town.save();
				}
			}
		} catch (NotRegisteredException ignored) {}
	}
}