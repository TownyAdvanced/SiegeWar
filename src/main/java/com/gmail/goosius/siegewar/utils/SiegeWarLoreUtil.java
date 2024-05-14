package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SiegeWarLoreUtil {
    private static final String SIEGE_BANNER = "siege_banner";
    private static final String SIEGE_SHIELD = "siege_shield";

    private static final NamespacedKey LORE_ITEM = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_item");
    private static final NamespacedKey TYPE = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_type");
    private static final NamespacedKey TOWN = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_town");
    private static final NamespacedKey ATTACKER = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_attacker");
    private static final NamespacedKey DEFENDER = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_defender");
    private static final NamespacedKey ATTACKER_POINTS = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_attacker_points");
    private static final NamespacedKey DEFENDER_POINTS = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_defender_points");
    private static final NamespacedKey WINNER = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_winner");
    private static final NamespacedKey STATUS = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_status");
    private static final NamespacedKey START = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_start");
    private static final NamespacedKey END = new NamespacedKey(SiegeWar.getSiegeWar(), "siege_lore_end");


    public static boolean isLoreItem(BlockState state) {
        if (state == null) return false;
        if (!(state instanceof PersistentDataHolder)) return false;
        PersistentDataHolder holder = (PersistentDataHolder) state;
        return holder.getPersistentDataContainer().has(LORE_ITEM, PersistentDataType.STRING);
    }

    public static boolean isLoreItem(BlockState state, String id) {
        if (id == null) return false;
        if (!isLoreItem(state)) return false;
        PersistentDataHolder holder = (PersistentDataHolder) state;
        String compare = holder.getPersistentDataContainer().get(LORE_ITEM, PersistentDataType.STRING);
        return compare.equals(id);
    }

    public static boolean isLoreItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!itemStack.hasItemMeta()) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(LORE_ITEM, PersistentDataType.STRING);
    }

    public static boolean isLoreItem(ItemStack itemStack, String id) {
        if (id == null) return false;
        if (!isLoreItem(itemStack)) return false;
        String compare = itemStack.getItemMeta().getPersistentDataContainer().get(LORE_ITEM, PersistentDataType.STRING);
        return compare.equals(id);
    }

    private static String colorAttacker(String string) {
        return Translation.of("siege_lore_color_attacker", string);
    }

    private static String colorDefender(String string) {
        return Translation.of("siege_lore_color_defender", string);
    }

    private static String colorNeutral(String string) {
        return Translation.of("siege_lore_color_neutral", string);
    }

    private static String colorKey(String string) {
        return Translation.of("siege_lore_color_key", string);
    }

    private static String colorValue(String string) {
        return Translation.of("siege_lore_color_value", string);
    }


    @Nullable
    private static String getSiegeType(PersistentDataContainer container) {
        return container.get(TYPE, PersistentDataType.STRING);
    }

    @Nullable
    private static String getSiegeTown(PersistentDataContainer container) {
        return container.get(TOWN, PersistentDataType.STRING);
    }

    @Nullable
    private static String getSiegeAttacker(PersistentDataContainer container) {
        return container.get(ATTACKER, PersistentDataType.STRING);
    }

    @Nullable
    private static String getSiegeDefender(PersistentDataContainer container) {
        return container.get(DEFENDER, PersistentDataType.STRING);
    }

    @Nullable
    private static Integer getSiegeAttackerPoints(PersistentDataContainer container) {
        return container.get(ATTACKER_POINTS, PersistentDataType.INTEGER);
    }

    @Nullable
    private static Integer getSiegeDefenderPoints(PersistentDataContainer container) {
        return container.get(DEFENDER_POINTS, PersistentDataType.INTEGER);
    }

    @Nullable
    private static String getSiegeWinningSide(PersistentDataContainer container) {
        return container.get(WINNER, PersistentDataType.STRING);
    }

    @Nullable
    private static String getSiegeStatus(PersistentDataContainer container) {
        return container.get(STATUS, PersistentDataType.STRING);
    }

    @Nullable
    private static Long getSiegeStart(PersistentDataContainer container) {
        return container.get(START, PersistentDataType.LONG);
    }

    @Nullable
    private static Long getSiegeEnd(PersistentDataContainer container) {
        return container.get(END, PersistentDataType.LONG);
    }


    private static String getFormattedUnknown() {
        return colorNeutral(Translation.of("siege_lore_unknown"));
    }

    private static String getFormattedType(PersistentDataContainer container) {
        String type = getSiegeType(container);
        if (type == null) return getFormattedUnknown();

        return colorValue(Translation.of("siege_lore_type", type));
    }

    private static String getFormattedTown(PersistentDataContainer container) {
        String town = getSiegeTown(container);
        if (town == null) return getFormattedUnknown();

        return colorDefender(town);
    }

    private static String getFormattedAttacker(PersistentDataContainer container) {
        String attacker = getSiegeAttacker(container);
        if (attacker == null) return getFormattedUnknown();

        return colorAttacker(attacker);
    }

    private static String getFormattedDefender(PersistentDataContainer container) {
        String defender = getSiegeDefender(container);
        if (defender == null) return getFormattedUnknown();

        return colorDefender(defender);
    }

    private static String getFormattedAttackerPoints(PersistentDataContainer container) {
        Integer points = getSiegeAttackerPoints(container);
        if (points == null) return getFormattedUnknown();

        return colorValue(String.valueOf(points));
    }

    private static String getFormattedDefenderPoints(PersistentDataContainer container) {
        Integer points = getSiegeDefenderPoints(container);
        if (points == null) return getFormattedUnknown();

        return colorValue(String.valueOf(points));
    }

    private static String getFormattedWinner(PersistentDataContainer container) {
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

    private static String getFormattedOpposition(PersistentDataContainer container) {
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

    private static String getFormattedStatus(PersistentDataContainer container) {
        String status = getSiegeStatus(container);
        if (status == null) return getFormattedUnknown();

        return colorValue(status);
    }

    private static String getFormattedStart(PersistentDataContainer container) {
        Long time = getSiegeStart(container);
        if (time == null) return getFormattedUnknown();

        return colorValue(new SimpleDateFormat(Translation.of("siege_lore_format_date")).format(time));

    }

    private static String getFormattedEnd(PersistentDataContainer container) {
        Long time = getSiegeEnd(container);
        if (time == null) return getFormattedUnknown();

        return colorValue(new SimpleDateFormat(Translation.of("siege_lore_format_date")).format(time));
    }

    public static void sendBannerChat(PersistentDataHolder holder, CommandSender target) {
        PersistentDataContainer data = holder.getPersistentDataContainer();

        Locale locale = Translation.getLocale(target);

        String winner = getSiegeWinningSide(data);
        if (winner == null) winner = "NOBODY";

        String chat = colorKey(Translation.of("siege_lore_banner_chat_1", locale, getFormattedType(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_2", locale, getFormattedTown(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_3", locale, getFormattedWinner(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_4_" + winner, locale, getFormattedOpposition(data))) +
                colorKey(Translation.of("siege_lore_banner_chat_5_" + winner, locale, getFormattedEnd(data)));

        Messaging.sendMsg(target, chat);
    }

    public static void setShieldStackFromHolder(ItemStack stack, PersistentDataHolder holder) {
        if (stack == null || holder == null) return;
        if (!stack.hasItemMeta()) return;

        ItemMeta meta = stack.getItemMeta();

        copyPersistentLoreDataToHolder(holder, meta);

        meta.getPersistentDataContainer().set(LORE_ITEM, PersistentDataType.STRING, SIEGE_SHIELD);

        String name = Translation.of("siege_lore_format_two_values",
                getFormattedTown(holder.getPersistentDataContainer()),
                colorNeutral(Translation.of("siege_lore_shield_name")));

        meta.setDisplayName(name);

        setBannerLore(meta, holder.getPersistentDataContainer());

        stack.setItemMeta(meta);
    }

    public static void setBannerStackFromHolder(ItemStack stack, PersistentDataHolder holder) {
        if (stack == null || holder == null) return;
        if (!stack.hasItemMeta()) return;

        ItemMeta meta = stack.getItemMeta();

        copyPersistentLoreDataToHolder(holder, meta);

        meta.getPersistentDataContainer().set(LORE_ITEM, PersistentDataType.STRING, SIEGE_BANNER);

        String name = Translation.of("siege_lore_format_two_values",
                getFormattedTown(holder.getPersistentDataContainer()),
                colorNeutral(Translation.of("siege_lore_banner_name")));

        meta.setDisplayName(name);

        setBannerLore(meta, holder.getPersistentDataContainer());

        stack.setItemMeta(meta);
    }

    private static void setBannerLore(ItemMeta meta, PersistentDataContainer data) {
        List<String> lore = new ArrayList<>();

        lore.add(getFormattedType(data));

        String attacker_line = Translation.of("siege_lore_format_key_value",
                colorKey(Translation.of("siege_lore_attacker")),
                Translation.of("siege_lore_format_two_values",
                        getFormattedAttacker(data),
                        colorValue(Translation.of("siege_lore_format_secondary_value", getFormattedAttackerPoints(data)))));
        lore.add(attacker_line);

        String defender_line = Translation.of("siege_lore_format_key_value",
                colorKey(Translation.of("siege_lore_defender")),
                Translation.of("siege_lore_format_two_values",
                        getFormattedDefender(data),
                        colorValue(Translation.of("siege_lore_format_secondary_value", getFormattedDefenderPoints(data)))));
        lore.add(defender_line);

        String winner_line = Translation.of("siege_lore_format_key_value",
                colorKey(Translation.of("siege_lore_winner")),
                Translation.of("siege_lore_format_two_values",
                        getFormattedWinner(data),
                        colorValue(Translation.of("siege_lore_format_secondary_value", getFormattedStatus(data)))));
        lore.add(winner_line);

        String start_line = Translation.of("siege_lore_format_key_value",
                colorKey(Translation.of("siege_lore_start")),
                colorValue(getFormattedStart(data)));
        lore.add(start_line);

        String end_line = Translation.of("siege_lore_format_key_value",
                colorKey(Translation.of("siege_lore_end")),
                colorValue(getFormattedEnd(data)));
        lore.add(end_line);


        meta.setLore(lore);
    }

    public static void copyPersistentLoreDataToHolder(PersistentDataHolder fromHolder, PersistentDataHolder toHolder) {
        PersistentDataContainer from = fromHolder.getPersistentDataContainer();
        PersistentDataContainer to = toHolder.getPersistentDataContainer();

        if (!from.has(LORE_ITEM, PersistentDataType.STRING)) return;
        to.set(LORE_ITEM, PersistentDataType.STRING, from.get(LORE_ITEM, PersistentDataType.STRING));
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

    public static void applySiegeStartLoreToBannerState(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof PersistentDataHolder)) return;

        PersistentDataHolder holder = (PersistentDataHolder) blockState;
        PersistentDataContainer container = holder.getPersistentDataContainer();

        container.set(LORE_ITEM, PersistentDataType.STRING, "siege_banner");
        container.set(TYPE, PersistentDataType.STRING, siege.getSiegeType().getName());
        container.set(TOWN, PersistentDataType.STRING, siege.getDefender().getName());
        container.set(ATTACKER, PersistentDataType.STRING, siege.getAttackingNationIfPossibleElseTown().getFormattedName());
        container.set(DEFENDER, PersistentDataType.STRING, siege.getDefendingNationIfPossibleElseTown().getFormattedName());
        container.set(START, PersistentDataType.LONG, System.currentTimeMillis());

        blockState.update();
    }

    public static void applySiegeEndLoreToBannerState(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!isLoreItem(blockState, "siege_banner")) return;

        PersistentDataHolder holder = (PersistentDataHolder) blockState;
        PersistentDataContainer container = holder.getPersistentDataContainer();

        container.set(WINNER, PersistentDataType.STRING, siege.getSiegeWinner().name());
        container.set(STATUS, PersistentDataType.STRING, siege.getStatus().getName());
        container.set(ATTACKER_POINTS, PersistentDataType.INTEGER, siege.getAttackerBattlePoints());
        container.set(DEFENDER_POINTS, PersistentDataType.INTEGER, siege.getDefenderBattlePoints());
        container.set(END, PersistentDataType.LONG, System.currentTimeMillis());

        blockState.update();
    }
}
