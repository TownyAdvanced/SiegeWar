package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.HeldItemsCombination;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
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
	 * 
	 * Players attempting to get banner control are not hidden by this feature.
	 *
	 * Map-Hiding has 2 modes
	 * 1. Manual - triggered if the player sets their main/off hand combinations 
	 *             to one of the configured combinations.
	 * 2. Automatic - triggered if the player is in the wilderness or in a townblock
	 *                where pvp is enabled.
	 */
	public static void evaluatePlayerMapHiding() {
		boolean hidePlayer;
		Town town;

		for(Player player: BukkitTools.getOnlinePlayers()) {
			try {
				//By default, player is not hidden
				hidePlayer = false;

				//Check if player should be hidden
				if (!SiegeController.getPlayersInBannerControlSessions().contains(player)) {

					//Check map-hiding mode
					switch(SiegeWarSettings.getWarSiegeMapHidingMode()) {
					
						//Manual mode - player is hidden if holding a certain combo of items
						case MANUAL:
							for(HeldItemsCombination heldItemsCombination: SiegeWarSettings.getWarSiegeMapHidingItems()) {
								//Off Hand
								if(!heldItemsCombination.isIgnoreOffHand() && player.getInventory().getItemInOffHand().getType() != heldItemsCombination.getOffHandItemType())
									continue;  //off hand does not match. Try next combo
								//Main hand
								if(!heldItemsCombination.isIgnoreMainHand() && player.getInventory().getItemInMainHand().getType() != heldItemsCombination.getMainHandItemType())
									continue; //main hand does not match. Try next combo
								//Hide player
								hidePlayer = true;
								break;
							}
							break;
							
						//Automatic mode - player is hidden if in wilderness or pvp-enabled town
						case AUTOMATIC:
							town = TownyAPI.getInstance().getTown(player.getLocation());
							if(town == null){
								//Wilderness
								hidePlayer = true;
							} else {
								//Town
								if(town.isPVP()) {
									hidePlayer = true;
								}
							}
							break;
					}					
				}

				if(hidePlayer) {
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
					System.out.println("Problem evaluating map hiding for player " + player.getName());
				} catch (Exception e2) {
					System.out.println("Problem evaluating map hiding for a player (could not read player name)");
				}
				e.printStackTrace();
			}
		}
	}
}
