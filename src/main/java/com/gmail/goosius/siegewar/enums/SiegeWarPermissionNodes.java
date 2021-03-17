package com.gmail.goosius.siegewar.enums;

/**
 * 
 * @author LlmDl
 *
 */
public enum SiegeWarPermissionNodes {

	// ----- Nation Siege Perms -----

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

	// ----- Town Siege Perms -----

	//Battle points
	SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS("siegewar.town.siege.battle.points"),

	//Actions
	SIEGEWAR_TOWN_SIEGE_CONQUEST_SIEGE_SURRENDER("siegewar.town.siege.conquest.siege.surrender"),
	SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_ABANDON("siegewar.town.siege.revolt.siege.abandon"),
	SIEGEWAR_TOWN_SIEGE_SUPPRESSION_SIEGE_SURRENDER("siegewar.town.siege.suppression.siege.surrender"),

	// ------------------------------
	
	// Perm related to cannon session integration
	SIEGEWAR_TOWN_SIEGE_START_CANNON_SESSION("siegewar.town.siege.startcannonsession"),
	// Siegewar related war sickness immunities
	SIEGEWAR_IMMUNE_TO_WAR_NAUSEA("siegewar.immune.to.war.nausea"),
	// Command Nodes:
	SIEGEWAR_COMMAND_SIEGEWAR("siegewar.command.siegewar.*"),
		SIEGEWAR_COMMAND_SIEGEWAR_TOWN("siegewar.command.siegewar.town.*"),
			SIEGEWAR_COMMAND_SIEGEWAR_TOWN_REVOLT("siegewar.command.siegewar.town.revolt"),
		SIEGEWAR_COMMAND_SIEGEWAR_NATION("siegewar.command.siegewar.nation.*"),
			SIEGEWAR_COMMAND_SIEGEWAR_NATION_PAYSOLDIERS("siegewar.command.siegewar.nation.paysoldiers"),
	    	SIEGEWAR_COMMAND_SIEGEWAR_NATION_RELEASE("siegewar.command.siegewar.nation.release"),
		SIEGEWAR_COMMAND_SIEGEWAR_COLLECT("siegewar.command.siegewar.collect"),
		SIEGEWAR_COMMAND_SIEGEWAR_HUD("siegewar.command.siegewar.hud"),
		SIEGEWAR_COMMAND_SIEGEWAR_GUIDE("siegewar.command.siegewar.guide"),
		SIEGEWAR_COMMAND_SIEGEWAR_PREFERENCE("siegewar.command.siegewar.preference"),

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
}
