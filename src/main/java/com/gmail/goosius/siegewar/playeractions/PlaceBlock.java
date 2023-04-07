package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarImmunityUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownPeacefulnessUtil;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.format.NamedTextColor;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translator;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

/**
 * This class is fired from the SiegeWarActionListener's TownyBuildEvent listener.
 *
 * The class evaluates the event, and determines if it is siege related.
 * 
 * If the place block event is determined to be a siege action,
 * this class then calls an appropriate class/method in the 'playeractions' package
 *
 * @author Goosius
 */
public class PlaceBlock {

	/**
	 * Evaluates a block placement request.
	 * If the block is a standing banner or chest, this method calls an appropriate private method.
	 *
	 * @param player The player placing the block
	 * @param block The block about to be placed
	 * @param event The event object related to the block placement
	 */
	public static void evaluateSiegeWarPlaceBlockRequest(Player player, Block block, TownyBuildEvent event) {
		final Translator translator = Translator.locale(player);
		try {
			//Ensure siege is enabled in this world
			if (!TownyAPI.getInstance().getTownyWorld(block.getWorld()).isWarAllowed())
				return;

			//Check if material is banner or chest
			Material mat = block.getType();
			if (Tag.BANNERS.isTagged(mat) && !mat.toString().contains("_W")) {
				try {
					//Standing Banner placement				
					if (evaluatePlaceStandingBanner(player, block)) {
						event.setCancelled(false);
						return;  //Special banner placement
					}
				} catch (TownyException e1) {
					Messaging.sendErrorMsg(player, e1.getMessage(player));
				}

			} else if (mat == Material.CHEST || mat == Material.TRAPPED_CHEST) {
				try {
					//Chest placement
					evaluatePlaceChest(player, block);
				} catch (TownyException e) {
					Messaging.sendErrorMsg(player, e.getMessage(player));
				}
			}

			//Trap warfare block protection
			Siege nearbySiege = SiegeController.getActiveSiegeAtLocation(event.getLocation());
			if(qualifiesAsTrapWarfareMitigation(event, nearbySiege)) {
				event.setCancelled(true);
				TownyMessaging.sendActionBarMessageToPlayer(player, Component.text(translator.of("msg_err_cannot_alter_blocks_near_siege_banner", NamedTextColor.DARK_RED)));
				return;
			}

			//Forbidden material placement prevention
			if(qualifiesAsSiegeZoneForbiddenMaterial(block, mat, nearbySiege))
				throw new TownyException(translator.of("msg_war_siege_zone_block_placement_forbidden"));

		} catch (TownyException e) {
			event.setCancelled(true);
			event.setCancelMessage(e.getMessage(player));
		}
	}

	private static boolean qualifiesAsTrapWarfareMitigation(TownyBuildEvent event, Siege nearbySiege) {
		return nearbySiege != null
			&& SiegeWarSettings.isTrapWarfareMitigationEnabled()
			&& SiegeWarDistanceUtil.isTargetLocationProtectedByTrapWarfareMitigation(event.getLocation(), nearbySiege);
	}

	private static boolean qualifiesAsSiegeZoneForbiddenMaterial(Block block, Material mat, Siege nearbySiege) {
		return nearbySiege != null
			&& SiegeWarSettings.getSiegeZoneWildernessForbiddenBlockMaterials().contains(mat)
			&& TownyAPI.getInstance().isWilderness(block);
	}

	/**
	 * Evaluates placing a standing banner
	 * @throws TownyException if the banner will not be allowed.
	 */
	private static boolean evaluatePlaceStandingBanner(Player player, Block block) throws TownyException {
		final Translator translator = Translator.locale(player);
		//Ensure the the banner is placed in wilderness
		if (!TownyAPI.getInstance().isWilderness(block))
			return false;

		//Ensure the player has a town
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null || !resident.hasTown())
			throw new TownyException(translator.of("msg_err_siege_war_action_not_a_town_member"));

		//Get resident's town and possibly their nation
		Town residentsTown = resident.getTownOrNull();
		Nation residentsNation = resident.getNationOrNull();
		
