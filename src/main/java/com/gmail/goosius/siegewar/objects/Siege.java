package com.gmail.goosius.siegewar.objects;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
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
import java.util.UUID;

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
 * After a siege ends, it enters an aftermath phase where it has an inactive type of siege status,
 * During this phase, the town cannot be attacked again,
 * and, depending on the siege type, the victor may have the options of "plunder" and/or "invade".
 *
 * @author Goosius
 */
public class Siege {
	private SiegeType siegeType;
	private Town town;
	private Government attacker;
	private Government defender;
	private String attackerName; //Only used in the siege-aftermath
	private String defenderName; //Only used in the siege-aftermath
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
	private SiegeSide siegeWinner;    //For when a siege is over, holds the winning side.
	private Map<Player, BannerControlSession> bannerControlSessions;
	private int timedBattlePointsEarnedFromCurrentBannerControl;
	private int attackerBattlePoints;
	private int defenderBattlePoints;
	private Set<String> successfulBattleContributors;   //UUID's of attacker-side residents who got BC at least once during the current battle
	private Map<String, Integer> residentTimedPointContributors;  //UUID:numContributions map of attacker-side residents who got BC at least once during the current siege
	private Map<UUID, Integer> primaryTownGovernments; //UUID:numBattleSessions map of governments who led the town during the siege. If town was is a nation, nation UUID will be used, otherwise town UUID will be used
	private double wallBreachPoints;	//Wall Breach points for the current battle session
	private Set<Resident> wallBreachBonusAwardees;  //Residents who have been awarded the wall-breach bonus for the current battle session
	private Set<Player> recentTownFriendlyCannonFirers;
	
