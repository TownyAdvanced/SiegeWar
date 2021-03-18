package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.util.Colors;
import org.bukkit.Bukkit;
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
        
        board.getObjective("WAR_HUD_OBJ").setDisplayName(SiegeHUDManager.checkLength(Colors.Gold + "§l" + siege.getTown().getName()) + " " + Translation.of("hud_title"));
        board.getTeam("siegeType").setSuffix(SiegeHUDManager.checkLength(siege.getSiegeType().getName()));
        board.getTeam("attackers").setSuffix(SiegeHUDManager.checkLength(siege.getNation().getName()));
        board.getTeam("defenders").setSuffix(SiegeHUDManager.checkLength(siege.getTown().getName()));
        board.getTeam("balance").setSuffix(siege.getSiegeBalance().toString());
        board.getTeam("timeRemaining").setSuffix(siege.getTimeRemaining());
        board.getTeam("bannerControl").setSuffix(siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase());
        board.getTeam("btAttackerPoints").setSuffix(siege.getFormattedAttackerBattlePoints());
        board.getTeam("btDefenderPoints").setSuffix(siege.getFormattedDefenderBattlePoints());
        board.getTeam("btTimeRemaining").setSuffix(siege.getFormattedBattleTimeRemaining());
    }

    public static void toggleOn(Player p, Siege siege) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("WAR_HUD_OBJ", "", Translation.of("hud_title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team siegeType = board.registerNewTeam("siegeType"),
            attackers = board.registerNewTeam("attackers"),
            defenders = board.registerNewTeam("defenders"),
            balance = board.registerNewTeam("balance"),
            timeRemaining = board.registerNewTeam("timeRemaining"),
            bannerControl = board.registerNewTeam("bannerControl"),
            battleAttackerScore = board.registerNewTeam("btAttackerPoints"),
            battleDefenderScore = board.registerNewTeam("btDefenderPoints"),
            battleTimeRemaining = board.registerNewTeam("btTimeRemaining");

        String siegeType_entry = Colors.LightGray + Translation.of("hud_siege_type"),
            attackers_entry = Colors.LightGray + Translation.of("hud_attackers"),
            defenders_entry = Colors.LightGray + Translation.of("hud_defenders"),
            balance_entry = Colors.LightGray + Translation.of("hud_siege_balance"),
            timeRemaining_entry = Colors.LightGray + Translation.of("hud_time_remaining"),
            bannerControl_entry = Colors.LightGray + Translation.of("hud_banner_control"),
            battleAttackerScore_entry = Colors.LightGray + Translation.of("hud_battle_attacker_points"),
            battleDefenderScore_entry = Colors.LightGray + Translation.of("hud_battle_defender_points"),
            battleTimeRemaining_entry = Colors.LightGray + Translation.of("hud_battle_time_remaining");

        siegeType.addEntry(siegeType_entry);
        attackers.addEntry(attackers_entry);
        defenders.addEntry(defenders_entry);
        balance.addEntry(balance_entry);
        bannerControl.addEntry(bannerControl_entry);
        timeRemaining.addEntry(timeRemaining_entry);
        battleDefenderScore.addEntry(battleDefenderScore_entry);
        battleAttackerScore.addEntry(battleAttackerScore_entry);
        battleTimeRemaining.addEntry(battleTimeRemaining_entry);

        objective.getScore(siegeType_entry).setScore(9);
        objective.getScore(attackers_entry).setScore(8);
        objective.getScore(defenders_entry).setScore(7);
        objective.getScore(balance_entry).setScore(6);
        objective.getScore(timeRemaining_entry).setScore(5);
        objective.getScore(bannerControl_entry).setScore(4);
        objective.getScore(battleAttackerScore_entry).setScore(3);
        objective.getScore(battleDefenderScore_entry).setScore(2);
        objective.getScore(battleTimeRemaining_entry).setScore(1);

        p.setScoreboard(board);
        updateInfo(p, siege);
    }
}