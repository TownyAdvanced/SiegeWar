package com.gmail.goosius.siegewar.integration.cannons;

import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
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

	public CannonsListener(CannonsIntegration integration) {
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
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getCannon().getLocation())) {

			Player gunnerPlayer = BukkitTools.getPlayer(event.getPlayer());
			if(!BattleSession.getBattleSession().isActive()) {
				Messaging.sendErrorMsg(gunnerPlayer, Translation.of("msg_err_cannot_fire_cannon_without_battle_session"));
				event.setCancelled(true);
				return;  //Cannon fire was cancelled	
			}

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
				Messaging.sendErrorMsg(gunnerPlayer, Translation.of("msg_err_cannot_fire_cannon_without_perms"));
				event.setCancelled(true);
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
