package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SiegeWarBannerLoreUtil {
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
