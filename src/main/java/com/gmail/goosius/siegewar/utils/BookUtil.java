package com.gmail.goosius.siegewar.utils;

import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.objects.HeldItemsCombination;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.war.common.townruin.TownRuinSettings;
import com.palmergames.bukkit.util.BookFactory;
import com.palmergames.util.TimeMgmt;

public class BookUtil {
	
	public static void buildBook(Player player) {

		String text = "";
		if (SiegeWarSettings.getWarSiegeAttackEnabled() && SiegeWarSettings.getWarSiegeEnabled()) {
			text = siegeWarUserGuide(text);
		} else 
			text = "Siege War is disabled in the config...";

		player.openBook(BookFactory.makeBook("SiegeWar Guide", "LlmDl", text));
	}
	
	private static String siegeWarUserGuide(String text) {
		/*
		 * Local variables
		 */
		String activeSession = TimeMgmt.getFormattedTimeValue(SiegeWarSettings.getWarSiegeBattleSessionsActivePhaseDurationMinutes() * TimeMgmt.ONE_MINUTE_IN_MILLIS);
		String restSession = TimeMgmt.getFormattedTimeValue(SiegeWarSettings.getWarSiegeBattleSessionsExpiredPhaseDurationMinutes() * TimeMgmt.ONE_MINUTE_IN_MILLIS);
		String maxSiege = TimeMgmt.getFormattedTimeValue(SiegeWarSettings.getWarSiegeMaxHoldoutTimeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS);
		String occupationtime = TimeMgmt.getFormattedTimeValue((long) SiegeWarSettings.getWarSiegeRevoltImmunityTimeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS);
		double counterPercent = SiegeWarSettings.getWarSiegeCounterattackBoosterExtraDeathPointsPerPlayerPercentage();
		boolean bankruptcy = TownySettings.isTownBankruptcyEnabled();
		boolean ruins = TownRuinSettings.getTownRuinsEnabled();
		boolean peaceful = SiegeWarSettings.getWarCommonPeacefulTownsEnabled();
		boolean surrender = SiegeWarSettings.getWarSiegeSurrenderEnabled();
		boolean abandon = SiegeWarSettings.getWarSiegeAbandonEnabled();
		String bannerCost = "zero";
		String plunderCost = "zero";
		if (TownyEconomyHandler.isActive()) {
			bannerCost = TownyEconomyHandler.getFormattedBalance(SiegeWarSettings.getWarSiegeAttackerCostUpFrontPerPlot());
			plunderCost = TownyEconomyHandler.getFormattedBalance(SiegeWarSettings.getWarSiegeAttackerPlunderAmountPerPlot());
		}
		
		/*
		 * Introduction
		 */
		text += "INTRODUCTION\n\n";
		text += "Siege War is a minimally destructive, war-on-demand system, focusing on geo-politics.\n\n";
		text += "Sieges are between nations (who attack,) and towns (who defend.)\n\n";

		/*
		 * Starting a siege
		 */
		text += "\nSTARTING A SIEGE\n\n";
		text += "A nation starts a siege when the king (or a general) places a coloured banner (known as the 'siege banner') just outside the target town.\n\n";
		text += "Town structures & stored items remain safe during the siege (because town perm protections are unaffected.)\n\n";
		text += "The cost to begin a siege is " + bannerCost  + " per plot owned by the Town which would be sieged, paid by the sieging nation. This war chest is eventually won by the siege winner.\n\n";
		if (SiegeWarSettings.getWarSiegeCapitalCostIncreasePercentage() > 0)
			text += "The cost to siege a nation's capital city is " + SiegeWarSettings.getWarSiegeCapitalCostIncreasePercentage() + "% more than non-capital towns.\n\n";
		// Immunity
		text += "Each nation can have a maximum of " + SiegeWarSettings.getWarSiegeMaxActiveSiegeAttacksPerNation() + " attack sieges at any one time.\n\n";
		text += "Recently besieged towns have temporary 'siege immunity', and cannot be sieged.";
		if(SiegeWarSettings.getWarSiegeSiegeImmunityTimeNewTownsHours() > 0)
			text += "New towns also have siege immunity, and cannot be sieged for " + SiegeWarSettings.getWarSiegeSiegeImmunityTimeNewTownsHours() + " hours after creation.\n\n";
		if(peaceful)
			text += "Peaceful towns cannot be sieged at all.";

		/*
		 * Siege participants info
		 */
		text += "SIEGE PARTICIPANTS\n\n";
		text += "There are 2 official military sides in every siege, the attackers, and the defenders.";
		text += "Attackers: Soldiers from the nation who started the siege, and their allies\n\n";
		text += "Defenders: Guards from the besieged town, as well as soldiers from the town's nation (if any) and from that nation's allies.\n\n";
		text += "All participants from each side can contribute fully to scoring.";
		if (SiegeWarSettings.getWarSiegeDeathPenaltyKeepInventoryEnabled()) {
			text += "If a participant dies in the siege zone, their items are kept";
			if (SiegeWarSettings.getWarSiegeDeathPenaltyDegradeInventoryEnabled())
				text+= " but weapon/armour/tool durability is degraded by " + SiegeWarSettings.getWarSiegeDeathPenaltyDegradeInventoryPercentage() + "% each time a player dies.\n\n";
			else
				text+= ".\n\n";
		}

		/*
		 * Scoring info
		 */
		text += "SIEGE SCORING\n\n";
		text += "Players win sieges by holding the ground within " + SiegeWarSettings.getBannerControlHorizontalDistanceBlocks() + " blocks of the siege banner (the 'timed point zone'), and/or by killing enemy soldiers within " + SiegeWarSettings.getWarSiegeZoneRadiusBlocks() + " blocks of the siege banner (the 'siege zone').\n\n";
		text += "Attackers gain " + SiegeWarSettings.getWarSiegePointsForAttackerOccupation() + " base points every 20 seconds for holding the banner. ";
		text += "Defenders gain " + SiegeWarSettings.getWarSiegePointsForDefenderOccupation() + " base points every 20 seconds for holding the banner. ";
		text += "Attackers gain " + SiegeWarSettings.getWarSiegePointsForAttackerDeath() + " base points if an attacker is killed in the siege zone. ";
		text += "Defenders gain " + SiegeWarSettings.getWarSiegePointsForDefenderDeath() + " base points if an defender is killed in the siege zone.\n\n";
		if (!SiegeWarSettings.isWarSiegeCounterattackBoosterDisabled() && counterPercent > 0) {
			text += "If one siege-side has banner control, then the other side will receive a kill point bonus of " + counterPercent + " % for each player on the banner control list.";
			text += "For example, if there are 3 players on the banner control list and one is killed, the enemy side will receive " + (3 * counterPercent) + "% more death points than normal.";
		}
		if (SiegeWarSettings.getWarSiegePopulationBasedPointBoostsEnabled())
			text += "The side of the siege which has a lower population will receive extra siege points.\n\n";


		/*
		 * Siege Area info
		 */
		text += "\nSIEGE AREA EFFECTS\n\n";
		if (SiegeWarSettings.getWarSiegeClaimingDisabledNearSiegeZones())
			text += "No towns will be able to claim land near to Siege Zones.\n";
		if (SiegeWarSettings.getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled())
			text += "Only residents and peaceful town residents will be able to use towny spawn commands to spawn into siege zones.\n";
		if (SiegeWarSettings.getWarSiegeBesiegedTownClaimingDisabled())
			text += "Besieged towns are unable to claim land.\n";
		if (SiegeWarSettings.getWarSiegeBesiegedTownUnClaimingDisabled())
			text += "Besieged towns are unable to unclaim land.\n";
		if (SiegeWarSettings.getWarSiegeBesiegedTownRecruitmentDisabled())
			text += "Besieged towns cannot recruit new members.\n";
		if (SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns())
			text += "PVP is forced on in besieged towns.\n";
		if (SiegeWarSettings.getWarSiegeExplosionsAlwaysOnInBesiegedTowns())
			text += "Explosions are forced on in besieged towns.\n";

		/*
		 * End Game info
		 */
		text += "\nHOW A SIEGE ENDS\n\n";
		text += "Maximum siege duration is " + maxSiege + " (this is important to allow players enough time to respond to siege attacks, especially casual & cross-timezone players.)\n\n";
		text += "At the end of the max siege duration, a winner is chosen based on the points (positive points mean the attacker wins, negative points mean the defender wins).\n\n";

		/*
		 * Abandon & Surrender
		 */
		if (surrender || abandon) {
			text += "A siege can also end early,";
			if (abandon)
				text += " if the attacking king (or a general) abandons the attack, by placing a white banner outside the town";
			if (surrender && abandon)
				text += ", or";
			if (surrender)
				text += " if the defending mayor surrenders, by placing a white banner inside the town";
			text += ".\n";
			if (abandon)
				text += "Abandoning a siege can be done " + SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeAbandonHours() + " hours into a siege.\n";
			if (surrender)
				text += "Surrendering a siege can be done " + SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeSurrenderHours() + " hours into a siege.\n";
			text += "\n";
		}

		// Plundering
		if (SiegeWarSettings.getWarSiegePlunderEnabled()) {
			text += "\nPOST-SIEGE ACTION - TOWN PLUNDER\n\n";
			text += "A town can be plundered after being defeated in a siege, by the attacking king (or a general) placing a chest just outside the town.";
			text += "This action will take money from the defeated town (" + plunderCost + " per townblock, and transfer it to the victorious nation.";
			if (bankruptcy)
				text += "If the town runs out of money it will not be destroyed, but rather set to a 'bankrupt' state, where the town cannot recruit, claim, or build until the debt is repaid.\n";
			else
				text += "If the town runs out of money it will be destroyed.";
			if (ruins)
				text += "If a town is ultimately destroyed, the town will be placed into a Ruined state, where town structures can be destroyed and items stolen from chests. \n\n";
			else
				text += "If a town is ultimately destroyed, that town will be deleted immediately.\n\n";
		}

		// Capturing
		text += "\nPOST-SIEGE ACTION - TOWN CAPTURE\n\n";
		if (SiegeWarSettings.getWarSiegeInvadeEnabled()) {
			text += "If the attacker has won the siege, the king (or a general) of the attacking nation may place a second coloured banner outside the town. This will capture the town, and forcibly add it to the victorious nation (which it cannot leave for " + occupationtime + ").\n\n";

			if (SiegeWarSettings.getWarSiegeTownLeaveDisabled())
				text += "An occupied town cannot leave their nation, but the nation can kick the town if they so desire.\n";

			// Revolt
			if (SiegeWarSettings.getWarSiegeRevoltEnabled()) {
				text += "An occupied town can revolt after " + occupationtime + ", freeing themselves from the occupying nation.\n";
				text += "A town which has revolted from their occupying nation will receive " + SiegeWarSettings.getWarSiegeRevoltImmunityTimeHours() + " hours of siege immunity.\n\n";
			} else
				text += "An occupied town can not revolt from their occupying nation.\n\n";
		}

		// Peaceful towns.
		if (peaceful) {
			text += "\nPEACEFUL TOWNS\n\n";
			text += "Peaceful towns can opt out of war (by toggling peaceful, a town receives immunity from siege attacks & taxes. In return, its nation choice is more restricted, and its residents suffer from 'war allergy' if they approach a siege zone).\n";
			text += "When a town chooses to toggle their peaceful status, it will take " + SiegeWarSettings.getWarCommonPeacefulTownsConfirmationRequirementDays() + " days for their decision to take effect.\n";
			text += "Peaceful towns " + (SiegeWarSettings.getWarCommonPeacefulTownsAllowedToMakeNation() ? "are" : "are not") + " allowed to make nations.\n";
			text += "Peaceful towns " + (SiegeWarSettings.getWarCommonPeacefulTownsAllowedToTogglePVP() ? "are" : "are not") + " allowed to toggle their pvp status.\n";
			text += "Peaceful towns may fall under the jurisdiction of a Guardian Town. Guardian Towns are towns which have " + SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement() + " townblocks claimed, which are within " + SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement() + " townblocks of the peaceful town.\n";
			text += "When a Guardian Town is within this distance the peaceful town will join the Guardian Town's nation.\n\n";
		}


		//Nation Refund
		if (SiegeWarSettings.getWarSiegeRefundInitialNationCostOnDelete()) {
			text += "MISCELLANEOUS FEATURE - NATION REFUNDS:\n\n";
			text += "Nations which are disbanded for any reason (upkeep, plunder, capture) will be refunded " + SiegeWarSettings.getWarSiegeNationCostRefundPercentageOnDelete() + "% of the nation cost, collected using '/sw nation refund'.\n";
		}

		// Map Sneaking
		if (SiegeWarSettings.getWarSiegeMapSneakingEnabled()) {
			text += "MISCELLANEOUS FEATURE - MAP SNEAKING:\n\n";
			text += "\nMap Sneaking is a tactical system used to hide players from being seen on the server Dynmap website. ";
			text += "Players who are on the Banner Control list cannot map sneak. ";
			text += "To map sneak the player must be holding a combination of items in their hands, which are: \n";
			for (HeldItemsCombination combo : SiegeWarSettings.getWarSiegeMapSneakingItems()) {
				text += " - " + combo.getMainHandItemType().name() + " & " + combo.getOffHandItemType().name() + "\n";
			}
			text += "\n";
		}

		//Battle sessions
		if(SiegeWarSettings.isWarSiegeBattleSessionsEnabled()) {
			text += "MISCELLANEOUS FEATURE - BATTLE SESSIONS:\n\n";
			text += "Fighting is organised into " + activeSession + " 'battle sessions' for each player. After each session, the player gets a " + restSession + " enforced break from combat (moderating fatigue.)\n\n";
		}


		return text;
	}
}
