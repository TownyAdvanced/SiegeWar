package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.objects.Siege;

public class SiegeWarWallBreachUtil {

    /**
     * Evaluate all wall breaches
     * 
     * A 'wall breach' is when a hostile-to-town combatant enters the homeblock of the town
     * If they are on the Banner Control list, the wall breach bonus is awarded
     * The bonus is awarded once per player ber battle session
     */
    public static void evaluateWallBreaches() {
        for (Siege siege : SiegeController.getSieges()) {

		}
    }
}
