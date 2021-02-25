package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.objects.BattleSession;
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
        
        board.getObjective("WAR_HUD_OBJ").setDisplayName(SiegeHUDManager.checkLength(Colors.Gold + "§l" + siege.getDefendingTown().getName()) + " " + Translation.of("hud_title"));
        board.getTeam("attackers").setSuffix(SiegeHUDManager.checkLength(siege.getAttackingNation().getName()));
        board.getTeam("defenders").setSuffix(SiegeHUDManager.checkLength(siege.getDefendingTown().getName()));
        board.getTeam("points").setSuffix(siege.getSiegePoints().toString());
        board.getTeam("timeRemaining").setSuffix(siege.getTimeRemaining());
        board.getTeam("bannerControl").setSuffix(siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase());
        board.getTeam("batAttackerScore").setSuffix(siege.getFormattedAttackerBattleScore());
        board.getTeam("batDefenderScore").setSuffix(siege.getFormattedDefenderBattleScore());
        board.getTeam("batTimeRemaining").setSuffix(BattleSession.getBattleSession().getFormattedTimeRemainingUntilBattleSessionEnds());
    }

    public static void toggleOn(Player p, Siege siege) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("WAR_HUD_OBJ", "", Translation.of("hud_title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);


        Team attackers = board.registerNewTeam("attackers"),
            defenders = board.registerNewTeam("defenders"),
            points = board.registerNewTeam("points"),
            timeRemaining = board.registerNewTeam("timeRemaining"),
            bannerControl = board.registerNewTeam("bannerControl"),
            battleAttackerScore = board.registerNewTeam("batAttackerScore"),
            battleDefenderScore = board.registerNewTeam("batDefenderScore"),
            battleTimeRemaining = board.registerNewTeam("batTimeRemaining");

        String attackers_entry = Colors.LightGray + Translation.of("hud_attackers"),
            defenders_entry = Colors.LightGray + Translation.of("hud_defenders"),
            points_entry = Colors.LightGray + Translation.of("hud_points"),
            timeRemaining_entry = Colors.LightGray + Translation.of("hud_time_remaining"),
            bannerControl_entry = Colors.LightGray + Translation.of("hud_banner_control"),
            battleAttackerScore_entry = Colors.LightGray + Translation.of("hud_battle_attacker_score"),
            battleDefenderScore_entry = Colors.LightGray + Translation.of("hud_battle_defender_score"),
            battleTimeRemaining_entry = Colors.LightGray + Translation.of("hud_battle_time_remaining");

        attackers.addEntry(attackers_entry);
        defenders.addEntry(defenders_entry);
        points.addEntry(points_entry);
        bannerControl.addEntry(bannerControl_entry);
        timeRemaining.addEntry(timeRemaining_entry);
        battleDefenderScore.addEntry(battleDefenderScore_entry);
        battleAttackerScore.addEntry(battleAttackerScore_entry);
        battleTimeRemaining.addEntry(battleTimeRemaining_entry);

        objective.getScore(attackers_entry).setScore(8);
        objective.getScore(defenders_entry).setScore(7);
        objective.getScore(points_entry).setScore(6);
        objective.getScore(bannerControl_entry).setScore(5);
        objective.getScore(timeRemaining_entry).setScore(4);
        objective.getScore(battleAttackerScore_entry).setScore(3);
        objective.getScore(battleDefenderScore_entry).setScore(2);
        objective.getScore(battleTimeRemaining_entry).setScore(1);

        p.setScoreboard(board);
        updateInfo(p, siege);
    }
}