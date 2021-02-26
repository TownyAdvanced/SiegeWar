package com.gmail.goosius.siegewar.objects;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static com.palmergames.util.TimeMgmt.ONE_HOUR_IN_MILLIS;

/**
 * This class represents a "Siege".
 * 
 * A siege is an attack by an nation on a town.
 * 
 * A siege is initiated by a nation leader with appropriate permissions,
 * It typically lasts for a moderate duration (e.g. hours or days),
 * and can be ended n a number of ways, including abandon, surrender, or points victory.
 * 
 * After a siege ends, it enters an aftermath phase where the status is no longer "In Progress",
 * During this phase, the town cannot be attacked again,
 * and if an attacker has won, they have the options of "plunder" or "invade".
 *
 * @author Goosius
 */
public class Siege {
	private String name;
	private Nation attackingNation;
	private Town defendingTown;
    private SiegeStatus status;
    private boolean townPlundered;
    private boolean townInvaded;
    private long startTime;           //Start of siege
    private long scheduledEndTime;    //Scheduled end of siege
    private long actualEndTime;       //Actual end time of siege
	private Location siegeBannerLocation;
	private int siegePoints;
	private double warChestAmount;
	private List<Resident> bannerControllingResidents;  //Soldiers currently controlling the banner
	private SiegeSide bannerControllingSide;
	private Map<Player, BannerControlSession> bannerControlSessions;
	private int battleScoreEarnedFromCurrentBannerControl;
	private boolean attackerHasLowestPopulation;
	private double siegePointModifierForSideWithLowestPopulation;
	private int cannonSessionRemainingShortTicks;  //Short ticks remaining until standard cannon protections are restored
	private int attackerBattleScore;
	private int defenderBattleScore;
	private Set<String> attackerBattleContributors;   //UUID's of attackers who contributed during the current battle
	private Map<String, Integer> attackerSiegeContributors;  //UUID:numContributions map of attackers who contributed during current siege

	public Siege(String name) {
        this.name = name;
        status = SiegeStatus.IN_PROGRESS;
		attackingNation = null;
		siegePoints = 0;
		siegeBannerLocation = null;
		warChestAmount = 0;
		bannerControllingResidents = new ArrayList<>();
		bannerControllingSide = SiegeSide.NOBODY;
		bannerControlSessions = new HashMap<>();
		attackerHasLowestPopulation = false;
		siegePointModifierForSideWithLowestPopulation = 0;  //0 is the special starting value
		cannonSessionRemainingShortTicks = 0;
		attackerBattleScore = 0;
		defenderBattleScore = 0;
		attackerBattleContributors = new HashSet<>();
		attackerSiegeContributors = new HashMap<>();
    }

	public Nation getAttackingNation() {
		return attackingNation;
	}
	
    public Town getDefendingTown() {
        return defendingTown;
    }

	public long getScheduledEndTime() {
        return scheduledEndTime;
    }

