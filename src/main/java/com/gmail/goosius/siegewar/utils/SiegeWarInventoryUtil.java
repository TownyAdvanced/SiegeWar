package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class SiegeWarInventoryUtil {

    public static void degradeInventory(PlayerDeathEvent playerDeathEvent) {
        Damageable damageable;
        double maxDurability;
        int currentDurability, damageToInflict, newDurability, durabilityWarning;
        boolean closeToBreaking = false;
        for (ItemStack itemStack : playerDeathEvent.getEntity().getInventory().getContents()) {
            if (itemStack != null && itemStack.getType().getMaxDurability() != 0 && !itemStack.getItemMeta().isUnbreakable()) {
                damageable = ((Damageable) itemStack.getItemMeta());
                maxDurability = itemStack.getType().getMaxDurability();
                currentDurability = damageable.getDamage();
                damageToInflict = (int)(maxDurability / 100 * SiegeWarSettings.getKeepInventoryOnSiegeZoneDeathToolsDegradePercentage());
                newDurability = currentDurability + damageToInflict;
                if (newDurability >= maxDurability) {
                    damageable.setDamage(Math.max((int)maxDurability-25, currentDurability));
                    closeToBreaking = true;
                }
                else {
                    damageable.setDamage(newDurability);
                    durabilityWarning = damageToInflict * 2 + currentDurability;
                    if (durabilityWarning >= maxDurability)
                        closeToBreaking = true;
                }
                itemStack.setItemMeta(damageable);
            }
        }
        if (closeToBreaking) //One or more items are close to breaking, send warning.
            Messaging.sendMsg(playerDeathEvent.getEntity(), Translatable.of("msg_inventory_degrade_warning"));
    }

    public static void keepInventory(PlayerDeathEvent playerDeathEvent) {
        playerDeathEvent.setKeepInventory(true);
        playerDeathEvent.getDrops().clear();
    }
}
