package com.gmail.goosius.siegewar.objects;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Government;
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
 * and can be ended n a number of ways, including when siege victory timer reaches 0, or abandon, or surrender.
 * 
 * After a siege ends, it enters an aftermath phase where the status is no longer "In Progress",
 * During this phase, the town cannot be attacked again,
 * and if an attacker has won, they have the options of "plunder" or "invade".
 *
 * @author Goosius
 */
public class Siege {
	private String name;
	private SiegeType siegeType;
	private Town town;
	private Nation nation;
    private SiegeStatus status;
    private boolean townPlundered;
    private boolean townInvaded;
    private long startTime;           //Start of siege
    private long scheduledEndTime;    //Scheduled end of siege
    private long actualEndTime;       //Actual end time of siege
	private Location siegeBannerLocation;
	private int siegeBalance;
	private double warChestAmount;
	private List<Resident> bannerControllingResidents;  //Soldiers currently controlling the banner
	private SiegeSide bannerControllingSide;
	private Map<Player, BannerControlSession> bannerControlSessions;
	private int timedBattlePointsEarnedFromCurrentBannerControl;
	private boolean attackerHasLowestPopulation;
	private double battlePointstModifierForSideWithLowestPopulation;
	private int cannonSessionRemainingShortTicks;  //Short ticks remaining until standard cannon protections are restored
	private int attackerBattlePoints;
	private int defenderBattlePoints;
	private Set<String> attackerBattleContributors;   //UUID's of attackers who contributed during the current battle
	private Map<String, Integer> attackerSiegeContributors;  //UUID:numContributions map of attackers who contributed during current siege

	public Siege(Town town) {
		this.town = town;
		name = "";
        siegeType = SiegeType.CONQUEST;
        status = SiegeStatus.IN_PROGRESS;
		nation = null;
		siegeBalance = 0;
		siegeBannerLocation = null;
		warChestAmount = 0;
		bannerControllingResidents = new ArrayList<>();
		bannerControllingSide = SiegeSide.NOBODY;
		bannerControlSessions = new HashMap<>();
		attackerHasLowestPopulation = false;
		battlePointstModifierForSideWithLowestPopulation = 0;  //0 is the special starting value
		cannonSessionRemainingShortTicks = 0;
		attackerBattlePoints = 0;
		defenderBattlePoints = 0;
		attackerBattleContributors = new HashSet<>();
		attackerSiegeContributors = new HashMap<>();
    }

	public Nation getNation() {
		return nation;
	}

    public Town getTown() {
        return town;
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

	//<attacker> vs <defender>
	public String getName() {
		return Translation.of("siege.name", getAttacker().getName(), getDefender().getName());
	}

	public Government getAttacker() {
		switch (siegeType) {
			case CONQUEST:
			case LIBERATION:
			case SUPPRESSION:
				return nation;
			case REVOLT:
				return town;
			default:
				throw new RuntimeException("Unknown siege type");
		}
	}

	public Government getDefender() {
		switch (siegeType) {
			case CONQUEST:
				if (town.hasNation()) {
					try {
						return town.getNation();
					} catch (NotRegisteredException e) {
						throw new RuntimeException("problem getting defender");
					}
				} else {
					return town;
				}
			case LIBERATION:
				if(TownOccupationController.isTownOccupied(town)) {
					return TownOccupationController.getTownOccupier(town);
				} else {
					return town;
				}
			case SUPPRESSION:
				return nation;
			case REVOLT:
				return TownOccupationController.getTownOccupier(town);
			default:
				throw new RuntimeException("Unknown siege type");
		}
	}

	public void setNation(Nation nation) {
		this.nation = nation;
	}

	public void setTown(Town town) {
		this.town = town;
	}

	public Location getFlagLocation() {
		return siegeBannerLocation;
	}

	public void setFlagLocation(Location location) {
		this.siegeBannerLocation = location;
	}

	public Integer getSiegeBalance() {
		return siegeBalance;
	}

	public void setSiegeBalance(int siegeBalance) {
		this.siegeBalance = siegeBalance;
	}

	public void adjustSiegeBalance(int adjustment) {
		siegeBalance += adjustment;
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

	public double getBattlePointsModifierForSideWithLowestPopulation() {
		return battlePointstModifierForSideWithLowestPopulation;
	}

	public void setBattlePointsModifierForSideWithLowestPopulation(double battlePointsModifierForSideWithLowestPopulation) {
		this.battlePointstModifierForSideWithLowestPopulation = battlePointsModifierForSideWithLowestPopulation;
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

	public int getAttackerBattlePoints() {
		return attackerBattlePoints;
	}

	public void setAttackerBattlePoints(int attackerBattlePoints) {
		this.attackerBattlePoints = attackerBattlePoints;
	}

	public String getFormattedAttackerBattlePoints() {
		if(attackerBattlePoints == 0) {
			return "0";
		} else {
			return "+" + attackerBattlePoints;
		}
	}

	public int getDefenderBattlePoints() {
		return defenderBattlePoints;
	}

	public void setDefenderBattlePoints(int defenderBattlePoints) {
		this.defenderBattlePoints = defenderBattlePoints;
	}

	public String getFormattedDefenderBattlePoints() {
		if(defenderBattlePoints == 0) {
			return "0";
		} else {
			return "-" + defenderBattlePoints;
		}
	}

	public void adjustAttackerBattlePoints(int battleScore) {
		attackerBattlePoints += battleScore;
	}

	public void adjustDefenderBattlePoints(int battleScore) {
		defenderBattlePoints += battleScore;
	}

	public String getFormattedBattleTimeRemaining() {
		if (BattleSession.getBattleSession().isActive()
			&& status == SiegeStatus.IN_PROGRESS
			&& (getAttackerBattlePoints() > 0
				|| getDefenderBattlePoints() > 0
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

	public int getTimedBattlePointsEarnedFromCurrentBannerControl() {
		return timedBattlePointsEarnedFromCurrentBannerControl;
	}

	public void setTimedBattlePointsEarnedFromCurrentBannerControl(int timedBattlePointsEarnedFromCurrentBannerControl) {
		this.timedBattlePointsEarnedFromCurrentBannerControl = timedBattlePointsEarnedFromCurrentBannerControl;
	}

	public void adjustBattlePointsEarnedFromCurrentBannerControl(int timedBattlePoints) {
		timedBattlePointsEarnedFromCurrentBannerControl += timedBattlePoints;
	}

	public SiegeType getSiegeType() {
		return siegeType;
	}

	public void setSiegeType(SiegeType siegeType) {
		if(siegeType == null) //Safety feature
			throw new RuntimeException("SiegeType cannot be null");
		this.siegeType = siegeType;
	}
}