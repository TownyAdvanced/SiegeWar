package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.HeldItemsCombination;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * This class contains utility functions related to the dynmap
 *
 * @author Goosius
 */
public class SiegeWarDynmapUtil {

	public static String MAP_SNEAK_METADATA_ID = "tacticallyInvisible";
	public static FixedMetadataValue MAP_SNEAK_FIXED_METADATA_VALUE = new FixedMetadataValue(Towny.getPlugin(), true);
	
	/**
	 * Evaluate players to see if they are 'map sneaking'
	 * 
	 * Map-sneaking makes a player invisible on the dynmap
	 * It is triggered if the player sets their main/off hand combinations 
	 * to one of the specified combinations (set in config file).
	 *
	 * Players in banner control sessions cannot map-sneak
	 */
	public static void evaluatePlayerMapSneaking() {
		boolean invisibleOnDynmap;

		for(Player player: BukkitTools.getOnlinePlayers()) {
			try {
				//Player is visible by default
				invisibleOnDynmap = false;

				//Check if player is invisible
				if (!SiegeController.getPlayersInBannerControlSessions().contains(player)) {

					//Check item combinations
					for(HeldItemsCombination heldItemsCombination: SiegeWarSettings.getWarSiegeMapSneakingItems()) {

						//Off Hand
						if(!heldItemsCombination.isIgnoreOffHand() && player.getInventory().getItemInOffHand().getType() != heldItemsCombination.getOffHandItemType())
							continue;  //off hand does not match. Try next combo

						//Main hand
						if(!heldItemsCombination.isIgnoreMainHand() && player.getInventory().getItemInMainHand().getType() != heldItemsCombination.getMainHandItemType())
							continue; //main hand does not match. Try next combo

						//Player invisible on map
						invisibleOnDynmap = true;
						break;
					}
				}

				if(invisibleOnDynmap) {
					if(!player.hasMetadata(MAP_SNEAK_METADATA_ID)) {
						player.setMetadata(MAP_SNEAK_METADATA_ID, MAP_SNEAK_FIXED_METADATA_VALUE);
					}
				} else {
					if (player.hasMetadata(MAP_SNEAK_METADATA_ID)) {
						player.removeMetadata(MAP_SNEAK_METADATA_ID, Towny.getPlugin());
					}
				}

			} catch (Exception e) {
				try {
					System.out.println("Problem evaluating map sneaking for player " + player.getName());
				} catch (Exception e2) {
					System.out.println("Problem evaluating map sneaking for a player (could not read player name)");
				}
				e.printStackTrace();
			}
		}
	}
}
