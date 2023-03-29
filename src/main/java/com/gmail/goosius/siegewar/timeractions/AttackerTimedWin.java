package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translation;

/**
 * This class is responsible for processing timed attacker wins
 * i.e. when the siege victory timer hits 0
 *
 * @author Goosius
 */
public class AttackerTimedWin {

    public static void attackerTimedWin(Siege siege) {
        Messaging.sendGlobalMessage(getTimedAttackerWinMessage(siege));
        AttackerWin.attackerWin(siege, SiegeStatus.ATTACKER_WIN);
    }

    private static String getTimedAttackerWinMessage(Siege siege) {
        String key = String.format("msg_%s_siege_timed_attacker_win", siege.getSiegeType().toString().toLowerCase());
        String message = "";
        switch (siege.getSiegeType()) {
            case CONQUEST:
                message = Translation.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName(),
                        siege.getDefendingNationIfPossibleElseTown().getName());
                break;
            case REVOLT:
                message = Translation.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName());
                break;
        }

        String key2 = String.format("msg_%s_siege_attacker_win_result", siege.getSiegeType().toString().toLowerCase());
        message += Translation.of(key2);

        return message;
    }
}
