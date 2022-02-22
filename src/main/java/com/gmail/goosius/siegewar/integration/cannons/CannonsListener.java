package com.gmail.goosius.siegewar.integration.cannons;

import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonRedstoneEvent;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
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
	 * When a cannon is in a siegezone:
	 * - To fire the cannon, you must have one or both of the following perms:
	 *   - siegewar.siege.nation.firecannon (as official participant)
	 *   - siegewar.siege.town.firecannon (as resident of besieged town)
	 * - To fire the cannon, you must be an official siege participant.
     * - When a defender fires a cannon, breach points are generated.
     * - When an attacker fires a cannon, breach points are used up.
	 *
	 * @param event the event
	 */
	@EventHandler
	public void cannonFireEvent(CannonFireEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.isWallBreachingEnabled()
			&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getCannon().getLocation())) {

			Player gunnerPlayer = BukkitTools.getPlayer(event.getPlayer());
			Siege siege = SiegeController.getSiegeAtLocation(event.getCannon().getLocation());
			Resident gunnerResident = TownyAPI.getInstance().getResident(gunnerPlayer);
			if(gunnerResident == null) {
				event.setCancelled(true);
				return;
			}

			if(gunnerResident.hasTown() 
				&& gunnerResident.getTownOrNull() == siege.getTown()
				&& TownyUniverse.getInstance().getPermissionSource().testPermission(gunnerPlayer, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_FIRECANNONS.getNode())) {
				//Has permission to fire
			} else if (gunnerResident.hasNation()
				&& TownyUniverse.getInstance().getPermissionSource().testPermission(gunnerPlayer, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_FIRECANNONS.getNode())) {
				//Has permission to fire
			} else {
				event.setCancelled(true);
				Messaging.sendErrorMsg(gunnerPlayer, Translation.of("msg_err_action_disable"));
			}

			SiegeSide gunnerSiegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(gunnerPlayer,gunnerResident.getTownOrNull(), siege);
			switch(gunnerSiegeSide) {
				case NOBODY:
					event.setCancelled(true);
					Messaging.sendErrorMsg(gunnerPlayer, Translation.of("msg_err_action_disable"));
				break;
				case ATTACKERS:
				case DEFENDERS:
					boolean isSideHostileToTown = SiegeWarAllegianceUtil.isSideHostileToTown(gunnerSiegeSide, siege);
				break;
			}
		
		}
	
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
			Player player = null;
			try {
				player = Bukkit.getPlayer(event.getPlayer());
				cannonsIntegration.processPlayerCannonInteraction(player, event.getCannon(), Translation.of("msg_err_cannot_fire_no_cannon_session"));
			} catch (TownyException te) {
				event.setCancelled(true);
				if (player != null) {
					Messaging.sendErrorMsg(player, te.getMessage());
				} else {
					SiegeWar.severe("Problem processing fire cannon event: " + te.getMessage());
				}
			} catch (Exception e) {
				event.setCancelled(true);
				SiegeWar.severe("Problem processing fire cannon event: " + e.getMessage());
				e.printStackTrace();
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
}
