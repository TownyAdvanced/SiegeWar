package com.gmail.goosius.siegewar.integration.cannons;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarAllegianceUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarWallBreachUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CannonsIntegration {

    public CannonsIntegration(SiegeWar plugin) {
        plugin.getServer().getPluginManager().registerEvents(new CannonsListener(this), plugin);
        SiegeWar.info("Cannons support enabled.");
    }

    /**
     * Determine if player can use breach points by cannon
     * 
     * @param player player
     * @param siege siege
     * 
     * @return true if the player can use breach points by cannon
     */
    public static boolean canPlayerUseBreachPointsByCannon(Player player, Siege siege) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if(!resident.hasTown())
            return false;
        if(!doesPlayerHaveCannonPerms(player, resident, siege))
            return false;
        if(!SiegeWarAllegianceUtil.isPlayerOnTownHostileSide(player, resident, siege))
            return false;
        return true;			
    }
    
    /**
     * Determine if player can generate breach points by cannon
     * 
     * @param player player
     * @param siege siege
     * 
     * @return true if the player can generate breach points by cannon
     */
    public static boolean canPlayerGenerateBreachPointsByCannon(Player player, Siege siege) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if(!resident.hasTown())
            return false;
        if(!doesPlayerHaveCannonPerms(player, resident, siege))
            return false;
        if(!SiegeWarAllegianceUtil.isPlayerOnTownFriendlySide(player, resident, siege))
            return false;
        return true;			
    }

    /**
     * Determine if a player has perms to shoot cannons in siegezones
     */
    private static boolean doesPlayerHaveCannonPerms(Player player, Resident resident, Siege siege) {
        return (resident.getTownOrNull() == siege.getTown()
                && TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_FIRE_CANNON_IN_SIEGEZONE.getNode()))
                ||
                (resident.hasNation()
                && TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_FIRE_CANNON_IN_SIEGEZONE.getNode()));
    }

    /**
     * Filter a given explode list by cannon effects
     * @param givenExplodeList given list of exploding blocks
     * @param event the explosion event
     *
     * @return filtered list
     * @throws TownyException if something is misconfigured
     */
    public static List<Block> filterExplodeListByCannonEffects(List<Block> givenExplodeList, TownyExplodingBlocksEvent event) throws TownyException {       
        if(SiegeWar.isCannonsPluginInstalled()
			&& SiegeWarSettings.isWallBreachingEnabled()
			&& SiegeWarSettings.isWallBreachingCannonsIntegrationEnabled()        
            && BattleSession.getBattleSession().isActive()
            && event.getEntity() != null
            && event.getEntity() instanceof Projectile
            && ((Projectile) event.getEntity()).getShooter() instanceof Player) {

            //Prepare filtered list
            List<Block> filteredExplodeList = new ArrayList<>(givenExplodeList);

            //Prepare some cache sets, to optimize processing
            Set<Town> cachedProtectedTowns = new HashSet<>();
            Set<Town> cachedUnprotectedTowns = new HashSet<>();
            Set<Material> cachedProtectedMaterials = new HashSet<>();
            Set<Material> cachedUnprotectedMaterials = new HashSet<>();
             
            //Find the blocks explosions which were removed by Towny, and see if they should be re-added.
            Player player = (Player)(((Projectile) event.getEntity()).getShooter());
            Town town;
            Siege siege;
            List<Block> vanillaExplodeList = event.getVanillaBlockList(); //The pre-towny-protection list
            for (Block block : vanillaExplodeList) {            
                if(givenExplodeList.contains(block))
                    continue;   //Block is unprotected & will explode. No breach points needed
                town = TownyAPI.getInstance().getTown(block.getLocation());
                if(town == null)
                    continue; 
                if(cachedProtectedTowns.contains(town))
                    continue;
                siege = SiegeController.getSiege(town);
                if(siege == null || !siege.getStatus().isActive()) {
                    cachedProtectedTowns.add(town); //No siege or inactive siege. Town is safe
                    continue;
                }
                if(!canPlayerUseBreachPointsByCannon(player, siege))
                    continue;
                cachedUnprotectedTowns.add(town);  //Player can breach at the siege. Town is unsafe
                if(!SiegeWarWallBreachUtil.payBreachPoints(SiegeWarSettings.getWallBreachingCannonExplosionCostPerBlock(), siege))
                    continue;   //Insufficient breach points to explode this block
				//Ensure height is ok
				if(!SiegeWarWallBreachUtil.validateBreachHeight(block, town, siege))
                    continue;
				//Ensure material is ok
				if(cachedProtectedMaterials.contains(block.getType())) {
				    //In cache, protected
				    continue; 
                } else if(!cachedUnprotectedMaterials.contains(block.getType())) {
                    //Not in cache
                    if(SiegeWarWallBreachUtil.validateDestroyMaterial(block, block.getLocation())) {
                        cachedUnprotectedMaterials.add(block.getType());
                    } else {
                        cachedProtectedMaterials.add(block.getType());
                        continue;
                    }    
                }

                /*
                 * Player has now paid the required breach points.
                 * Allow block to explode
                 */
                 filteredExplodeList.add(block);
            }

            //Send message if a wall breach is about to occur
            if(filteredExplodeList.size() > givenExplodeList.size())
        		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + Translation.of("msg_wall_breach_successful")));

            //Return filtered list
            return filteredExplodeList;
        } else {
            //Return given list
            return givenExplodeList;
        }
    }

    public static void clearRecentTownFriendlycannonFirers(Siege siege) {
        siege.clearRecentTownFriendlycannonFirers();
    }
}
