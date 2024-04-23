package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.events.SiegeEndEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarLoreUtil;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Arrays;

/**
 *
 * @author lexiccn
 *
 */
public class SiegeWarLoreListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSiegeStart(SiegeWarStartEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        //Set name, type, attacker, defender, start time
        SiegeWarLoreUtil.bannerSiegeStart(event.getSiege());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSiegeEnd(SiegeEndEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        //Set outcome, winner, points, end time
        SiegeWarLoreUtil.bannerSiegeEnd(event.getSiege());
    }

    @EventHandler
    public void onInteractBanner(PlayerInteractEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        PersistentDataContainer container;
        if (event.hasBlock()) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            if (event.getClickedBlock().isEmpty()) return;
            if (!Tag.BANNERS.isTagged(event.getClickedBlock().getType())) return;
            if (!(event.getClickedBlock().getState() instanceof PersistentDataHolder)) return;
            PersistentDataHolder holder = (PersistentDataHolder) event.getClickedBlock().getState();
            container = holder.getPersistentDataContainer();
        } else if (event.hasItem()) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
            if (!Tag.BANNERS.isTagged(event.getMaterial())) return;
            ItemStack item = event.getItem();
            if (!item.hasItemMeta()) return;
            container = item.getItemMeta().getPersistentDataContainer();
        } else return;

        if (!SiegeWarLoreUtil.hasLoreKey(container)) return;

        Messaging.sendMsg(event.getPlayer(), SiegeWarLoreUtil.bannerChat(container));
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        ItemStack resultItem = event.getInventory().getResult();
        if (resultItem == null) return;
        if (resultItem.getType() != Material.SHIELD) return;

        ItemStack bannerItem = Arrays.stream(event.getInventory().getMatrix()).filter(itemStack -> {
            if (itemStack == null) return false;
            if (itemStack.getItemMeta() instanceof BannerMeta) {
                return SiegeWarLoreUtil.hasLoreKey(itemStack.getItemMeta().getPersistentDataContainer());
            }
            return false;
        }).findFirst().orElse(null);
        if (bannerItem == null) return;

        ItemMeta resultMeta = resultItem.getItemMeta();

        SiegeWarLoreUtil.bannerCopyData(bannerItem.getItemMeta().getPersistentDataContainer(), resultMeta.getPersistentDataContainer());
        SiegeWarLoreUtil.shieldItem(resultMeta, bannerItem.getItemMeta().getPersistentDataContainer());

        resultItem.setItemMeta(resultMeta);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTryStartSiege(PreSiegeCampEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        if (!(event.getFlag().getState() instanceof Banner)) return;
        Banner flag = (Banner)event.getFlag().getState();

        if (!SiegeWarLoreUtil.hasLoreKey(flag.getPersistentDataContainer())) return;

        event.setCancelled(true);
        event.setCancellationMsg(Translation.of("siege_lore_error_banner_cannot_be_used"));

        Bukkit.getLogger().severe("" + event.isCancelled());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        if (!(event.getItemInHand().getItemMeta() instanceof BannerMeta)) return;
        if (!(event.getBlock().getState() instanceof Banner)) return;
        Banner state = (Banner)event.getBlock().getState();
        BannerMeta meta = (BannerMeta)event.getItemInHand().getItemMeta();

        if (!SiegeWarLoreUtil.hasLoreKey(meta.getPersistentDataContainer())) return;

        SiegeWarLoreUtil.bannerCopyData(meta.getPersistentDataContainer(), state.getPersistentDataContainer());

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

        ItemStack stack = item.getItemStack();

        SiegeWarLoreUtil.bannerCopyData(state.getPersistentDataContainer(), meta.getPersistentDataContainer());
        SiegeWarLoreUtil.bannerItem(meta, state.getPersistentDataContainer());

        stack.setItemMeta(meta);
        item.setItemStack(stack);
    }
}
