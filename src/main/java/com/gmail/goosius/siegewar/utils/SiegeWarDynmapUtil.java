package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.HeldItemsCombination;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * This class contains utility functions related to the dynmap
 *
 * @author Goosius
 */
public class SiegeWarDynmapUtil {

	public static String MAP_HIDING_METADATA_ID = "tacticallyInvisible";
	public static FixedMetadataValue MAP_HIDING_FIXED_METADATA_VALUE = new FixedMetadataValue(Towny.getPlugin(), true);
	
	/**
	 * Evaluate players to see if they are should be hidden on the dynamp.
	 */
	public static void evaluateMapHiding() {
		for(Player player: BukkitTools.getOnlinePlayers()) {
			try {
				if(shouldPlayerBeMapHidden(player)) {
					if(!player.hasMetadata(MAP_HIDING_METADATA_ID)) {
						player.setMetadata(MAP_HIDING_METADATA_ID, MAP_HIDING_FIXED_METADATA_VALUE);
					}
				} else {
					if (player.hasMetadata(MAP_HIDING_METADATA_ID)) {
						player.removeMetadata(MAP_HIDING_METADATA_ID, Towny.getPlugin());
					}
				}
			} catch (Exception e) {
				try {
					SiegeWar.severe("Problem evaluating map hiding for player " + player.getName());
				} catch (Exception e2) {
					SiegeWar.severe("Problem evaluating map hiding for a player (could not read player name)");
				}
				e.printStackTrace();
			}
		}
	}

	/**
	 * Determine if a player should be map-hidden
	 *
	 * @param player the player
	 * @return true if they should be hidden
	 */
	private static boolean shouldPlayerBeMapHidden(Player player) {
		//Don't hide if they are in a BC session
		if (SiegeController.getPlayersInBannerControlSessions().contains(player)) {
			return false;
		}

		//Evaluate automatic mode
		if (SiegeWarSettings.getWarSiegeMapHidingModeAutomaticModeEnabled()) {
			Town town = TownyAPI.getInstance().getTown(player.getLocation());

			if(town == null) {
				//Wilderness
				if(SiegeWarSettings.getWarSiegeMapHidingModeAutomaticModeScopeWilderness())
					return true;
			} else {
				//Ruins
				if(SiegeWarSettings.getWarSiegeMapHidingModeAutomaticModeScopeRuins() && town.isRuined())
					return true;
				//Besieged town
				if(SiegeWarSettings.getWarSiegeMapHidingModeAutomaticModeScopeBesiegedTowns() && SiegeController.hasActiveSiege(town))
					return true;
			}

			//Active siege zone / near-siege-banner
			if(SiegeWarSettings.getWarSiegeMapHidingModeAutomaticModeScopeSiegezones()) {
				int requiredRadiusBlocks = (int)(SiegeWarSettings.getWarSiegeZoneRadiusBlocks() * SiegeWarSettings.getWarSiegeMapHidingModeAutomaticModeScopeSiegezonesRadius());				
				for (Siege siege : SiegeController.getSieges()) {
					if(siege.getStatus().isActive()
						&& SiegeWarDistanceUtil.areLocationsCloseHorizontally(player.getLocation(), siege.getFlagLocation(), requiredRadiusBlocks)) {
						return true;
					}
				}
			}
		}

		//Evaluate triggering
		if(SiegeWarSettings.getWarSiegeMapHidingTriggeringEnabled()) {
			for(HeldItemsCombination heldItemsCombination: SiegeWarSettings.getWarSiegeMapHidingTriggeringItems()) {
				//Off Hand
				if(!heldItemsCombination.isIgnoreOffHand() && player.getInventory().getItemInOffHand().getType() != heldItemsCombination.getOffHandItemType())
					continue;  //off hand does not match. Try next combo
				//Main hand
				if(!heldItemsCombination.isIgnoreMainHand() && player.getInventory().getItemInMainHand().getType() != heldItemsCombination.getMainHandItemType())
					continue; //main hand does not match. Try next combo
				//Hide player
				return true;
			}
		}

		//Player has not met any conditions for map hiding. Do not hide them.
		return false;
	}
}
