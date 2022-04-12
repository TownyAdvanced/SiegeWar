package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarAllegianceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarBlockUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarWallBreachUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;


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

        //Trap warfare block protection
        if(SiegeWarSettings.isTrapWarfareMitigationEnabled()
                && SiegeWarDistanceUtil.isLocationInSiegeZoneWildernessAndBelowSiegeBannerAltitude(event.getBlock().getLocation(), SiegeWarSettings.isTrapWarfareMitigationNearBannerOnly())) {
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + translator.of("msg_err_cannot_alter_blocks_below_banner_in_siege_zone")));
            event.setCancelled(true);
            return;
        }

        //Prevent destruction of siege-banner or support block
        if (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(event.getBlock())
        || SiegeWarBlockUtil.isBlockNearAnActiveSiegeCampBanner(event.getBlock())) {
            event.setMessage(translator.of("msg_err_siege_war_cannot_destroy_siege_banner"));
            event.setCancelled(true);
            return;
        }
    }

	/**
	 * Evaluate a possible wall breach
	 *
	 * @return true if a wall breach has occurred
	 */
	private static boolean evaluateWallBreach(Translator translator, Block block, TownyDestroyEvent event) throws TownyException {
		if(TownyAPI.getInstance().isWilderness(block))
			return false; //Wall breaching only applies in towns

		Town town = TownyAPI.getInstance().getTown(block.getLocation());
		if(!SiegeController.hasActiveSiege(town))
			return false; //SW doesn't un-cancel events in unsieged towns
		//Ensure player has permission
		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(event.getPlayer(), SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_USE_BREACH_POINTS.getNode())) {
			event.setMessage(translator.of("msg_err_action_disable"));
			return false;
		}
		//No wall breaching outside battle sessions
		if(!BattleSession.getBattleSession().isActive()) {
			event.setMessage(translator.of("msg_err_cannot_breach_without_battle_session"));
			return false;
		}
		//Ensure player is on the town-hostile siege side
		Resident resident = TownyAPI.getInstance().getResident(event.getPlayer());
		if(resident == null)
			return false;
		Siege siege = SiegeController.getSiege(town);
		if(!SiegeWarAllegianceUtil.isPlayerOnTownHostileSide(event.getPlayer(), resident, siege))
			return false;
		//Ensure there are enough breach points
		if(siege.getWallBreachPoints() < SiegeWarSettings.getWallBreachingBlockDestructionCost()) {
			event.setMessage(translator.of("msg_err_not_enough_breach_points_for_action", SiegeWarSettings.getWallBreachingBlockDestructionCost(), siege.getFormattedBreachPoints()));
			return false;
		}
		//Ensure height is ok
		if(!SiegeWarWallBreachUtil.validateBreachHeight(block, town, siege)) {
			event.setMessage(translator.of("msg_err_cannot_breach_at_this_height", SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMin(), SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMax()));
			return false;
		}
		//Ensure material is ok
		if(!SiegeWarWallBreachUtil.validateDestroyMaterial(block, event.getLocation())) {
			event.setMessage(translator.of("msg_err_cannot_destroy_at_this_height", SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMin(), SiegeWarSettings.getWallBreachingHomeblockBreachHeightLimitMax()));
			return false;
		}
		//IF we get here, it is a wall breach!!
		//Reduce breach points
		siege.setWallBreachPoints(siege.getWallBreachPoints() - SiegeWarSettings.getWallBreachingBlockDestructionCost());
		//Un-cancel the event
		event.setCancelled(false);
		//Send message to player
		event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + translator.of("msg_wall_breach_successful")));
		return true;
	}
}
