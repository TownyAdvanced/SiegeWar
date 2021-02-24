package com.gmail.goosius.siegewar.objects;

import java.util.List;
import java.util.Map;

/**
 * This class represents a "Battle Session".
 *
 * A battle session is period of time, typically 50 minutes,
 * in which individual "battles" take place at each siege.
 *
 * During a battle session, siege points can be gained/lost in every battle.
 *
 * At the end of each battle session:
 * - The winner of every battle is declared
 *    - either the "Attacker" or the "Defender", or "Draw"
 *    - If nobody contested a battle, no winner is declared.
 * - All scoreboards will show that the winner won +1 "battle".
 * - Siege points are reset to 0
 * - Plunder is awarded to any soldiers on the winning side,
 *   who gained banner control during the battle.
 *   (Attackers steal from the town, defenders steals from the war chest)
 *
 * Until the next battle session starts:
 * - This period is known as a "break", and typically lasts 10 minutes.
 * - During this period, siege points remain at 0, and banner control is not possible.
 *
 * At the normal end of a siege, whoever has won the most "battles", wins the siege.
 *
 * @author Goosius
 */
public class BattleSession {

	private static BattleSession battleSession = null;  //The singleton instance
	private boolean active; 			//Is the session active, or is it on break ?
	private long scheduledEndTIme;	//The time this battle session is scheduled to end

	public BattleSession() {
		active = false;
	}

	//Singleton
	public static BattleSession getBattleSession() {
		if(battleSession == null) {
			battleSession = new BattleSession();
		}
		return battleSession;
	}
}
