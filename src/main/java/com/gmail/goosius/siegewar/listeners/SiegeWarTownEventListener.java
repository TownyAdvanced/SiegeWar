package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeRemoveReason;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.BattleSession;
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
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.towny.object.WorldCoord;
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

	@EventHandler(ignoreCancelled = true)
	public void onTownGoesToRuin(TownRuinedEvent event) {
		//Remove siege if town has one
		if (SiegeController.hasSiege(event.getTown()))
			SiegeController.removeSiege(SiegeController.getSiege(event.getTown()), SiegeRemoveReason.TOWN_RUIN);
		//Remove occupier if town has one
		if (TownOccupationController.isTownOccupied(event.getTown()))
			TownOccupationController.removeTownOccupation(event.getTown());
	}
	
	/*
	 * If town is under siege, town cannot recruit new members
	 */
	@EventHandler(ignoreCancelled = true)
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
	@EventHandler(ignoreCancelled = true)
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
	@EventHandler(ignoreCancelled = true)
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
	@EventHandler(ignoreCancelled = true)
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
	 * If town is peaceful (configurable), sieged, or occupied, it can't move homeblock.
	 * otherwise the move homeblock command could be / definitely would be
	 * used by players as an easy exploit to escape occupation.
	 * 
	 * If a guardian town moves its homeblock, all peaceful towns it was guarding, are released.
	 * NOTE: As per the "simplicity" theme of SW.2.0.0, 
	 * this is preferred over the alternative scheme of keeping the qualified towns and releasing the disqualified towns.
	 * 
	 */
	@EventHandler(ignoreCancelled = true)
	public void on(TownPreSetHomeBlockEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Translator translator = Translator.locale(event.getPlayer());
			if(SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
				&& SiegeWarSettings.arePeacefulTownsNotAllowedToMoveHomeBlock()
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
	@EventHandler(ignoreCancelled = true)
	public void onDeleteTown(DeleteTownEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if (SiegeController.hasSiege(event.getTownUUID()))
			SiegeController.removeSiege(SiegeController.getSiegeByTownUUID(event.getTownUUID()), SiegeRemoveReason.TOWN_DELETE);
	}

	@EventHandler(ignoreCancelled = true)
	public void onTownMerge(TownPreMergeEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if (SiegeController.hasSiege(event.getSuccumbingTown())) {
			event.setCancelMessage(Translation.of("msg_err_cannot_merge_towns"));
			event.setCancelled(true);
		}

		if (TownOccupationController.isTownOccupied(event.getSuccumbingTown())) {
			if(!(TownOccupationController.isTownOccupied(event.getRemainingTown()) && event.getSuccumbingTown().getNationOrNull().equals(event.getRemainingTown().getNationOrNull()))) {
				event.setCancelMessage(Translation.of("msg_err_cannot_merge_towns_without_same_occupier"));
				event.setCancelled(true);
			}
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
	@EventHandler(ignoreCancelled = true)
	public void onTownIsAboutToPayRegularTaxToNation(PreTownPaysNationTaxEvent event) {
		if(!SiegeWarSettings.getWarSiegeEnabled())
			return;

		if(TownOccupationController.isTownOccupied(event.getTown())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTownRankGivenToPlayer(TownAddResidentRankEvent event) {
		if (!SiegeWarSettings.arePeacefulTownsNotAllowedToAssignMilitaryRanks())
			return;

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
	 * If the town's nation has an active offensive siege and a battle session is active, CANCEL town spawn event to outposts
	 *
	 * Lowest priority so that more important features like the siegezone-tp-block can can take precedence 
	 *
	 * @param event town spawn event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void on(TownSpawnEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()) {
			Town toTown = event.getToTown();

			Nation nation = toTown.getNationOrNull();
			TownBlock tb = WorldCoord.parseWorldCoord(event.getTo()).getTownBlockOrNull();
			if (nation == null || tb == null)
				return;
			//If enabled in config, prevent teleportation to outposts during offensive siege while battle session is active.
			if (!SiegeController.getActiveOffensiveSieges(nation).isEmpty()
					&& tb.isOutpost()
					&& SiegeWarSettings.getWarSiegeOutpostTeleportationDisabled() && BattleSession.getBattleSession().isActive()) {
				Translator translator = Translator.locale(event.getPlayer());

				event.setCancelMessage(translator.of("siegewar_plugin_prefix") + translator.of("msg_err_cannot_spawn_outpost_during_battle_session"));
				event.setCancelled(true); //Stop the teleport

			}
		}
	}
}
