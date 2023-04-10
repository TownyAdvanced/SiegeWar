package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Translatable;

/**
 * This class is responsible for processing timed defender wins
 * i.e. when the siege victory timer hits 0
 *
 * @author Goosius
 */
public class DefenderTimedWin {

    public static void defenderTimedWin(Siege siege) {
        if(Math.abs(siege.getSiegeBalance()) >= SiegeWarSettings.getSpecialVictoryEffectsDecisiveVictoryThreshold()) {
            siege.setStatus(SiegeStatus.DEFENDER_WIN);
        } else {
            siege.setStatus(SiegeStatus.DEFENDER_CLOSE_WIN);
        }
        Messaging.sendGlobalMessage(getTimedDefenderWinMessage(siege));
        DefenderWin.defenderWin(siege);
    }

    private static Translatable getTimedDefenderWinMessage(Siege siege) {
        //Base victory message
        String key = String.format("msg_%s_siege_timed_defender_win", siege.getSiegeType().toLowerCase());
        Translatable message = null;
        switch (siege.getSiegeType()) {
            case CONQUEST:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName(),
                        siege.getAttacker().getName());
                break;
            case REVOLT:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName());
                break;
        }

        //Standard effects message
        String key2 = String.format("msg_%s_siege_defender_win_result", siege.getSiegeType().toLowerCase());
        message.append(Translatable.of(key2));

        //Special effects message
        switch (siege.getSiegeType()) {
            case CONQUEST:
                if(siege.getStatus() == SiegeStatus.DEFENDER_CLOSE_WIN) {
                    message.append(Translatable.of("msg_conquest_siege_defender_close_win_special_effects",
                            SiegeWarSettings.getSpecialVictoryEffectsPlunderReductionPercentageOnCloseVictory() + "%"));
                }
                break;
            case REVOLT:
                if(siege.getStatus() == SiegeStatus.DEFENDER_WIN) {
                    message.append(Translatable.of("msg_revolt_siege_defender_decisive_win_special_effects",
                            SiegeWarSettings.getSpecialVictoryWeaknessOnRevoltSiegeDecisiveDefenderVictory()));
                }
                break;
        }
        return message;
    }

}
