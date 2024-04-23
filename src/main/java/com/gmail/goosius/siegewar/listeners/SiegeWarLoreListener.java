package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 *
 * @author lexiccn
 *
 */
public class SiegeWarLoreListener implements Listener {

    private String getChatLoreForBanner(PersistentDataContainer data) {
        String winner = Translation.of("siege_lore_color_value",
                Translation.of("siege_lore_unknown"));
        String opposition = Translation.of("siege_lore_color_value",
                Translation.of("siege_lore_unknown"));
        String key;
        switch (SiegeWarLoreUtil.getSiegeWinningSideName(data)) {
            case "ATTACKERS": {
                winner = Translation.of("siege_lore_color_attacker",
                        SiegeWarLoreUtil.getSiegeAttackerName(data));
                opposition = Translation.of("siege_lore_color_defender",
                        SiegeWarLoreUtil.getSiegeDefenderName(data));
                key = "_attack";
                break;
            }
            case "DEFENDERS": {
                winner = Translation.of("siege_lore_color_defender",
                        SiegeWarLoreUtil.getSiegeDefenderName(data));
                opposition = Translation.of("siege_lore_color_attacker",
                        SiegeWarLoreUtil.getSiegeAttackerName(data));
                key = "_defence";
                break;
            }
            case "NOBODY": {
                winner = Translation.of("siege_lore_color_neutral",
                        SiegeSide.NOBODY.getFormattedName());
                opposition = Translation.of("siege_lore_color_neutral",
                        SiegeSide.NOBODY.getFormattedName());
            }
            default: {
                key = "_unknown";
                break;
            }
        }


        String type = Translation.of("siege_lore_color_value",
                Translation.of("siege_lore_banner_type", SiegeWarLoreUtil.getSiegeTypeName(data)));
        String town = Translation.of("siege_lore_color_defender",
                SiegeWarLoreUtil.getSiegeTownName(data));

        String end = Translation.of("siege_lore_color_value",
                new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(SiegeWarLoreUtil.getSiegeEndTime(data)));

        return Translation.of("siege_lore_color_key",
                    Translation.of("siege_lore_banner_chat_1", type)) +
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_chat_2", town)) +
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_chat_3", winner)) +
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_chat_4" + key, opposition)) +
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_chat_5" + key, end));
    }

    @EventHandler
    public void onInteractBanner(PlayerInteractEvent event) {
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

        Messaging.sendMsg(event.getPlayer(), getChatLoreForBanner(container));
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
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

        SiegeWarLoreUtil.copyBannerData(bannerItem.getItemMeta().getPersistentDataContainer(), resultMeta.getPersistentDataContainer());
        SiegeWarLoreUtil.setShieldItem(resultMeta, bannerItem.getItemMeta().getPersistentDataContainer());

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

        ItemStack stack = item.getItemStack();

        SiegeWarLoreUtil.copyBannerData(state.getPersistentDataContainer(), meta.getPersistentDataContainer());
        SiegeWarLoreUtil.setBannerItem(meta, state.getPersistentDataContainer());

        stack.setItemMeta(meta);
        item.setItemStack(stack);
    }
}
