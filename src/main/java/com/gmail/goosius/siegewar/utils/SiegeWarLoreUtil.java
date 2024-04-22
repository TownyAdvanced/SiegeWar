package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SiegeWarLoreUtil {
    public static final NamespacedKey LORE = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore");

    public static final NamespacedKey TYPE = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_type");
    public static final NamespacedKey TOWN = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_town");
    public static final NamespacedKey ATTACKER = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_attacker");
    public static final NamespacedKey DEFENDER = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_defender");
    public static final NamespacedKey ATTACKER_POINTS = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_attacker_points");
    public static final NamespacedKey DEFENDER_POINTS = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_defender_points");
    public static final NamespacedKey WINNER = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_winner");
    public static final NamespacedKey STATUS = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_status");
    public static final NamespacedKey START = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_start");
    public static final NamespacedKey END = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_banner_end");

    public static void setLoreKey(PersistentDataContainer container) {
        container.set(LORE, PersistentDataType.BYTE, (byte)0);
    }

    public static boolean hasLoreKey(PersistentDataContainer container) {
        return container.has(LORE, PersistentDataType.BYTE);
    }

    public static void setBannerItem(ItemMeta meta, PersistentDataContainer data) {
        if (!hasLoreKey(data)) return;

        String town = data.getOrDefault(TOWN, PersistentDataType.STRING, Translation.of("siege_lore_unknown"));

        String name = Translation.of("siege_lore_two_values",
                Translation.of("siege_lore_color_defender", town),
                Translation.of("siege_lore_color_neutral",
                        Translation.of("siege_lore_banner_name")));

        meta.setDisplayName(name);

        setBannerLore(meta, data);
    }

    public static void setShieldItem(ItemMeta meta, PersistentDataContainer data) {
        if (!hasLoreKey(data)) return;

        String town = data.getOrDefault(TOWN, PersistentDataType.STRING, Translation.of("siege_lore_unknown"));

        String name = Translation.of("siege_lore_two_values",
                Translation.of("siege_lore_color_defender", town),
                Translation.of("siege_lore_color_neutral",
                        Translation.of("siege_lore_shield_name")));

        meta.setDisplayName(name);

        setBannerLore(meta, data);
    }

    public static void setBannerLore(ItemMeta meta, PersistentDataContainer data) {
        List<String> lore = new ArrayList<>();

        String type = data.getOrDefault(TYPE, PersistentDataType.STRING, Translation.of("siege_lore_unknown"));
        String attacker = data.getOrDefault(ATTACKER, PersistentDataType.STRING, Translation.of("siege_lore_unknown"));
        String defender = data.getOrDefault(DEFENDER, PersistentDataType.STRING, Translation.of("siege_lore_unknown"));
        int attacker_points = data.getOrDefault(ATTACKER_POINTS, PersistentDataType.INTEGER, 0);
        int defender_points = data.getOrDefault(ATTACKER_POINTS, PersistentDataType.INTEGER, 0);
        String winner = data.getOrDefault(WINNER, PersistentDataType.STRING, SiegeSide.NOBODY.name());
        String status = data.getOrDefault(STATUS, PersistentDataType.STRING, SiegeStatus.UNKNOWN.getName());
        long start = data.getOrDefault(START, PersistentDataType.LONG, 0L);
        long end = data.getOrDefault(END, PersistentDataType.LONG, 0L);

        String colored_winner;
        switch (winner) {
            case "ATTACKERS":
                colored_winner = Translation.of("siege_lore_color_attacker", attacker);
                break;
            case "DEFENDERS":
                colored_winner = Translation.of("siege_lore_color_defender", defender);
                break;
            case "NOBODY":
                colored_winner = Translation.of("siege_lore_color_neutral", SiegeSide.NOBODY.getFormattedName().translate());
                break;
            default:
                colored_winner = Translation.of("siege_lore_color_value", Translation.of("siege_lore_unknown"));
                break;
        }

        String type_line = Translation.of("siege_lore_color_value",
                Translation.of("siege_lore_banner_type", type));
        lore.add(type_line);

        String attacker_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_attacker")),
                Translation.of("siege_lore_two_values",
                        Translation.of("siege_lore_color_attacker", attacker),
                        Translation.of("siege_lore_color_value",
                                Translation.of("siege_lore_secondary_value", attacker_points))));
        lore.add(attacker_line);

        String defender_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_defender")),
                Translation.of("siege_lore_two_values",
                        Translation.of("siege_lore_color_defender", defender),
                        Translation.of("siege_lore_color_value",
                                Translation.of("siege_lore_secondary_value", defender_points))));
        lore.add(defender_line);

        String winner_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_winner")),
                Translation.of("siege_lore_two_values",
                        colored_winner,
                        Translation.of("siege_lore_color_value",
                                Translation.of("siege_lore_secondary_value", status))));
        lore.add(winner_line);

        String start_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_start")),
                Translation.of("siege_lore_color_value",
                        new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(start)));
        lore.add(start_line);

        String end_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_end")),
                Translation.of("siege_lore_color_value",
                        new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(end)));
        lore.add(end_line);


        meta.setLore(lore);
    }

    public static void copyBannerData(PersistentDataContainer from, PersistentDataContainer to) {
        if (!hasLoreKey(from)) return;
        setLoreKey(to);
        if (from.has(TYPE, PersistentDataType.STRING))
            to.set(TYPE, PersistentDataType.STRING, from.get(TYPE, PersistentDataType.STRING));
        if (from.has(TOWN, PersistentDataType.STRING))
            to.set(TOWN, PersistentDataType.STRING, from.get(TOWN, PersistentDataType.STRING));
        if (from.has(ATTACKER, PersistentDataType.STRING))
            to.set(ATTACKER, PersistentDataType.STRING, from.get(ATTACKER, PersistentDataType.STRING));
        if (from.has(DEFENDER, PersistentDataType.STRING))
            to.set(DEFENDER, PersistentDataType.STRING, from.get(DEFENDER, PersistentDataType.STRING));
        if (from.has(ATTACKER_POINTS, PersistentDataType.INTEGER))
            to.set(ATTACKER_POINTS, PersistentDataType.INTEGER, from.get(ATTACKER_POINTS, PersistentDataType.INTEGER));
        if (from.has(DEFENDER_POINTS, PersistentDataType.INTEGER))
            to.set(DEFENDER_POINTS, PersistentDataType.INTEGER, from.get(DEFENDER_POINTS, PersistentDataType.INTEGER));
        if (from.has(WINNER, PersistentDataType.STRING))
            to.set(WINNER, PersistentDataType.STRING, from.get(WINNER, PersistentDataType.STRING));
        if (from.has(STATUS, PersistentDataType.STRING))
            to.set(STATUS, PersistentDataType.STRING, from.get(STATUS, PersistentDataType.STRING));
        if (from.has(START, PersistentDataType.LONG))
            to.set(START, PersistentDataType.LONG, from.get(START, PersistentDataType.LONG));
        if (from.has(END, PersistentDataType.LONG))
            to.set(END, PersistentDataType.LONG, from.get(END, PersistentDataType.LONG));
    }

    public static void bannerSiegeStart(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof Banner)) return;
        Banner bannerState = (Banner) blockState;

        PersistentDataContainer container = bannerState.getPersistentDataContainer();

        setLoreKey(container);

        container.set(TYPE, PersistentDataType.STRING, siege.getSiegeType().getName());
        container.set(TOWN, PersistentDataType.STRING, siege.getDefender().getName());

        container.set(ATTACKER, PersistentDataType.STRING, siege.getAttackingNationIfPossibleElseTown().getFormattedName());
        container.set(DEFENDER, PersistentDataType.STRING, siege.getDefendingNationIfPossibleElseTown().getFormattedName());

        container.set(START, PersistentDataType.LONG, System.currentTimeMillis());

        bannerState.update();
    }

    public static void bannerSiegeEnd(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof Banner)) return;
        Banner bannerState = (Banner) blockState;

        PersistentDataContainer container = bannerState.getPersistentDataContainer();

        if (!hasLoreKey(container)) return;

        container.set(WINNER, PersistentDataType.STRING, siege.getSiegeWinner().name());
        container.set(STATUS, PersistentDataType.STRING, siege.getStatus().getName());

        container.set(ATTACKER_POINTS, PersistentDataType.INTEGER, siege.getAttackerBattlePoints());
        container.set(DEFENDER_POINTS, PersistentDataType.INTEGER, siege.getDefenderBattlePoints());

        container.set(END, PersistentDataType.LONG, System.currentTimeMillis());

        bannerState.update();
    }
}
