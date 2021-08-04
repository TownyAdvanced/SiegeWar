package com.gmail.goosius.siegewar.enums;

/**
 * 
 * @author LlmDl
 *
 */
public enum SiegeWarPermissionNodes {

	// ----- Nation Action Nodes -----
	//Battle points
	SIEGEWAR_NATION_SIEGE_BATTLE_POINTS("siegewar.nation.siege.battle.points"),
	//Actions
	SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_START("siegewar.nation.siege.conquest.siege.start"),
	SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_ABANDON("siegewar.nation.siege.conquest.siege.abandon"),
	SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_START("siegewar.nation.siege.liberation.siege.start"),
	SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_ABANDON("siegewar.nation.siege.liberation.siege.abandon"),
	SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_SURRENDER("siegewar.nation.siege.liberation.siege.surrender"),
	SIEGEWAR_NATION_SIEGE_REVOLT_SIEGE_SURRENDER("siegewar.nation.siege.revolt.siege.surrender"),
	SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_START("siegewar.nation.siege.suppression.siege.start"),
	SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_ABANDON("siegewar.nation.siege.suppression.siege.abandon"),
	//Post Siege Actions
	SIEGEWAR_NATION_SIEGE_INVADE("siegewar.nation.siege.invade"),
	SIEGEWAR_NATION_SIEGE_PLUNDER("siegewar.nation.siege.plunder"),

	// ----- Town Action Nodes -----
	//Battle points
	SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS("siegewar.town.siege.battle.points"),
	//Actions
	SIEGEWAR_TOWN_SIEGE_CONQUEST_SIEGE_SURRENDER("siegewar.town.siege.conquest.siege.surrender"),
	SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_START("siegewar.town.siege.revolt.siege.abandon"),
	SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_ABANDON("siegewar.town.siege.revolt.siege.start"),
	SIEGEWAR_TOWN_SIEGE_SUPPRESSION_SIEGE_SURRENDER("siegewar.town.siege.suppression.siege.surrender"),

	// Node related to cannon session integration
	SIEGEWAR_TOWN_SIEGE_START_CANNON_SESSION("siegewar.town.siege.startcannonsession"),
	// Siegewar related war sickness immunities
	SIEGEWAR_IMMUNE_TO_WAR_NAUSEA("siegewar.immune.to.war.nausea"),

	// Siegezone Immunity
	SIEGEWAR_SIEGEZONE_DAMAGE_IMMUNITY("siegewar.siegezone.damage.immunity"),

	//Siegezone restrictions
	SIEGEWAR_SIEGEZONE_CANNOT_HIT_PLAYERS("siegewar.siegezone.cannot.hit.players"),
	SIEGEWAR_SIEGEZONE_CANNOT_THROW_POTIONS("siegewar.siegezone.cannot.throw.potions"),
	SIEGEWAR_SIEGEZONE_CANNOT_GET_BANNER_CONTROL("siegewar.siegezone.cannot.get.banner.control"),	
	SIEGEWAR_SIEGEZONE_CANNOT_CARRY_ITEMS("siegewar.siegezone.cannot.carry.items"),

	//Notifications
	SIEGEWAR_NOTIFICATIONS_ALL("siegewar.notifications.all"),
	
	// ----- Player Command Nodes -----
	SIEGEWAR_COMMAND_SIEGEWAR("siegewar.command.siegewar.*"),
		SIEGEWAR_COMMAND_SIEGEWAR_TOWN("siegewar.command.siegewar.town.*"),
			SIEGEWAR_COMMAND_SIEGEWAR_TOWN_INVITE_OCCUPATION("siegewar.command.siegewar.town.inviteoccupation"),
		SIEGEWAR_COMMAND_SIEGEWAR_NATION("siegewar.command.siegewar.nation.*"),
			SIEGEWAR_COMMAND_SIEGEWAR_NATION_PAYSOLDIERS("siegewar.command.siegewar.nation.paysoldiers"),
	    	SIEGEWAR_COMMAND_SIEGEWAR_NATION_REMOVE_OCCUPATION("siegewar.command.siegewar.nation.removeoccupation"),
	    	SIEGEWAR_COMMAND_SIEGEWAR_NATION_TRANSFER_OCCUPATION("siegewar.command.siegewar.nation.transferoccupation"),
		SIEGEWAR_COMMAND_SIEGEWAR_COLLECT("siegewar.command.siegewar.collect"),
		SIEGEWAR_COMMAND_SIEGEWAR_HUD("siegewar.command.siegewar.hud"),
		SIEGEWAR_COMMAND_SIEGEWAR_GUIDE("siegewar.command.siegewar.guide"),
		SIEGEWAR_COMMAND_SIEGEWAR_PREFERENCE("siegewar.command.siegewar.preference"),

	// ----- Admin Command Nodes -----
	SIEGEWAR_COMMAND_SIEGEWARADMIN("siegewar.command.siegewaradmin.*"),
		SIEGEWAR_COMMAND_SIEGEWARADMIN_IMMUNITY("siegewar.command.siegewaradmin.immunity"),
		SIEGEWAR_COMMAND_SIEGEWARADMIN_RELOAD("siegewar.command.siegewaradmin.reload"),
		SIEGEWAR_COMMAND_SIEGEWARADMIN_SIEGE("siegewar.command.siegewaradmin.siege"),
		SIEGEWAR_COMMAND_SIEGEWARADMIN_TOWN("siegewar.command.siegewaradmin.town");

	private String value;

	/**
	 * Constructor
	 * 
	 * @param permission - Permission.
	 */
	SiegeWarPermissionNodes(String permission) {

		this.value = permission;
	}

	/**
	 * Retrieves the permission node
	 * 
	 * @return The permission node
	 */
	public String getNode() {

		return value;
	}

	/**
	 * Retrieves the permission node
	 * replacing the character *
	 * 
	 * @param replace - String
	 * @return The permission node
	 */
	public String getNode(String replace) {

		return value.replace("*", replace);
	}

	public String getNode(int replace) {

		return value.replace("*", replace + "");
	}

	public static String getPermissionNodeToStartSiege(SiegeType siegeType) {
		switch (siegeType) {
			case CONQUEST:
				return SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_START.getNode();
			case LIBERATION:
				return SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_START.getNode();
			case REVOLT:
				return SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_START.getNode();
			case SUPPRESSION:
				return SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_START.getNode();
			default:
				throw new RuntimeException("Unknown siege type");
		}
	}

	public static String getPermissionNodeToAbandonAttack(SiegeType siegeType) {
		switch (siegeType) {
			case CONQUEST:
				return SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_ABANDON.getNode();
			case LIBERATION:
				return SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_ABANDON.getNode();
			case REVOLT:
				return SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_ABANDON.getNode();
			case SUPPRESSION:
				return SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_ABANDON.getNode();
			default:
				throw new RuntimeException("Uknown siege type");
		}
	}

	public static String getPermissionNodeToSurrenderDefence(SiegeType siegeType) {
		switch (siegeType) {
			case CONQUEST:
				return SIEGEWAR_TOWN_SIEGE_CONQUEST_SIEGE_SURRENDER.getNode();
			case LIBERATION:
				return SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_SURRENDER.getNode();
			case REVOLT:
				return SIEGEWAR_NATION_SIEGE_REVOLT_SIEGE_SURRENDER.getNode();
			case SUPPRESSION:
				return SIEGEWAR_TOWN_SIEGE_SUPPRESSION_SIEGE_SURRENDER.getNode();
			default:
				throw new RuntimeException("Uknown siege type");
		}
	}


}
