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
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBattleSessionUtil;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.event.ClickEvent;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.towny.utils.TownyComponents;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

public class SiegeWarStatusScreenListener implements Listener {

	public static final String keyValueFormat = "%s%s %s%s";
	
	/*
	 * SiegeWar will show a resident if they can collect money due to them,
	 * via the resident status screen. Components are clickable to
	 * claim monies owed.
	 */
	@EventHandler
	public void onResidentStatusScreen(ResidentStatusScreenEvent event) {
		int salary = ResidentMetaDataController.getMilitarySalaryAmount(event.getResident());
		if (salary > 0) {
			final Translator translator = Translator.locale(Translation.getLocale(event.getCommandSender()));
			event.getStatusScreen().addComponentOf("siegeWarNationSalary",
					formatKeyValue(translator.of("status_military_salary"), formatMoney(salary))
						.hoverEvent(HoverEvent.showText(Component.text(translator.of("hover_message_click_to_claim"))))
						.clickEvent(ClickEvent.runCommand("/sw collect")));
		}
	}
	
	/*
	 * SiegeWar will add lines to Nation which have a siege
	 */
	@EventHandler
	public void onNationStatusScreen(NationStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			final Translator translator = Translator.locale(Translation.getLocale(event.getCommandSender()));
			Nation nation = event.getNation();
			List<Component> out = new ArrayList<>();
			// Occupied Home Towns[3]: Town1, Town2, Town3
			List<Town> occupiedHomeTowns = TownOccupationController.getOccupiedHomeTowns(nation);
			if (occupiedHomeTowns.size() > 0) {
				Component comp = Component.newline()
						.append(translator.comp("status_nation_occupied_home_towns", occupiedHomeTowns.size())
						.append(getFormattedTownList(occupiedHomeTowns))
						.clickEvent(ClickEvent.runCommand("/nation siegewar occupiedhometowns " + nation.getName()))
						.hoverEvent(HoverEvent.showText(translator.comp("status_hover_click_for_more"))));
				event.getStatusScreen().addComponentOf("siegeWarNationOccupiedHomeTowns", comp);
			}

			// Occupied Foreign Towns[3]: Town4, Town5, Town6
			List<Town> occupiedForeignTowns = TownOccupationController.getOccupiedForeignTowns(nation);
			if (occupiedForeignTowns.size() > 0) {
				Component comp = Component.newline()
						.append(translator.comp("status_nation_occupied_foreign_towns", occupiedForeignTowns.size())
						.append(getFormattedTownList(occupiedForeignTowns))
						.clickEvent(ClickEvent.runCommand("/nation siegewar occupiedforeigntowns " + nation.getName()))
						.hoverEvent(HoverEvent.showText(translator.comp("status_hover_click_for_more"))));
				event.getStatusScreen().addComponentOf("siegeWarNationOccupiedForeignTowns", comp);
			}

			// Offensive Sieges [3]: TownA, TownB, TownC
	        List<Town> siegeAttacks = new ArrayList<>(SiegeController.getActiveOffensiveSieges(nation).values());
			if (siegeAttacks.size() > 0)
				out.add(translator.comp("status_nation_offensive_sieges", siegeAttacks.size())
					.append(getFormattedTownList(siegeAttacks)));

	        // Defensive Sieges [3]: TownX, TownY, TownZ
	        List<Town> siegeDefences = new ArrayList<>(SiegeController.getActiveDefensiveSieges(nation).values());
			if (siegeDefences.size() > 0)
				out.add(translator.comp("status_nation_defensive_sieges", siegeDefences.size())
					.append(getFormattedTownList(siegeDefences)));

			if (SiegeWarSettings.getWarSiegeNationStatisticsEnabled()) {
				out.add(translator.comp("status_nation_town_stats", NationMetaDataController.getTotalTownsGained(nation), NationMetaDataController.getTotalTownsLost(nation)));
				out.add(translator.comp("status_nation_plunder_stats", NationMetaDataController.getTotalPlunderGained(nation), NationMetaDataController.getTotalPlunderLost(nation)));
			}
			
			Component comp = Component.empty();
			for (Component line : out)
				comp = comp.append(line).append(Component.newline());
			event.getStatusScreen().addComponentOf("siegeWarNation", comp);
		}
	}
	
	/*
	 * SiegeWar will add lines to towns which have a siege
	 */
	@EventHandler
	public void onTownStatusScreen(TownStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			final Translator translator = Translator.locale(Translation.getLocale(event.getCommandSender()));
			
			Town town = event.getTown();

			//Occupying Nation: Empire of the Fluffy Bunnies
			if(SiegeWarSettings.getWarSiegeInvadeEnabled() && TownOccupationController.isTownOccupied(town)) {
				Nation townOccupier = TownOccupationController.getTownOccupier(town);
				event.getStatusScreen().addComponentOf("siegeWar_townOccupier", translator.comp("status_town_occupying_nation", townOccupier.getFormattedName()));
			}
			
	        //Revolt Immunity Timer: 71.8 hours
	        long immunity = TownMetaDataController.getRevoltImmunityEndTime(town);
	        if (SiegeWarSettings.getRevoltSiegesEnabled() && immunity == -1l || System.currentTimeMillis() < immunity) {
	            String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
	            event.getStatusScreen().addComponentOf("siegeWar_revoltImmunityTimer", translator.comp("status_town_revolt_immunity_timer", time));
	        }

	        immunity = TownMetaDataController.getSiegeImmunityEndTime(town);
	        if (SiegeController.hasSiege(town)) {
	        	List<Component> out = new ArrayList<>();
				Siege siege = SiegeController.getSiege(town);
				SiegeStatus siegeStatus= siege.getStatus();
				String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis()); 

				// > Type: Conquest
				out.add(translator.comp("status_town_siege_type", siege.getSiegeType().getTranslatedName()));

				// > Status: In Progress
				out.add(translator.comp("status_town_siege_status", getStatusTownSiegeSummary(siege, translator)));

				// > Attacker: Darkness
				out.add(translator.comp("status_town_siege_attacker", siege.getAttackerNameForDisplay()));

				// > Defender: Light
				out.add(translator.comp("status_town_siege_defender", siege.getDefenderNameForDisplay()));

				switch (siegeStatus) {
					case IN_PROGRESS:
						// > Balance: 530
						Component balanceLine = translator.comp("status_town_siege_status_siege_balance", siege.getSiegeBalance());
						// > Balance: 530 | Pending: +130
						int pending = SiegeWarBattleSessionUtil.calculateSiegeBalanceAdjustment(siege);
						if(pending != 0)
							balanceLine.append(translator.comp("status_town_siege_pending_balance_adjustment", ((pending > 0 ? "+" : "") + pending)));
						out.add(balanceLine); 

						if(SiegeWarSettings.isBannerXYZTextEnabled()) {
							// > Banner XYZ: {2223,82,9877}
							out.add(
									translator.comp("status_town_siege_status_banner_xyz",
											siege.getFlagLocation().getBlockX(),
											siege.getFlagLocation().getBlockY(),
											siege.getFlagLocation().getBlockZ())
							);
						}

						// >  Victory Timer: 5.3 hours
						Component victoryTimer = translator.comp("status_town_siege_victory_timer", siege.getFormattedHoursUntilScheduledCompletion());
						out.add(victoryTimer);

						// >  War Chest: $12,800
						if(TownyEconomyHandler.isActive()) {
							String warChest = TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount());
							out.add(translator.comp("status_town_siege_status_warchest", warChest));
						}

						//Battle:
						Component battle = translator.comp("status_town_siege_battle");
						out.add(battle);

						// > Banner Control: Attackers [4] Killbot401x, NerfeyMcNerferson, WarCriminal80372
						if (siege.getBannerControllingSide() == SiegeSide.NOBODY) {
							out.add(translator.comp("status_town_banner_control_nobody", siege.getBannerControllingSide().getFormattedName().forLocale(event.getCommandSender())));
						} else {
							
							String[] bannerControllingResidents = (String[]) siege.getBannerControllingResidents().stream().map(res -> res.getFormattedName()).toArray(); 
							if (bannerControllingResidents.length > 34) {
								String[] entire = bannerControllingResidents;
								bannerControllingResidents = new String[36];
								System.arraycopy(entire, 0, bannerControllingResidents, 0, 35);
								bannerControllingResidents[35] = translator.of("status_town_reslist_overlength");
							}
							out.add(translator.comp("status_town_banner_control", siege.getBannerControllingSide().getFormattedName().forLocale(event.getCommandSender()), siege.getBannerControllingResidents().size())
									.append(Component.text(StringMgmt.join(bannerControllingResidents, ", "))));
						}

						// > Points: +90 / -220
						out.add(translator.comp("status_town_siege_battle_points", siege.getFormattedAttackerBattlePoints(), siege.getFormattedDefenderBattlePoints()));

						// > Time Remaining: 22 minutes
						out.add(translator.comp("status_town_siege_battle_time_remaining", siege.getFormattedBattleTimeRemaining(translator)));
						
						// > Breach Points: 15
						if(SiegeWarSettings.isWallBreachingEnabled() && SiegeWarSettings.getWallBreachBonusBattlePoints() != 0)
							out.add(translator.comp("status_town_siege_breach_points", siege.getFormattedBreachPoints()));
						break;

	                case ATTACKER_WIN:
	                case DEFENDER_SURRENDER:
					case DEFENDER_WIN:
					case ATTACKER_ABANDON:
	                    Component invadedPlunderedStatus = getInvadedPlunderedStatusLine(siege, translator);
						if(!invadedPlunderedStatus.equals(Component.empty()))
							out.add(invadedPlunderedStatus);

	                    Component siegeImmunityTimer = translator.comp("status_town_siege_immunity_timer", time);
	                    out.add(siegeImmunityTimer);
	                    break;

	                case PENDING_DEFENDER_SURRENDER:
	                case PENDING_ATTACKER_ABANDON:
					case UNKNOWN:
						break;
	            }

				Component hoverText = Component.empty();
				for (Component line : out) {
					hoverText = hoverText.append(line).append(Component.newline());
				}
				event.getStatusScreen().addComponentOf("siegeWar_siegeHover", hoverFormat(translator.of("status_sieged")).hoverEvent(HoverEvent.showText(hoverText)));

	        } else {
	            if(!SiegeController.hasActiveSiege(town)
	            	&& (System.currentTimeMillis() < immunity)
					|| immunity == -1l) {
	                //Siege:
	                // > Immunity Timer: 40.8 hours
					String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
					Component immunityComp = Component.newline()
							.append(translator.comp("status_town_siege"))
							.append(Component.newline())
							.append(translator.comp("status_town_siege_immunity_timer", time));
					event.getStatusScreen().addComponentOf("siegeWar_siegeImmunity", immunityComp);
	            }
	        }
		}
	}

	private static String getStatusTownSiegeSummary(@NotNull Siege siege, Translator translator) {
        switch (siege.getStatus()) {
            case IN_PROGRESS:
                return translator.of("status_town_siege_status_in_progress");
            case ATTACKER_WIN:
                return translator.of("status_town_siege_status_attacker_win");
            case DEFENDER_SURRENDER:
                return translator.of("status_town_siege_status_defender_surrender");
            case DEFENDER_WIN:
                return translator.of("status_town_siege_status_defender_win");
            case ATTACKER_ABANDON:
                return translator.of("status_town_siege_status_attacker_abandon");
            case PENDING_DEFENDER_SURRENDER:
                return translator.of("status_town_siege_status_pending_defender_surrender", siege.getTimeRemaining());
            case PENDING_ATTACKER_ABANDON:
                return translator.of("status_town_siege_status_pending_attacker_abandon", siege.getTimeRemaining());
            default:
                return "???";
        }
    }

    private static Component getInvadedPlunderedStatusLine(Siege siege, Translator translator) {
		switch(siege.getSiegeType()) {
			case CONQUEST:
			case LIBERATION:
				switch (siege.getStatus()) {
					case ATTACKER_WIN:
					case DEFENDER_SURRENDER:
						return getPlunderStatusLine(siege, translator).append(getInvadeStatusLine(siege, translator));
					default:
						break;
				}
				break;
			case SUPPRESSION:
				switch (siege.getStatus()) {
					case ATTACKER_WIN:
					case DEFENDER_SURRENDER:
						return getPlunderStatusLine(siege, translator);
					default:
						break;
				}
				break;
			case REVOLT:
				switch (siege.getStatus()) {
					case DEFENDER_WIN:
					case ATTACKER_ABANDON:
						return getPlunderStatusLine(siege, translator);
					default:
						break;
				}
				break;
		}
		return Component.empty();
	}

	private static Component getPlunderStatusLine(Siege siege, Translator translator) {
		String plunderedYesNo = siege.isTownPlundered() ? translator.of("status_yes") : translator.of("status_no_green");
		return translator.comp("status_town_siege_status_plundered", plunderedYesNo);
	}

	private static Component getInvadeStatusLine(Siege siege, Translator translator) {
		if(siege.getSiegeType() == SiegeType.REVOLT && siege.getSiegeType() == SiegeType.SUPPRESSION) {
			return Component.empty();
		} else {
			String invadedYesNo = siege.isTownInvaded() ? translator.of("status_yes") : translator.of("status_no_green");
			return translator.comp("status_town_siege_status_invaded", invadedYesNo);
		}
	}

	private Component hoverFormat(String hover) {
		return Component.text(String.format(hover,
				Translation.of("status_format_hover_bracket_colour"),
				Translation.of("status_format_hover_key"),
				Translation.of("status_format_hover_bracket_colour")));
	}
	
	private Component formatKeyValue(String key, String value) {
		return TownyComponents.miniMessage(String.format(keyValueFormat, Translation.of("status_format_key_value_key"), key, Translation.of("status_format_key_value_value"), value));
	}
	
	private static Component getFormattedTownList(List<Town> towns) {
		List<String> lines = new ArrayList<>();
		int i = 0;
		for (Town town : towns) {
			i++;
			if (i == 11) {
				lines.add(Translation.of("status_town_reslist_overlength"));
				return TownyComponents.miniMessage(StringMgmt.join(lines, ", "));
			}
			lines.add(town.getName());
		}
		return TownyComponents.miniMessage(StringMgmt.join(lines, ", "));
	}

	private String formatMoney(int refund) {
		return TownyEconomyHandler.getFormattedBalance(refund);
	}
}
