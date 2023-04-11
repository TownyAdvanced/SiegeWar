package com.gmail.goosius.siegewar.timeractions;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.palmergames.bukkit.towny.object.Nation;

/**
 * This class is responsible for processing siege defender wins
 *
 * @author Goosius
 */
public class DefenderWin
{
	/**
	 * This method triggers siege values to be updated for a defender win
	 * SiegeStatus will already have been set
	 *
	 * @param siege the siege
	 */
    public static void defenderWin(Siege siege) {
    	siege.setSiegeWinner(SiegeSide.DEFENDERS);
		SiegeWarSiegeCompletionUtil.setCommonSiegeCompletionValues(siege);
		if(siege.getSiegeType() == SiegeType.CONQUEST) {
			if(siege.getStatus() == SiegeStatus.DEFENDER_WIN) {
				SiegeWarMoneyUtil.giveWarChestToWinner(siege, siege.getTown());
			} else {
				SiegeWarMoneyUtil.giveWarChestToBoth(siege, siege.getTown(), siege.getAttacker());
			}
		} else {
			if(siege.getStatus() == SiegeStatus.DEFENDER_WIN) {
				Nation nation = (Nation)siege.getAttacker();
				int currentDemoralizationAmount = SiegeWarNationUtil.getDemoralizationAmount(nation);
				int newDemoralizationAmount = currentDemoralizationAmount + SiegeWarSettings.getRevoltSiegeDecisiveDefenderVictoryWeaknessAmount();
				SiegeWarNationUtil.setDemoralizationAmount(nation, newDemoralizationAmount);
				SiegeWarNationUtil.setDemoralizationDays(nation, SiegeWarSettings.getRevoltSiegeDecisiveDefenderVictoryWeaknessDurationDays());
			}
		}
    }

}
