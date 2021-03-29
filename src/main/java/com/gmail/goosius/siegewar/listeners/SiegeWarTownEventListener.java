package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.PermissionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.event.town.TownPreMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownUnconquerEvent;
import com.palmergames.bukkit.towny.event.town.TownMapColourCalculationEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleOpenEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownTogglePVPEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.TimeMgmt;

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
		if (SiegeController.hasSiege(event.getTown()))
			SiegeController.removeSiege(SiegeController.getSiege(event.getTown()), SiegeSide.ATTACKERS);
	}
	
	/*
	 * If town is under siege, town cannot recruit new members
	 */
	@EventHandler
	public void onTownAddResident(TownPreAddResidentEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeBesiegedTownRecruitmentDisabled()) {

			if (SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_recruit"));
				return;
			}

			if (SiegeWarSettings.isNationSiegeEffectsEnabled()
					&& SiegeController.isNationASiegeDefender(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_affected_town_cannot_recruit"));
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
	 * On toggle pvp, SW can stop a town toggling pvp.
	 */
	@EventHandler
	public void onTownTogglePVP(TownTogglePVPEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			if (SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns()) {

				//Is the town under siege
				if (SiegeController.hasActiveSiege(event.getTown())) {
					event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_toggle_pvp"));
					event.setCancelled(true);
					return;
				}

				//Is the town affected by nation siege effects
				if(SiegeWarSettings.isNationSiegeEffectsEnabled()
						&& SiegeController.isNationASiegeDefender(event.getTown())) {
					event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_affected_nation_town_cannot_toggle_pvp"));
					event.setCancelled(true);
					return;
				}
			}

			if (SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
					&& !SiegeWarSettings.getWarCommonPeacefulTownsAllowedToTogglePVP()
					&& event.getTown().isNeutral()
					&& !event.getTown().isPVP()) {
				event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("msg_err_peaceful_town_pvp_forced_off"));
				event.setCancelled(true);
				return;
			}
		}
	}
	
	/*
	 * On toggle open, SW will stop a town toggling open.
	 */
	@EventHandler
	public void onTownToggleOpen(TownToggleOpenEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeBesiegedTownRecruitmentDisabled()) {

			//If town is besieged
			if(SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_toggle_open_off"));
				event.setCancelled(true);
				return;
			}

			//Is the town affected by nation siege effects
			if(SiegeWarSettings.isNationSiegeEffectsEnabled()
					&& SiegeController.isNationASiegeDefender(event.getTown())) {
				event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_affected_town_cannot_toggle_open_off"));
				event.setCancelled(true);
				return;
			}
		}
	}
	
	/*
	 * On toggle neutral, SW will evaluate a number of things.
	 */
	@EventHandler
	public void onTownToggleNeutral(TownToggleNeutralEvent event) {
		if (!SiegeWarSettings.getWarSiegeEnabled())
			return;
		
		if(!SiegeWarSettings.getWarCommonPeacefulTownsEnabled()) {
			event.setCancellationMsg(Translation.of("msg_err_command_disable"));
			event.setCancelled(true);
			return;
		}
		
		Town town = event.getTown();
		
		if (event.isAdminAction()) {
			TownMetaDataController.setDesiredPeacefulnessSetting(town, event.getFutureState());
			TownMetaDataController.setPeacefulnessChangeDays(town, 0);
			if (event.getFutureState() == true)
				SiegeWarTownUtil.disableTownPVP(town);
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
							try {
								peacefulTownResident.removeNationRank(nationRank);
							} catch (NotRegisteredException ignored) {}
						}
					}
				}
				event.setCancellationMsg(Translation.of("plugin_prefix") + Translation.of("status_town_peacefulness_status_change_timer", days));
				event.setCancelled(true);
				
			} else {
				//Here, a countdown is in progress, and the town wishes to cancel the countdown,
				TownMetaDataController.setDesiredPeacefulnessSetting(town, town.isNeutral());
				TownMetaDataController.setPeacefulnessChangeDays(town, 0);
				//Send message to town
				TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_peacefulness_countdown_cancelled")));				
				event.setCancellationMsg(Translation.of("msg_war_common_town_peacefulness_countdown_cancelled"));
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
					event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_claim"));
					return;
				}

				//If the town is affected by nation siege effects, they cannot claim any land
				if (SiegeWarSettings.isNationSiegeEffectsEnabled()
						&& SiegeController.isNationASiegeDefender(event.getTown())) {
					event.setCancelled(true);
					event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_affected_town_cannot_claim"));
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
							event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_claim_too_near_siege_zone"));
							break;
						}
					} catch (Exception e) {
						//Problem with this particular siegezone. Ignore siegezone
						try {
							System.out.println("Problem with verifying claim against the following siege zone" + siege.getTown().getName() + ". Claim allowed.");
						} catch (Exception e2) {
							System.out.println("Problem with verifying claim against a siege zone (name could not be read). Claim allowed");
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
			event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_war_common_occupied_town_cannot_unclaim"));
			return;
		}
			
		if(SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarSiegeBesiegedTownUnClaimingDisabled()
			&& SiegeController.hasSiege(event.getTown())
			&& (
				SiegeController.getSiege(event.getTown()).getStatus().isActive()
				|| SiegeController.getSiege(event.getTown()).getStatus() == SiegeStatus.ATTACKER_WIN
				|| SiegeController.getSiege(event.getTown()).getStatus() == SiegeStatus.DEFENDER_SURRENDER
				)
			)
		{
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_besieged_town_cannot_unclaim"));
		}
	}
	
	/*
	 * Simply saving the siege will set the name of the siege.
	 */
	@EventHandler
	public void onTownRename(RenameTownEvent event) {
		if (SiegeController.hasSiege(event.getTown())) {
			SiegeController.saveSiege(SiegeController.getSiege(event.getTown()));
			SiegeController.renameSiegedTownName(event.getOldName(), event.getTown().getName());
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

	/*
	 * SiegeWar will add lines to towns which have a siege
	 */
	@EventHandler
	public void onTownStatusScreen(TownStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			List<String> out = new ArrayList<>();
			Town town = event.getTown();

			//Occupying Nation: Empire of the Fluffy Bunnies
			if(SiegeWarSettings.getWarSiegeInvadeEnabled() && TownOccupationController.isTownOccupied(town)) {
				Nation townOccupier = TownOccupationController.getTownOccupier(town);
				out.add(Translation.of("status_town_occupying_nation", townOccupier.getFormattedName()));
			}
			
	        //Revolt Immunity Timer: 71.8 hours
	        if (SiegeWarSettings.getRevoltSiegesEnabled() && System.currentTimeMillis() < TownMetaDataController.getRevoltImmunityEndTime(town)) {
	        	String time = TimeMgmt.getFormattedTimeValue(TownMetaDataController.getRevoltImmunityEndTime(town)- System.currentTimeMillis());        	
	            out.add(Translation.of("status_town_revolt_immunity_timer", time));
	        }

	        if (SiegeController.hasSiege(town)) {
				Siege siege = SiegeController.getSiege(town);
				SiegeStatus siegeStatus= siege.getStatus();
				String time = TimeMgmt.getFormattedTimeValue(TownMetaDataController.getSiegeImmunityEndTime(town)- System.currentTimeMillis());

				//Siege:
				out.add(Translation.of("status_town_siege"));

				// > Type: Conquest
				out.add(Translation.of("status_town_siege_type", siege.getSiegeType().getName()));

				// > Status: In Progress
				out.add(Translation.of("status_town_siege_status", getStatusTownSiegeSummary(siege)));

				// > Attacker: Land of Darkness (Nation)
				out.add(Translation.of("status_town_siege_attacker", siege.getAttacker().getFormattedName()));

				// > Defender: Land of Light (Nation)
				out.add(Translation.of("status_town_siege_defender", siege.getDefendingNationIfPossibleElseTown().getFormattedName()));

				switch (siegeStatus) {
	                case IN_PROGRESS:
						// > Balance: 530
						out.add(Translation.of("status_town_siege_status_siege_balance", siege.getSiegeBalance()));

						if(SiegeWarSettings.isBannerXYZTextEnabled()) {
							// > Banner XYZ: {2223,82,9877}
							out.add(
									Translation.of("status_town_siege_status_banner_xyz",
											siege.getFlagLocation().getBlockX(),
											siege.getFlagLocation().getBlockY(),
											siege.getFlagLocation().getBlockZ())
							);
						}

						// >  Victory Timer: 5.3 hours
						String victoryTimer = Translation.of("status_town_siege_victory_timer", siege.getFormattedHoursUntilScheduledCompletion());
						out.add(victoryTimer);

						// >  War Chest: $12,800
						String warChest = TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount());
						out.add(Translation.of("status_town_siege_status_warchest", warChest));

						if(BattleSession.getBattleSession().isActive()
							&& (siege.getAttackerBattlePoints() > 0
								|| siege.getDefenderBattlePoints() > 0
								|| siege.getBannerControllingSide() != SiegeSide.NOBODY
								|| siege.getBannerControlSessions().size() > 0)) {

							//Battle:
							String battle = Translation.of("status_town_siege_battle");
							out.add(battle);

							// > Banner Control: Attackers [4] Killbot401x, NerfeyMcNerferson, WarCriminal80372
							if (siege.getBannerControllingSide() == SiegeSide.NOBODY) {
								out.add(Translation.of("status_town_banner_control_nobody", siege.getBannerControllingSide().getFormattedName()));
							} else {
								String[] bannerControllingResidents = TownyFormatter.getFormattedNames(siege.getBannerControllingResidents());
								if (bannerControllingResidents.length > 34) {
									String[] entire = bannerControllingResidents;
									bannerControllingResidents = new String[36];
									System.arraycopy(entire, 0, bannerControllingResidents, 0, 35);
									bannerControllingResidents[35] = Translation.of("status_town_reslist_overlength");
								}
								out.addAll(ChatTools.listArr(bannerControllingResidents, Translation.of("status_town_banner_control", siege.getBannerControllingSide().getFormattedName(), siege.getBannerControllingResidents().size())));
							}

							// > Points: +90 / -220
							out.add(Translation.of("status_town_siege_battle_points", siege.getFormattedAttackerBattlePoints(), siege.getFormattedDefenderBattlePoints()));

							// > Time Remaining: 22 minutes
							out.add(Translation.of("status_town_siege_battle_time_remaining", BattleSession.getBattleSession().getFormattedTimeRemainingUntilBattleSessionEnds()));
						}
	                    break;

	                case ATTACKER_WIN:
	                case DEFENDER_SURRENDER:
					case DEFENDER_WIN:
					case ATTACKER_ABANDON:
	                    String invadedPlunderedStatus = getInvadedPlunderedStatusLine(siege);
						if(!invadedPlunderedStatus.isEmpty())
							out.add(invadedPlunderedStatus);

	                    String siegeImmunityTimer = Translation.of("status_town_siege_immunity_timer", time);
	                    out.add(siegeImmunityTimer);
	                    break;

	                case PENDING_DEFENDER_SURRENDER:
	                case PENDING_ATTACKER_ABANDON:
					case UNKNOWN:
						break;
	            }
	        } else {
	            if(!SiegeController.hasActiveSiege(town)
	            	&& System.currentTimeMillis() < TownMetaDataController.getSiegeImmunityEndTime(town)) {
	                //Siege:
	                // > Immunity Timer: 40.8 hours
	                out.add(Translation.of("status_town_siege"));
	                String time = TimeMgmt.getFormattedTimeValue(TownMetaDataController.getSiegeImmunityEndTime(town)- System.currentTimeMillis()); 
	                out.add(Translation.of("status_town_siege_immunity_timer", time));
	            }
	        }
	        event.addLines(out);
		}
	}

    private static String getStatusTownSiegeSummary(@NotNull Siege siege) {
        switch (siege.getStatus()) {
            case IN_PROGRESS:
                return Translation.of("status_town_siege_status_in_progress");
            case ATTACKER_WIN:
                return Translation.of("status_town_siege_status_attacker_win");
            case DEFENDER_SURRENDER:
                return Translation.of("status_town_siege_status_defender_surrender");
            case DEFENDER_WIN:
                return Translation.of("status_town_siege_status_defender_win");
            case ATTACKER_ABANDON:
                return Translation.of("status_town_siege_status_attacker_abandon");
            case PENDING_DEFENDER_SURRENDER:
                return Translation.of("status_town_siege_status_pending_defender_surrender", siege.getTimeRemaining());
            case PENDING_ATTACKER_ABANDON:
                return Translation.of("status_town_siege_status_pending_attacker_abandon", siege.getTimeRemaining());
            default:
                return "???";
        }
    }

    private static String getInvadedPlunderedStatusLine(Siege siege) {
		switch(siege.getSiegeType()) {
			case CONQUEST:
			case LIBERATION:
				switch (siege.getStatus()) {
					case ATTACKER_WIN:
					case DEFENDER_SURRENDER:
						return getPlunderStatusLine(siege) + getInvadeStatusLine(siege);
					default:
						break;
				}
				break;
			case SUPPRESSION:
				switch (siege.getStatus()) {
					case ATTACKER_WIN:
					case DEFENDER_SURRENDER:
						return getPlunderStatusLine(siege);
					default:
						break;
				}
				break;
			case REVOLT:
				switch (siege.getStatus()) {
					case DEFENDER_WIN:
					case ATTACKER_ABANDON:
						return getPlunderStatusLine(siege);
					default:
						break;
				}
				break;
		}
		return "";
	}

	private static String getPlunderStatusLine(Siege siege) {
		String plunderedYesNo = siege.isTownPlundered() ? Translation.of("status_yes") : Translation.of("status_no_green");
		return Translation.of("status_town_siege_status_plundered", plunderedYesNo);
	}

	private static String getInvadeStatusLine(Siege siege) {
		if(siege.getSiegeType() == SiegeType.REVOLT && siege.getSiegeType() == SiegeType.SUPPRESSION) {
			return "";
		} else {
			String invadedYesNo = siege.isTownInvaded() ? Translation.of("status_yes") : Translation.of("status_no_green");
			return Translation.of("status_town_siege_status_invaded", invadedYesNo);
		}
	}

    @EventHandler
    public void onTownUnconquer(TownUnconquerEvent event) {
    	if (SiegeWarSettings.getWarSiegeEnabled())
    		event.setCancelled(true);
    }

	@EventHandler
	public void onTownMerge(TownPreMergeEvent event) {
		if (SiegeController.hasSiege(event.getSuccumbingTown())) {
			event.setCancelMessage(Translation.of("msg_err_cannot_merge_towns"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void on(TownMapColourCalculationEvent event) {
		if(TownOccupationController.isTownOccupied(event.getTown())) {
			String mapColorHexCode = TownOccupationController.getTownOccupier(event.getTown()).getMapColorHexCode();
			event.setMapColorHexCode(mapColorHexCode);
		}
	}
}
