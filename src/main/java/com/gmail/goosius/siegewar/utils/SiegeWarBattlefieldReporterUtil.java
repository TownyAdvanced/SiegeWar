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
        List<Double> xDelta = new ArrayList<>();
        List<Double> yDelta = new ArrayList<>();
        List<Double> zDelta = new ArrayList<>();

        for(Player player: Bukkit.getOnlinePlayers()) { 
            if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_SIEGEZONE_CANNOT_CARRY_ITEMS.getNode())
               && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
                //Identify non-tool items
                itemsToDrop.clear();
                xDelta.clear();
                yDelta.clear();
                zDelta.clear();;
                for(ItemStack itemStack: player.getInventory().getStorageContents()) {
                    if(itemStack != null) {        
                        itemsToDrop.add(itemStack);
                        xDelta.add((Math.random() * 10) - 5);
                        yDelta.add((Math.random() * 10) + 5);                        
                        zDelta.add((Math.random() * 10) - 5);                  
                    }
                }

                if(itemsToDrop.size() != 0) {
                    //Drop items and scatter them around
                    Towny.getPlugin().getServer().getScheduler().runTask(Towny.getPlugin(), new Runnable() {
                        public void run() {
                            for(int i = 0; i < itemsToDrop.size(); i++) {                            
                                player.getWorld().dropItemNaturally(
                                    player.getLocation().add(xDelta.get(i), yDelta.get(i), zDelta.get(i)), 
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
