package com.gmail.goosius.siegewar.playeractions;


import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.util.MathUtil;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to start conquest sieges
 *
 * @author Goosius
 */
public class StartConquestSiege {

	/**
	 * Process a start conquest siege request.
	 *
	 * At this point we know that the resident has a nation and the town is not occupied
	 *
	 * This method does some final checks and if they pass, the siege is initiated.
	 *
	 * @param player the player
	 * @param townOfSiegeStarter town
	 * @param nationOfSiegeStarter nation which is attacking.
	 * @param townBlock the townblock where the attack is taking place.
	 * @param targetTown the town about to be attacked
	 * @param bannerBlock the banner block
	 *
	 * @throws TownyException when attack cannot be made.
	 *
	 */
    public static void processStartSiegeRequest(Player player,
												Town townOfSiegeStarter,
												Nation nationOfSiegeStarter,
												TownBlock townBlock,
												Town targetTown,
												Block bannerBlock) throws TownyException {

		// Allow siege or throw TownyException.
		allowSiegeOrThrow(player, nationOfSiegeStarter, targetTown);

		// Start a SiegeCamp that will kick off the Siege (or if SiegeAssemblies are disabled, start the Siege immediately.)
		SiegeController.startSiegeCampProcess(player, bannerBlock, SiegeType.CONQUEST, targetTown, nationOfSiegeStarter, targetTown, townOfSiegeStarter, townBlock);
	}

	private static void allowSiegeOrThrow(Player player, Nation nationOfSiegeStarter, Town targetTown) throws TownyException {
		final Translator translator = Translator.locale(Translation.getLocale(player));

		if (!SiegeWarSettings.getConquestSiegesEnabled()
		|| !TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToStartSiege(SiegeType.CONQUEST)))
			throw new TownyException(translator.of("msg_err_action_disable"));

		if (targetTown.hasNation()) {
			Nation nationOfDefendingTown = targetTown.getNationOrNull();

			if (nationOfSiegeStarter == nationOfDefendingTown)
				throw new TownyException(translator.of("msg_err_siege_war_cannot_attack_town_in_own_nation"));

			if (!nationOfSiegeStarter.hasEnemy(nationOfDefendingTown))
				throw new TownyException(translator.of("msg_err_siege_war_cannot_attack_non_enemy_nation"));
		}

		if(SiegeWarDistanceUtil.isTownTooFarFromNationCapitalByWorld(nationOfSiegeStarter, targetTown))
			throw new TownyException(translator.of("msg_err_nation_homeblock_in_another_world"));

		if(SiegeWarDistanceUtil.isTownTooFarFromNationCapitalByDistance(nationOfSiegeStarter, targetTown))
			throw new TownyException(String.format(translator.of("msg_err_town_not_close_enough_to_nation"), targetTown.getName()));

		if(SiegeWarNationUtil.doesNationHaveTooManyTowns(nationOfSiegeStarter))
			throw new TownyException(String.format(translator.of("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
	}

	private static boolean townsAreTooFarApart(Nation residentsNation, Town targetTown) throws TownyException {
		return MathUtil.distance(residentsNation.getCapital().getHomeBlock().getCoord(), targetTown.getHomeBlock().getCoord()) > TownySettings.getNationRequiresProximity();
	}

	private static boolean townsAreNotInTheSameWorld(Nation nation, Town targetTown) throws TownyException {
		return !nation.getCapital().getHomeBlock().getWorld().getName().equals(targetTown.getHomeBlock().getWorld().getName());
	}
}
