package com.gmail.goosius.siegewar.integration.cannons;

import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarAllegianceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

/**
 * 
 * @author Goosius
 *
 */
public class CannonsListener implements Listener {

	private final CannonsIntegration cannonsIntegration;

	public CannonsListener(CannonsIntegration integration) {
		this.cannonsIntegration = integration;
	}

	/**
	 * Process a Cannon Fire Event, where a player fires a cannon
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonFireEvent(CannonFireEvent event) {	
		if (!event.isCancelled()
			&& SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.isWallBreachingEnabled()
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getCannon().getLocation())) {

			//Battle session must be active
			Player gunnerPlayer = BukkitTools.getPlayer(event.getPlayer());
			if(!BattleSession.getBattleSession().isActive()) {
				event.setCancelled(true);
				Messaging.sendErrorMsg(gunnerPlayer, "Can't fire cannons in siegezones unless a battle session is active");
				return;
			}

			//Payer must have perms
			Siege siege = SiegeController.getSiegeAtLocation(event.getCannon().getLocation());
			Resident gunnerResident = TownyAPI.getInstance().getResident(gunnerPlayer);
			if(
				! ( 
					(gunnerResident.hasTown() 
					&& gunnerResident.getTownOrNull() == siege.getTown()
					&& TownyUniverse.getInstance().getPermissionSource().testPermission(gunnerPlayer, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_FIRE_CANNON_IN_SIEGEZONE.getNode()))

					|| 

					(gunnerResident.hasNation()
					&& TownyUniverse.getInstance().getPermissionSource().testPermission(gunnerPlayer, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_FIRE_CANNONS_IN_SIEGEZONE.getNode()))
				 )
			) {
				event.setCancelled(true);
				Messaging.sendErrorMsg(gunnerPlayer, Translation.of("You do not have permissions to fire cannons in this siegezone."));			
				return;
			}

			//Player must be an official siege participant
			SiegeSide gunnerSiegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(gunnerPlayer,gunnerResident.getTownOrNull(), siege);
			if (gunnerSiegeSide == SiegeSide.NOBODY) {
				event.setCancelled(true);
				Messaging.sendErrorMsg(gunnerPlayer, Translation.of("You do not have permissions to fire cannons in this siegezone."));
				return;
			} 
			
			//Take-away/generate Breach Points, then fire!
			boolean isSiegeSideHostileToTown = SiegeWarAllegianceUtil.isSideHostileToTown(gunnerSiegeSide, siege);
			if(isSiegeSideHostileToTown) {
				int breachPointCost = SiegeWarSettings.getWallBreachingCannonFireCost();
				if(breachPointCost > siege.getWallBreachPoints()) {
					Messaging.sendErrorMsg(gunnerPlayer, Translation.of("Not enough breach points to fire this cannon."));
					event.setCancelled(true);				
					return;
				} else {
					siege.setWallBreachPoints(siege.getWallBreachPoints() - breachPointCost);
					return;
				}
			} else {
				//If the player has not already fired this tick, points are not generated
				if(!siege.getRecentTownFriendlyCannonFirers().contains(gunnerPlayer)) {
					int generatedBreachPoints = SiegeWarSettings.getWallBreachingCannonFirePointGenerationRate() * siege.getTown().getTownBlocks().size();	
					siege.setWallBreachPoints(siege.getWallBreachPoints() - generatedBreachPoints);
					siege.addRecentTownFriendlyCannonFirer(gunnerPlayer);
				}				
				return;
			}
		}
	}

	/**
	 * Process a Redstone Cannon Fire Event
	 * - If the cannon is in a siegezone,this is prevented
	 *  .... because in siegezones we need to attribute cannon fires to players
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonRedstoneEvent(CannonRedstoneEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.isWallBreachingEnabled()
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getCannon().getLocation())) {
				event.setCancelled(true);
		}
	}
}
