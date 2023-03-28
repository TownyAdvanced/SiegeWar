package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.ChatTools;
import com.gmail.goosius.siegewar.utils.SiegeWarBattleSessionUtil;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.event.ClickEvent;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SiegeWarStatusScreenListener implements Listener {

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
					formatKeyValue(translator.of("status_military_salary"), formatMoney(salary)),
					HoverEvent.showText(Component.text(translator.of("hover_message_click_to_claim"))),
					ClickEvent.runCommand("/sw collect"));
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

			double occupationTax = NationMetaDataController.getNationPeacefulOccupationTax(nation); 
			if (occupationTax > 0 && TownyEconomyHandler.isActive()) {
				String tax = TownyEconomyHandler.getFormattedBalance(occupationTax);
				Component comp = Component.text(translator.of("status_nation_peaceful_occupation_tax", tax));
				event.getStatusScreen().addComponentOf("siegeWarPeacefulOccupationTax", comp);
			}

			List<String> out = new ArrayList<>();

			// Offensive Sieges [3]: TownA, TownB, TownC
	        List<Town> siegeAttacks = new ArrayList<>(SiegeController.getActiveOffensiveSieges(nation).values());
			if (siegeAttacks.size() > 0)
				out.add(translator.of("status_nation_offensive_sieges", siegeAttacks.size())
					+ getFormattedTownList(siegeAttacks));

	        // Defensive Sieges [3]: TownX, TownY, TownZ
	        List<Town> siegeDefences = new ArrayList<>(SiegeController.getActiveDefensiveSieges(nation).values());
			if (siegeDefences.size() > 0)
				out.add(translator.of("status_nation_defensive_sieges", siegeDefences.size())
					+ getFormattedTownList(siegeDefences));

			if (SiegeWarSettings.getWarSiegeNationStatisticsEnabled()) {
				out.add(translator.of("status_nation_town_stats", NationMetaDataController.getTotalTownsGained(nation), NationMetaDataController.getTotalTownsLost(nation)));
				out.add(translator.of("status_nation_plunder_stats", NationMetaDataController.getTotalPlunderGained(nation), NationMetaDataController.getTotalPlunderLost(nation)));
			}
			
			Component comp = Component.empty();
			for (String line : out)
				comp = comp.append(Component.newline()).append(Component.text(line));
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
			
			if (TownMetaDataController.hasPlunderDebt(town)) {
				int days = TownMetaDataController.getPlunderDebtDays(town);
				double amount = TownMetaDataController.getDailyPlunderDebt(town);
				event.getStatusScreen().addComponentOf("siegeWar_plunderDebt", Component.text(translator.of("status_town_plunder_debt", getMoney(days * amount), days, getMoney(amount))));
			}

	        //Revolt Immunity Timer: 71.8 hours
	        long immunity = TownMetaDataController.getRevoltImmunityEndTime(town);
	        if (SiegeWarSettings.getRevoltSiegesEnabled() && immunity == -1l || System.currentTimeMillis() < immunity) {
	            String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
	            event.getStatusScreen().addComponentOf("siegeWar_revoltImmunityTimer", translator.of("status_town_revolt_immunity_timer", time));
	        }

	        immunity = TownMetaDataController.getSiegeImmunityEndTime(town);
	        if (SiegeController.hasSiege(town)) {
	        	List<String> out = new ArrayList<>();
				Siege siege = SiegeController.getSiege(town);
				SiegeStatus siegeStatus= siege.getStatus();
				String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis()); 

				// > Type: Conquest
				out.add(translator.of("status_town_siege_type", siege.getSiegeType().getTranslatedName()));

				// > Status: In Progress
				out.add(translator.of("status_town_siege_status", getStatusTownSiegeSummary(siege, translator)));

				// > Attacker: Darkness
				out.add(translator.of("status_town_siege_attacker", siege.getAttackerNameForDisplay()));

				// > Defender: Light
				out.add(translator.of("status_town_siege_defender", siege.getDefenderNameForDisplay()));

				switch (siegeStatus) {
					case IN_PROGRESS:
						// > Balance: 530
						String balanceLine = translator.of("status_town_siege_status_siege_balance", siege.getSiegeBalance());
						// > Balance: 530 | Pending: +130
						int pending = SiegeWarBattleSessionUtil.calculateSiegeBalanceAdjustment(siege);
						if(pending != 0)
							balanceLine += translator.of("status_town_siege_pending_balance_adjustment", ((pending > 0 ? "+" : "") + pending));
						out.add(balanceLine); 

						if(SiegeWarSettings.isBannerXYZTextEnabled()) {
							// > Banner XYZ: {2223,82,9877}
							out.add(
									translator.of("status_town_siege_status_banner_xyz",
											siege.getFlagLocation().getBlockX(),
											siege.getFlagLocation().getBlockY(),
											siege.getFlagLocation().getBlockZ())
							);
						}

						// >  Victory Timer: 5.3 hours
						String victoryTimer = translator.of("status_town_siege_victory_timer", siege.getFormattedHoursUntilScheduledCompletion());
						out.add(victoryTimer);

						// >  War Chest: $12,800
						if(TownyEconomyHandler.isActive()) {
							String warChest = TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount());
							out.add(translator.of("status_town_siege_status_warchest", warChest));
						}

						//Battle:
						String battle = translator.of("status_town_siege_battle");
						out.add(battle);

						// > Banner Control: Attackers [4] Killbot401x, NerfeyMcNerferson, WarCriminal80372
						if (siege.getBannerControllingSide() == SiegeSide.NOBODY) {
							out.add(translator.of("status_town_banner_control_nobody", siege.getBannerControllingSide().getFormattedName().forLocale(event.getCommandSender())));
						} else {
							
							String[] bannerControllingResidents = TownyFormatter.getFormattedNames(siege.getBannerControllingResidents().toArray(new Resident[0]));
							if (bannerControllingResidents.length > 34) {
								String[] entire = bannerControllingResidents;
								bannerControllingResidents = new String[36];
								System.arraycopy(entire, 0, bannerControllingResidents, 0, 35);
								bannerControllingResidents[35] = translator.of("status_town_reslist_overlength");
							}
							out.addAll(ChatTools.listArr(bannerControllingResidents, translator.of("status_town_banner_control", siege.getBannerControllingSide().getFormattedName().forLocale(event.getCommandSender()), siege.getBannerControllingResidents().size())));
						}

						// > Points: +90 / -220
						out.add(translator.of("status_town_siege_battle_points", siege.getFormattedAttackerBattlePoints(), siege.getFormattedDefenderBattlePoints()));

						// > Time Remaining: 22 minutes
						out.add(translator.of("status_town_siege_battle_time_remaining", siege.getFormattedBattleTimeRemaining(translator)));
						
						// > Breach Points: 15
						if(SiegeWarSettings.isWallBreachingEnabled() && SiegeWarSettings.getWallBreachBonusBattlePoints() != 0)
							out.add(translator.of("status_town_siege_breach_points", siege.getFormattedBreachPoints()));
						break;

	                case ATTACKER_WIN:
	                case DEFENDER_SURRENDER:
					case DEFENDER_WIN:
					case ATTACKER_ABANDON:
	                    String invadedPlunderedStatus = getInvadedPlunderedStatusLine(siege, translator);
						if(!invadedPlunderedStatus.isEmpty())
							out.add(invadedPlunderedStatus);

	                    String siegeImmunityTimer = translator.of("status_town_siege_immunity_timer", time);
	                    out.add(siegeImmunityTimer);
	                    break;

	                case PENDING_DEFENDER_SURRENDER:
	                case PENDING_ATTACKER_ABANDON:
					case UNKNOWN:
						break;
	            }

				Component hoverText = Component.empty();
				for (String line : out) {
					hoverText = hoverText.append(Component.text(line).append(Component.newline()));
				}
				event.getStatusScreen().addComponentOf("siegeWar_siegeHover", 
						Component.empty()
							.append(Component.newline())
							.append(Component.text(hoverFormat(translator.of("status_sieged")))
							.hoverEvent(HoverEvent.showText(hoverText))));

	        } else {
	            if(!SiegeController.hasActiveSiege(town)
	            	&& (System.currentTimeMillis() < immunity)
					|| immunity == -1l) {
	                //Siege:
	                // > Immunity Timer: 40.8 hours
					String time = immunity == -1l ? Translation.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
					Component immunityComp = Component.empty()
							.append(Component.newline())
							.append(Component.text(Translation.of("status_town_siege")))
							.append(Component.newline())
							.append(Component.text(Translation.of("status_town_siege_immunity_timer", time))); 
					event.getStatusScreen().addComponentOf("siegeWar_siegeImmunity", immunityComp);
	            }
	        }
		}
	}

	private String getMoney(double amount) {
		return TownyEconomyHandler.isActive() ? TownyEconomyHandler.getFormattedBalance(amount) : String.valueOf(amount);
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

    private static String getInvadedPlunderedStatusLine(Siege siege, Translator translator) {
		switch(siege.getSiegeType()) {
			case CONQUEST:
				switch (siege.getStatus()) {
					case ATTACKER_WIN:
					case DEFENDER_SURRENDER:
						return getPlunderStatusLine(siege, translator) + getInvadeStatusLine(siege, translator);
					default:
						break;
				}
				break;
			case REVOLT:
				switch (siege.getStatus()) {
					case DEFENDER_WIN:
					case ATTACKER_ABANDON:
						return getPlunderStatusLine(siege, translator) + getInvadeStatusLine(siege, translator);
					default:
						break;
				}
				break;
		}
		return "";
	}

	private static String getPlunderStatusLine(Siege siege, Translator translator) {
		String plunderedYesNo = siege.isTownPlundered() ? translator.of("status_yes") : translator.of("status_no_green");
		return translator.of("status_town_siege_status_plundered", plunderedYesNo);
	}

	private static String getInvadeStatusLine(Siege siege, Translator translator) {
		String invadedYesNo = siege.isTownInvaded() ? translator.of("status_yes") : translator.of("status_no_green");
		return translator.of("status_town_siege_status_invaded", invadedYesNo);
	}

	private String hoverFormat(String hover) {
		return String.format(hover,
				Translation.of("status_format_hover_bracket_colour"),
				Translation.of("status_format_hover_key"),
				Translation.of("status_format_hover_bracket_colour"));
	}
	
	private String formatKeyValue(String key, String value) {
		return Translation.of("status_format_key_value_key") + key + 
			Translation.of("status_format_key_value_value") + value;
	}
	
	private static String getFormattedTownList(List<Town> towns) {
		List<String> lines = new ArrayList<>();
		int i = 0;
		for (Town town : towns) {
			i++;
			if (i == 11) {
				lines.add(Translation.of("status_town_reslist_overlength"));
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
