package com.gmail.goosius.siegewar.enums;

/**
 * 
 * @author LlmDl
 *
 */
public enum SiegeWarPermissionNodes {

	SIEGEWAR_NATION_SIEGE_POINTS("siegewar.nation.siege.points"),
	SIEGEWAR_NATION_SIEGE_LEADERSHIP("siegewar.nation.siege.leadership"),
	SIEGEWAR_NATION_SIEGE_ATTACK("siegewar.nation.siege.attack"),
	SIEGEWAR_NATION_SIEGE_ABANDON("siegewar.nation.siege.abandon"),
	SIEGEWAR_NATION_SIEGE_INVADE("siegewar.nation.siege.invade"),
	SIEGEWAR_NATION_SIEGE_PLUNDER("siegewar.nation.siege.plunder"),
	SIEGEWAR_TOWN_SIEGE_POINTS("siegewar.town.siege.points"),
	SIEGEWAR_TOWN_SIEGE_SURRENDER("siegewar.town.siege.surrender"),
	SIEGEWAR_COMMAND_TOWNYADMIN_SET_SIEGEIMMUNITIES("siegewar.command.townyadmin.set.siegeimmunities"),
	// Siegewar related war sickness immunities
	SIEGEWAR_IMMUNE_TO_WAR_NAUSEA("siegewar.immune.to.war.nausea"),
	SIEGEWAR_IMMUNE_TO_BATTLE_FATIGUE("siegewar.immune.to.battle.fatigue"),
	// Command Nodes:
	SIEGEWAR_COMMAND_SIEGEWAR_NATION_REFUND("siegewar.command.siegewar.nation.refund"),
	SIEGEWAR_COMMAND_SIEGEWARADMIN_IMMUNITY("siegewar.command.siegewaradmin.immunity");
	
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
