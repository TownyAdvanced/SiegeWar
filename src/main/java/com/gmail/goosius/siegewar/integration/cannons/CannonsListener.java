package com.gmail.goosius.siegewar.integration.cannons;

import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
		//Generate new breach points, if appropriate
		if (!event.isCancelled()
			&& SiegeWar.isCannonsPluginInstalled()			
			&& SiegeWarSettings.getWarSiegeEnabled()
			&& TownyAPI.getInstance().getTownyWorld(event.getCannon().getLocation().getWorld()).isWarAllowed()
			&& SiegeWarSettings.isWallBreachingEnabled()
			&& SiegeWarSettings.isWallBreachingCannonsIntegrationEnabled()
			&& BattleSession.getBattleSession().isActive()
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getCannon().getLocation())) {

			Player gunnerPlayer = BukkitTools.getPlayer(event.getPlayer());
			Siege siege = SiegeController.getSiegeAtLocation(event.getCannon().getLocation());

			if(CannonsIntegration.canPlayerUseBreachPointsByCannon(gunnerPlayer, siege)) {
				return; //Player can fire cannon
			
			} else if(CannonsIntegration.canPlayerGenerateBreachPointsByCannon(gunnerPlayer, siege)) {
				//If the player has not already fired this tick, breach points are generated.
				if(!siege.getRecentTownFriendlyCannonFirers().contains(gunnerPlayer)) {
					double wallBreachPointsIncrease = SiegeWarSettings.getWallBreachingCannonFirePointGenerationRate() * siege.getTown().getTownBlocks().size();				
        			siege.increaseWallBreachPointsToCap(wallBreachPointsIncrease);
					siege.addRecentTownFriendlyCannonFirer(gunnerPlayer);
				}
				return;	//Player can fire cannon
			} else {
				event.setCancelled(true);
				Messaging.sendErrorMsg(gunnerPlayer, "You cannot fire this cannon. Check that you have rank which allows cannon-firing, and that you are an official participant in the local siege.");
				return;  //Cannon fire was cancelled
			}
		}
	}

	/**
	 * Process a Redstone Cannon Fire Event
	 * - If the cannon is in a siegezone, this is prevented
	 *  .... because in siegezones we need to attribute cannon fires to players
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonRedstoneEvent(CannonRedstoneEvent event) {
		if (!event.isCancelled()
			&& SiegeWar.isCannonsPluginInstalled()	
			&& SiegeWarSettings.getWarSiegeEnabled()
			&& TownyAPI.getInstance().getTownyWorld(event.getCannon().getLocation().getWorld()).isWarAllowed()
			&& SiegeWarSettings.isWallBreachingEnabled()
			&& SiegeWarSettings.isWallBreachingCannonsIntegrationEnabled()
			&& BattleSession.getBattleSession().isActive()
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getCannon().getLocation())) {
				event.setCancelled(true);
		}
	}
}
