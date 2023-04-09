package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translatable;

/**
 * This class is responsible for processing timed attacker wins
 * i.e. when the siege victory timer hits 0
 *
 * @author Goosius
 */
public class AttackerTimedWin {

    public static void attackerTimedWin(Siege siege) {
        asdsad
        Messaging.sendGlobalMessage(getTimedAttackerWinMessage(siege));
        AttackerWin.attackerWin(siege, SiegeStatus.ATTACKER_WIN);
    }

    private static Translatable getTimedAttackerWinMessage(Siege siege) {
        String key = String.format("msg_%s_siege_timed_attacker_win", siege.getSiegeType().toLowerCase());
        Translatable message = null;
        switch (siege.getSiegeType()) {
            case CONQUEST:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName(),
                        siege.getDefendingNationIfPossibleElseTown().getName());
                break;
            case REVOLT:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName());
                break;
        }
        String key2 = String.format("msg_%s_siege_attacker_win_result", siege.getSiegeType().toLowerCase());
        message.append(Translatable.of(key2));
        return message;
    }
}
