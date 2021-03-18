package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.Translation;

/**
 * This class is responsible for processing timed defender wins
 * i.e. when the siege victory timer hits 0
 *
 * @author Goosius
 */
public class DefenderTimedWin {

    public static void defenderTimedWin(Siege siege) {
        DefenderWin.defenderWin(siege, SiegeStatus.DEFENDER_WIN);
        Messaging.sendGlobalMessage(getTimedDefenderWinMessage(siege));
    }

    private static String getTimedDefenderWinMessage(Siege siege) {
        String key = String.format("msg_%s_siege_timed_defender_win", siege.getSiegeType().toString().toLowerCase());
        String message = "";
        switch (siege.getSiegeType()) {
            case CONQUEST:
            case SUPPRESSION:
            case LIBERATION:
                message = Translation.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName(),
                        siege.getAttacker().getName());
                break;
            case REVOLT:
                message = Translation.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName());
                break;
        }
        message += Translation.of("msg_immediate_defender_victory");
        return message;
    }

}
