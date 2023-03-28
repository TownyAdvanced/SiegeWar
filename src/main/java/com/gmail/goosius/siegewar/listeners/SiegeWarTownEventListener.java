package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.town.*;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarTownEventListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarTownEventListener(SiegeWar instance) {

		plugin = instance;
	}

	@EventHandler
	public void onTownGoesToRuin(TownRuinedEvent event) {
		//Remove siege if town has one
		if (SiegeController.hasSiege(event.getTown()))
			SiegeController.removeSiege(SiegeController.getSiege(event.getTown()), SiegeSide.ATTACKERS);
		//Remove occupier if town has one
		if (TownOccupationController.isTownOccupied(event.getTown()))
			TownOccupationController.removeTownOccupation(event.getTown());
	}
	
	/*
	 * If town is under siege, town cannot recruit new members
	 */
	@EventHandler
	public void onTownAddResident(TownPreAddResidentEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeBesiegedTownRecruitmentDisabled()) {

			if (SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_recruit"));
				return;
			}

			//Cannot recruit if nation is fighting a home-defence war
			if (SiegeWarSettings.isNationSiegeImmunityEnabled()
					&& SiegeController.isTownsNationFightingAHomeDefenceWar(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_affected_home_nation_town_cannot_recruit"));
				return;
			}
		}
	}

	/*
	 * Upon creation of a town, towns can be set to neutral.
	 */
	@EventHandler
	public void onCreateNewTown(NewTownEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Town town = event.getTown();
			TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + (long)(SiegeWarSettings.getWarSiegeSiegeImmunityTimeNewTownsHours() * TimeMgmt.ONE_HOUR_IN_MILLIS));
			TownMetaDataController.setDesiredPeacefulnessSetting(town, TownySettings.getTownDefaultNeutral());
			town.save();
		}
	}

	/*
	 * On toggle neutral, SW will evaluate a number of things.
	 */
	@EventHandler (ignoreCancelled = true)
	public void onTownToggleNeutral(TownToggleNeutralEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		
		if(!SiegeWarSettings.getWarCommonPeacefulTownsEnabled() && !event.isAdminAction()) {
			event.setCancelMessage(Translation.of("msg_err_command_disable"));
			event.setCancelled(true);
			return;
		}
		
		// Check if we're becoming peaceful, a capital city and capitals cannot become peaceful.
		if (event.getFutureState() && event.getTown().isCapital() 
				&& !SiegeWarSettings.capitalsAllowedTownPeacefulness() && !event.isAdminAction()) {
			event.setCancelMessage(Translation.of("msg_err_command_disable"));
			event.setCancelled(true);
			return;
		}
		
		Town town = event.getTown();
		
		if (event.isAdminAction()) {
			TownMetaDataController.setDesiredPeacefulnessSetting(town, event.getFutureState());
			TownMetaDataController.setPeacefulnessChangeDays(town, 0);
			return;
		} else {
			int days;
			if(System.currentTimeMillis() < (town.getRegistered() + (TimeMgmt.ONE_DAY_IN_MILLIS * 7))) {
				days = SiegeWarSettings.getWarCommonPeacefulTownsNewTownConfirmationRequirementDays();
			} else {
				days = SiegeWarSettings.getWarCommonPeacefulTownsConfirmationRequirementDays();
			}
			
			if (TownMetaDataController.getPeacefulnessChangeConfirmationCounterDays(town) == 0) {
				
				//Here, no countdown is in progress, and the town wishes to change peacefulness status
				TownMetaDataController.setDesiredPeacefulnessSetting(town, !town.isNeutral());
				TownMetaDataController.setPeacefulnessChangeDays(town, days);
				
				//Send message to town
				if (TownMetaDataController.getDesiredPeacefulnessSetting(town))
					TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_declared_peaceful"), days));
				else
					TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_declared_non_peaceful"), days));
				
				//Remove any military nation ranks of residents
				for(Resident peacefulTownResident: town.getResidents()) {
					for (String nationRank : new ArrayList<>(peacefulTownResident.getNationRanks())) {
						if (PermissionUtil.doesNationRankAllowPermissionNode(nationRank, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS)) {
							peacefulTownResident.removeNationRank(nationRank);
						}
					}
				}
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("status_town_peacefulness_status_change_timer", days));
				event.setCancelled(true);
				
			} else {
				//Here, a countdown is in progress, and the town wishes to cancel the countdown,
				TownMetaDataController.setDesiredPeacefulnessSetting(town, town.isNeutral());
				TownMetaDataController.setPeacefulnessChangeDays(town, 0);
				//Send message to town
				TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_peacefulness_countdown_cancelled")));				
				event.setCancelMessage(Translation.of("msg_war_common_town_peacefulness_countdown_cancelled"));
				event.setCancelled(true);
			}
		}
	}

	/*
	 * Upon attempting to claim land, SW will stop it under some conditions.
	 */
	@EventHandler
	public void onTownClaim(TownPreClaimEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if (SiegeWarSettings.getWarSiegeBesiegedTownClaimingDisabled()) {

				//If the claimer's town is under siege, they cannot claim any land
				if (SiegeController.hasActiveSiege(event.getTown())) {
					event.setCancelled(true);
					event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_claim"));
					return;
				}

				//If the town is fighting a home-defence war, they cannot claim any land
				if (SiegeWarSettings.isNationSiegeImmunityEnabled()
						&& SiegeController.isTownsNationFightingAHomeDefenceWar(event.getTown())) {
					event.setCancelled(true);
					event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_affected_home_nation_town_cannot_claim"));
					return;
				}
			}

			//If the land is too near any active siege zone, it cannot be claimed.
			if(SiegeWarSettings.getWarSiegeClaimingDisabledNearSiegeZones()) {
				for(Siege siege: SiegeController.getSieges()) {
					try {
						if (siege.getStatus().isActive()
							&& SiegeWarDistanceUtil.isInSiegeZone(event.getPlayer(), siege)) {
							event.setCancelled(true);
							event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_claim_too_near_siege_zone"));
							break;
						}
					} catch (Exception e) {
						//Problem with this particular siegezone. Ignore siegezone
						try {
							SiegeWar.severe("Problem with verifying claim against the following siege zone" + siege.getTown().getName() + ". Claim allowed.");
						} catch (Exception e2) {
							SiegeWar.severe("Problem with verifying claim against a siege zone (name could not be read). Claim allowed");
						}
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/*
	 * Siege War will prevent unclaiming land in some situations.
	 */
	@EventHandler
	public void onTownUnclaim(TownPreUnclaimCmdEvent event) {
		if (SiegeWarSettings.getWarCommonOccupiedTownUnClaimingDisabled() && TownOccupationController.isTownOccupied(event.getTown())) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_war_common_occupied_town_cannot_unclaim"));
			return;
		}
			
		if(SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeBesiegedTownUnClaimingDisabled()) {

			//Town besieged
			if(SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_unclaim"));
				return;
			}

			//Town fighting a home-defence war
			if (SiegeWarSettings.isNationSiegeImmunityEnabled()
					&& SiegeController.isTownsNationFightingAHomeDefenceWar(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_siege_affected_home_nation_town_cannot_unclaim"));
				return;
			}
		}
	}

	/*
	 * If town is peaceful, sieged, or occupied, it can't move homeblock.
	 * otherwise the move homeblock command could be / definitely would be
	 * used by players as an easy and hard-to-moderate exploit to escape occupation.
	 */
	@EventHandler
	public void on(TownPreSetHomeBlockEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if(SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
				&& event.getTown().isNeutral()) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_peaceful_town_cannot_move_homeblock"));
			}

			if(SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_besieged_town_cannot_move_homeblock"));
			}

			if(TownOccupationController.isTownOccupied(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_occupied_town_cannot_move_homeblock"));
			}
		}
	}

	/*
	 * A town being deleted with a siege means the siege ends.
	 */
	@EventHandler
	public void onDeleteTown(DeleteTownEvent event) {
		if (SiegeController.hasSiege(event.getTownUUID()))
			SiegeController.removeSiege(SiegeController.getSiegeByTownUUID(event.getTownUUID()), SiegeSide.ATTACKERS);
	}

	@EventHandler
	public void onTownMerge(TownPreMergeEvent event) {
		if (SiegeController.hasSiege(event.getSuccumbingTown())) {
			event.setCancelMessage(Translation.of("msg_err_cannot_merge_towns"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void on(TownMapColourNationalCalculationEvent event) {
		if(TownOccupationController.isTownOccupied(event.getTown())) {
			String mapColorHexCode = TownOccupationController.getTownOccupier(event.getTown()).getMapColorHexCode();
			event.setMapColorHexCode(mapColorHexCode);
		}
	}

}
