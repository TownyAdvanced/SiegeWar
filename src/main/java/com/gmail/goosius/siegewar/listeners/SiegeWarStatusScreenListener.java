package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

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
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
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
		int salary = ResidentMetaDataController.getMilitarySalaryAmount(event.getResident());
		if (salary > 0) {
			final Translator translator = Translator.locale(event.getCommandSender());
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
			final Translator translator = Translator.locale(event.getCommandSender());
			Nation nation = event.getNation();

			/*
			 * Display occupation tax rate
			 * As long as the configured max is >0, display the tax rate.
			 * Because for nations with a custom 0 value, this will be clearer than a missing field.
			 */
			if(TownyEconomyHandler.isActive() && SiegeWarSettings.getMaxOccupationTaxPerPlot() > 0) {
				double occupationTaxPerPlot = NationMetaDataController.getNationOccupationTaxPerPlot(nation);

				if(occupationTaxPerPlot == -1) {
					occupationTaxPerPlot = SiegeWarSettings.getMaxOccupationTaxPerPlot();
				}
				
				String occupationTaxString = TownyEconomyHandler.getFormattedBalance(occupationTaxPerPlot);
				Component occupationTaxComponent = Component.text(translator.of("status_splitter")).append(Component.text(translator.of("status_nation_occupation_tax_per_plot", occupationTaxString)));
				Component existingComponent;
				Component updatedComponent;

				//If nation tax is there, replace nationtax with "{nation_tax}{occ_tax}
				//If nation tax is not there, replace bankString with "{bank}{occ_tax}
				if(event.getStatusScreen().hasComponent("nationtax")) {
					existingComponent = event.getStatusScreen().getComponentOrNull("nationtax");
					updatedComponent = existingComponent.append(occupationTaxComponent);
					event.getStatusScreen().addComponentOf("nationtax", updatedComponent);
				} else {
					existingComponent = event.getStatusScreen().getComponentOrNull("bankString");
					updatedComponent = existingComponent.append(occupationTaxComponent);
					event.getStatusScreen().addComponentOf("bankString", updatedComponent);
				}
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
			final Translator translator = Translator.locale(event.getCommandSender());
			
			Town town = event.getTown();

			if(SiegeWarTownPeacefulnessUtil.isTownPeaceful(town)) {
				//Generate the correct subtitle line:
				//1. Get the list of existing subtitle entries
				List<String> existingSubtitleEntries = getTownSubtitle(event.getTown(), TownyAPI.getInstance().getTownyWorld(town.getWorld()), translator);
				//2. Add the peacefulness flag to the start of the list
				String peacefulnessFlag = translator.of("status_town_peacefulness_flag");
				existingSubtitleEntries.add(0, peacefulnessFlag);
				//3. Generate the subtitle component
				String townSubtitle = com.palmergames.bukkit.util.ChatTools.formatSubTitle(StringMgmt.join(existingSubtitleEntries, " "));
				Component subtitleComponent = Component.text(townSubtitle);

				//Put the subtitle line on the screen
				if(event.getStatusScreen().hasComponent("subtitle")) {
					//Replace subtitle line
					event.getStatusScreen().replaceComponent("subtitle", subtitleComponent);
				} else {
					//Replace title line with combined title + subtitle lines
					Component titleComponent = event.getStatusScreen().getComponentOrNull("title");
					Component titleAndSubtitleComponent = titleComponent.appendNewline().append(subtitleComponent);
					event.getStatusScreen().replaceComponent("title", titleAndSubtitleComponent);
				}
			}

			/*
			 * Display occupation tax
			 * As long as the configured max is >0, display the tax.
			 * Because for nations with a custom 0 value, this will be clearer than a missing field.
			 */
			if(TownyEconomyHandler.isActive()
					&& TownOccupationController.isTownOccupied(town)
					&& SiegeWarSettings.getMaxOccupationTaxPerPlot() > 0) {

				double occupationTax = TownOccupationController.getNationOccupationTax(town);
				String occupationTaxString = TownyEconomyHandler.getFormattedBalance(occupationTax);
				Component occupationTaxComponent = Component.text(translator.of("status_splitter")).append(Component.text(translator.of("status_town_occupation_tax", occupationTaxString)));
				Component existingComponent;
				Component updatedComponent;

				//If towntax is there, replace towntax with "{occ_tax}{towntax}
				//If towntax is not there, replace bankString with "{bankString}{occ_tax}
				if(event.getStatusScreen().hasComponent("towntax")) {
					existingComponent = event.getStatusScreen().getComponentOrNull("towntax");
					updatedComponent = occupationTaxComponent.append(existingComponent);
					event.getStatusScreen().addComponentOf("towntax", updatedComponent);
				} else {
					existingComponent = event.getStatusScreen().getComponentOrNull("bankString");
					updatedComponent = existingComponent.append(occupationTaxComponent);
					event.getStatusScreen().addComponentOf("bankString", updatedComponent);
				}
			}
			
			if (TownMetaDataController.hasPlunderDebt(town)) {
				int days = TownMetaDataController.getPlunderDebtDays(town);
				double amount = TownMetaDataController.getDailyPlunderDebt(town);
				event.getStatusScreen().addComponentOf("siegeWar_plunderDebt", Component.text(translator.of("status_town_plunder_debt", getMoney(days * amount), days, getMoney(amount))));
			}

			//Days to Peacefulness Status Change: 2
			if(SiegeWarTownPeacefulnessUtil.getTownPeacefulnessChangeCountdownDays(town) > 0) {
				Component peacefulnessCountdownDays = Component.text(translator.of("status_town_days_to_peacefulness_status_change", SiegeWarTownPeacefulnessUtil.getTownPeacefulnessChangeCountdownDays(town)));
				event.getStatusScreen().addComponentOf("siegeWar_peacefulnessCountdownDays", peacefulnessCountdownDays);
			}

	        //Revolt Immunity Timer: 71.8 hours
	        long immunity = TownMetaDataController.getRevoltImmunityEndTime(town);
	        if (SiegeWarSettings.getRevoltSiegesEnabled() && immunity == -1l || System.currentTimeMillis() < immunity) {
	            String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
				Component revoltImmunityTimer = Component.text(translator.of("status_town_revolt_immunity_timer", time));
				event.getStatusScreen().addComponentOf("siegeWar_revoltImmunityTimer", revoltImmunityTimer);
	        }

	        immunity = TownMetaDataController.getSiegeImmunityEndTime(town);
	        if (SiegeController.hasSiege(town)) {
	        	List<String> out = new ArrayList<>();
				Siege siege = SiegeController.getSiege(town);
				SiegeStatus siegeStatus= siege.getStatus();
				String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());

				// > Attacker: Darkness
				out.add(translator.of("status_town_siege_attacker", siege.getAttackerNameForDisplay()));

				// > Defender: Light
				out.add(translator.of("status_town_siege_defender", siege.getDefenderNameForDisplay()));

				// > Type: Conquest
				out.add(translator.of("status_town_siege_type", siege.getSiegeType().getTranslatedName()));

				switch (siegeStatus) {
					case IN_PROGRESS:
					case PENDING_ATTACKER_ABANDON:
					case PENDING_DEFENDER_SURRENDER:
						// >  War Chest: $12,800
						if(TownyEconomyHandler.isActive()) {
							String warChest = TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount());
							out.add(translator.of("status_town_siege_status_warchest", warChest));
						}

						// >  Progress: 5/7
						out.add(translator.of("status_town_siege_progress", siege.getNumBattleSessionsCompleted(), SiegeWarSettings.getSiegeDurationBattleSessions()));
				}

				// > Status: Contested
				out.add(translator.of("status_town_siege_status", getStatusTownSiegeSummary(siege, translator)));

				switch (siegeStatus) {
					case IN_PROGRESS:
						// > Balance: 530 | Pending: +130
						String balanceLine = translator.of("status_town_siege_status_siege_balance", siege.getSiegeBalance());
						int pending = SiegeWarBattleSessionUtil.calculateSiegeBalanceAdjustment(siege);
						if(pending != 0)
							balanceLine += translator.of("status_town_siege_pending_balance_adjustment", ((pending > 0 ? "+" : "") + pending));
						out.add(balanceLine);

						// > Banner XYZ: {2223,82,9877}
						if(SiegeWarSettings.isBannerXYZTextEnabled()) {
							out.add(
									translator.of("status_town_siege_status_banner_xyz",
											siege.getFlagLocation().getBlockX(),
											siege.getFlagLocation().getBlockY(),
											siege.getFlagLocation().getBlockZ())
							);
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
						break;

					case ATTACKER_WIN:
					case DEFENDER_WIN:
	                case DEFENDER_SURRENDER:
					case ATTACKER_ABANDON:

						// > Captured: No
						out.add(getInvadeStatusLine(siege, translator));

						// > Plundered: No
						out.add(getPlunderStatusLine(siege, translator));

						// > Immunity: 7 days
						String siegeImmunityTimer = translator.of("status_town_siege_immunity_timer", time);
						out.add(siegeImmunityTimer);
	            }

				Component hoverText = Component.empty();
				hoverText = hoverText.append(Component.text(translator.of("status_town_siege")));
				for (String line : out) {
					hoverText = hoverText.append(Component.newline().append(Component.text(line)));
				}
				event.getStatusScreen().addComponentOf("siegeWar_siegeHover", 
						Component.empty()
							.append(Component.newline())
							.append(Component.text(hoverFormat(translator.of("status_sieged")))
							.hoverEvent(HoverEvent.showText(hoverText))));

	        } else {
	            if(System.currentTimeMillis() < immunity || immunity == -1l) {
	                //Siege:
	                // > Immunity Timer: 40.8 hours
					String time = immunity == -1l ? translator.of("msg_permanent") : TimeMgmt.getFormattedTimeValue(immunity- System.currentTimeMillis());
					Component immunityComp = Component.empty()
							.append(Component.newline())
							.append(Component.text(translator.of("status_town_siege")))
							.append(Component.newline())
							.append(Component.text(translator.of("status_town_siege_immunity_timer", time))); 
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
                return translator.of("status_town_siege_status_pending_defender_surrender");
            case PENDING_ATTACKER_ABANDON:
                return translator.of("status_town_siege_status_pending_attacker_abandon");
            default:
                return "???";
        }
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

	/**
	 * This method was copy-pasted verbatim from Towny.
	 * Once the method is made public in Towny,
	 * this method can be deleted
	 *
	 * Returns the 2nd line of the Town StatusScreen.
	 * @param town Town for which to get the StatusScreen.
	 * @param world TownyWorld in which the town considers home.
	 * @param translator Translator used in language selection.
	 * @return Formatted 2nd line of the Town StatusScreen.
	 */
	private static List<String> getTownSubtitle(Town town, TownyWorld world, Translator translator) {
		List<String> sub = new ArrayList<>();
		if (!town.isAdminDisabledPVP() && (town.isPVP() || world.isForcePVP()))
			sub.add(translator.of("status_title_pvp"));
		if (town.isOpen())
			sub.add(translator.of("status_title_open"));
		if (town.isPublic())
			sub.add(translator.of("status_public"));
		if (town.isNeutral())
			sub.add(translator.of("status_town_title_peaceful"));
		if (town.isConquered())
			sub.add(translator.of("msg_conquered"));
		return sub;
	}
}
