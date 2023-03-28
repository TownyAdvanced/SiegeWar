package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NationPreAddTownEvent;
import com.palmergames.bukkit.towny.event.NationPreRemoveEnemyEvent;
import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import com.palmergames.bukkit.towny.event.nation.NationRankAddEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.townblockstatus.NationZoneTownBlockStatusEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
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
		//In Siegewar, if target town is peaceful, can't add military rank
		if(SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
			&& PermissionUtil.doesNationRankAllowPermissionNode(event.getRank(), SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS)
			&& TownyAPI.getInstance().getResidentTownOrNull(event.getResident()).isNeutral()) { // We know that the resident's town will not be null based on the tests already done.
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_war_siege_cannot_add_nation_military_rank_to_peaceful_resident"));
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
		/*
		 * Adjust sieges if needed
		 */
		for (Siege siege : SiegeController.getSieges()) {
			switch(siege.getSiegeType()) {
				case CONQUEST:
					/*
					 * If attacker (which is a nation) disappears, we must delete the siege
					 */
					if(event.getNationUUID() == siege.getAttacker().getUUID()) {
						SiegeController.removeSiege(siege, SiegeSide.DEFENDERS);
					}
					break;
				case REVOLT:
					/*
					 * Revolt
					 * If defender (which is a nation) disappears, we must delete the siege
					 */
					if (event.getNationUUID() == siege.getDefender().getUUID()) {
						SiegeController.removeSiege(siege, SiegeSide.DEFENDERS);
					}
				break;
			}
		}


		//TODO - Add in nation refund
	}

	@EventHandler
	public void onPreNationEnemyRemove(NationPreRemoveEnemyEvent event) {
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

	/*
	 * If nation is fighting a home-defence war it cannot add new towns
	 */
	@EventHandler
	public void on(NationPreAddTownEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeWarSettings.isNationSiegeImmunityEnabled()
				&& SiegeController.isNationFightingAHomeDefenceWar(event.getNation())) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_affected_home_nation_cannot_recruit"));
		}
	}

	/**
	 * Prevents nations using /n toggle neutral
	 * - Because in SW this has no effect
	 * 
	 * @param event the event
	 */
	@EventHandler(ignoreCancelled = true)
	public void on(NationToggleNeutralEvent event) {
		if (event.getFutureState()) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("msg_err_nation_neutrality_not_supported"));
		}
	}

	public void onNationChangeKingEvent(NationKingChangeEvent event) {
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
}
