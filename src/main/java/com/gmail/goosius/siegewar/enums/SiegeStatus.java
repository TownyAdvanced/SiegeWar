package com.gmail.goosius.siegewar.enums;

import com.palmergames.bukkit.towny.object.Translation;

/**
 * This class represents the "status" of a siege
 * 
 * "In Process" means the siege is active, with the outcome not yet decided
 * Any other status means the siege has "finished", with the outcome decided.
 * 
 * @author Goosius
 */
public enum SiegeStatus {
    IN_PROGRESS(true, Translation.of("siege_status_in_progress")), 
	ATTACKER_WIN(false, Translation.of("siege_status_attacker_win")), 
	DEFENDER_WIN(false, Translation.of("siege_status_defender_win")), 
	ATTACKER_CLOSE_WIN(false, Translation.of("siege_status_attacker_close_win")),
	DEFENDER_CLOSE_WIN(false, Translation.of("siege_status_defender_close_win")),
	ATTACKER_ABANDON(false, Translation.of("siege_status_attacker_abandon")), 
	DEFENDER_SURRENDER(false, Translation.of("siege_status_defender_surrender")), 
	PENDING_ATTACKER_ABANDON(true, Translation.of("siege_status_pending_attacker_abandon")), 
	PENDING_DEFENDER_SURRENDER(true, Translation.of("siege_status_pending_defender_surrender")), 
	UNKNOWN(false, Translation.of("siege_status_unknown"));

    private boolean active;
    private String name;
    
    SiegeStatus(boolean active, String name) {
    	this.active = active;
    	this.name = name;
	}

    public static SiegeStatus parseString(String line) {
        switch (line) {
            case "IN_PROGRESS":
                return IN_PROGRESS;
            case "ATTACKER_WIN":
                return ATTACKER_WIN;
            case "DEFENDER_WIN":
                return DEFENDER_WIN;
			case "ATTACKER_CLOSE_WIN":
				return ATTACKER_CLOSE_WIN;
			case "DEFENDER_CLOSE_WIN":
				return DEFENDER_CLOSE_WIN;
            case "ATTACKER_ABANDON":
                return ATTACKER_ABANDON;
            case "DEFENDER_SURRENDER":
                return DEFENDER_SURRENDER;
			case "PENDING_ATTACKER_ABANDON":
				return PENDING_ATTACKER_ABANDON;
			case "PENDING_DEFENDER_SURRENDER":
				return PENDING_DEFENDER_SURRENDER;
            default:
                return UNKNOWN;
        }
    }

	/**
	 * @return true if we are in the 'active' phase of the siege
	 */
	public boolean isActive() {
    	return this.active;
	}
	
	/**
	 * Currently unused.
	 * 
	 * @return translated siege status.
	 */
	public String getName() {
		return this.name;
	}
}
