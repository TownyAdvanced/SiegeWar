package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.events.SiegeEndEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarLoreUtil;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
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
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Arrays;

/**
 *
 * @author lexiccn
 *
 */
public class SiegeWarLoreListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSiegeStart(SiegeWarStartEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        //Set name, type, attacker, defender, start time
        Siege siege = event.getSiege();
        SiegeWar.getSiegeWar().getScheduler().run(siege.getFlagLocation(), () -> SiegeWarLoreUtil.applySiegeStartLoreToBannerState(siege));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSiegeEnd(SiegeEndEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        //Set outcome, winner, points, end time
        Siege siege = event.getSiege();
        SiegeWar.getSiegeWar().getScheduler().run(siege.getFlagLocation(), () -> SiegeWarLoreUtil.applySiegeEndLoreToBannerState(siege));
    }

    @EventHandler
    public void onInteractBanner(PlayerInteractEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        PersistentDataHolder holder;

        if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!SiegeWarLoreUtil.isLoreItem(event.getClickedBlock().getState(), "siege_banner")) return;
            holder = (PersistentDataHolder) event.getClickedBlock().getState();
        } else if (event.getItem() != null && event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (!SiegeWarLoreUtil.isLoreItem(event.getItem(), "siege_banner")) return;
            holder = event.getItem().getItemMeta();
        } else return;

        SiegeWarLoreUtil.sendBannerChat(holder, event.getPlayer());
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        ItemStack resultItem = event.getInventory().getResult();
        if (resultItem == null) return;
        if (resultItem.getType() != Material.SHIELD) return;

        ItemStack bannerItem = Arrays.stream(event.getInventory().getMatrix())
                .filter(itemStack -> SiegeWarLoreUtil.isLoreItem(itemStack, "siege_banner"))
                .findFirst().orElse(null);
        if (bannerItem == null) return;

        SiegeWarLoreUtil.setShieldStackFromHolder(resultItem, bannerItem.getItemMeta());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTryStartSiege(PreSiegeCampEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;
        if (!SiegeWarLoreUtil.isLoreItem(event.getFlag().getState(), "siege_banner")) return;

        event.setCancelled(true);
        event.setCancellationMsg(Translation.of("siege_lore_error_banner_cannot_be_used"));
    }

    /**
     * Copies lore data from a "siege_banner" ItemMeta PDC to a banner BlockState PDC
     * This is necessary as Spigot/Minecraft do not copy PDC or Lore from items to blocks (and back)
     * LOW priority to run and use ItemInHand methods before {@link com.gmail.goosius.siegewar.listeners.SiegeWarActionListener#onBlockBuild(TownyBuildEvent)}
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        ItemStack stack = event.getItemInHand();
        if (!SiegeWarLoreUtil.isLoreItem(stack, "siege_banner")) return;

        BlockState state = event.getBlock().getState();
        if (!(state instanceof PersistentDataHolder) || !Tag.BANNERS.isTagged(state.getType())) return;

        SiegeWarLoreUtil.copyPersistentLoreDataToHolder(stack.getItemMeta(), (PersistentDataHolder) state);

        state.update();
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropBanner(BlockDropItemEvent event) {
        if (!SiegeWarSettings.isSiegeLoreEnabled()) return;

        if (event.getItems().isEmpty()) return;

        BlockState state = event.getBlockState();
        if (!SiegeWarLoreUtil.isLoreItem(state, "siege_banner")) return;

        Item item = event.getItems().get(0);
        ItemStack stack = item.getItemStack();

        SiegeWarLoreUtil.setBannerStackFromHolder(stack, (PersistentDataHolder) state);

        item.setItemStack(stack);
    }
}
