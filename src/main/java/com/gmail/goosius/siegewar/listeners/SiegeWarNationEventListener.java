package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarPermissionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.gmail.goosius.siegewar.utils.TownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NationPreAddTownEvent;
import com.palmergames.bukkit.towny.event.PreDeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;
import com.palmergames.bukkit.towny.event.nation.NationRankAddEvent;
import com.palmergames.bukkit.towny.event.nation.NationTownLeaveEvent;
import com.palmergames.bukkit.towny.event.nation.PreNewNationEvent;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.townblockstatus.NationZoneTownBlockStatusEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.util.ChatTools;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeWarNationEventListener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;
	
	public SiegeWarNationEventListener(SiegeWar instance) {

		plugin = instance;
	}
	/*
	 * SW limits which Towns can join or be added to a nation.
	 */
	@EventHandler
	public void onNationAddTownEvent(NationPreAddTownEvent event) {
		if(SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarCommonPeacefulTownsEnabled() && event.getTown().isNeutral()) {
			if(!TownPeacefulnessUtil.canPeacefulTownJoinNation(event.getTown(), event.getNation())) {
				event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_war_siege_peaceful_town_cannot_join_nation",
						event.getTown().getName(),
						event.getNation().getName(),
						SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement(),
						SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement()));
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * SW warns peaceful towns who make nations their decision may be a poor one, but does not stop them.
	 */
	@EventHandler
	public void onNewNationEvent(PreNewNationEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
				&& event.getTown().isNeutral()) {
			if (!SiegeWarSettings.getWarCommonPeacefulTownsAllowedToMakeNation()) {
				event.setCancelled(true);
				event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_war_siege_peaceful_towns_cannot_make_nations"));
			} else 
				Messaging.sendMsg(event.getTown().getMayor().getPlayer(), Translation.of("msg_war_siege_warning_peaceful_town_should_not_create_nation"));
		}
	}
	
	/*
	 * SW will warn a nation about to delete itself that it can claim a refund after the fact.
	 */
	@EventHandler
	public void onNationDeleteEvent(PreDeleteNationEvent event) {
		//If nation refund is enabled, warn the player that they will get a refund (and indicate how to claim it).
		if (SiegeWarSettings.getWarSiegeEnabled() && TownySettings.isUsingEconomy()
				&& SiegeWarSettings.getWarSiegeRefundInitialNationCostOnDelete()) {
			int amountToRefund = (int)(TownySettings.getNewNationPrice() * 0.01 * SiegeWarSettings.getWarSiegeNationCostRefundPercentageOnDelete());
			Messaging.sendMsg(event.getNation().getKing().getPlayer(), Translation.of("msg_err_siege_war_delete_nation_warning", TownyEconomyHandler.getFormattedBalance(amountToRefund)));
		}

	}
	
	/*
	 * SW can prevent towns leaving their nations.
	 */
	@EventHandler
	public void onTownTriesToLeaveNation(NationPreTownLeaveEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Town town = event.getTown();

			//A peaceful town might not be able to leave
			if (SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
					&& town.isNeutral()
					&& !TownPeacefulnessUtil.canPeacefulTownLeaveNation(town)) {

				event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_war_siege_peaceful_town_cannot_revolt_nearby_guardian_towns_one",
						SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement(),
						SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement()));
				event.setCancelled(true);
				return;
			}

			//A town cannot leave unless its revolt immunity timer is finished
			if (SiegeWarSettings.getWarSiegeTownLeaveDisabled()) {

				if (!SiegeWarSettings.getWarSiegeRevoltEnabled()) {
					event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_war_town_voluntary_leave_impossible"));
					event.setCancelled(true);
				}
				if (System.currentTimeMillis() < TownMetaDataController.getRevoltImmunityEndTime(town)) {
					event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_err_siege_war_revolt_immunity_active"));
					event.setCancelled(true);
				} else {
					// Towny will cancel the leaving on lowest priority if the town is conquered.
					// We want to un-cancel it.
					if (event.isCancelled())
						event.setCancelled(false);
				}
			}
		}
	}

	@EventHandler
	public void onTownLeavesNation(NationTownLeaveEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.getWarSiegeRevoltEnabled()) {
			//Activate revolt immunity
			SiegeWarTimeUtil.activateRevoltImmunityTimer(event.getTown());
			event.getTown().setConquered(false);
			event.getTown().setConqueredDays(0);
			event.getTown().save();

			Messaging.sendGlobalMessage(
				Translation.of("msg_siege_war_revolt",
				event.getTown().getFormattedName(),
				event.getTown().getMayor().getFormattedName(),
				event.getNation().getFormattedName()));
		}	
	}
	
	@EventHandler
	public void onNationRankGivenToPlayer(NationRankAddEvent event) throws NotRegisteredException {
		//In Siegewar, if target town is peaceful, can't add military rank
		if(SiegeWarSettings.getWarSiegeEnabled()
			&& SiegeWarSettings.getWarCommonPeacefulTownsEnabled()
			&& SiegeWarPermissionUtil.doesNationRankAllowPermissionNode(event.getRank(), SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_POINTS)
			&& event.getResident().getTown().isNeutral()) { // We know that the resident's town will not be null based on the tests already done.
			event.setCancelled(true);
			event.setCancelMessage(Translation.of("plugin_prefix") + Translation.of("msg_war_siege_cannot_add_nation_military_rank_to_peaceful_resident"));
		}
		
	}
	
	/*
	 * Simply saving the siege will set the name of the siege.
	 */
	@EventHandler
	public void onNationRename(RenameNationEvent event) {
		if (SiegeController.hasSieges(event.getNation())) {
			for (Siege siege : SiegeController.getSieges(event.getNation()))
				SiegeController.saveSiege(siege);
		}
	}
	
	/*
	 * SiegeWar will disable nation-zones if the town has a siege.
	 */
	@EventHandler
	public void onNationZoneStatus(NationZoneTownBlockStatusEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled() 
			&& SiegeController.hasActiveSiege(event.getTown()))	{
			event.setCancelled(true);
		}
	}
	
	/*
	 * SiegeWar will add lines to Nation which have a siege
	 */
    @EventHandler
	public void onNationStatusScreen(NationStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			Nation nation = event.getNation();
			
	        // Siege Attacks [3]: TownA, TownB, TownC
	        List<Town> siegeAttacks = getTownsUnderSiegeAttack(nation);
	        String[] formattedSiegeAttacks = TownyFormatter.getFormattedNames(siegeAttacks.toArray(new Town[0]));
	        List<String> out = new ArrayList<>(ChatTools.listArr(formattedSiegeAttacks, Translation.of("status_nation_siege_attacks", siegeAttacks.size())));

	        // Siege Defences [3]: TownX, TownY, TownZ
	        List<Town> siegeDefences = getTownsUnderSiegeDefence(nation);
	        String[] formattedSiegeDefences = TownyFormatter.getFormattedNames(siegeDefences.toArray(new Town[0]));
	        out.addAll(ChatTools.listArr(formattedSiegeDefences, Translation.of("status_nation_siege_defences", siegeDefences.size())));
	        
	        event.addLines(out);
		}
	}
    
	public static List<Town> getTownsUnderSiegeAttack(Nation nation) {
		List<Town> result = new ArrayList<>();
		for(Siege siege : SiegeController.getSieges()) {
			if(siege.getAttackingNation().equals(nation)) {				
				result.add(siege.getDefendingTown());
			}
		}
		return result;
	}

	public static List<Town> getTownsUnderSiegeDefence(Nation nation) {
		List<Town> result = new ArrayList<Town>();
		for(Town town: nation.getTowns()) {
			if(SiegeController.hasActiveSiege(town))
				result.add(town);
		}
		return result;
	}
	
	/*
	 * A nation being deleted with a siege means the siege ends.
	 */
	@EventHandler
	public void onDeleteNation(DeleteNationEvent event) {
		Resident king = TownyUniverse.getInstance().getResident(event.getNationKing());
		if (king != null)
			SiegeWarMoneyUtil.makeNationRefundAvailable(king);
		
		for (Siege siege : SiegeController.getSiegesByNationUUID(event.getNationUUID())) {
			SiegeController.removeSiege(siege, SiegeSide.DEFENDERS);
		}
	}


}
