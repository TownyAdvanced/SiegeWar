package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.NationBonusCalculationEvent;
import com.palmergames.bukkit.towny.event.NationPreRemoveEnemyEvent;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NationPreAddTownEvent;
import com.palmergames.bukkit.towny.event.nation.NationRankAddEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;
import com.palmergames.bukkit.towny.event.nation.NationListDisplayedNumOnlinePlayersCalculationEvent;
import com.palmergames.bukkit.towny.event.nation.NationListDisplayedNumTownsCalculationEvent;
import com.palmergames.bukkit.towny.event.nation.NationListDisplayedNumResidentsCalculationEvent;
import com.palmergames.bukkit.towny.event.nation.NationListDisplayedNumTownBlocksCalculationEvent;
import com.palmergames.bukkit.towny.event.nation.DisplayedNationsListSortEvent;
import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.townblockstatus.NationZoneTownBlockStatusEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

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
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		/*
		 * Adjust sieges if needed
		 */
		for (Siege siege : SiegeController.getSieges()) {
			switch(siege.getSiegeType()) {
				case CONQUEST:
				case SUPPRESSION:
					/* 
					 * Conquest or Suppression:
					 * If attacker (which is a nation) disappears, we must delete the siege
					 */
					if(event.getNationUUID() == siege.getAttacker().getUUID()) {
						SiegeController.removeSiege(siege, SiegeSide.DEFENDERS);
					}
					break;
				case LIBERATION:									
					/*
					 * Liberation:
					 * If attacker (which is a nation) disappears, we must delete the siege
					 * If defender (which is a nation) disappears,
					 *    we must ensure that the attacker does not lose any progress in the siege.
					 *    We do this by transforming the siege into a conquest siege
					 */
					if(event.getNationUUID() == siege.getAttacker().getUUID()) { 
						SiegeController.removeSiege(siege, SiegeSide.DEFENDERS);
						break;
			
					} else if (event.getNationUUID() == siege.getDefender().getUUID()) { 
						siege.setSiegeType(SiegeType.CONQUEST);
						siege.setDefender(siege.getTown());
						SiegeController.saveSiege(siege);
						break;
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

		/*
		 * Remove any town occupation data associated with that nation
		 */
		TownOccupationController.removeForeignTownOccupations(TownyAPI.getInstance().getNation(event.getNationUUID()));
		
		//Award nation refund
		event.getNationUUID();
		Resident king = event.getLeader();
		if (king != null) {
			SiegeWarMoneyUtil.makeNationRefundAvailable(king);
		}
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

	/**
	 * Updates the number of bonus blocks when Towny calculates it
	 *
	 * All unoccupied home towns are counted
	 * All occupied foreign towns are counted
	 */
	@EventHandler
	public void on(NationBonusCalculationEvent event) {
		Nation effectiveNation = SiegeWarNationUtil.getEffectiveNation(event.getNation());
		event.setBonusBlocks(TownySettings.getNationLevel(effectiveNation).townBlockLimitBonus());
	}

	/**
	 * Update the nation numresidents calculation when towny displays the nations list
	 *
	 * All unoccupied home towns are counted
	 * All occupied foreign towns are counted
	 */
	@EventHandler
	public void on(NationListDisplayedNumResidentsCalculationEvent event) {
		Nation effectiveNation = SiegeWarNationUtil.getEffectiveNation(event.getNation());
		event.setDisplayedValue(effectiveNation.getNumResidents());
	}

	/**
	 * Update the nation numtowns calculation when towny displays the nations list
	 *
	 * All unoccupied home towns are counted
	 * All occupied foreign towns are counted
	 */
	@EventHandler
	public void on(NationListDisplayedNumTownsCalculationEvent event) {
		Nation effectiveNation = SiegeWarNationUtil.getEffectiveNation(event.getNation());
		event.setDisplayedValue(effectiveNation.getNumTowns());
	}

	/**
	 * Update the nation numtownblocks calculation when towny displays the nations list
	 *
	 * All unoccupied home towns are counted
	 * All occupied foreign towns are counted
	 */
	@EventHandler
	public void on(NationListDisplayedNumTownBlocksCalculationEvent event) {
		Nation effectiveNation = SiegeWarNationUtil.getEffectiveNation(event.getNation());
		event.setDisplayedValue(effectiveNation.getNumTownblocks());
	}

	/**
	 * Update the nation onlineplayers calculation when towny displays the nations list
	 *
	 * All unoccupied home towns are counted
	 * All occupied foreign towns are counted
	 */
	@EventHandler
	public void on(NationListDisplayedNumOnlinePlayersCalculationEvent event) {
		int effectiveNumOnlinePlayers = 0;
		Resident resident;
		Nation effectiveNation = SiegeWarNationUtil.getEffectiveNation(event.getNation());
		for(Player player: Bukkit.getOnlinePlayers()) {
			if(TownyUniverse.getInstance().hasResident(player.getUniqueId())) {
				resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
				if(resident.hasNation() && effectiveNation.getTowns().contains(TownyAPI.getInstance().getResidentTownOrNull(resident)))
					effectiveNumOnlinePlayers++;
			}
		}
		event.setDisplayedValue(effectiveNumOnlinePlayers);
	}

	/**
	 * Override Towny's prevention of occupied towns leaving
	 */
	@EventHandler
	public void onTownTriesToLeaveNation(NationPreTownLeaveEvent event) {
		// Towny will cancel the leaving on lowest priority if the town is conquered.
		// We want to un-cancel it.
		if (event.isCancelled())
			event.setCancelled(false);
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
	 * Re-Sorts the nations list when Towny sorts it
	 * 
	 * - Towny uses this list for the /n list display
	 * 
	 * - SiegeWar re-orders it to account for town occupation
	 *   - Unoccupied towns are counted as part of their natural nation
	 *   - Occupied towns are counted as part of the occupying nation
	 */
	@EventHandler
	public void on(DisplayedNationsListSortEvent event) {
		//Get originally sorted list
		List<Nation> nationList = event.getNations();
		//Re-sort list, taking occupation into account
		switch (event.getComparatorType()) {
			case RESIDENTS:
				nationList.sort(SiegeWarNationUtil.BY_NUM_RESIDENTS);
				break;
			case TOWNBLOCKS:
				nationList.sort(SiegeWarNationUtil.BY_NUM_TOWNBLOCKS);
				break;
			case ONLINE:
				nationList.sort(SiegeWarNationUtil.BY_NUM_ONLINE_PLAYERS);
				break;
			case TOWNS:
				nationList.sort(SiegeWarNationUtil.BY_NUM_TOWNS);
				break;
			default:
				return;	
		}
		//Give the re-sorted list to the event object
		event.setNations(nationList);
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
