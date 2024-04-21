package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SiegeWarLoreUtil {
    public static final NamespacedKey NAME = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_name");
    public static final NamespacedKey TYPE = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_type");
    public static final NamespacedKey ATTACKER = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_attacker");
    public static final NamespacedKey DEFENDER = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_defender");
    public static final NamespacedKey ATTACKER_POINTS = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_attacker_points");
    public static final NamespacedKey DEFENDER_POINTS = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_defender_points");
    public static final NamespacedKey OUTCOME = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_win_type");
    public static final NamespacedKey WINNER = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_win_side");
    public static final NamespacedKey START = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_start");
    public static final NamespacedKey END = new NamespacedKey(SiegeWar.getSiegeWar(), "banner_end");

    public static void copyBannerData(PersistentDataContainer from, PersistentDataContainer to) {
        if (from.has(NAME, PersistentDataType.STRING))
            to.set(NAME, PersistentDataType.STRING, from.get(NAME, PersistentDataType.STRING));
        if (from.has(TYPE, PersistentDataType.STRING))
            to.set(TYPE, PersistentDataType.STRING, from.get(TYPE, PersistentDataType.STRING));
        if (from.has(ATTACKER, PersistentDataType.STRING))
            to.set(ATTACKER, PersistentDataType.STRING, from.get(ATTACKER, PersistentDataType.STRING));
        if (from.has(DEFENDER, PersistentDataType.STRING))
            to.set(DEFENDER, PersistentDataType.STRING, from.get(DEFENDER, PersistentDataType.STRING));
        if (from.has(ATTACKER_POINTS, PersistentDataType.INTEGER))
            to.set(ATTACKER_POINTS, PersistentDataType.INTEGER, from.get(ATTACKER_POINTS, PersistentDataType.INTEGER));
        if (from.has(DEFENDER_POINTS, PersistentDataType.INTEGER))
            to.set(DEFENDER_POINTS, PersistentDataType.INTEGER, from.get(DEFENDER_POINTS, PersistentDataType.INTEGER));
        if (from.has(OUTCOME, PersistentDataType.STRING))
            to.set(OUTCOME, PersistentDataType.STRING, from.get(OUTCOME, PersistentDataType.STRING));
        if (from.has(WINNER, PersistentDataType.STRING))
            to.set(WINNER, PersistentDataType.STRING, from.get(WINNER, PersistentDataType.STRING));
        if (from.has(START, PersistentDataType.LONG))
            to.set(START, PersistentDataType.LONG, from.get(START, PersistentDataType.LONG));
        if (from.has(END, PersistentDataType.LONG))
            to.set(END, PersistentDataType.LONG, from.get(END, PersistentDataType.LONG));
    }

    public static void setupBanner(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof Banner)) return;
        Banner bannerState = (Banner) blockState;

        PersistentDataContainer container = bannerState.getPersistentDataContainer();

        container.set(TYPE, PersistentDataType.STRING, siege.getSiegeType().getTranslatedName().key());

        container.set(NAME, PersistentDataType.STRING, Translation.of("siege.name", siege.getAttacker().getName(), siege.getDefender().getName()));
        container.set(ATTACKER, PersistentDataType.STRING, siege.getAttacker().getFormattedName());
        container.set(DEFENDER, PersistentDataType.STRING, siege.getDefender().getFormattedName());

        container.set(START, PersistentDataType.LONG, System.currentTimeMillis());

        bannerState.update();
    }

    public static void finaliseBanner(Siege siege) {
        BlockState blockState = siege.getFlagBlock().getState();
        if (!(blockState instanceof Banner)) return;
        Banner bannerState = (Banner) blockState;

        PersistentDataContainer container = bannerState.getPersistentDataContainer();

        container.set(OUTCOME, PersistentDataType.STRING, "siege_status_"+siege.getStatus().name());

        if (siege.getSiegeWinner() == SiegeSide.ATTACKERS) container.set(WINNER, PersistentDataType.STRING, siege.getAttackerNameForDisplay());
        else if (siege.getSiegeWinner() == SiegeSide.DEFENDERS) container.set(WINNER, PersistentDataType.STRING, siege.getDefenderNameForDisplay());

        container.set(ATTACKER_POINTS, PersistentDataType.INTEGER, siege.getAttackerBattlePoints());
        container.set(DEFENDER_POINTS, PersistentDataType.INTEGER, siege.getDefenderBattlePoints());

        container.set(END, PersistentDataType.LONG, System.currentTimeMillis());

        bannerState.update();
    }
}