    public long getActualEndTime() {
        return actualEndTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setScheduledEndTime(long scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public void setActualEndTime(long actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public long getStartTime() {
        return startTime;
    }
	
	public void setStatus(SiegeStatus status) {
        this.status = status;
    }

    public void setTownPlundered(boolean townPlundered) {
        this.townPlundered = townPlundered;
    }

    public void setTownInvaded(boolean townInvaded) {
        this.townInvaded = townInvaded;
    }
    
    public SiegeStatus getStatus() {
        return status;
    }

    public boolean isTownPlundered() {
        return townPlundered;
    }

    public boolean isTownInvaded() {
        return townInvaded;
    }

    public double getTimeUntilCompletionMillis() {
        return scheduledEndTime - System.currentTimeMillis();
    }

    public String getFormattedHoursUntilScheduledCompletion() {
        if(status == SiegeStatus.IN_PROGRESS) {
            double timeUntilCompletionMillis = getTimeUntilCompletionMillis();
            return TimeMgmt.getFormattedTimeValue(timeUntilCompletionMillis);
        } else {
            return "0";
        }
    }

    public boolean getTownPlundered() {
        return townPlundered;
    }

    public boolean getTownInvaded() {
        return townInvaded;
    }

	public long getDurationMillis() {
		return System.currentTimeMillis() - startTime;
	}

	public long getTimeUntilSurrenderConfirmationMillis() {
		return (long)((SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeSurrenderHours() * ONE_HOUR_IN_MILLIS) - getDurationMillis());
	}

	public long getTimeUntilAbandonConfirmationMillis() {
		return (long)((SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeAbandonHours() * ONE_HOUR_IN_MILLIS) - getDurationMillis());
	}

	public String getName() {
		return name;
	}
	
	public void setAttackingNation(Nation attackingNation) {
		this.attackingNation = attackingNation;
	}

	public void setDefendingTown(Town defendingTown) {
		this.defendingTown = defendingTown;
	}

	public Location getFlagLocation() {
		return siegeBannerLocation;
	}

	public void setFlagLocation(Location location) {
		this.siegeBannerLocation = location;
	}

	public Integer getSiegePoints() {
		return siegePoints;
	}

	public void setSiegePoints(int siegePoints) {
		this.siegePoints = siegePoints;
	}

	public void adjustSiegePoints(int adjustment) {
		siegePoints += adjustment;
	}

	public double getWarChestAmount() {
		return warChestAmount;
	}

	public void setWarChestAmount(double warChestAmount) {
		this.warChestAmount = warChestAmount;
	}

	public List<Resident> getBannerControllingResidents() {
		return new ArrayList<>(bannerControllingResidents);
	}

	public void addBannerControllingResident(Resident resident) {
		bannerControllingResidents.add(resident);
	}

	public void removeBannerControllingResident(Resident resident) {
		bannerControllingResidents.remove(resident);
	}

	public void clearBannerControllingResidents() {
		bannerControllingResidents.clear();
	}

	public void clearBannerControlSessions() {
		bannerControlSessions.clear();
	}

	public SiegeSide getBannerControllingSide() {
		return bannerControllingSide;
	}

	public void setBannerControllingSide(SiegeSide bannerControllingSide) {
		this.bannerControllingSide = bannerControllingSide;
	}

	public Map<Player, BannerControlSession> getBannerControlSessions() {
		return new HashMap<>(bannerControlSessions);
	}

	public void removeBannerControlSession(BannerControlSession bannerControlSession) {
		bannerControlSessions.remove(bannerControlSession.getPlayer());
	}

	public void addBannerControlSession(Player player, BannerControlSession bannerControlSession) {
		bannerControlSessions.put(player, bannerControlSession);
	}

	public void setName(String newSiegeName) {
		this.name = newSiegeName;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public double getSiegePointModifierForSideWithLowestPopulation() {
		return siegePointModifierForSideWithLowestPopulation;
	}

	public void setSiegePointModifierForSideWithLowestPopulation(double siegePointModifierForSideWithLowestPopulation) {
		this.siegePointModifierForSideWithLowestPopulation = siegePointModifierForSideWithLowestPopulation;
	}

	public boolean isAttackerHasLowestPopulation() {
		return attackerHasLowestPopulation;
	}

	public void setAttackerHasLowestPopulation(boolean attackerHasLowestPopulation) {
		this.attackerHasLowestPopulation = attackerHasLowestPopulation;
	}

	/**
	 * @return amount of time left as a String.
	 */
	public String getTimeRemaining() {
		double timeLeft;
		switch (this.getStatus()) {
			case IN_PROGRESS:
				timeLeft = getTimeUntilCompletionMillis();
				break;
			case PENDING_ATTACKER_ABANDON:
				timeLeft = (SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeAbandonHours() * ONE_HOUR_IN_MILLIS) - getDurationMillis();
				break;
			case PENDING_DEFENDER_SURRENDER:
				timeLeft = (SiegeWarSettings.getWarSiegeMinSiegeDurationBeforeSurrenderHours() * ONE_HOUR_IN_MILLIS) - getDurationMillis();
				break;
			default:
				timeLeft = 0;
		}
		return TimeMgmt.getFormattedTimeValue(timeLeft);
	}

	public int getCannonSessionRemainingShortTicks() {
		return cannonSessionRemainingShortTicks;
	}

	public void setCannonSessionRemainingShortTicks(int val) {
		cannonSessionRemainingShortTicks = val;
	}

	public void decrementCannonSessionRemainingShortTicks(){
		cannonSessionRemainingShortTicks--;
	}

	public int getAttackerBattleScore() {
		return attackerBattleScore;
	}

	public void setAttackerBattleScore(int attackerBattleScore) {
		this.attackerBattleScore = attackerBattleScore;
	}

	public String getFormattedAttackerBattleScore() {
		if(attackerBattleScore == 0) {
			return "0";
		} else {
			return "+" + attackerBattleScore;
		}
	}

	public int getDefenderBattleScore() {
		return defenderBattleScore;
	}

	public void setDefenderBattleScore(int defenderBattleScore) {
		this.defenderBattleScore = defenderBattleScore;
	}

	public String getFormattedDefenderBattleScore() {
		if(defenderBattleScore == 0) {
			return "0";
		} else {
			return "-" + defenderBattleScore;
		}
	}

	public void adjustAttackerBattleScore(int battleScore) {
		attackerBattleScore += battleScore;
	}

	public void adjustDefenderBattleScore(int battleScore) {
		defenderBattleScore += battleScore;
	}

	public String getFormattedBattleTimeRemaining() {
		if (BattleSession.getBattleSession().isActive()
			&& status == SiegeStatus.IN_PROGRESS
			&& (getAttackerBattleScore() > 0
				|| getDefenderBattleScore() > 0
				|| getBannerControllingSide() != SiegeSide.NOBODY
				|| getBannerControlSessions().size() > 0)) {
			return BattleSession.getBattleSession().getFormattedTimeRemainingUntilBattleSessionEnds();
		} else {
			return Translation.of("msg_na");
		}
	}

	public Set<String> getAttackerBattleContributors() {
		return attackerBattleContributors;
	}

	public void setAttackerBattleContributors(Set<String> attackerBattleContributors) {
		this.attackerBattleContributors = attackerBattleContributors;
	}

	public void clearAttackerBattleContributors() {
		attackerBattleContributors.clear();
	}
	public Map<String, Integer> getAttackerSiegeContributors() {
		return attackerSiegeContributors;
	}

	public void setAttackerSiegeContributors(Map<String, Integer> attackerSiegeContributors) {
		this.attackerSiegeContributors = attackerSiegeContributors;
	}

	public void registerAttackerBattleContributorsFromBannerControl() {
		for(Resident resident: bannerControllingResidents) {
			attackerBattleContributors.add(resident.getUUID().toString());
		}
	}

	public void propagateAttackerBattleContributorsToAttackerSiegeContributors() {
		for(String playerUuid: attackerBattleContributors) {
			if(attackerSiegeContributors.containsKey(playerUuid)) {
				attackerSiegeContributors.put(playerUuid, attackerSiegeContributors.get(playerUuid) + 1);
			} else {
				attackerSiegeContributors.put(playerUuid, 1);
			}
		}
	}

	public int getBattleScoreEarnedFromCurrentBannerControl() {
		return battleScoreEarnedFromCurrentBannerControl;
	}

	public void setBattleScoreEarnedFromCurrentBannerControl(int battleScoreEarnedFromCurrentBannerControl) {
		this.battleScoreEarnedFromCurrentBannerControl = battleScoreEarnedFromCurrentBannerControl;
	}

	public void adjustBattleScoreEarnedFromCurrentBannerControl(int timedBattleScore) {
		battleScoreEarnedFromCurrentBannerControl += timedBattleScore;
	}
}