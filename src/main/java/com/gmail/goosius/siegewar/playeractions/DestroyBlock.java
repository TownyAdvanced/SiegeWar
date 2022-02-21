package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarAllegianceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;

/**
 * This class handles siege-related destroy-block requests
 *
 * @author Goosius
 */
public class DestroyBlock {

	/**
	 * Evaluates a block destroy request.
	 *
	 * @param event The event object
	 */
	public static void evaluateSiegeWarDestroyBlockRequest(TownyDestroyEvent event) throws TownyException {
            //Ensure siege is enabled in this world
            Block block = event.getBlock();
			if (!TownyAPI.getInstance().getTownyWorld(block.getWorld()).isWarAllowed())
				return;

			//If the event has already been cancelled by Towny...
			if(event.isCancelled()) {		
				if(!SiegeWarSettings.isWallBreachingEnabled())
					return; //Without wall breaching, SW doesn't un-cancel events
				Town town = TownyAPI.getInstance().getTown(block.getLocation());
				if(town == null)
					return; //SW currently doesn't un-cancel wilderness events
				if(!SiegeController.hasActiveSiege(town))
					return; //SW doesn't un-cancel events in unsieged towns				

				//Ensure player has permission
				if (!TownyUniverse.getInstance().getPermissionSource().testPermission(event.getPlayer(), SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_USE_BREACH_POINTS.getNode())) {
					event.setMessage(Translation.of("msg_err_action_disable"));
					return; 
				}

				//Ensure player is on the town-hostile siege side				
				Resident resident = TownyAPI.getInstance().getResident(event.getPlayer());
				if(resident == null)
					return;
				if(!resident.hasNation())
					return;
				Siege siege = SiegeController.getSiege(town);
				SiegeSide playerSiegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(event.getPlayer(), resident.getTownOrNull(), siege);
				if(playerSiegeSide == SiegeSide.NOBODY)
					return; 
				switch(siege.getSiegeType()) {
					case CONQUEST:
					case SUPPRESSION:
						if(playerSiegeSide != SiegeSide.ATTACKERS)
							return;
						break;
					case REVOLT:
					case LIBERATION:
						if(playerSiegeSide != SiegeSide.DEFENDERS)
							return;
						break;
				}

				//Ensure there are enough breach points				
				if(siege.getWallBreachPoints() < SiegeWarSettings.getWallBreachingBlockDestructionCost()) {			
					event.setMessage(Translation.of("msg_err_not_enough_breach_points_for_action", SiegeWarSettings.getWallBreachingBlockDestructionCost(), siege.getFormattedBreachPoints()));
					return;
				}		

				//Ensure the height is ok
				if(SiegeWarDistanceUtil.isBlockCloseToTownBlock(block, town.getHomeBlockOrNull(), 2)) {					
					int heightOfBlockRelativeToSiegeBanner = block.getY() - siege.getFlagLocation().getBlockY();
					if(heightOfBlockRelativeToSiegeBanner < SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMin()) {
						event.setMessage(Translation.of("msg_err_cannot_destroy_at_this_height", SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMin(), SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMax()));
						return;
					}
					if(heightOfBlockRelativeToSiegeBanner > SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMax()) {
						event.setMessage(Translation.of("msg_err_cannot_destroy_at_this_height", SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMin(), SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMax()));
						return;
					}
				}	

				//Ensure the material is ok to destroy
				if(SiegeWarSettings.isWallBreachingDestroyEntityBlacklist()
					&& isEntityBeingTargeted(block.getLocation())) {
					event.setMessage(Translation.of("msg_err_breaching_cannot_destroy_this_material"));
					return;
				}
				if(SiegeWarSettings.isWallBreachingDestroyContainerBlacklist()
					&& block instanceof Container) {
					event.setMessage(Translation.of("msg_err_breaching_cannot_destroy_this_material"));
					return;
				}
				if(SiegeWarSettings.getWallBreachingDestroyBlocksBlacklist()
					.contains(block.getType())) {
					event.setMessage(Translation.of("msg_err_breaching_cannot_destroy_this_material"));
					return;
				}

				//IF we get here, it is a wall breach!!					
				//Reduce breach points
				siege.setWallBreachPoints(siege.getWallBreachPoints() - SiegeWarSettings.getWallBreachingBlockDestructionCost());
				//Un-cancel the event
				event.setCancelled(false);
				return;
			}

        //Trap warfare block protection
        if(SiegeWarSettings.isTrapWarfareMitigationEnabled()
                && SiegeWarDistanceUtil.isLocationInActiveTimedPointZoneAndBelowSiegeBannerAltitude(event.getBlock().getLocation())) {
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + Translation.of("msg_err_cannot_alter_blocks_below_banner_in_timed_point_zone")));
            event.setCancelled(true);
            return;
        }
        
        //Prevent destruction of siege-banner or support block
        if (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(event.getBlock())
        || SiegeWarBlockUtil.isBlockNearAnActiveSiegeCampBanner(event.getBlock())) {
            event.setMessage(Translation.of("msg_err_siege_war_cannot_destroy_siege_banner"));
            event.setCancelled(true);
            return;
        }
    }

	/**
	 * Determine if an entity is being targeted for destruction
	 * 
	 * We can do this because blocks have an integers only location (e.g. -20,60,140),
	 * but entities have doubles (e.g. -20.445,60.444,140.999)
	 * 
	 * @param location the given location
	 * @return true if an entity is being targeted
	 */
	private static boolean isEntityBeingTargeted(Location location) {
		if(location.getX() % 1 == 0
			&& location.getY() % 1 == 0
			&& location.getZ() % 1 == 0) {
			return false;
		} else {
			return true;
		}
	}
}
