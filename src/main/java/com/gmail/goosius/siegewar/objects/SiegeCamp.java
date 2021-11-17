package com.gmail.goosius.siegewar.objects;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.events.PreSiegeWarStartEvent;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.util.TimeTools;

public class SiegeCamp {
	private final Player player;
	private final Block bannerBlock;
	private final SiegeType siegeType;
	private final Town targetTown;
	private final Government attacker;
	private final Government defender;
	private final Town townOfSiegeStarter;
	private final boolean useWarChest;
	private TownBlock townBlock;
	private int attackerPoints = 0;
	private final long endTime;

	public SiegeCamp(Player player, Block bannerBlock, SiegeType siegeType, Town targetTown, Government attacker,
			Government defender, Town townOfSiegeStarter, boolean useWarChest, TownBlock townBlock) {
		this.player = player;
		this.bannerBlock = bannerBlock;
		this.siegeType = siegeType;
		this.targetTown = targetTown;
		this.attacker = attacker;
		this.defender = defender;
		this.townOfSiegeStarter = townOfSiegeStarter;
		this.useWarChest = useWarChest;
		this.townBlock = townBlock;
		this.endTime = System.currentTimeMillis() + TimeTools.getMillis("10m");
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the bannerBlock
	 */
	public Block getBannerBlock() {
		return bannerBlock;
	}

	/**
	 * @return the siegeType
	 */
	public SiegeType getSiegeType() {
		return siegeType;
	}

	/**
	 * @return the targetTown
	 */
	public Town getTargetTown() {
		return targetTown;
	}

	/**
	 * @return the attacker
	 */
	public Government getAttacker() {
		return attacker;
	}

	/**
	 * @return the defender
	 */
	public Government getDefender() {
		return defender;
	}

	/**
	 * @return the townOfSiegeStarter
	 */
	public Town getTownOfSiegeStarter() {
		return townOfSiegeStarter;
	}

	/**
	 * @return the useWarChest
	 */
	public boolean isUseWarChest() {
		return useWarChest;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @return the attackerPoints
	 */
	public int getAttackerPoints() {
		return attackerPoints;
	}

	/**
	 * @param attackerPoints the attackerPoints to set
	 */
	public void setAttackerPoints(int attackerPoints) {
		this.attackerPoints = attackerPoints;
	}

	/**
	 * Starts the Siege after the success of the SiegeCamp.
	 */
	public void startSiege() {

		// Call event
		PreSiegeWarStartEvent preSiegeWarStartEvent = new PreSiegeWarStartEvent(siegeType, targetTown, (Nation) defender, townOfSiegeStarter, bannerBlock, townBlock);
		Bukkit.getPluginManager().callEvent(preSiegeWarStartEvent);

		// Setup attack
		if (!preSiegeWarStartEvent.isCancelled()) {
			SiegeController.startSiege(
					bannerBlock, 
					siegeType, 
					targetTown, 
					targetTown, 
					defender,
					townOfSiegeStarter, 
					false);
		} else {
			Messaging.sendErrorMsg(player, preSiegeWarStartEvent.getCancellationMsg());
		}
	}

}
