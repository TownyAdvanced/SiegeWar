package com.gmail.goosius.siegewar.utils;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyPermission.PermLevel;
import com.palmergames.bukkit.towny.object.TownyPermissionChange.Action;

/**
 * Util class containing useful methods for town permissions etc.
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
}
