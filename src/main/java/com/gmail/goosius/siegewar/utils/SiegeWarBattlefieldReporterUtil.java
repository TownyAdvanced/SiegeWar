package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.settings.Translation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SiegeWarBattlefieldReporterUtil {

    /**
     * For battlefield reporters in siegezones,
     * drop any non-tool items they are carrying
     */
    public static void dropNonToolItemsFromBattlefieldReportersInSiegezones() {	
        boolean someItemsDropped;
        for(Player player: Bukkit.getOnlinePlayers()) {
            if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_SIEGEZONE_CANNOT_CARRY_NON_TOOL_ITEMS.getNode())
               && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
                //Drop non-tool items
                someItemsDropped = false;
                for(ItemStack itemStack: player.getInventory().getStorageContents()) {
                    if(itemStack != null
                        && !itemStack.getType().toString().endsWith("AXE")
                        && !itemStack.getType().toString().endsWith("SHOVEL")) {
                        //Drop item
                        player.getWorld().dropItem(player.getLocation(), itemStack);
                        if(!someItemsDropped)
                            someItemsDropped = true;
                    }
                }
                //Notify player
                player.sendMessage(Translation.of("plugin_prefix") + Translation.of("msg_you_can_only_carry_tools_in_siegezones"));
            } 
        }
    }
}