	public Siege(Town town) {
		this.town = town;
        siegeType = null;
        attacker = null;
        defender = null;
        attackerName = "";
        defenderName = "";
        status = null;
		siegeBalance = 0;
		siegeBannerLocation = null;
		warChestAmount = 0;
		bannerControllingResidents = new ArrayList<>();
		bannerControllingSide = SiegeSide.NOBODY;
		bannerControlSessions = new HashMap<>();
		attackerBattlePoints = 0;
		defenderBattlePoints = 0;
		successfulBattleContributors = new HashSet<>();
		residentTimedPointContributors = new HashMap<>();
		primaryTownGovernments = new HashMap<>();
		wallBreachPoints = 0;
		wallBreachBonusAwardees = new HashSet<>();
		recentTownFriendlyCannonFirers = new HashSet<>();
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

	public Government getAttacker() {
		return attacker;
	}

	public void setAttacker(Government attacker) {
		this.attacker = attacker;
	}

	public Government getDefender() {
		return defender;
	}

	/**
	 * Get the defending nation if there is one,
	 * else get defending town
	 * 
	 * @return the defender,
	 */
	public Government getDefendingNationIfPossibleElseTown() {
		if(defender instanceof Town && ((Town)defender).hasNation())
			return TownyAPI.getInstance().getTownNationOrNull((Town) defender);

		return defender;
	}

	/**
	 * Get the attacking nation if there is one,
	 * else get attacking town
	 *
	 * @return the attacker,
	 */
	public Government getAttackingNationIfPossibleElseTown() {
		if(attacker instanceof Town && ((Town)attacker).hasNation())
			return TownyAPI.getInstance().getTownNationOrNull((Town) attacker);
		return attacker;
	}

	public String getAttackerNameForDisplay() {
		if(status.isActive()) {
			return getAttackingNationIfPossibleElseTown().getName();
		} else {
			return getAttackerName();
		}
	}
	
	public String getDefenderNameForDisplay() {
		if(status.isActive()) {
			return getDefendingNationIfPossibleElseTown().getName();
		} else {
			return getDefenderName();
		}
	}


	public void setDefender(Government defender) {
		this.defender = defender;
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

	public Set<String> getSuccessfulBattleContributors() {
		return successfulBattleContributors;
	}

	public void clearSuccessfulBattleContributors() {
		successfulBattleContributors.clear();
	}

	public Map<String, Integer> getResidentTimedPointContributors() {
		return residentTimedPointContributors;
	}

	public void setResidentTimedPointContributors(Map<String, Integer> residentTimedPointContributors) {
		this.residentTimedPointContributors = residentTimedPointContributors;
	}

	public void registerSuccessfulBattleContributorsFromBannerControl() {
		for(Resident resident: bannerControllingResidents) {
			successfulBattleContributors.add(resident.getUUID().toString());
		}
	}

	public void propagateSuccessfulBattleContributorsToResidentTimedPointContributors() {
		for(String playerUuid: successfulBattleContributors) {
			if(residentTimedPointContributors.containsKey(playerUuid)) {
				residentTimedPointContributors.put(playerUuid, residentTimedPointContributors.get(playerUuid) + 1);
			} else {
				residentTimedPointContributors.put(playerUuid, 1);
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

	/**
	 * Record who is the primary government of the town
	 * If the town has a nation, nation uuid will be recorded,
	 * otherwise town uuid will be recorded.
	 */
	public void recordPrimaryTownGovernment() {
		//Identify key
		UUID governmentUUID;
		if(town.hasNation())
			governmentUUID = TownyAPI.getInstance().getTownNationOrNull(town).getUUID();
		else
			governmentUUID = town.getUUID();

		//Record battle session contribution
		if(primaryTownGovernments.containsKey(governmentUUID)) {
			int numBattleSessions = primaryTownGovernments.get(governmentUUID);
			numBattleSessions++;
			primaryTownGovernments.put(governmentUUID, numBattleSessions);
		} else {
			primaryTownGovernments.put(governmentUUID, 1);
		}
	}

	public Map<UUID, Integer> getPrimaryTownGovernments() {
		return primaryTownGovernments;
	}

	public void setPrimaryTownGovernments(Map<UUID, Integer> primaryTownGovernments) {
		this.primaryTownGovernments = primaryTownGovernments;
	}


	public int getTotalBattleSessions() {
		int result = 0;
		for(int homeNationContribution: primaryTownGovernments.values()) {
			result += homeNationContribution;
		}
		return result;
	}

	public SiegeSide getSiegeWinner() {
		return siegeWinner;
	}

	public void setSiegeWinner(SiegeSide siegeWinner) {
		this.siegeWinner = siegeWinner;
	}

	public String getAttackerName() {
		return attackerName;
	}

	public void setAttackerName(String attackerName) {
		this.attackerName = attackerName;
	}

	public String getDefenderName() {
		return defenderName;
	}

	public void setDefenderName(String defenderName) {
		this.defenderName = defenderName;
	}

	public double getWallBreachPoints() {
		return wallBreachPoints;
	}

	public void setWallBreachPoints(double wallBreachPoints) {
		this.wallBreachPoints = wallBreachPoints;
	}

	public void increaseWallBreachPointsToCap(double wallBreachPointsIncrease) {
		setWallBreachPoints(            
			Math.min(
				wallBreachPointsIncrease,
				SiegeWarSettings.getWallBreachingMaxPoolSize()));	
	}

	public Set<Resident> getWallBreachBonusAwardees() {
		return wallBreachBonusAwardees;
	}

	public void setWallBreachBonusAwardees(Set<Resident> wallBreachBonusAwardees) {
		this.wallBreachBonusAwardees = wallBreachBonusAwardees;
	}
	
	public String getFormattedBreachPoints() {
		return Integer.toString((int)(getWallBreachPoints()));
	}

	public Set<Player> getRecentTownFriendlyCannonFirers() {
		return recentTownFriendlyCannonFirers;
	}

	public void addRecentTownFriendlyCannonFirer(Player gunnerPlayer) {
		recentTownFriendlyCannonFirers.add(gunnerPlayer);
	}

	public void clearRecentTownFriendlycannonFirers() {
		recentTownFriendlyCannonFirers.clear();
	}
}