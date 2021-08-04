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
     * drop any non-tool items they are carrying
     */
    public static void dropNonToolItemsFromBattlefieldReportersInSiegezones() {	
        List<ItemStack> itemsToDrop = new ArrayList<>();
        for(Player player: Bukkit.getOnlinePlayers()) {
            if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_SIEGEZONE_CANNOT_CARRY_NON_TOOL_ITEMS.getNode())
               && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
                //Identify non-tool items
                itemsToDrop.clear();
                for(ItemStack itemStack: player.getInventory().getStorageContents()) {
                    if(itemStack != null
                        && !itemStack.getType().toString().endsWith("AXE")
                        && !itemStack.getType().toString().endsWith("SHOVEL")) {
                        //Drop item
                        itemsToDrop.add(itemStack);
                    }
                }
                
                if(itemsToDrop.size() != 0) {
                    //Drop items
                    Towny.getPlugin().getServer().getScheduler().runTask(Towny.getPlugin(), new Runnable() {
                        public void run() {
                            for(ItemStack itemToDrop: itemsToDrop) {
                                player.getWorld().dropItem(player.getLocation(), itemToDrop);
                            }
                        }
                    });                   
                    //Notify player
                    player.sendMessage(Translation.of("plugin_prefix") + Translation.of("msg_you_can_only_carry_tools_in_siegezones"));
                }
            } 
        }
    }
}
