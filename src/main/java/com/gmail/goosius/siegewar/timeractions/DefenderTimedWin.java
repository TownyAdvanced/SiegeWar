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
            siege.setStatus(SiegeStatus.DEFENDER_DECISIVE_WIN);
            Messaging.sendGlobalMessage(getStandardTimedDefenderWinMessage(siege));
            Translatable specialEffectsMessage = getSpecialTimedDefenderWinMessage(siege);
            if(specialEffectsMessage != null)
                Messaging.sendGlobalMessage(specialEffectsMessage);
        } else {
            siege.setStatus(SiegeStatus.DEFENDER_CLOSE_WIN);
            Messaging.sendGlobalMessage(getStandardTimedDefenderWinMessage(siege));
            Translatable specialEffectsMessage = getSpecialTimedDefenderWinMessage(siege);
            if(specialEffectsMessage != null)
                Messaging.sendGlobalMessage(specialEffectsMessage);
        }
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
        siege.setEndMessage(message.toString());
        return message;
    }

    private static Translatable getSpecialTimedDefenderWinMessage(Siege siege) {
        //Special effects message
        Translatable message = null;
        switch (siege.getSiegeType()) {
            case CONQUEST:
                if(siege.getStatus() == SiegeStatus.DEFENDER_CLOSE_WIN) {
                    message = Translatable.of("msg_conquest_siege_defender_close_win_warchest_reduced",
                            SiegeWarSettings.getSpecialVictoryEffectsPlunderReductionPercentageOnCloseVictory() + "%");
                }
                break;
            case REVOLT:
                if(siege.getStatus() == SiegeStatus.DEFENDER_DECISIVE_WIN) {
                    message = Translatable.of("msg_revolt_siege_defender_decisive_win_demoralization",
                            siege.getAttacker().getName(),
                            SiegeWarSettings.getSpecialVictoryEffectsSiegeBalancePenaltyOnDecisiveRebelVictory(),
                            SiegeWarSettings.getSpecialVictoryEffectsSiegeBalancePenaltyDurationDays());
                }
                break;
        }
        return message;
    }

}