		//Ensure there is at least 1 adjacent town
		List<TownBlock> adjacentCardinalTownBlocks = SiegeWarBlockUtil.getCardinalAdjacentTownBlocks(block);
		List<TownBlock> adjacentNonCardinalTownBlocks = SiegeWarBlockUtil.getNonCardinalAdjacentTownBlocks(block);
		if(adjacentCardinalTownBlocks.size() == 0 && adjacentNonCardinalTownBlocks.size() == 0)
			return false;

		//Ensure there is just one cardinal town block
		if (adjacentCardinalTownBlocks.size() > 1)
			throw new TownyException(translator.of("msg_err_siege_war_too_many_adjacent_towns"));

		//Get 1st nearby townblock
		TownBlock townBlock = adjacentCardinalTownBlocks.size() > 0
			? adjacentCardinalTownBlocks.get(0)
			: adjacentNonCardinalTownBlocks.get(0);

		if (isWhiteBanner(block)) {
			evaluatePlaceWhiteBannerNearTown(player, residentsTown, residentsNation, townBlock.getTownOrNull());
		} else {
			evaluatePlaceColouredBannerNearTown(player, residentsTown, residentsNation, townBlock, townBlock.getTownOrNull(), block);
		}
		return true;
	}

		/**
         * Evaluates placing a white banner near a town
         *
         * Effects depend on the nature of the siege (if any) and the allegiances of the banner placer
		 *
         * @param player the player placing the banner
         * @param residentsTown the town of the player placing the banner
		 * @param residentsNation the nation of the player placing the banner (can be null)
		 * @param nearbyTown the nearby town
         */
	private static void evaluatePlaceWhiteBannerNearTown(Player player,
														 Town residentsTown,
														 Nation residentsNation,
														 Town nearbyTown) throws TownyException {
		final Translator translator = Translator.locale(player);
		//Ensure that there is a siege
		if (!SiegeController.hasSiege(nearbyTown))
			throw new TownyException(translator.of("msg_err_town_cannot_end_siege_as_no_siege"));

		//Get siege
		Siege siege = SiegeController.getSiege(nearbyTown);
		if(siege.getStatus() != SiegeStatus.IN_PROGRESS)
			throw new TownyException(translator.of("msg_err_town_cannot_end_siege_as_finished"));

		/*
		 * Check what type of action this qualifies as.
		 * Depending on the scenario, it may be 'abandonAttack' or 'surrenderDefence'
		 */
		if (residentsNation != null && residentsNation == siege.getAttacker()) {
			//Member of attacking nation
			AbandonAttack.processAbandonAttackRequest(player, siege);
		} else if (residentsTown == nearbyTown) {
			//Member of defending town
			SurrenderDefence.processSurrenderDefenceRequest(player, siege);
		} else {
			throw new TownyException(translator.of("msg_err_action_disable"));
		}
	}

	/**
	 * Evaluates coloured banner near a town
	 *
	 * Effects depend on multiple factors, including:
	 *  - Whether the town is peaceful or not
	 *  - Whether there is already a siege at the town.
	 *  - The allegiances of the town and of the banner placer
	 *
	 * @param player the player placing the banner
	 * @param residentsTown the town of the player placing the banner
	 * @param residentsNation the nation of the player placing the banner (can be null)
	 * @param nearbyTownBlock the nearby town block
	 * @param nearbyTown the nearby town
	 * @param bannerBlock the banner block
	 */
	public static void evaluatePlaceColouredBannerNearTown(Player player,
														   Town residentsTown,
														   Nation residentsNation,
														   TownBlock nearbyTownBlock,
														   Town nearbyTown,
														   Block bannerBlock) throws TownyException {
		if(!nearbyTown.isAllowedToWar())
			throw new TownyException(Translatable.of("msg_err_this_town_is_not_allowed_to_be_attacked"));
		
		if(SiegeWarTownPeacefulnessUtil.isTownPeaceful(nearbyTown)) {
			//Town is peaceful, so this action is a subversion or peaceful-revolt attempt
			if(residentsTown == nearbyTown) {
				if(TownOccupationController.isTownOccupied(residentsTown)) {
					throw new TownyException(Translatable.of("neutral_towns_cannot_revolt"));
				}
			} else {
				PeacefullySubvertTown.processActionRequest(player, residentsNation, nearbyTown);
			}
		} else {
			//Town is not peaceful, so this action is a start-siege or invade-town request
			if (SiegeController.hasSiege(nearbyTown)) {
				//If there is a siege, it is an attempt to invade the town
				InvadeTown.processInvadeTownRequest(player, residentsNation, nearbyTown, SiegeController.getSiege(nearbyTown));
			} else {
				//If there is no siege, it is an attempt to start a new siege
				evaluateStartNewSiegeAttempt(player, residentsTown, residentsNation, nearbyTownBlock, nearbyTown, bannerBlock);
			}
		}
	}

	/**
	 * Evaluate an attempt to start a new siege on the nearby town
	 *
	 * Synchronized so that players cannot circumvent restrictions,
	 * e.g. by attacking two towns at the exact same moment.
	 */
	private static synchronized void evaluateStartNewSiegeAttempt(Player player,
																  Town residentsTown,
																  Nation residentsNation,
													 			  TownBlock nearbyTownBlock,
													 			  Town nearbyTown,
													 			  Block bannerBlock) throws TownyException {
		final Translator translator = Translator.locale(player);
		if (nearbyTown.isRuined())
			throw new TownyException(translator.of("msg_err_cannot_start_siege_at_ruined_town"));

		if(SiegeWarBlockUtil.isSupportBlockUnstable(bannerBlock))
			throw new TownyException(translator.of("msg_err_siege_war_banner_support_block_not_stable"));

		if (!SiegeWarSettings.doesTodayAllowASiegeToStart())
			throw new TownyException(translator.of("msg_err_cannot_start_sieges_today"));

		if (residentsTown == nearbyTown) {
			// Throws exception if town cannot pay.
			SiegeWarMoneyUtil.throwIfTownCannotAffordToStartSiege(nearbyTown);
			//Start Revolt siege
			StartRevoltSiege.processStartSiegeRequest(player, residentsTown, residentsNation, nearbyTownBlock, nearbyTown, bannerBlock);
			//Immediately remove occupation
			TownOccupationController.removeTownOccupation(nearbyTown);
		} else {
			if (residentsNation == null)
				throw new TownyException(translator.of("msg_err_siege_war_action_not_a_nation_member"));

			if (SiegeWarImmunityUtil.isTownSiegeImmune(nearbyTown))
				throw new TownyException(translator.of("msg_err_cannot_start_siege_due_to_siege_immunity"));

			// Throws exception if nation cannot pay.
			SiegeWarMoneyUtil.throwIfNationCannotAffordToStartSiege(residentsNation, nearbyTown);

			if (SiegeWarSettings.doesThisNationHaveTooManyActiveSieges(residentsNation))
				throw new TownyException(translator.of("msg_err_siege_war_nation_has_too_many_active_siege_attacks"));

			//Conquest siege
			StartConquestSiege.processStartSiegeRequest(player, residentsTown, residentsNation, nearbyTownBlock, nearbyTown, bannerBlock);
		}
	}

	/**
	 * Evaluates placing a chest.
	 * Determines if the event will be considered as a plunder request.
	 * @throws TownyException when the chest is not allowed to be placed.
	 */
	private static void evaluatePlaceChest(Player player, Block block) throws TownyException {
		if (!SiegeWarSettings.getWarSiegePlunderEnabled() || !TownyAPI.getInstance().isWilderness(block))
			return;
		
		final Translator translator = Translator.locale(player);
		
		if(!TownyEconomyHandler.isActive())
			throw new TownyException(translator.of("msg_err_siege_war_cannot_plunder_without_economy"));

		Set<Siege> adjacentSieges = SiegeWarBlockUtil.getAllAdjacentSieges(block);
		
		//If there are no sieges nearby, do normal block placement
		if(adjacentSieges.size() == 0)
			return;

		//Ensure there is only one adjacent siege
		if(adjacentSieges.size() > 1)
			throw new TownyException(translator.of("msg_err_siege_war_too_many_adjacent_towns"));

		//Attempt plunder.
		PlunderTown.processPlunderTownRequest(player, adjacentSieges.iterator().next());
	}
	
	private static boolean isWhiteBanner(Block block) {
		return block.getType() == Material.WHITE_BANNER  && ((Banner) block.getState()).getPatterns().size() == 0;
	}

}

