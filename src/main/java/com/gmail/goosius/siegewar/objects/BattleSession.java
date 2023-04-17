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
	private long scheduledEndTime;	//The time this battle session is scheduled to end
	private Long scheduledStartTime;  //The time this battle session is scheduled to start
	private long startTime;			//The time the session actually started
	private boolean chatDisabled;
	private long scheduledGeneralChatRestorationTime;

	public BattleSession() {
		active = false;
		scheduledEndTime = 0;
		scheduledStartTime = null;
		chatDisabled = false;
		scheduledGeneralChatRestorationTime = 0;
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
		return scheduledEndTime;
	}

	public void setScheduledEndTime(long t) {
		scheduledEndTime = t;
	}

	public String getFormattedTimeRemainingUntilBattleSessionEnds() {
		return TimeMgmt.getFormattedTimeValue(getTimeRemainingUntilBattleSessionEnds());
	}

	public long getTimeRemainingUntilBattleSessionEnds() {
		long timeLeftMillis = scheduledEndTime - System.currentTimeMillis();
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

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean isChatDisabled() {
		return chatDisabled;
	}

	public void setChatDisabled(boolean chatDisabled) {
		this.chatDisabled = chatDisabled;
	}

	public long getScheduledGeneralChatRestorationTime() {
		return scheduledGeneralChatRestorationTime;
	}

	public void setScheduledGeneralChatRestorationTime(double scheduledGeneralChatRestorationTime) {
		this.scheduledGeneralChatRestorationTime = (long)scheduledGeneralChatRestorationTime;
	}

}
