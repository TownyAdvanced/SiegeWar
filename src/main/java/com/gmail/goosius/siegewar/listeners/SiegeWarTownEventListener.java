package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.utils.SiegeWarTownOccupationUtil;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.time.dailytaxes.PreTownPaysNationTaxEvent;
import com.palmergames.bukkit.towny.event.town.TownPreMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownMapColourNationalCalculationEvent;
import com.palmergames.bukkit.towny.event.town.TownPreSetHomeBlockEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.TimeMgmt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
		if (SiegeWarTownOccupationUtil.isTownOccupied(event.getTown()))
			SiegeWarTownOccupationUtil.removeTownOccupation(event.getTown());
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
			SiegeWarTownPeacefulnessUtil.setDesiredTownPeacefulness(town, TownySettings.getTownDefaultNeutral());
			town.save();
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
		if (SiegeWarSettings.getWarCommonOccupiedTownUnClaimingDisabled() && SiegeWarTownOccupationUtil.isTownOccupied(event.getTown())) {
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
				&& SiegeWarTownPeacefulnessUtil.isTownPeaceful(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_peaceful_town_cannot_move_homeblock"));
			}

			if(SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("siegewar_plugin_prefix") + Translation.of("msg_err_besieged_town_cannot_move_homeblock"));
			}

			if(SiegeWarTownOccupationUtil.isTownOccupied(event.getTown())) {
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
		if(SiegeWarTownOccupationUtil.isTownOccupied(event.getTown())) {
			String mapColorHexCode = SiegeWarTownOccupationUtil.getTownOccupier(event.getTown()).getMapColorHexCode();
			event.setMapColorHexCode(mapColorHexCode);
		}
	}

	/**
	 * Prevents towns using /t toggle neutral
	 * - Because in SW this is not supported
	 *
	 * @param event the event
	 */
	@EventHandler(ignoreCancelled = true)
	public void on(TownToggleNeutralEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if (event.getFutureState()) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("msg_err_town_neutrality_not_supported"));
		}
	}

	/**
	 * In SiegeWar, occupied towns do not pay their nation's regular tax
	 * (Instead they pay the separate nation's occupation tax)
	 *
	 * @param event the pre-tax event
	 */
	@EventHandler
	public void onTownIsAboutToPayRegularTaxToNation(PreTownPaysNationTaxEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if(SiegeWarTownOccupationUtil.isTownOccupied(event.getTown())) {
			event.setCancelled(true);
		}
	}

}
