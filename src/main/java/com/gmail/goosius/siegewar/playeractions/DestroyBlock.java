package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.format.NamedTextColor;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Translation;
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

		final Translator translator = Translator.locale(Translation.getLocale(event.getPlayer()));

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

		// Trap warfare block protection
		if (qualifiesAsTrapWarfare(event, nearbySiege)) {
			TownyMessaging.sendActionBarMessageToPlayer(event.getPlayer(), Component.text(translator.of("msg_err_cannot_alter_blocks_near_siege_banner"), NamedTextColor.DARK_RED));
			event.setCancelled(true);
			return;
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

	private static boolean qualifiesAsTrapWarfare(TownyDestroyEvent event, Siege nearbySiege) {
		return SiegeWarSettings.isTrapWarfareMitigationEnabled()
			&& SiegeWarDistanceUtil.isTargetLocationProtectedByTrapWarfareMitigation(event.getLocation(), nearbySiege);
	}

}
