package com.gmail.goosius.siegewar.objects;


import com.palmergames.util.TimeMgmt;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a "Battle Session".
 *
 * A battle session is period of time, typically 50 minutes,
 * in which individual "battles" take place at each siege.
 *
 * During a battle, battle points can be gained/lost by either side.
 *
 * When a battle ends:
 * - The winner depends on who scores the most battle points.
 * - It will be Attacker, Defender, or a Draw
 * - If nobody contested a battle, no winner is declared.
 * - If there is a winner, their battle points are applied to the siege balance.
 *
 * After a battle ends
 * - Battle points are reset to 0
 * - All banner control is wiped
 * - There is a break (defualt 10 mins) until the next battle session starts
 *   ... during the break, no battle points gains or banner control are possible.
 *
 * FYI
 * In the context of the overall Siege, the siege balance determines the winner
 * (not the battle points)
 *
 * @author Goosius
 */
public class BattleSession {

	private static BattleSession battleSession = null;  //The singleton instance
	private boolean active; 			//Is the session active, or is it on break ?
	private long scheduledEndTIme;	//The time this battle session is scheduled to end
	private Long scheduledStartTime;  //The time this battle session is scheduled to start

	public BattleSession() {
		active = false;
		scheduledEndTIme = 0;
	}

	//Singleton
	public static BattleSession getBattleSession() {
		if(battleSession == null) {
			battleSession = new BattleSession();
		}
		return battleSession;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getScheduledEndTime() {
		return scheduledEndTIme;
	}

	public void setScheduledEndTime(long t) {
		scheduledEndTIme = t;
	}

	public String getFormattedTimeRemainingUntilBattleSessionEnds() {
		return TimeMgmt.getFormattedTimeValue(getTimeRemainingUntilBattleSessionEnds());
	}

	public long getTimeRemainingUntilBattleSessionEnds() {
		long timeLeftMillis = scheduledEndTIme - System.currentTimeMillis();
		if (timeLeftMillis > 0) {
			return timeLeftMillis;
		} else {
			return 0;
		}
	}

	@Nullable
	public Long getScheduledStartTime() {
		return scheduledStartTime;
	}

	public void setScheduledStartTime(Long scheduledStartTime) {
		this.scheduledStartTime = scheduledStartTime;
	}
}
