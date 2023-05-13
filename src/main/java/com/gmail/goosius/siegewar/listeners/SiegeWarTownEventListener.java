package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentRankEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.TownSpawnEvent;
import com.palmergames.bukkit.towny.event.time.dailytaxes.PreTownPaysNationTaxEvent;
import com.palmergames.bukkit.towny.event.town.TownPreMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownPreSetHomeBlockEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.util.TimeMgmt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
			SiegeController.removeSiege(SiegeController.getSiege(event.getTown()));
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
		}
	}

	/*
	 * Upon creation of a town, the Towny town neutrality setting is wiped
	 * The SW peacefulness setting may then get applied
	 * Also the SW siege immunity time is set
	 */
	@EventHandler
	public void onCreateNewTown(NewTownEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Town town = event.getTown();
			town.setNeutral(false);
			if(SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
				SiegeWarTownPeacefulnessUtil.setTownPeacefulness(town, SiegeWarSettings.getNewTownPeacefulness());
				SiegeWarTownPeacefulnessUtil.setDesiredTownPeacefulness(town, SiegeWarSettings.getNewTownPeacefulness());
			}
			TownMetaDataController.setSiegeImmunityEndTime(town, System.currentTimeMillis() + (long)(SiegeWarSettings.getSiegeImmunityNewTownsHours() * TimeMgmt.ONE_HOUR_IN_MILLIS));
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
		Translator translator = Translator.locale(event.getResident().getPlayer());
		if (SiegeWarSettings.getWarCommonOccupiedTownUnClaimingDisabled() && TownOccupationController.isTownOccupied(event.getTown())) {
			event.setCancelled(true);
			event.setCancelMessage(translator.of("siegewar_plugin_prefix") + translator.of("msg_err_war_common_occupied_town_cannot_unclaim"));
			return;
		}

		if(SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeBesiegedTownUnClaimingDisabled()) {

			//Town besieged
			if(SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(translator.of("siegewar_plugin_prefix") + translator.of("msg_err_siege_besieged_town_cannot_unclaim"));
				return;
			}
		}
	}

	/*
	 * If town is peaceful, sieged, or occupied, it can't move homeblock.
	 * otherwise the move homeblock command could be / definitely would be
	 * used by players as an easy exploit to escape occupation.
	 * 
	 * If a guardian town moves its homeblock, all peaceful towns it was guarding, are released.
	 * NOTE: As per the "simplicity" theme of SW.2.0.0, 
	 * this is preferred over the alternative scheme of keeping the qualified towns and releasing the disqualified towns.
	 * 
	 */
	@EventHandler
	public void on(TownPreSetHomeBlockEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Translator translator = Translator.locale(event.getPlayer());
			if(SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
				&& SiegeWarTownPeacefulnessUtil.isTownPeaceful(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(translator.of("siegewar_plugin_prefix") + translator.of("msg_err_peaceful_town_cannot_move_homeblock"));
			}

			if(SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(translator.of("siegewar_plugin_prefix") + translator.of("msg_err_besieged_town_cannot_move_homeblock"));
			}

			if(TownOccupationController.isTownOccupied(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(translator.of("siegewar_plugin_prefix") + translator.of("msg_err_occupied_town_cannot_move_homeblock"));
			}
			
			if(event.getTown().hasNation()) {
				int numPeacefulTownsReleased = SiegeWarTownPeacefulnessUtil.releasePeacefulTownsOnGuardianTownHomeBlockMove(event.getTown());
				if(numPeacefulTownsReleased > 0) {
					Translatable message = Translatable.of("msg_peaceful_towns_released_on_homeblock_move", event.getTown().getName(), numPeacefulTownsReleased);
					TownyMessaging.sendPrefixedNationMessage(event.getTown().getNationOrNull(), message);
				}
			}
		}
	}

	/*
	 * A town being deleted with a siege means the siege ends.
	 */
	@EventHandler
	public void onDeleteTown(DeleteTownEvent event) {
		if (SiegeController.hasSiege(event.getTownUUID()))
			SiegeController.removeSiege(SiegeController.getSiegeByTownUUID(event.getTownUUID()));
	}

	@EventHandler
	public void onTownMerge(TownPreMergeEvent event) {
		if (SiegeController.hasSiege(event.getSuccumbingTown())) {
			event.setCancelMessage(Translation.of("msg_err_cannot_merge_towns"));
			event.setCancelled(true);
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
			event.setCancelMessage(Translatable.of("msg_err_town_neutrality_not_supported").forLocale(event.getPlayer()));
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

		if(TownOccupationController.isTownOccupied(event.getTown())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onTownRankGivenToPlayer(TownAddResidentRankEvent event) {
		//In Siegewar, if target town is peaceful or occupied, can't add military rank
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& PermissionUtil.doesTownRankAllowPermissionNode(event.getRank(), SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS)) {

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


	/**
	 * If this is a peaceful town, UN-CANCEL the town spawn event
	 *
	 * @param event town spawn event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void on(TownSpawnEvent event) {
		SiegeWar.info("TTT");

		SiegeWar.info("Cancelled: " + event.isCancelled());

		if(SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
				&& SiegeWarSettings.isPeacefulTownPublicSpawnEnabled()) {
			if (event.isCancelled()
					&& SiegeWarTownPeacefulnessUtil.isTownPeaceful(event.getToTown())
					&& event.getToTown().isPublic()) {
				event.setCancelled(false); //UN-Cancel the event
			}
		}
	}

}
