package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.fastboard.FastBoard;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SiegeWarHud {
    private static final Map<UUID, FastBoard> BOARDS = new HashMap<>();

    public static void updateInfo(Player p, Siege siege) {
        FastBoard board = BOARDS.computeIfAbsent(p.getUniqueId(), id -> new FastBoard(p));
        Translator translator = Translator.locale(p);

        String siegeType_entry           = ChatColor.GRAY + translator.of("hud_siege_type");
        String attackers_entry           = ChatColor.GRAY + translator.of("hud_attackers");
        String defenders_entry           = ChatColor.GRAY + translator.of("hud_defenders");
        String balance_entry             = ChatColor.GRAY + translator.of("hud_siege_balance");
        String siegeProgress_entry       = ChatColor.GRAY + translator.of("hud_siege_progress");
        String siegeStatus_entry         = ChatColor.GRAY + translator.of("hud_siege_status");
        String warchest_entry            = ChatColor.GRAY + translator.of("hud_warchest");
        String bannerControl_entry       = ChatColor.GRAY + translator.of("hud_banner_control");
        String battleAttackerScore_entry = ChatColor.GRAY + translator.of("hud_battle_attacker_points");
        String battleDefenderScore_entry = ChatColor.GRAY + translator.of("hud_battle_defender_points");
        String battleTimeRemaining_entry = ChatColor.GRAY + translator.of("hud_battle_time_remaining");

        String titleSuffix  = translator.of("hud_title",
                SiegeHUDManager.checkLength(siege.getTown().getName()));
        String typeSuffix   = SiegeHUDManager.checkLength(
                siege.getSiegeType().getTranslatedName().forLocale(p));
        String atkSuffix    = SiegeHUDManager.checkLength(siege.getAttackerNameForDisplay());
        String defSuffix    = SiegeHUDManager.checkLength(siege.getDefenderNameForDisplay());
        String balSuffix    = siege.getSiegeBalance().toString();
        String progSuffix   = siege.getNumBattleSessionsCompleted()
                + "/" + SiegeWarSettings.getSiegeDurationBattleSessions();
        String statusSuffix = siege.getStatus().getName();
        String chestSuffix  = TownyEconomyHandler.isActive()
                ? TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount())
                : "-";
        String bannerSuffix = siege.getBannerControllingSide()
                .getFormattedName().forLocale(p)
                + (siege.getBannerControllingSide() == SiegeSide.NOBODY
                ? ""
                : " (" + siege.getBannerControllingResidents().size() + ")");
        String atkBpSuffix  = siege.getFormattedAttackerBattlePoints();
        String defBpSuffix  = siege.getFormattedDefenderBattlePoints();
        String timeSuffix   = siege.getFormattedBattleTimeRemaining(translator);

        board.updateTitle(titleSuffix);
        board.updateLines(
                attackers_entry           + " " + atkSuffix,
                defenders_entry           + " " + defSuffix,
                siegeType_entry           + " " + typeSuffix,
                warchest_entry            + " " + chestSuffix,
                siegeProgress_entry       + " " + progSuffix,
                siegeStatus_entry         + " " + statusSuffix,
                balance_entry             + " " + balSuffix,
                bannerControl_entry       + " " + bannerSuffix,
                battleAttackerScore_entry + " " + atkBpSuffix,
                battleDefenderScore_entry + " " + defBpSuffix,
                battleTimeRemaining_entry + " " + timeSuffix
        );

        int epmcLine = board.getLines().size() + 1;
        board.updateLine(epmcLine, ChatColor.GREEN + "play.earthpol.com");
    }

    public static void toggleOn(Player p, Siege siege) {
        FastBoard board = new FastBoard(p);
        BOARDS.put(p.getUniqueId(), board);

        updateInfo(p, siege);
    }

    public static void toggleOff(Player p) {
        FastBoard board = BOARDS.remove(p.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }
}