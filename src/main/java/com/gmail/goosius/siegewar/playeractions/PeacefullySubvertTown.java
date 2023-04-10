package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSubvertTownEvent;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyInventory;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This class is responsible for processing requests by nations to peacefully 'subvert' towns.
 *
 * If such a request is successful, the target town gets subverted immediately a.k.a occupied.
 *
 * @author Goosius
 */
public class PeacefullySubvertTown {

	/**
	 * Process a subvert town request
	 *
	 * @param player the player attempting the subvert.
	 * @param residentsNation the nation of the player (can be null)
	 * @param targetPeacefulTown the target peaceful town. We know the town is peaceful, and the player is not a resident.
	 *
	 * @throws TownyException if subvert is not allowed
	 */
	public static void processActionRequest(Player player, Nation residentsNation, Town targetPeacefulTown) throws TownyException {
		// Throws an exception if the peaceful subversion of this town would not be allowed.
		allowSubversionOrThrow(player, residentsNation, targetPeacefulTown);

		PreSubvertTownEvent preEvent = new PreSubvertTownEvent(player, residentsNation, targetPeacefulTown);
		Bukkit.getPluginManager().callEvent(preEvent);
		if (preEvent.isCancelled()) {
			if (!preEvent.getCancellationMsg().isEmpty())
				Messaging.sendErrorMsg(player, preEvent.getCancellationMsg());
		} else {
			//Subvert town now
			subvertTown(residentsNation, targetPeacefulTown);
		}
	}
	
	/**
	 * Subvert the town
	 *
	 * @param subvertingNation the nation doing the subverting
	 * @param targetTown the target town
	 */
	private static void subvertTown(Nation subvertingNation, Town targetTown) {
		/*
		 * Messaging
		 * This section is here rather than the customary bottom of the method
		 * because we want to send the siegewar messages (town invaded, nation defeated etc.)
		 * before we send the standard towny messages (town has left nation, nation has been deleted etc.)
		 */
		Messaging.sendGlobalMessage(
			Translatable.of("msg_peaceful_town_subverted",
					targetTown.getName(),
					subvertingNation.getName()
		));
		Nation nationOfSubvertedTown = targetTown.getNationOrNull();
		if(nationOfSubvertedTown != null && nationOfSubvertedTown.getNumTowns() == 1) {
			Messaging.sendGlobalMessage(
					Translatable.of("msg_siege_war_nation_defeated", nationOfSubvertedTown.getName()));
		}

		//Occupy town (also saves data)
		TownOccupationController.setTownOccupation(targetTown, subvertingNation);
	}

