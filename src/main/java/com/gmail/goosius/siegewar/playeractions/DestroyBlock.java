package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockProtectionUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translator;

import org.bukkit.block.Block;


/**
 * This class handles siege-related destroy-block requests
 *
 * @author Goosius
 */
public class DestroyBlock {

	/**
	 * Evaluates a block destroy request.
	 *
	 * @param event The event object
	 * @throws TownyException if something is misconfigured
	 */
	public static void evaluateSiegeWarDestroyBlockRequest(TownyDestroyEvent event) throws TownyException {
		//Ensure siege is enabled in this world
		Block block = event.getBlock();
		if (!TownyAPI.getInstance().getTownyWorld(block.getWorld()).isWarAllowed())
			return;

		final Translator translator = Translator.locale(event.getPlayer());

		//Get nearby siege
		Siege nearbySiege = SiegeController.getActiveSiegeAtLocation(event.getLocation());
		if(nearbySiege == null) {
			// Prevent destruction of siege camp banner or support block
			if (qualifiesAsSiegeCamp(event)) {
				event.setCancelMessage(translator.of("msg_err_siege_war_cannot_destroy_siege_camp_banner"));
				event.setCancelled(true);
			}
			return;
		}

		//Trap warfare block protection
		if(event.hasTownBlock()) {
			//Trap warfare besieged-town block protection
			if (SiegeWarSettings.isBesiegedTownTownTrapWarfareMitigationEnabled()
					&& SiegeWarBlockProtectionUtil.isTownLocationProtectedByTrapWarfareMitigation(event.getLocation(), event.getTownBlock().getTown())) {
				event.setCancelled(true);
				event.setCancelMessage(translator.of("msg_err_cannot_alter_blocks_near_siege_banner"));
			}
		} else {
			//Trap warfare wilderness block protection
			if(SiegeWarSettings.isWildernessTrapWarfareMitigationEnabled()) {
				//Trap warfare wilderness block protection
				if (SiegeWarBlockProtectionUtil.isWildernessLocationProtectedByTrapWarfareMitigation(event.getLocation(), nearbySiege)) {
					event.setCancelled(true);
					event.setCancelMessage(translator.of("msg_err_cannot_alter_blocks_near_siege_banner"));
					return;
				}
			}
		}

		//Prevent destruction of siege-banner or support block
		if (qualifiesAsBreakingASiegeBanner(event, nearbySiege)) {
			event.setCancelMessage(translator.of("msg_err_siege_war_cannot_destroy_siege_banner"));
			event.setCancelled(true);
			return;
		}

		// Prevent destruction of siege camp banner or support block
		if (qualifiesAsSiegeCamp(event)) {
			event.setCancelMessage(translator.of("msg_err_siege_war_cannot_destroy_siege_camp_banner"));
			event.setCancelled(true);
			return;
		}
	}

	private static boolean qualifiesAsSiegeCamp(TownyDestroyEvent event) {
		return SiegeWarSettings.areSiegeCampsEnabled()
			&& SiegeWarBlockUtil.isBlockNearAnActiveSiegeCampBanner(event.getBlock());
	}

	private static boolean qualifiesAsBreakingASiegeBanner(TownyDestroyEvent event, Siege nearbySiege) {
		return nearbySiege.isFlagBannerOrBlockBelow(event.getBlock());
	}

}
