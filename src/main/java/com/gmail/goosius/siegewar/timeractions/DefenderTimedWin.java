package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.object.Translatable;

/**
 * This class is responsible for processing timed defender wins
 * i.e. when the siege victory timer hits 0
 *
 * @author Goosius
 */
public class DefenderTimedWin {

    public static void defenderTimedWin(Siege siege) {
        Messaging.sendGlobalMessage(getTimedDefenderWinMessage(siege));
        DefenderWin.defenderWin(siege, SiegeStatus.DEFENDER_WIN);
    }

    private static Translatable[] getTimedDefenderWinMessage(Siege siege) {
        String key = String.format("msg_%s_siege_timed_defender_win", siege.getSiegeType().toString().toLowerCase());
        Translatable[] message =  new Translatable[2];
        switch (siege.getSiegeType()) {
            case CONQUEST:
                message[0] = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName(),
                        siege.getAttacker().getName());
                break;
            case REVOLT:
                message[0] = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getDefender().getName());
                break;
        }
        message[1] = Translatable.of("msg_immediate_defender_victory");
        return message;
    }

}
