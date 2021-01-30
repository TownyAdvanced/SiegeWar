package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyPermission.PermLevel;
import com.palmergames.bukkit.towny.object.TownyPermissionChange.Action;

/**
 * Util class containing methods related to town flags/permssions.
 */
public class SiegeWarTownUtil {
    public static void disableTownPVP(Town town) {
		if (town.isPVP())
				town.setPVP(false);

		for (TownBlock plot : town.getTownBlocks()) {
			if (plot.hasPlotObjectGroup()) {
				TownyPermission groupPermissions = plot.getPlotObjectGroup().getPermissions();
				if (groupPermissions.pvp) {
					groupPermissions.pvp = false;
					plot.getPlotObjectGroup().setPermissions(groupPermissions);
				}
			} 	
			if (plot.getPermissions().pvp) {
				if (plot.getType() == TownBlockType.ARENA)
					plot.setType(TownBlockType.RESIDENTIAL);
			
				plot.getPermissions().pvp = false;
				plot.setChanged(true);
			}
		}
		town.save();
    }
    
    public static void disableNationPerms(Town town) {
        town.getPermissions().change(Action.PERM_LEVEL, false, PermLevel.NATION);

        for (TownBlock plot : town.getTownBlocks()) {
            if (plot.hasResident())
                continue;

            if (plot.hasPlotObjectGroup()) {
                TownyPermission groupPermission = plot.getPlotObjectGroup().getPermissions();
                groupPermission.change(Action.PERM_LEVEL, false, PermLevel.NATION);
                plot.getPlotObjectGroup().setPermissions(groupPermission);
            }

            plot.getPermissions().change(Action.PERM_LEVEL, false, PermLevel.NATION);
            plot.save();
        }
        town.save();
    }

    /**
	 * Sets pvp and explosions in a town to the desired setting, if enabled in the config.
	 * 
	 * @param town The town to set the flags for.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	public static void setTownFlags(Town town, boolean desiredSetting) {
		if (town.getPermissions().pvp != desiredSetting && SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns())
			town.getPermissions().pvp = desiredSetting;
		if (town.getPermissions().explosion != desiredSetting && SiegeWarSettings.getWarSiegeExplosionsAlwaysOnInBesiegedTowns())
			town.getPermissions().explosion = desiredSetting;
	}
}
