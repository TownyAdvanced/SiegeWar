package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;

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
import com.gmail.goosius.siegewar.utils.SiegeWarPermissionUtil;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.event.time.dailytaxes.PreTownPaysNationTaxEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleExplosionEvent;
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
		if (SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeWarSettings.getWarSiegeBesiegedTownRecruitmentDisabled()
				&& SiegeController.hasActiveSiege(event.getTown())) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("msg_err_siege_besieged_town_cannot_recruit"));
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
			TownMetaDataController.setDesiredPeacefullnessSetting(town, TownySettings.getTownDefaultNeutral());
			TownyUniverse.getInstance().getDataSource().saveTown(town);
		}
	}
	
	/*
	 * On toggle explosions, SW will stop a town toggling explosions.
	 */
	@EventHandler
	public void onTownToggleExplosion(TownToggleExplosionEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeWarSettings.getWarSiegeExplosionsAlwaysOnInBesiegedTowns()
				&& SiegeController.hasActiveSiege(event.getTown()))  {
			event.setCancellationMsg(Translation.of("msg_err_siege_besieged_town_cannot_toggle_explosions"));
			event.setCancelled(true);
		}
	}
	
	/*
	 * On toggle pvp, SW will stop a town toggling pvp.
	 */
	@EventHandler
	public void onTownTogglePVP(TownTogglePVPEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns()
				&& SiegeController.hasActiveSiege(event.getTown()))  {
			event.setCancellationMsg(Translation.of("msg_err_siege_besieged_town_cannot_toggle_pvp"));
			event.setCancelled(true);
		}
	}
	
	/*
	 * On toggle open, SW will stop a town toggling open.
	 */
	@EventHandler
	public void onTownToggleOpen(TownToggleOpenEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled()
				&& SiegeWarSettings.getWarSiegeBesiegedTownRecruitmentDisabled()
				&& SiegeController.hasActiveSiege(event.getTown())) {
			event.setCancellationMsg(Translation.of("msg_err_siege_besieged_town_cannot_toggle_open_off"));
			event.setCancelled(true);
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
				TownMetaDataController.setDesiredPeacefullnessSetting(town, !town.isNeutral());
				TownMetaDataController.setPeacefulnessChangeDays(town, days);
				
				//Send message to town
				if (TownMetaDataController.getDesiredPeacefulnessSetting(town))
					TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_declared_peaceful"), days));
				else
					TownyMessaging.sendPrefixedTownMessage(town, String.format(Translation.of("msg_war_common_town_declared_non_peaceful"), days));
				
				//Remove any military nation ranks of residents
				for(Resident peacefulTownResident: town.getResidents()) {
					for (String nationRank : new ArrayList<>(peacefulTownResident.getNationRanks())) {
						if (SiegeWarPermissionUtil.doesNationRankAllowPermissionNode(nationRank, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_POINTS)) {
							try {
								peacefulTownResident.removeNationRank(nationRank);
							} catch (NotRegisteredException ignored) {}
						}
					}
				}
				event.setCancellationMsg(Translation.of("status_town_peacefulness_status_change_timer", days));
				event.setCancelled(true);
				
			} else {
				//Here, a countdown is in progress, and the town wishes to cancel the countdown,
				TownMetaDataController.setDesiredPeacefullnessSetting(town, town.isNeutral());
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
			//If the claimer's town is under siege, they cannot claim any land
			if (SiegeWarSettings.getWarSiegeBesiegedTownClaimingDisabled()
				&& SiegeController.hasActiveSiege(event.getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("msg_err_siege_besieged_town_cannot_claim"));
				return;
			}

			//If the land is too near any active siege zone, it cannot be claimed.
			if(SiegeWarSettings.getWarSiegeClaimingDisabledNearSiegeZones()) {
				for(Siege siege: SiegeController.getSieges()) {
					try {
						if (siege.getStatus().isActive()
							&& SiegeWarDistanceUtil.isInSiegeZone(event.getPlayer(), siege)) {
							event.setCancelled(true);
							event.setCancelMessage(Translation.of("msg_err_siege_claim_too_near_siege_zone"));
							break;
						}
					} catch (Exception e) {
						//Problem with this particular siegezone. Ignore siegezone
						try {
							System.out.println("Problem with verifying claim against the following siege zone" + siege.getName() + ". Claim allowed.");
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
		if (SiegeWarSettings.getWarCommonOccupiedTownUnClaimingDisabled() && event.getTown().isConquered()) {
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("msg_err_war_common_occupied_town_cannot_unclaim"));
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
			event.setCancelMessage(Translation.of("msg_err_siege_besieged_town_cannot_unclaim"));
		}
	}
	
	/*
	 * Simply saving the siege will set the name of the siege.
	 */
	@EventHandler
	public void onTownRename(RenameTownEvent event) {
		if (SiegeController.hasSiege(event.getTown())) {
			SiegeController.saveSiege(SiegeController.getSiege(event.getTown()));
		}
	}

	/*
	 * A town being deleted with a siege means the siege ends.
	 */
	@EventHandler
	public void onDeleteTown(DeleteTownEvent event) {
		if (SiegeController.hasSiege(event.getTownUUID()))
			SiegeController.removeSiege(SiegeController.getSiege(event.getTownUUID()), SiegeSide.ATTACKERS);
	}

	/*
	 * In SiegeWar neutral/peaceful towns do not pay their Nation tax. 
	 */
	@EventHandler
	public void onTownPayNationTax(PreTownPaysNationTaxEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarCommonPeacefulTownsEnabled() && event.getTown().isNeutral()) {
			event.setCancelled(true);
		}
	}

	/*
	 * SiegeWar will add lines to towns which have a siege
	 */
	@EventHandler
	public void onTownStatusScreen(TownStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			List<String> out = new ArrayList<>();
			Town town = event.getTown();
			
	        //Revolt Immunity Timer: 71.8 hours
	        if (SiegeWarSettings.getWarSiegeRevoltEnabled() && System.currentTimeMillis() < TownMetaDataController.getRevoltImmunityEndTime(town)) {        	
	        	String time = TimeMgmt.getFormattedTimeValue(TownMetaDataController.getRevoltImmunityEndTime(town)- System.currentTimeMillis());        	
	            out.add(Translation.of("status_town_revolt_immunity_timer", time));
	        }

	        if (SiegeController.hasSiege(town)) {
	            Siege siege = SiegeController.getSiege(town);
	            String time = TimeMgmt.getFormattedTimeValue(TownMetaDataController.getRevoltImmunityEndTime(town)- System.currentTimeMillis());
	            switch (siege.getStatus()) {
	                case IN_PROGRESS:
	                    //Siege:
	                    String siegeStatus = Translation.of("status_town_siege_status", getStatusTownSiegeSummary(siege));
	                    out.add(siegeStatus);

	                    // > Banner XYZ: {2223,82,9877}
	                    out.add(
	                            Translation.of("status_town_siege_status_banner_xyz",
	                            siege.getFlagLocation().getBlockX(),
	                            siege.getFlagLocation().getBlockY(),
	                            siege.getFlagLocation().getBlockZ())
	                    );

	                    // > Attacker: Land of Empire (Nation) {+30}
	                    int pointsInt = siege.getSiegePoints();
	                    String pointsString = pointsInt > 0 ? "+" + pointsInt : "" + pointsInt;
	                    out.add(Translation.of("status_town_siege_status_besieger", siege.getAttackingNation().getFormattedName(), pointsString));

	                    // >  Victory Timer: 5.3 hours
	                    String victoryTimer = Translation.of("status_town_siege_victory_timer", siege.getFormattedHoursUntilScheduledCompletion());
	                    out.add(victoryTimer);

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
	                    break;

	                    
	                case ATTACKER_WIN:
	                case DEFENDER_SURRENDER:
	                    siegeStatus = Translation.of("status_town_siege_status", getStatusTownSiegeSummary(siege));
	                    String invadedYesNo = siege.isTownInvaded() ? Translation.of("status_yes") : Translation.of("status_no_green");
	                    String plunderedYesNo = siege.isTownPlundered() ? Translation.of("status_yes") : Translation.of("status_no_green");
	                    String invadedPlunderedStatus = Translation.of("status_town_siege_invaded_plundered_status", invadedYesNo, plunderedYesNo);
	                    String siegeImmunityTimer = Translation.of("status_town_siege_immunity_timer", time);
	                    out.add(siegeStatus);
	                    out.add(invadedPlunderedStatus);
	                    out.add(siegeImmunityTimer);
	                    break;

	                case DEFENDER_WIN:
	                case ATTACKER_ABANDON:
	                    siegeStatus = Translation.of("status_town_siege_status", getStatusTownSiegeSummary(siege));
	                    siegeImmunityTimer = Translation.of("status_town_siege_immunity_timer", time);
	                    out.add(siegeStatus);
	                    out.add(siegeImmunityTimer);
	                    break;

	                case PENDING_DEFENDER_SURRENDER:
	                case PENDING_ATTACKER_ABANDON:
	                    siegeStatus = Translation.of("status_town_siege_status", getStatusTownSiegeSummary(siege));
	                    out.add(siegeStatus);
	                    break;
	            }
	        } else {
	            if (SiegeWarSettings.getWarSiegeAttackEnabled() 
	            	&& !(SiegeController.hasActiveSiege(town))
	            	&& System.currentTimeMillis() < TownMetaDataController.getSiegeImmunityEndTime(town)) {
	                //Siege:
	                // > Immunity Timer: 40.8 hours
	                out.add(Translation.of("status_town_siege_status", ""));
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
                return Translation.of("status_town_siege_status_attacker_win", siege.getAttackingNation().getFormattedName());
            case DEFENDER_SURRENDER:
                return Translation.of("status_town_siege_status_defender_surrender", siege.getAttackingNation().getFormattedName());
            case DEFENDER_WIN:
                return Translation.of("status_town_siege_status_defender_win");
            case ATTACKER_ABANDON:
                return Translation.of("status_town_siege_status_attacker_abandon");
            case PENDING_DEFENDER_SURRENDER:
                return Translation.of("status_town_siege_status_pending_defender_surrender", siege.getFormattedTimeUntilDefenderSurrender());
            case PENDING_ATTACKER_ABANDON:
                return Translation.of("status_town_siege_status_pending_attacker_abandon", siege.getFormattedTimeUntilAttackerAbandon());
            default:
                return "???";
        }
    }
    
}
