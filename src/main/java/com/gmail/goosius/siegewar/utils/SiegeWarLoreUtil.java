package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SiegeWarLoreUtil {
    public static final NamespacedKey LORE = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore");
    public static final NamespacedKey NAME = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_name");
    public static final NamespacedKey TYPE = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_type");
    public static final NamespacedKey ATTACKER = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_attacker");
    public static final NamespacedKey DEFENDER = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_defender");
    public static final NamespacedKey WINNER = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_win_side");
    public static final NamespacedKey START = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_start");
    public static final NamespacedKey END = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_end");

    public static void setLoreKey(PersistentDataContainer container) {
        container.set(LORE, PersistentDataType.BYTE, (byte)0);
    }

    public static boolean hasLoreKey(PersistentDataContainer container) {
        return container.has(LORE, PersistentDataType.BYTE);
    }

    public static void setBannerLore(BannerMeta meta, PersistentDataContainer data) {
        if (!hasLoreKey(data)) return;

        if (data.has(NAME, PersistentDataType.STRING))
            meta.setDisplayName(data.get(NAME, PersistentDataType.STRING));

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (data.has(TYPE, PersistentDataType.STRING)) lore.add(data.get(TYPE, PersistentDataType.STRING));
        if (data.has(ATTACKER, PersistentDataType.STRING)) lore.add(data.get(ATTACKER, PersistentDataType.STRING));
        if (data.has(DEFENDER, PersistentDataType.STRING)) lore.add(data.get(DEFENDER, PersistentDataType.STRING));
        if (data.has(WINNER, PersistentDataType.STRING)) lore.add(data.get(WINNER, PersistentDataType.STRING));
        if (data.has(START, PersistentDataType.STRING)) lore.add(data.get(START, PersistentDataType.STRING));
        if (data.has(END, PersistentDataType.STRING)) lore.add(data.get(END, PersistentDataType.STRING));

        meta.setLore(lore);
    }

    public static void copyBannerData(PersistentDataContainer from, PersistentDataContainer to) {
        if (!hasLoreKey(from)) return;
        setLoreKey(to);
        if (from.has(NAME, PersistentDataType.STRING))
            to.set(NAME, PersistentDataType.STRING, from.get(NAME, PersistentDataType.STRING));
        if (from.has(TYPE, PersistentDataType.STRING))
            to.set(TYPE, PersistentDataType.STRING, from.get(TYPE, PersistentDataType.STRING));
        if (from.has(ATTACKER, PersistentDataType.STRING))
            to.set(ATTACKER, PersistentDataType.STRING, from.get(ATTACKER, PersistentDataType.STRING));
        if (from.has(DEFENDER, PersistentDataType.STRING))
            to.set(DEFENDER, PersistentDataType.STRING, from.get(DEFENDER, PersistentDataType.STRING));
        if (from.has(WINNER, PersistentDataType.STRING))
            to.set(WINNER, PersistentDataType.STRING, from.get(WINNER, PersistentDataType.STRING));
        if (from.has(START, PersistentDataType.STRING))
            to.set(START, PersistentDataType.STRING, from.get(START, PersistentDataType.STRING));
        if (from.has(END, PersistentDataType.STRING))
            to.set(END, PersistentDataType.STRING, from.get(END, PersistentDataType.STRING));
    }

    public static void setupBanner(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof Banner)) return;
        Banner bannerState = (Banner) blockState;

        PersistentDataContainer container = bannerState.getPersistentDataContainer();

        setLoreKey(container);

        container.set(TYPE, PersistentDataType.STRING, Translation.of("siege_lore_banner_type", siege.getSiegeType().getName()));

        String attacker = Translation.of("siege_lore_color_attacker", siege.getAttacker().getFormattedName());
        String defender = Translation.of("siege_lore_color_defender", siege.getDefender().getFormattedName());

        container.set(NAME, PersistentDataType.STRING, Translation.of("siege_lore_banner_name", attacker, defender));
        container.set(ATTACKER, PersistentDataType.STRING, Translation.of("siege_lore_banner_attacker", attacker));
        container.set(DEFENDER, PersistentDataType.STRING, Translation.of("siege_lore_banner_defender", defender));

        String start = new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(new Date(System.currentTimeMillis()));

        container.set(START, PersistentDataType.STRING, Translation.of("siege_lore_banner_start", start));

        bannerState.update();
    }

    public static void finaliseBanner(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof Banner)) return;
        Banner bannerState = (Banner) blockState;

        PersistentDataContainer container = bannerState.getPersistentDataContainer();

        if (!hasLoreKey(container)) return;

        String winner;
        if (siege.getSiegeWinner() == SiegeSide.ATTACKERS) winner = Translation.of("siege_lore_color_attacker", siege.getAttackerName());
        else if (siege.getSiegeWinner() == SiegeSide.DEFENDERS) winner = Translation.of("siege_lore_color_defender", siege.getDefenderName());
        else winner = Translation.of("siege_lore_color_neutral", SiegeSide.NOBODY.getFormattedName().translate());
        container.set(WINNER, PersistentDataType.STRING, Translation.of("siege_lore_banner_winner_and_outcome", winner, siege.getStatus().getName()));

        String attacker = container.getOrDefault(ATTACKER, PersistentDataType.STRING, "&eUnknown");
        container.set(ATTACKER, PersistentDataType.STRING, Translation.of("siege_lore_banner_points", attacker, siege.getAttackerBattlePoints()));

        String defender = container.getOrDefault(DEFENDER, PersistentDataType.STRING, "&eUnknown");
        container.set(DEFENDER, PersistentDataType.STRING, Translation.of("siege_lore_banner_points", defender, siege.getDefenderBattlePoints()));

        String end = new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(new Date(System.currentTimeMillis()));

        container.set(END, PersistentDataType.STRING, Translation.of("siege_lore_banner_end", end));

        bannerState.update();
    }
}
