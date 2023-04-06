package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class SiegeWarHud {
    public static void updateInfo(Player p, Siege siege) {
        Scoreboard board = p.getScoreboard();
        if (board == null) {
            toggleOn(p, siege);
            return;
        }
        final Translator translator = Translator.locale(p);
        board.getObjective("WAR_HUD_OBJ").setDisplayName(SiegeHUDManager.checkLength(ChatColor.GOLD + "§l" + siege.getTown().getName()) + " " + translator.of("hud_title"));
        board.getTeam("siegeType").setSuffix(SiegeHUDManager.checkLength(siege.getSiegeType().getTranslatedName().forLocale(p)));
        board.getTeam("attackers").setSuffix(SiegeHUDManager.checkLength(siege.getAttackerNameForDisplay()));
        board.getTeam("defenders").setSuffix(SiegeHUDManager.checkLength(siege.getDefenderNameForDisplay()));
        board.getTeam("balance").setSuffix(siege.getSiegeBalance().toString());
        board.getTeam("siegeProgress").setSuffix(siege.getNumBattleSessionsCompleted() + "/" + SiegeWarSettings.getSiegeDurationBattleSessions());
        board.getTeam("siegeStatus").setSuffix(siege.getStatus().getName());
        if(TownyEconomyHandler.isActive()) {
            board.getTeam("warchest").setSuffix(TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));
        } else {
            board.getTeam("warchest").setSuffix("-");
        }        
        board.getTeam("bannerControl").setSuffix(
            siege.getBannerControllingSide().getFormattedName().forLocale(p)
            + (siege.getBannerControllingSide() == SiegeSide.NOBODY ? "" :  " (" + siege.getBannerControllingResidents().size() + ")"));
        board.getTeam("btAttackerPoints").setSuffix(siege.getFormattedAttackerBattlePoints());
        board.getTeam("btDefenderPoints").setSuffix(siege.getFormattedDefenderBattlePoints());
        board.getTeam("btTimeRemaining").setSuffix(siege.getFormattedBattleTimeRemaining(translator));
    }

    public static void toggleOn(Player p, Siege siege) {
    	final Translator translator = Translator.locale(p);
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("WAR_HUD_OBJ", "", translator.of("hud_title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team siegeType = board.registerNewTeam("siegeType"),
            attackers = board.registerNewTeam("attackers"),
            defenders = board.registerNewTeam("defenders"),
            balance = board.registerNewTeam("balance"),
            siegeProgress = board.registerNewTeam("siegeProgress"),
            siegeStatus = board.registerNewTeam("siegeStatus"),
            warchest = board.registerNewTeam("warchest"),
            bannerControl = board.registerNewTeam("bannerControl"),
            battleAttackerScore = board.registerNewTeam("btAttackerPoints"),
            battleDefenderScore = board.registerNewTeam("btDefenderPoints"),
            battleTimeRemaining = board.registerNewTeam("btTimeRemaining");

            String siegeType_entry = ChatColor.GRAY + translator.of("hud_siege_type"),
            attackers_entry = ChatColor.GRAY + translator.of("hud_attackers"),
            defenders_entry = ChatColor.GRAY + translator.of("hud_defenders"),
            balance_entry = ChatColor.GRAY + translator.of("hud_siege_balance"),
            siegeProgress_entry = ChatColor.GRAY + translator.of("hud_siege_progress"),
            siegeStatus_entry = ChatColor.GRAY + translator.of("hud_siege_status"),
            warchest_entry = ChatColor.GRAY + translator.of("hud_warchest"),
            bannerControl_entry = ChatColor.GRAY + translator.of("hud_banner_control"),
            battleAttackerScore_entry = ChatColor.GRAY + translator.of("hud_battle_attacker_points"),
            battleDefenderScore_entry = ChatColor.GRAY + translator.of("hud_battle_defender_points"),
            battleTimeRemaining_entry = ChatColor.GRAY + translator.of("hud_battle_time_remaining");

        siegeType.addEntry(siegeType_entry);
        attackers.addEntry(attackers_entry);
        defenders.addEntry(defenders_entry);
        balance.addEntry(balance_entry);
        bannerControl.addEntry(bannerControl_entry);
        siegeProgress.addEntry(siegeProgress_entry);
        siegeStatus.addEntry(siegeStatus_entry);
        warchest.addEntry(warchest_entry);
        battleDefenderScore.addEntry(battleDefenderScore_entry);
        battleAttackerScore.addEntry(battleAttackerScore_entry);
        battleTimeRemaining.addEntry(battleTimeRemaining_entry);
        
        int topScore = 10;
        
        objective.getScore(attackers_entry).setScore(topScore--);
        objective.getScore(defenders_entry).setScore(topScore--);
        objective.getScore(siegeType_entry).setScore(topScore--);
        objective.getScore(warchest_entry).setScore(topScore--);
        objective.getScore(siegeProgress_entry).setScore(topScore--);
        objective.getScore(siegeStatus_entry).setScore(topScore--);
        objective.getScore(balance_entry).setScore(topScore--);
        objective.getScore(bannerControl_entry).setScore(topScore--);
        objective.getScore(battleAttackerScore_entry).setScore(topScore--);
        objective.getScore(battleDefenderScore_entry).setScore(topScore--);
        objective.getScore(battleTimeRemaining_entry).setScore(topScore--);

        p.setScoreboard(board);
        updateInfo(p, siege);
    }
}