package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SiegeWarBattlefieldReporterUtil {

    /**
     * For battlefield reporters in siegezones,
     * drop any items they are carrying
     */
    public static void dropNonToolItemsFromBattlefieldReportersInSiegezones() {	
        List<ItemStack> itemsToDrop = new ArrayList<>();
        List<Double> randomX = new ArrayList<>();
        List<Double> randomZ = new ArrayList<>();
        
        for(Player player: Bukkit.getOnlinePlayers()) { 
            if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_SIEGEZONE_CANNOT_CARRY_ITEMS.getNode())
               && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
                //Identify non-tool items
                itemsToDrop.clear();
                randomX.clear();
                randomZ.clear();;
                for(ItemStack itemStack: player.getInventory().getStorageContents()) {
                    if(itemStack != null) {        
                        itemsToDrop.add(itemStack);
                        randomX.add((Math.random() * 10) - 5);
                        randomZ.add((Math.random() * 10) - 5);                  
                    }
                }
                
                if(itemsToDrop.size() != 0) {
                    //Drop items and scatter them around
                    Towny.getPlugin().getServer().getScheduler().runTask(Towny.getPlugin(), new Runnable() {
                        public void run() {
                            for(int i = 0; i < itemsToDrop.size(); i++) {                            
                                player.getWorld().dropItemNaturally(
                                    player.getLocation().add(randomX.get(i), 0, randomZ.get(i)), 
                                    itemsToDrop.get(i));
                            }
                            player.getInventory().clear();
                        }
                    });                   
                    //Notify player
                    player.sendMessage(Translation.of("plugin_prefix") + Translation.of("msg_you_cannot_carry_items_in_siegezones"));
                }
            } 
        }
    }
}
