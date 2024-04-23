package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static String colorAttacker(String string) {
        return Translation.of("siege_lore_color_attacker", string);
    }

    public static String colorDefender(String string) {
        return Translation.of("siege_lore_color_defender", string);
    }

    public static String colorNeutral(String string) {
        return Translation.of("siege_lore_color_neutral", string);
    }

    public static String colorKey(String string) {
        return Translation.of("siege_lore_color_key", string);
    }

    public static String colorValue(String string) {
        return Translation.of("siege_lore_color_value", string);
    }


    @Nullable
    public static String getSiegeType(PersistentDataContainer container) {
        return container.get(TYPE, PersistentDataType.STRING);
    }

    @Nullable
    public static String getSiegeTown(PersistentDataContainer container) {
        return container.get(TOWN, PersistentDataType.STRING);
    }

    @Nullable
    public static String getSiegeAttacker(PersistentDataContainer container) {
        return container.get(ATTACKER, PersistentDataType.STRING);
    }

    @Nullable
    public static String getSiegeDefender(PersistentDataContainer container) {
        return container.get(DEFENDER, PersistentDataType.STRING);
    }

    @Nullable
    public static Integer getSiegeAttackerPoints(PersistentDataContainer container) {
        return container.get(ATTACKER_POINTS, PersistentDataType.INTEGER);
    }

    @Nullable
    public static Integer getSiegeDefenderPoints(PersistentDataContainer container) {
        return container.get(DEFENDER_POINTS, PersistentDataType.INTEGER);
    }

    @Nullable
    public static String getSiegeWinningSide(PersistentDataContainer container) {
        return container.get(WINNER, PersistentDataType.STRING);
    }

    @Nullable
    public static String getSiegeStatus(PersistentDataContainer container) {
        return container.get(STATUS, PersistentDataType.STRING);
    }

    @Nullable
    public static Long getSiegeStart(PersistentDataContainer container) {
        return container.get(START, PersistentDataType.LONG);
    }

    @Nullable
    public static Long getSiegeEnd(PersistentDataContainer container) {
        return container.get(END, PersistentDataType.LONG);
    }


    public static String getFormattedUnknown() {
        return colorNeutral(Translation.of("siege_lore_unknown"));
    }

    public static String getFormattedType(PersistentDataContainer container) {
        String type = getSiegeType(container);
        if (type == null) return getFormattedUnknown();

        return colorValue(Translation.of("siege_lore_banner_type", type));
    }

    public static String getFormattedTown(PersistentDataContainer container) {
        String town = getSiegeTown(container);
        if (town == null) return getFormattedUnknown();

        return colorDefender(town);
    }

    public static String getFormattedAttacker(PersistentDataContainer container) {
        String attacker = getSiegeAttacker(container);
        if (attacker == null) return getFormattedUnknown();

        return colorAttacker(attacker);
    }

    public static String getFormattedDefender(PersistentDataContainer container) {
        String defender = getSiegeDefender(container);
        if (defender == null) return getFormattedUnknown();

        return colorDefender(defender);
    }

    public static String getFormattedAttackerPoints(PersistentDataContainer container) {
        Integer points = getSiegeAttackerPoints(container);
        if (points == null) return getFormattedUnknown();

        return colorValue(String.valueOf(points));
    }

    public static String getFormattedDefenderPoints(PersistentDataContainer container) {
        Integer points = getSiegeDefenderPoints(container);
        if (points == null) return getFormattedUnknown();

        return colorValue(String.valueOf(points));
    }

    public static String getFormattedWinner(PersistentDataContainer container) {
        String winning_side = getSiegeWinningSide(container);
        if (winning_side == null) return getFormattedUnknown();

        switch (winning_side) {
            case "ATTACKERS":
                return getFormattedAttacker(container);
            case "DEFENDERS":
                return getFormattedDefender(container);
            case "NOBODY":
                return colorValue(SiegeSide.NOBODY.name());
            default:
                throw new RuntimeException("Unknown winning SiegeSide in SiegeWar lore item.");
        }
    }

    public static String getFormattedOpposition(PersistentDataContainer container) {
        String winning_side = getSiegeWinningSide(container);
        if (winning_side == null) return getFormattedUnknown();

        switch (winning_side) {
            case "ATTACKERS":
                return getFormattedDefender(container);
            case "DEFENDERS":
                return getFormattedAttacker(container);
            case "NOBODY":
                return colorValue(SiegeSide.NOBODY.name());
            default:
                throw new RuntimeException("Unknown winning SiegeSide in SiegeWar lore item.");
        }
    }

    public static String getFormattedStatus(PersistentDataContainer container) {
        String status = getSiegeStatus(container);
        if (status == null) return getFormattedUnknown();

        return colorValue(status);
    }

    public static String getFormattedStart(PersistentDataContainer container) {
        Long time = getSiegeStart(container);
        if (time == null) return getFormattedUnknown();

        return colorValue(new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(time));

    }

    public static String getFormattedEnd(PersistentDataContainer container) {
        Long time = getSiegeEnd(container);
        if (time == null) return getFormattedUnknown();

        return colorValue(new SimpleDateFormat(Translation.of("siege_lore_date_format")).format(time));
    }

    public static void shieldItem(ItemMeta meta, PersistentDataContainer data) {
        if (!hasLoreKey(data)) return;

        String name = Translation.of("siege_lore_two_values",
                getFormattedTown(data),
                colorNeutral(Translation.of("siege_lore_shield_name")));

        meta.setDisplayName(name);

        bannerLore(meta, data);
    }

    public static String bannerChat(PersistentDataContainer data) {
        String winner = getSiegeWinningSide(data);
        if (winner == null) winner = "NOBODY";
        return colorKey(Translation.of("siege_lore_banner_chat_1", getFormattedType(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_2", getFormattedTown(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_3", getFormattedWinner(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_4_" + winner, getFormattedOpposition(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_5_" + winner, getFormattedEnd(data)));
    }

    public static void bannerItem(ItemMeta meta, PersistentDataContainer data) {
        if (!hasLoreKey(data)) return;

        String town = getSiegeTown(data);

        String name = Translation.of("siege_lore_two_values",
                Translation.of("siege_lore_color_defender", town),
                Translation.of("siege_lore_color_neutral",
                        Translation.of("siege_lore_banner_name")));

        meta.setDisplayName(name);

        bannerLore(meta, data);
    }

    public static void bannerLore(ItemMeta meta, PersistentDataContainer data) {
        List<String> lore = new ArrayList<>();

        lore.add(getFormattedType(data));

        String attacker_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_attacker")),
                Translation.of("siege_lore_two_values",
                        getFormattedAttacker(data),
                        Translation.of("siege_lore_color_value",
                                Translation.of("siege_lore_secondary_value", getFormattedAttackerPoints(data)))));
        lore.add(attacker_line);

        String defender_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_defender")),
                Translation.of("siege_lore_two_values",
                        getFormattedDefender(data),
                        Translation.of("siege_lore_color_value",
                                Translation.of("siege_lore_secondary_value", getFormattedDefenderPoints(data)))));
        lore.add(defender_line);

        String winner_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_winner")),
                Translation.of("siege_lore_two_values",
                        getFormattedWinner(data),
                        Translation.of("siege_lore_color_value",
                                Translation.of("siege_lore_secondary_value", getFormattedStatus(data)))));
        lore.add(winner_line);

        String start_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_start")),
                Translation.of("siege_lore_color_value",
                        getFormattedStart(data)));
        lore.add(start_line);

        String end_line = Translation.of("siege_lore_key_value",
                Translation.of("siege_lore_color_key",
                        Translation.of("siege_lore_banner_end")),
                Translation.of("siege_lore_color_value",
                        getFormattedEnd(data)));
        lore.add(end_line);


        meta.setLore(lore);
    }

    public static void bannerCopyData(PersistentDataContainer from, PersistentDataContainer to) {
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
