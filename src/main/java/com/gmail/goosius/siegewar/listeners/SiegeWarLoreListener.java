package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarLoreUtil;
import org.bukkit.block.Banner;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.BannerMeta;

/**
 *
 * @author lexiccn
 *
 */
public class SiegeWarLoreListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTryStartSiege(PreSiegeCampEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        if (!(event.getFlag().getState() instanceof Banner)) return;
        Banner flag = (Banner)event.getFlag().getState();

        if (!SiegeWarLoreUtil.hasLoreKey(flag.getPersistentDataContainer())) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        if (!(event.getItemInHand().getItemMeta() instanceof BannerMeta)) return;
        if (!(event.getBlock().getState() instanceof Banner)) return;
        Banner state = (Banner)event.getBlock().getState();
        BannerMeta meta = (BannerMeta)event.getItemInHand().getItemMeta();

        if (!SiegeWarLoreUtil.hasLoreKey(meta.getPersistentDataContainer())) return;

        SiegeWarLoreUtil.copyBannerData(meta.getPersistentDataContainer(), state.getPersistentDataContainer());
        state.update();
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropBanner(BlockDropItemEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        if (event.getItems().isEmpty()) return;
        Item item = event.getItems().get(0);
        if (!(item.getItemStack().getItemMeta() instanceof BannerMeta)) return;
        if (!(event.getBlockState() instanceof Banner)) return;
        Banner state = (Banner)event.getBlockState();
        BannerMeta meta = (BannerMeta)item.getItemStack().getItemMeta();

        if (!SiegeWarLoreUtil.hasLoreKey(state.getPersistentDataContainer())) return;

        SiegeWarLoreUtil.setBannerLore(meta, state.getPersistentDataContainer());
        SiegeWarLoreUtil.copyBannerData(state.getPersistentDataContainer(), meta.getPersistentDataContainer());
    }
}