	private static void allowSubversionOrThrow(Player player, Nation residentsNation, Town targetPeacefulTown) throws TownyException {
		if(!targetPeacefulTown.hasHomeBlock())
			return;  //If you don't have a homeblock, insta-subvert, otherwise we cannot check distance to any guardian towns

		final Translator translator =  Translator.locale(player);

		if(!SiegeWarSettings.isPeacefulTownsSubvertEnabled())
			throw new TownyException(translator.of("msg_err_action_disable"));

		if(residentsNation == null)
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_nation_member"));  //Can't subvert if nationless

		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_SUBVERTPEACEFULTOWN.getNode()))
			throw new TownyException(translator.of("msg_err_cannot_subvert_not_enough_permissions"));

		if(SiegeController.hasActiveSiege(targetPeacefulTown))
			throw new TownyException(translator.of("msg_err_cannot_change_occupation_of_besieged_town"));

		if(TownOccupationController.isTownOccupiedByNation(residentsNation, targetPeacefulTown))
			throw new TownyException(translator.of("msg_err_cannot_subvert_town_already_occupied"));

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByWorld(residentsNation, targetPeacefulTown);

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByDistance(residentsNation, targetPeacefulTown);

		SiegeWarNationUtil.throwIfNationHasTooManyTowns(residentsNation);
		
		SiegeWarNationUtil.throwIfNationHasTooManyTowns(residentsNation);

		throwIfGuardianTownExistsAndSubverterDoesNotOwnIt(targetPeacefulTown, residentsNation);
	}

	/**
	 * Throw if the given nation does not own the guardian town of the target peaceful town.
	 * 
	 * @param subvertingNation the nation attempting the subversion
	 */
	public static void throwIfGuardianTownExistsAndSubverterDoesNotOwnIt(Town targetPeacefulTown, Nation subvertingNation) throws TownyException {
		Town guardianTown = calculateGuardianTown(targetPeacefulTown);
		
		if(guardianTown == null)
			return;  //There is no guardian town. Subversion allowed
		
		if(!guardianTown.hasNation())
			throw new TownyException("Cannot subvert because your nation does not own the guardian town of this peaceful town. The guardian town is: %s.");
	
		if(guardianTown.getNationOrNull() != subvertingNation)
			throw new TownyException("Cannot subvert because your nation does not own the guardian town of this peaceful town. The guardian town is: %s");
	}
	
	private static @Nullable Town calculateGuardianTown(Town peacefulTown) {
		int guardianTownPlotsRequirement = SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement();
		
		Town guardianTown = null;
		int winningDistanceInTownBlocks = SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement() + 1;  //A candidate guardian town must beat this distance (be less) to become leading candidate
		for(Town candidateGuardianTown: TownyAPI.getInstance().getTowns()) {
			if(!candidateGuardianTown.isRuined()
				&& candidateGuardianTown.hasHomeBlock()
				&& !SiegeWarTownPeacefulnessUtil.isTownPeaceful(candidateGuardianTown)
				&& !SiegeController.hasActiveSiege(candidateGuardianTown) 
				&& SiegeWarDistanceUtil.areTownsClose(peacefulTown, candidateGuardianTown, winningDistanceInTownBlocks)) {

				guardianTown = candidateGuardianTown;
				winningDistanceInTownBlocks = getDistanceInTownBlocks(peacefulTown, candidateGuardianTown);
				distanceToCandidateGuardianTownInTownBlocks = 
			}
			{
				
				distanceToCandidateGuardianTown = SiegeWarDistanceUtil.areLocationsCloseHorizontally()
				
			}
					&& town.getNumTownBlocks() >= guardianTownPlotsRequirement
		}
		
		return guardianTown;
	}


	public static Set<Town> getValidGuardianTowns(Town peacefulTown) {
		Set<Town> validGuardianTowns = new HashSet<>();
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		try {
			int guardianTownPlotsRequirement = SiegeWarSettings.getPeacefulTownsGuardianTownPlotsRequirement();
			int guardianTownMaxDistanceRequirementTownblocks = SiegeWarSettings.getPeacefulTownsGuardianTownMinDistanceRequirement();

			//Find valid guardian towns
			List<Town> candidateTowns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
			for(Town candidateTown: candidateTowns) {
				if(!candidateTown.isNeutral()
						&& candidateTown.hasNation()
						&& candidateTown.getNation().isOpen()
						&& candidateTown.getTownBlocks().size() >= guardianTownPlotsRequirement
						&& SiegeWarDistanceUtil.areTownsClose(peacefulTown, candidateTown, guardianTownMaxDistanceRequirementTownblocks)) {
					validGuardianTowns.add(candidateTown);
				}
			}
		} catch (Exception e) {
			try {
				System.err.println("Problem getting valid guardian towns for - " + peacefulTown.getName());
			} catch (Exception e2) {
				System.err.println("Problem getting valid guardian towns (could not read peaceful town name)");
			}
			e.printStackTrace();
		}

		//Return result
		return validGuardianTowns;
	}
	
	/**
	 * Verify if the given nation has enough Towny-Influence to subvert the given town
	 *
	 * @param nation the nation attempting subversion
	 * @param targetTown the town targeted for subversion
	 *
	 * @throws TownyException if the nation does not have enough Towny-Influence
	 */
	private static void verifyThatNationHasEnoughTownyInfluenceToSubvertTown(Nation nation, Town targetTown) throws TownyException {
		Map<Nation, Integer> townyInfluenceMap = SiegeWarTownPeacefulnessUtil.calculateTownyInfluenceMap(targetTown);
		if(townyInfluenceMap.size() == 0)
			//No nation has towny-influence in the local area
			throw new TownyException(Translatable.of("msg_err_cannot_subvert_town_zero_influence"));

		Nation topNation = townyInfluenceMap.keySet().iterator().next();
		if(topNation != nation)
			//A different nation is top of the towny-influence map
			throw new TownyException(Translatable.of("msg_err_cannot_subvert_town_insufficient_influence", topNation.getName(),
					townyInfluenceMap.get(topNation),            // Top scorer. 
					townyInfluenceMap.getOrDefault(nation, 0))); // The nation's score.
	}
}
