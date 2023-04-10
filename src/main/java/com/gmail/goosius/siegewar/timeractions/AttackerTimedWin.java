package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Translatable;

/**
 * This class is responsible for processing timed attacker wins
 * i.e. when the siege victory timer hits 0
 *
 * @author Goosius
 */
public class AttackerTimedWin {

    public static void attackerTimedWin(Siege siege) {
        if(Math.abs(siege.getSiegeBalance()) >= SiegeWarSettings.getSpecialVictoryEffectsDecisiveVictoryThreshold()) {
            siege.setStatus(SiegeStatus.ATTACKER_WIN);
        } else {
            siege.setStatus(SiegeStatus.ATTACKER_CLOSE_WIN);
        }
        Messaging.sendGlobalMessage(getTimedAttackerWinMessage(siege));
        AttackerWin.attackerWin(siege);
    }

    private static Translatable getTimedAttackerWinMessage(Siege siege) {
        //Base victory message
        String key = String.format("msg_%s_siege_timed_attacker_win", siege.getSiegeType().toLowerCase());
        Translatable message = null;
        switch (siege.getSiegeType()) {
            case CONQUEST:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName(),
                        siege.getStatus().getTimedVictoryTypeText(),
                        siege.getDefendingNationIfPossibleElseTown().getName());
                break;
            case REVOLT:
                message = Translatable.of(key,
                        siege.getTown().getName(),
                        siege.getAttacker().getName());
                        siege.getStatus().getTimedVictoryTypeText();
                break;
        }

        //Standard effects message
        String key2 = String.format("msg_%s_siege_attacker_win_result", siege.getSiegeType().toLowerCase());
        message.append(Translatable.of(key2));

        //Special effects message
        switch (siege.getSiegeType()) {
            case CONQUEST:
                if(siege.getStatus() == SiegeStatus.ATTACKER_CLOSE_WIN) {
                    message.append(Translatable.of("msg_conquest_siege_attacker_close_win_special_effects", 
                            SiegeWarSettings.getSpecialVictoryEffectsWarchestReductionPercentageOnCloseVictory() + "%",
                            SiegeWarSettings.getSpecialVictoryEffectsPlunderReductionPercentageOnCloseVictory() + "%"));
                }
                break;
            case REVOLT:
                if(siege.getStatus() == SiegeStatus.ATTACKER_CLOSE_WIN) {
                    message.append(Translatable.of("msg_revolt_siege_attacker_close_win_special_effects",
                            SiegeWarSettings.getSpecialVictoryEffectsPlunderReductionPercentageOnCloseVictory() + "%"));
                }
                break;
        }
        return message;
    }

}
