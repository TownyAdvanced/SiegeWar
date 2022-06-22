package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.util.MathUtil;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to start liberation sieges
 *
 * @author Goosius
 */
public class StartLiberationSiege {

    /**
     * Process a start liberation siege request.
     * <p>
     * At this point we know that
     * - the resident has a nation
     * - the town is occupied,
     * - the player's nation is not the occupier.
     * <p>
     * This method does some final checks and if they pass, the siege is initiated.
     *
     * @param player               the player
     * @param townOfSiegeStarter   town
     * @param nationOfSiegeStarter nation which is attacking.
     * @param occupierNation 
     * @param townBlock            the townblock where the attack is taking place.
     * @param targetTown           the town about to be attacked
     * @param bannerBlock          the banner block
     * @throws TownyException when attack cannot be made.
     */
    public static void processStartSiegeRequest(Player player,
                                                Town townOfSiegeStarter,
                                                Nation nationOfSiegeStarter,
                                                Nation occupierNation,
                                                TownBlock townBlock,
                                                Town targetTown,
                                                Block bannerBlock) throws TownyException {

		// Allow the siege or throw a TownyException. 
		allowSiegeOrThrow(player, nationOfSiegeStarter, occupierNation, targetTown);

		SiegeCamp camp = new SiegeCamp(player, bannerBlock, SiegeType.LIBERATION, targetTown, nationOfSiegeStarter, occupierNation, townOfSiegeStarter, townBlock);

		PreSiegeCampEvent event = new PreSiegeCampEvent(camp);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			throw new TownyException(event.getCancellationMsg());

		if (SiegeWarSettings.areSiegeCampsEnabled())
			// Launch a SiegeCamp, a (by default) 10 minute minigame. If successful the Siege will be initiated in ernest.
			SiegeController.beginSiegeCamp(camp);
		else
			// SiegeCamps are disabled, just do the Siege.
			camp.startSiege();
	}

	private static void allowSiegeOrThrow(Player player, Nation nationOfSiegeStarter, Nation occupierNation, Town targetTown) throws TownyException {
		final Translator translator = Translator.locale(Translation.getLocale(player));
		if (!SiegeWarSettings.getLiberationSiegesEnabled() 
		|| !TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.getPermissionNodeToStartSiege(SiegeType.LIBERATION)))
			throw new TownyException(translator.of("msg_err_action_disable"));

        if (!nationOfSiegeStarter.hasEnemy(occupierNation))
            throw new TownyException(translator.of("msg_err_siege_war_cannot_attack_occupied_town_non_enemy_nation"));

		if (TownySettings.getNationRequiresProximity() > 0) {
			if (townsAreNotInTheSameWorld(nationOfSiegeStarter, targetTown))
				throw new TownyException(translator.of("msg_err_nation_homeblock_in_another_world"));

			if (townsAreTooFarApart(nationOfSiegeStarter, targetTown))
				throw new TownyException(translator.of("msg_err_town_not_close_enough_to_nation", targetTown.getName()));
		}
	}

	private static boolean townsAreTooFarApart(Nation residentsNation, Town targetTown) throws TownyException {
		return MathUtil.distance(residentsNation.getCapital().getHomeBlock().getCoord(), targetTown.getHomeBlock().getCoord()) > TownySettings.getNationRequiresProximity();
	}

	private static boolean townsAreNotInTheSameWorld(Nation nation, Town targetTown) throws TownyException {
		return !nation.getCapital().getHomeBlock().getWorld().getName().equals(targetTown.getHomeBlock().getWorld().getName());
	}
}
