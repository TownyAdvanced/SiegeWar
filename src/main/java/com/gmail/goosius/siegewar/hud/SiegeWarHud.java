package com.gmail.goosius.siegewar.hud;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.util.Colors;
import org.apache.commons.lang.WordUtils;
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
        final Translator translator = Translator.locale(Translation.getLocale(p));
        board.getObjective("WAR_HUD_OBJ").setDisplayName(SiegeHUDManager.checkLength(Colors.Gold + "§l" + siege.getTown().getName()) + " " + translator.of("hud_title"));
        board.getTeam("siegeType").setSuffix(SiegeHUDManager.checkLength(siege.getSiegeType().getTranslatedName().forLocale(p)));
        board.getTeam("attackers").setSuffix(SiegeHUDManager.checkLength(siege.getAttackerNameForDisplay()));
        board.getTeam("defenders").setSuffix(SiegeHUDManager.checkLength(siege.getDefenderNameForDisplay()));
        board.getTeam("balance").setSuffix(siege.getSiegeBalance().toString());
        board.getTeam("timeRemaining").setSuffix(siege.getTimeRemaining());
        if(TownyEconomyHandler.isActive()) {
            board.getTeam("warchest").setSuffix(TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));
        } else {
            board.getTeam("warchest").setSuffix("-");
        }        
        board.getTeam("bannerControl").setSuffix(
            WordUtils.capitalizeFully(siege.getBannerControllingSide().name())
            + (siege.getBannerControllingSide() == SiegeSide.NOBODY ? "" :  " (" + siege.getBannerControllingResidents().size() + ")"));
        board.getTeam("btAttackerPoints").setSuffix(siege.getFormattedAttackerBattlePoints());
        board.getTeam("btDefenderPoints").setSuffix(siege.getFormattedDefenderBattlePoints());
        board.getTeam("btTimeRemaining").setSuffix(siege.getFormattedBattleTimeRemaining(translator));
        boolean displayBreachPoints = SiegeWarSettings.isWallBreachingEnabled() && SiegeWarSettings.getWallBreachBonusBattlePoints() != 0;
        if(displayBreachPoints)       
            board.getTeam("breachPoints").setSuffix(siege.getFormattedBreachPoints());        
    }

    public static void toggleOn(Player p, Siege siege) {
    	final Translator translator = Translator.locale(Translation.getLocale(p));
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("WAR_HUD_OBJ", "", translator.of("hud_title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team siegeType = board.registerNewTeam("siegeType"),
            attackers = board.registerNewTeam("attackers"),
            defenders = board.registerNewTeam("defenders"),
            balance = board.registerNewTeam("balance"),
            timeRemaining = board.registerNewTeam("timeRemaining"),
            warchest = board.registerNewTeam("warchest"),
            bannerControl = board.registerNewTeam("bannerControl"),
            battleAttackerScore = board.registerNewTeam("btAttackerPoints"),
            battleDefenderScore = board.registerNewTeam("btDefenderPoints"),
            battleTimeRemaining = board.registerNewTeam("btTimeRemaining");

            String siegeType_entry = Colors.LightGray + translator.of("hud_siege_type"),
            attackers_entry = Colors.LightGray + translator.of("hud_attackers"),
            defenders_entry = Colors.LightGray + translator.of("hud_defenders"),
            balance_entry = Colors.LightGray + translator.of("hud_siege_balance"),
            timeRemaining_entry = Colors.LightGray + translator.of("hud_time_remaining"),
            warchest_entry = Colors.LightGray + translator.of("hud_warchest"),
            bannerControl_entry = Colors.LightGray + translator.of("hud_banner_control"),
            battleAttackerScore_entry = Colors.LightGray + translator.of("hud_battle_attacker_points"),
            battleDefenderScore_entry = Colors.LightGray + translator.of("hud_battle_defender_points"),
            battleTimeRemaining_entry = Colors.LightGray + translator.of("hud_battle_time_remaining");

        siegeType.addEntry(siegeType_entry);
        attackers.addEntry(attackers_entry);
        defenders.addEntry(defenders_entry);
        balance.addEntry(balance_entry);
        bannerControl.addEntry(bannerControl_entry);
        timeRemaining.addEntry(timeRemaining_entry);
        warchest.addEntry(warchest_entry);
        battleDefenderScore.addEntry(battleDefenderScore_entry);
        battleAttackerScore.addEntry(battleAttackerScore_entry);
        battleTimeRemaining.addEntry(battleTimeRemaining_entry);
        
        int topScore;
        boolean displayBreachPoints = SiegeWarSettings.isWallBreachingEnabled() && SiegeWarSettings.getWallBreachBonusBattlePoints() != 0;
        if(displayBreachPoints)
            topScore = 11;
        else
            topScore = 10;
        
        objective.getScore(siegeType_entry).setScore(topScore--);
        objective.getScore(attackers_entry).setScore(topScore--);
        objective.getScore(defenders_entry).setScore(topScore--);
        objective.getScore(balance_entry).setScore(topScore--);
        objective.getScore(timeRemaining_entry).setScore(topScore--);
        objective.getScore(warchest_entry).setScore(topScore--);
        objective.getScore(bannerControl_entry).setScore(topScore--);
        objective.getScore(battleAttackerScore_entry).setScore(topScore--);
        objective.getScore(battleDefenderScore_entry).setScore(topScore--);
        objective.getScore(battleTimeRemaining_entry).setScore(topScore--);

        if(displayBreachPoints) {
            Team breachPoints = board.registerNewTeam("breachPoints");
            String breachPoints_entry = Colors.LightGray + translator.of("hud_breach_points");
            breachPoints.addEntry(breachPoints_entry);
            objective.getScore(breachPoints_entry).setScore(topScore--);
        }

        p.setScoreboard(board);
        updateInfo(p, siege);
    }
}