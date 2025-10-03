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
        if(Math.abs(siege.getSiegeBalance()) >= SiegeWarSettings.getTimedVictoryTypesCrushingVictoryThreshold()) {
            siege.setStatus(SiegeStatus.DEFENDER_CRUSHING_WIN);
        } else if(Math.abs(siege.getSiegeBalance()) >= SiegeWarSettings.getTimedVictoryTypesDecisiveVictoryThreshold()) {
            siege.setStatus(SiegeStatus.DEFENDER_DECISIVE_WIN);
        } else {
            siege.setStatus(SiegeStatus.DEFENDER_CLOSE_WIN);
        }
        Messaging.sendGlobalMessage(getStandardTimedDefenderWinMessage(siege));
        DefenderWin.defenderWin(siege);
    }

    private static Translatable getStandardTimedDefenderWinMessage(Siege siege) {
        //Base victory message
        String key = String.format("msg_%s_siege_timed_defender_win", siege.getSiegeType().toLowerCase());
        Translatable message = null;
        switch (siege.getSiegeType()) {
            case CONQUEST:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName(),
                        siege.getStatus().getTimedVictoryTypeText(),
                        siege.getAttacker().getName());
                break;
            case REVOLT:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName(),
                        siege.getStatus().getTimedVictoryTypeText());
                break;
        }

        //Standard effects message
        String key2 = String.format("msg_%s_siege_defender_win_result", siege.getSiegeType().toLowerCase());
        message.append(Translatable.of(key2));
        siege.setEndMessage(message.defaultLocale());
        return message;
    }
}
