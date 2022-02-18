package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.ChatTools;
import com.gmail.goosius.siegewar.utils.SiegeWarBattleSessionUtil;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.TextComponent;
import com.palmergames.adventure.text.event.ClickEvent;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

public class SiegeWarStatusScreenListener implements Listener {

	/*
	 * SiegeWar will show a resident if they can collect money due to them,
	 * via the resident status screen. Components are clickable to
	 * claim monies owed.
	 */
	@EventHandler
	public void onResidentStatusScreen(ResidentStatusScreenEvent event) {
		int plunder = ResidentMetaDataController.getPlunderAmount(event.getResident());
		if (plunder > 0) {
			event.getStatusScreen().addComponentOf("siegeWarNationPlunder",
					formatKeyValue(Translation.of("status_plunder"), formatMoney(plunder)),
					HoverEvent.showText(Component.text(Translation.of("hover_message_click_to_claim"))),
					ClickEvent.runCommand("/sw collect"));
		}
		int salary = ResidentMetaDataController.getMilitarySalaryAmount(event.getResident());
		if (salary > 0) {
			event.getStatusScreen().addComponentOf("siegeWarNationSalary",
					formatKeyValue(Translation.of("status_military_salary"), formatMoney(salary)),
					HoverEvent.showText(Component.text(Translation.of("hover_message_click_to_claim"))),
					ClickEvent.runCommand("/sw collect"));
		}
	}
	
