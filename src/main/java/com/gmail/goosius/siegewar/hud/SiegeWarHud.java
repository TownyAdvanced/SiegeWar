package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.util.TimeMgmt;

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
        
        board.getObjective("WAR_HUD_OBJ").setDisplayName(Colors.Gold + "§l" + siege.getDefendingTown().getName() + " " + Translation.of("hud_title"));
        board.getTeam("attackers").setSuffix(siege.getAttackingNation().getName());
        board.getTeam("defenders").setSuffix(siege.getDefendingTown().getName());
        board.getTeam("points").setSuffix(siege.getSiegePoints().toString());
        board.getTeam("bannerControl").setSuffix(siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase());
        //board.getTeam("timeRemaining").setSuffix(siege.getFormattedHoursUntilScheduledCompletion());
        switch (siege.getStatus()) {
            case PENDING_ATTACKER_ABANDON: {
                board.getTeam("timeRemaining").setSuffix(siege.getFormattedTimeUntilAttackerAbandon());
                break;
            }
            case PENDING_DEFENDER_SURRENDER: {
                board.getTeam("timeRemaining").setSuffix(siege.getFormattedTimeUntilDefenderSurrender());
                break;
            }
            default: {
                board.getTeam("timeRemaining").setSuffix(TimeMgmt.getFormattedTimeValue(siege.getTimeUntilCompletionMillis()));
                break;
            }
        }
    }

    public static void toggleOn(Player p, Siege siege) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("WAR_HUD_OBJ", "", Translation.of("hud_title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team attackers = board.registerNewTeam("attackers"),
            defenders = board.registerNewTeam("defenders"),
            points = board.registerNewTeam("points"),
            bannerControl = board.registerNewTeam("bannerControl"),
            timeRemaining = board.registerNewTeam("timeRemaining");

        String attackers_entry = Colors.LightGray + Translation.of("hud_attackers"),
            defenders_entry = Colors.LightGray + Translation.of("hud_defenders"),
            points_entry = Colors.LightGray + Translation.of("hud_points"),
            bannerControl_entry = Colors.LightGray + Translation.of("hud_banner_control"),
            timeRemaining_entry = Colors.LightGray + Translation.of("hud_time_remaining");

        attackers.addEntry(attackers_entry);
        defenders.addEntry(defenders_entry);
        points.addEntry(points_entry);
        bannerControl.addEntry(bannerControl_entry);
        timeRemaining.addEntry(timeRemaining_entry);

        objective.getScore(attackers_entry).setScore(5);
        objective.getScore(defenders_entry).setScore(4);
        objective.getScore(points_entry).setScore(3);
        objective.getScore(bannerControl_entry).setScore(2);
        objective.getScore(timeRemaining_entry).setScore(1);

        p.setScoreboard(board);
        updateInfo(p, siege);
    }
}