package com.gmail.goosius.siegewar.playeractions;


import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translator;
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
		final Translator translator = Translator.locale(player);

		if (!SiegeWarSettings.getConquestSiegesEnabled()
		|| !TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_STARTCONQUESTSIEGE.getNode()))
			throw new TownyException(translator.of("msg_err_action_disable"));

		if (targetTown.hasNation()) {
			Nation nationOfDefendingTown = targetTown.getNationOrNull();

			if (nationOfSiegeStarter == nationOfDefendingTown)
				throw new TownyException(translator.of("msg_err_siege_war_cannot_attack_town_in_own_nation"));

			if (!nationOfSiegeStarter.hasEnemy(nationOfDefendingTown))
				throw new TownyException(translator.of("msg_err_siege_war_cannot_attack_non_enemy_nation"));
		}

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByWorld(nationOfSiegeStarter, targetTown);

		SiegeWarDistanceUtil.throwIfTownIsTooFarFromNationCapitalByDistance(nationOfSiegeStarter, targetTown);

		SiegeWarNationUtil.throwIfNationHasTooManyTowns(nationOfSiegeStarter);
	}

}
