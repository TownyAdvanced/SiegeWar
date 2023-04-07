package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NationPreRemoveEnemyEvent;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;
import com.palmergames.bukkit.towny.event.nation.NationRankAddEvent;
import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.townblockstatus.NationZoneTownBlockStatusEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarNationEventListener implements Listener {


	@EventHandler
	public void onNationRankGivenToPlayer(NationRankAddEvent event) {
		//In Siegewar, if target town is peaceful or occupied, can't add military rank
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& PermissionUtil.doesNationRankAllowPermissionNode(event.getRank(), SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS)) {

			//Get residents town
			Town town = TownyAPI.getInstance().getResidentTownOrNull(event.getResident());
			if(town != null) {
				if(SiegeWarTownPeacefulnessUtil.isTownPeaceful(town)) {
					event.setCancelled(true);
					event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_war_siege_cannot_add_military_rank_to_peaceful_resident"));
				} else if (TownOccupationController.isTownOccupied(town)) {
					event.setCancelled(true);
					event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_war_siege_cannot_add_military_rank_to_occupied_resident"));
				}
			}
		}
		
	}
	
	/*
	 * SiegeWar will disable nation-zones if the town has a siege.
	 */
	@EventHandler
	public void onNationZoneStatus(NationZoneTownBlockStatusEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() 
			&& SiegeController.hasActiveSiege(event.getTown()))	{
			event.setCancelled(true);
		}
	}
	
	/*
	 * A nation being deleted with a siege means the siege ends,
	 * and a king may receive a refund.
	 */
	@EventHandler
	public void onDeleteNation(DeleteNationEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		/*
		 * Adjust sieges if needed
		 */
		for (Siege siege : SiegeController.getSieges()) {
			/*
			 * If attacker (which is always a nation) disappears, we must delete the siege
			 */
			if (event.getNationUUID() == siege.getAttacker().getUUID()) {
				SiegeController.removeSiege(siege);
			}
		}

		//Award nation refund
		event.getNationUUID();
		Resident king = event.getLeader();
		if (king != null) {
			SiegeWarMoneyUtil.makeNationRefundAvailable(king);
		}
	}

	@EventHandler
	public void onPreNationEnemyRemove(NationPreRemoveEnemyEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		boolean cancel = false;
		Nation nation = event.getNation();
		Nation enemyNation = event.getEnemy();

		for(Siege siege: SiegeController.getSieges()) {
			if (!siege.getStatus().isActive())
				continue;

			//Cancel if you are attacking them
			if (siege.getAttacker() == nation
					&& siege.getDefendingNationIfPossibleElseTown() == enemyNation) {
				cancel = true;
				break;
			}

			//Cancel if they are attacking you
			if (siege.getAttacker() == enemyNation
					&& siege.getDefendingNationIfPossibleElseTown() == nation) {
				cancel = true;
				break;
			}

			if (siege.isRevoltSiege()) {
				//Cancel if one of your towns is revolting against them
				if (siege.getTown().hasNation()
						&& TownyAPI.getInstance().getTownNationOrNull(siege.getTown()) == nation
						&& siege.getDefender() == enemyNation) {
					cancel = true;
					break;
				}

				//Cancel if one of their towns is revolting against you
				if (siege.getTown().hasNation()
						&& TownyAPI.getInstance().getTownNationOrNull(siege.getTown()) == enemyNation
						&& siege.getDefender() == nation) {
					cancel = true;
					break;
				}
			}
		}

		if(cancel) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_cannot_remove_enemy"));
		}
	}

	/**
	 * Prevents nations using /n toggle neutral
	 * - Because in SW this is not supported
	 * 
	 * @param event the event
	 */
	@EventHandler(ignoreCancelled = true)
	public void on(NationToggleNeutralEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if (event.getFutureState()) {
			event.setCancelled(true);
			event.setCancelMessage(Translatable.of("msg_err_nation_neutrality_not_supported").forLocale(event.getSender()));
		}
	}

	public void onNationChangeKingEvent(NationKingChangeEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		Town oldCapital = event.getOldKing().getTownOrNull();
		Town newCapital = event.getNewKing().getTownOrNull();
		if (SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeBesiegedCapitalsCannotChangeKing()
			&& event.isCapitalChange()
			&& (SiegeController.hasSiege(oldCapital) || SiegeController.hasSiege(newCapital))) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_besieged_capital_cannot_change_king"));
		}
	}

	/**
	 * In SiegeWar, occupied towns cannot leave their nation in the normal way.
	 * Intead they must be either kicked, or win a revolt siege.
	 *
	 * @param event the nation town pre leave event
	 */
	@EventHandler
	public void onTownAttemptsToLeaveNation(NationPreTownLeaveEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if(TownOccupationController.isTownOccupied(event.getTown())) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("msg_err_occupied_towns_cannot_leave_their_nations"));
		}
	}

}
