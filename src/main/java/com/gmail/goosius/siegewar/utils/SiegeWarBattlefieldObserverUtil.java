package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SiegeWarBattlefieldObserverUtil {

    /**
     * For battlefield observers in siegezones:
     *
     * 1. Refill their hunger bar
     * 2. Drop any items they are carrying, except for pick, axe, shovel, and shears
     */
    public static void evaluateBattlefieldObserversInSiegezones() {
        List<ItemStack> itemsToDrop = new ArrayList<>();
        List<ItemStack> itemsToKeep = new ArrayList<>();        
        List<Double> xDelta = new ArrayList<>();
        List<Double> yDelta = new ArrayList<>();
        List<Double> zDelta = new ArrayList<>();

        for(Player player: Bukkit.getOnlinePlayers()) { 
            if(!player.isOp()
                && player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_SIEGEZONE_CANNOT_CARRY_ITEMS.getNode())
                && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {

                //Player cannot carry food. So don't let them starve!.
                if(player.getFoodLevel() < 20) {
                    Towny.getPlugin().getServer().getScheduler().runTask(Towny.getPlugin(), new Runnable() {
                        public void run() {
                            player.setFoodLevel(20);
                        }
                    });
                }

                //Identify non-tool items
                itemsToDrop.clear();
                itemsToKeep.clear();
                xDelta.clear();
                yDelta.clear();
                zDelta.clear();;
                for(ItemStack itemStack: player.getInventory().getContents()) {
                    if(itemStack != null) {
                        if (itemStack.getType().toString().endsWith("AXE")
                            || itemStack.getType().toString().endsWith("SHOVEL")
                            || itemStack.getType().toString().endsWith("SHEARS")) {
                                itemsToKeep.add(itemStack);
                        } else {
                            itemsToDrop.add(itemStack);
                            xDelta.add((Math.random() * 10) - 5);
                            yDelta.add((Math.random() * 10) + 5);                        
                            zDelta.add((Math.random() * 10) - 5);                                              
                        }
                    }
                }

                if(itemsToDrop.size() != 0) {
                    //Drop items and scatter them around
                    Towny.getPlugin().getServer().getScheduler().runTask(Towny.getPlugin(), new Runnable() {
                        public void run() {
                            //Clear inventory (We do not use remove() here, because unfortunately it only works on the storage contents)
                            player.getInventory().clear();
                            //Add any kept items back into inventory
                            for(ItemStack itemStack: itemsToKeep) {
                                player.getInventory().addItem(itemStack);
                            }
                            //Drop items on ground near player
                            for(int i = 0; i < itemsToDrop.size(); i++) {                            
                                player.getWorld().dropItemNaturally(
                                    player.getLocation().add(xDelta.get(i), yDelta.get(i), zDelta.get(i)), 
                                    itemsToDrop.get(i));
                            }
                        }                       
                    });                   
                    //Notify player
                    Messaging.sendMsg(player, Translatable.of("msg_you_cannot_carry_items_in_siegezones"));
                }
            } 
        }
    }
}