	/*
	 * SiegeWar will add lines to Nation which have a siege
	 */
	@EventHandler
	public void onNationStatusScreen(NationStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Nation nation = event.getNation();
			List<String> out = new ArrayList<>();
			// Occupied Home Towns[3]: Town1, Town2, Town3
			List<Town> occupiedHomeTowns = TownOccupationController.getOccupiedHomeTowns(nation);
			if (occupiedHomeTowns.size() > 0) {
				TextComponent comp = Component.newline()
						.append(Component.text(Translation.of("status_nation_occupied_home_towns", occupiedHomeTowns.size())
							+ getFormattedTownList(occupiedHomeTowns))
						.clickEvent(ClickEvent.runCommand("/nation siegewar occupiedhometowns " + nation.getName()))
						.hoverEvent(HoverEvent.showText(Component.text(com.palmergames.bukkit.towny.object.Translation.of("status_hover_click_for_more")))));
				event.getStatusScreen().addComponentOf("siegeWarNationOccupiedHomeTowns", comp);
			}

			// Occupied Foreign Towns[3]: Town4, Town5, Town6
			List<Town> occupiedForeignTowns = TownOccupationController.getOccupiedForeignTowns(nation);
			if (occupiedForeignTowns.size() > 0) {
				TextComponent comp = Component.newline()
						.append(Component.text(Translation.of("status_nation_occupied_foreign_towns", occupiedForeignTowns.size())
							+ getFormattedTownList(occupiedForeignTowns))
						.clickEvent(ClickEvent.runCommand("/nation siegewar occupiedforeigntowns " + nation.getName()))
						.hoverEvent(HoverEvent.showText(Component.text(com.palmergames.bukkit.towny.object.Translation.of("status_hover_click_for_more")))));
				event.getStatusScreen().addComponentOf("siegeWarNationOccupiedForeignTowns", comp);
			}

			// Offensive Sieges [3]: TownA, TownB, TownC
	        List<Town> siegeAttacks = new ArrayList<>(SiegeController.getActiveOffensiveSieges(nation).values());
			if (siegeAttacks.size() > 0)
				out.add(Translation.of("status_nation_offensive_sieges", siegeAttacks.size())
					+ getFormattedTownList(siegeAttacks));

	        // Defensive Sieges [3]: TownX, TownY, TownZ
	        List<Town> siegeDefences = new ArrayList<>(SiegeController.getActiveDefensiveSieges(nation).values());
			if (siegeDefences.size() > 0)
				out.add(Translation.of("status_nation_defensive_sieges", siegeDefences.size())
					+ getFormattedTownList(siegeDefences));

			if (SiegeWarSettings.getWarSiegeNationStatisticsEnabled()) {
				out.add(Translation.of("status_nation_town_stats", NationMetaDataController.getTotalTownsGained(nation), NationMetaDataController.getTotalTownsLost(nation)));
				out.add(Translation.of("status_nation_plunder_stats", NationMetaDataController.getTotalPlunderGained(nation), NationMetaDataController.getTotalPlunderLost(nation)));
			}
			
			TextComponent comp = Component.empty();
			for (String line : out)
				comp = comp.append(Component.text(line)).append(Component.newline());
			event.getStatusScreen().addComponentOf("siegeWarNation", comp);
		}
	}
	
	/*
	 * SiegeWar will add lines to towns which have a siege
	 */
	@EventHandler
	public void onTownStatusScreen(TownStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			
			Town town = event.getTown();

			//Occupying Nation: Empire of the Fluffy Bunnies
			if(SiegeWarSettings.getWarSiegeInvadeEnabled() && TownOccupationController.isTownOccupied(town)) {
				Nation townOccupier = TownOccupationController.getTownOccupier(town);
				event.getStatusScreen().addComponentOf("siegeWar_townOccupier", Translation.of("status_town_occupying_nation", townOccupier.getFormattedName()));
			}
			
	        //Revolt Immunity Timer: 71.8 hours
	        long immunity = TownMetaDataController.getRevoltImmunityEndTime(town);
	        if (SiegeWarSettings.getRevoltSiegesEnabled() && immunity == -1l || System.currentTimeMillis() < immunity) {
	            String time = immunity == -1l ? Translation.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
	            event.getStatusScreen().addComponentOf("siegeWar_revoltImmunityTimer", Translation.of("status_town_revolt_immunity_timer", time));
	        }

	        immunity = TownMetaDataController.getSiegeImmunityEndTime(town);
	        if (SiegeController.hasSiege(town)) {
	        	List<String> out = new ArrayList<>();
				Siege siege = SiegeController.getSiege(town);
				SiegeStatus siegeStatus= siege.getStatus();
				String time = immunity == -1l ? Translation.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis()); 

				// > Type: Conquest
				out.add(Translation.of("status_town_siege_type", siege.getSiegeType().getName()));

				// > Status: In Progress
				out.add(Translation.of("status_town_siege_status", getStatusTownSiegeSummary(siege)));

				// > Attacker: Darkness
				out.add(Translation.of("status_town_siege_attacker", siege.getAttackerNameForDisplay()));

				// > Defender: Light
				out.add(Translation.of("status_town_siege_defender", siege.getDefenderNameForDisplay()));

				switch (siegeStatus) {
					case IN_PROGRESS:
						// > Balance: 530 | Pending: +130
						String balanceLine = Translation.of("status_town_siege_status_siege_balance", siege.getSiegeBalance());
						// If the battle is active with points add the " | Pending: +130"
						if (battleIsActive(siege)) {
							int pending = SiegeWarBattleSessionUtil.calculateSiegeBalanceAdjustment(siege);
							balanceLine += Translation.of("status_town_siege_pending_balance_adjustment", ((pending > 0 ? "+" : "") + pending));
						}
						out.add(balanceLine); 

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
						if(TownyEconomyHandler.isActive()) {
							String warChest = TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount());
							out.add(Translation.of("status_town_siege_status_warchest", warChest));
						}

						if(battleIsActive(siege)) {

							//Battle:
							String battle = Translation.of("status_town_siege_battle");
							out.add(battle);

							// > Banner Control: Attackers [4] Killbot401x, NerfeyMcNerferson, WarCriminal80372
							if (siege.getBannerControllingSide() == SiegeSide.NOBODY) {
								out.add(Translation.of("status_town_banner_control_nobody", siege.getBannerControllingSide().getFormattedName()));
							} else {
								
								String[] bannerControllingResidents = TownyFormatter.getFormattedNames(siege.getBannerControllingResidents().toArray(new Resident[0]));
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
							
							// > Breach Points: 15
							if(SiegeWarSettings.isWallBreachingEnabled() && SiegeWarSettings.getWallBreachBonusBattlePoints() != -1)
								out.add(Translation.of("status_town_siege_breach_points", siege.getFormattedBreachPoints()));
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

				TextComponent hoverText = Component.empty();
				for (String line : out) {
					hoverText = hoverText.append(Component.text(line).append(Component.newline()));
				}
				event.getStatusScreen().addComponentOf("siegeWar_siegeHover", hoverFormat(Translation.of("status_sieged")), HoverEvent.showText(hoverText));

	        } else {
	            if(!SiegeController.hasActiveSiege(town)
	            	&& (System.currentTimeMillis() < immunity)
					|| immunity == -1l) {
	                //Siege:
	                // > Immunity Timer: 40.8 hours
					String time = immunity == -1l ? Translation.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
					TextComponent immunityComp = Component.newline()
							.append(Component.text(Translation.of("status_town_siege")))
							.append(Component.newline())
							.append(Component.text(Translation.of("status_town_siege_immunity_timer", time))); 
					event.getStatusScreen().addComponentOf("siegeWar_siegeImmunity", immunityComp);
	            }
	        }
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
	
	private static boolean battleIsActive(Siege siege) {
		return BattleSession.getBattleSession().isActive()
				&& (siege.getAttackerBattlePoints() > 0
				 || siege.getDefenderBattlePoints() > 0
				 || siege.getBannerControllingSide() != SiegeSide.NOBODY
				 || siege.getBannerControlSessions().size() > 0);
	}
	
	private String hoverFormat(String hover) {
		return String.format(hover,
				com.palmergames.bukkit.towny.object.Translation.of("status_format_hover_bracket_colour"),
				com.palmergames.bukkit.towny.object.Translation.of("status_format_hover_key"),
				com.palmergames.bukkit.towny.object.Translation.of("status_format_hover_bracket_colour"));
	}
	
	private String formatKeyValue(String key, String value) {
		return com.palmergames.bukkit.towny.object.Translation.of("status_format_key_value_key") +
			key + 
			com.palmergames.bukkit.towny.object.Translation.of("status_format_key_value_value") +
			value;
	}
	
	private static String getFormattedTownList(List<Town> towns) {
		List<String> lines = new ArrayList<>();
		int i = 0;
		for (Town town : towns) {
			i++;
			if (i == 11) {
				lines.add(com.palmergames.bukkit.towny.object.Translation.of("status_town_reslist_overlength"));
				return StringMgmt.join(lines, ", ");
			}
			lines.add(town.getName());
		}
		return StringMgmt.join(lines, ", ");
	}

	private String formatMoney(int refund) {
		return TownyEconomyHandler.getFormattedBalance(refund);
	}
}
