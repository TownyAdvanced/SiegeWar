package com.gmail.goosius.siegewar;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BannerControlSession;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;

public class SiegeWarAPI {

	/**
	 * @return a List of the Sieges.
	 */
	public static List<Siege> getSieges() {
		return SiegeController.getSieges();
	}

	/**
	 * @return a Collection of all of the Towns which are Sieged.
	 */
	public static Collection<Town> getSiegedTowns() {
		return SiegeController.getSiegedTowns();
	}

	/**
	 * @return a Collection of all of the Towns which have active Sieges.
	 */
	public static Collection<Town> getActivelySiegedTowns() {
		return getSieges().stream().filter(s -> isActive(s)).map(Siege::getTown).collect(Collectors.toSet());
	}

	/**
	 * @return a Collection of all of the names of the Sieged Towns.
	 */
	public static Collection<String> getNamesOfSiegedTowns() {
		return getSieges().stream().map(s -> s.getTown().getName()).collect(Collectors.toSet());
	}

	/**
	 * @return a Collection of all of the names of the Towns which have active
	 *         Sieges.
	 */
	public static Collection<String> getNamesOfActivelySiegedTowns() {
		return getSieges().stream().filter(s -> isActive(s)).map(s -> s.getTown().getName())
				.collect(Collectors.toSet());
	}

	/*
	 * Siege Stat methods
	 */

	/**
	 * Is the Siege considered Active? Active meaning the {@link SiegeStatus} is
	 * IN_PROGREES, PENDING_ATTACKER_ABANDON, or PENDING_DEFENDER_SURRENDER
	 * 
	 * @param siege Siege to check.
	 * @return true if Active.
	 */
	public static boolean isActive(Siege siege) {
		return siege.getStatus().isActive();
	}

	/*
	 * Has Siege
	 */

	/**
	 * Is the Player a member of a Town which is being Sieged?
	 * 
	 * @param player Player to check.
	 * @return true if the player's Town is Sieged.
	 */
	public static boolean hasSiege(Player player) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		return resident != null && hasSiege(resident);
	}

	/**
	 * Is the Resident a member of a Town which is being Sieged?
	 * 
	 * @param resident Resident to check.
	 * @return true if the resident's Town is Sieged.
	 */
	public static boolean hasSiege(Resident resident) {
		return resident.hasTown() && hasSiege(resident.getTownOrNull());
	}

	/**
	 * Is the Town being Sieged?
	 * 
	 * @param town Town to check
	 * @return true if the Town is Sieged.
	 */
	public static boolean hasSiege(Town town) {
		return townHasSiege(town.getUUID());
	}

	/**
	 * Is the Town belonging to the UUID being Sieged?
	 * 
	 * @param uuid UUID to check
	 * @return true if the Town is Sieged.
	 */
	public static boolean townHasSiege(UUID uuid) {
		return SiegeController.hasSiege(uuid);
	}

	/**
	 * Does the Town have an Active Siege? Active meaning the {@link SiegeStatus} is
	 * IN_PROGREES, PENDING_ATTACKER_ABANDON, or PENDING_DEFENDER_SURRENDER
	 * 
	 * @param town Town to check.
	 * @return true if the Town has an Active Siege.
	 */
	public static boolean hasActiveSiege(Town town) {
		return hasSiege(town) && getSiegeOrNull(town).getStatus().isActive();
	}

	/*
	 * Get Siege by Player.
	 */

	/**
	 * Get the Siege on the given Player's Town.
	 * 
	 * @param player Player to get a Siege from.
	 * @return the Siege that is being made on the given Player's Town or null.
	 */
	@Nullable
	public static Siege getSiegeOrNull(Player player) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		return resident != null ? getSiegeOrNull(resident) : null;
	}

	/**
	 * Get the Siege on the given Player's Town.
	 * 
	 * @param player Player to get a Siege from.
	 * @return the Siege on the given Player's Town.
	 * @throws TownyException thrown when the Player is not a member of a Town that
	 *                        is being sieged.
	 */
	public static Siege getSiegeOrThrow(Player player) throws TownyException {
		Siege siege = getSiegeOrNull(player);
		if (siege == null)
			throw new TownyException(Translatable.of("msg_err_player_town_is_not_sieged"));
		return siege;
	}

	/**
	 * Get the Siege on the given Player's Town, if present.
	 * 
	 * @param player Player to get a Siege from.
	 * @return an Optional<Siege> that is being made on the given Player's Town, if
	 *         present.
	 */
	public static Optional<Siege> getSiege(Player player) {
		return Optional.ofNullable(getSiegeOrNull(player));
	}

	/*
	 * Get Siege by Resident.
	 */

	/**
	 * Get the Siege on the given Resident's Town.
	 * 
	 * @param resident Resident to get a Siege from.
	 * @return the Siege that is being made on the given Resident's Town or null.
	 */
	@Nullable
	public static Siege getSiegeOrNull(Resident resident) {
		return resident.hasTown() ? getSiegeOrNull(resident.getTownOrNull()) : null;
	}

	/**
	 * Get the Siege on the given Resident's Town.
	 * 
	 * @param resident Resident to get a Siege from.
	 * @return the Siege on the given Resident's Town.
	 * @throws TownyException thrown when the Resident is not a member of a Town
	 *                        that is being sieged.
	 */
	public static Siege getSiegeOrThrow(Resident resident) throws TownyException {
		Siege siege = getSiegeOrNull(resident);
		if (siege == null)
			throw new TownyException(Translatable.of("msg_err_resident_town_is_not_sieged"));
		return siege;
	}

	/**
	 * Get the Siege on the given Resident's Town, if present.
	 * 
	 * @param resident Resident to get a Siege from.
	 * @return an Optional<Siege> that is being made on the given Resident's Town,
	 *         if present.
	 */
	public static Optional<Siege> getSiege(Resident resident) {
		return Optional.ofNullable(getSiegeOrNull(resident));
	}

	/*
	 * Get Siege by Town.
	 */

	/**
	 * Get the Siege on the given Town.
	 * 
	 * @param town Town to get a Siege from.
	 * @return the Siege that is being made on the given Town or null.
	 */
	@Nullable
	public static Siege getSiegeOrNull(Town town) {
		return SiegeController.getSiege(town);
	}

	/**
	 * Get the Siege on the given Town.
	 * 
	 * @param town Town to get a Siege from.
	 * @return the Siege on the given Town.
	 * @throws TownyException thrown when the Town is not under siege.
	 */
	public static Siege getSiegeOrThrow(Town town) throws TownyException {
		Siege siege = getSiegeOrNull(town);
		if (siege == null)
			throw new TownyException(Translatable.of("msg_err_town_is_not_sieged"));
		return siege;
	}

	/**
	 * Get the Siege on the given Town, if present.
	 * 
	 * @param town Town to get a Siege from.
	 * @return an Optional<Siege> that is being made on the given Town, if present.
	 */
	public static Optional<Siege> getSiege(Town town) {
		return Optional.ofNullable(getSiegeOrNull(town));
	}

	/*
	 * Get Sieges by Nation.
	 */

	/**
	 * Get a list of the Sieges that the Nation has started.
	 * 
	 * @param nation Nation to check.
	 * @return a List of Sieges that the Nation has started.
	 */
	public static List<Siege> getSiegesBelongingToNation(Nation nation) {
		return getSieges().stream().filter(s -> s.getAttacker().equals(nation)).collect(Collectors.toList());
	}

	/**
	 * Gets a list of towns with an active siege that have a certain nation
	 *
	 * @param nation The nation that the town must be in.
	 * @return The list of towns that are under siege in that nation.
	 */
	public static List<Town> getActivelySiegedTowns(Nation nation) {
		return SiegeController.getSiegedTowns(nation);
	}

	/**
	 * Gets a Map of Active offensive Sieges and the sieged Town, from a given
	 * Nation.
	 * 
	 * @param nation Nation to check.
	 * @return a Map<Siege, Town> of the Nation's offensive Sieges and the Sieges'
	 *         sieged Towns.
	 */
	public static Map<Siege, Town> getActiveOffensiveSieges(Nation nation) {
		return SiegeController.getActiveOffensiveSieges(nation);
	}

	/**
	 * Gets a Map of Active defensive Sieges and the sieged Town, from a given
	 * Nation.
	 * 
	 * @param nation Nation to check.
	 * @return a Map<Siege, Town> of the Nation's defensive Sieges and the Sieges'
	 *         sieged Towns.
	 */
	public static Map<Siege, Town> getActiveDefensiveSieges(Nation nation) {
		return SiegeController.getActiveDefensiveSieges(nation);
	}

	/*
	 * Location methods.
	 */

	/**
	 * Get the active Siege at the given Player's Location If more than one active
	 * siege is found, return the closest one If no active sieges are found, return
	 * null.
	 *
	 * @param player Player
	 * @return active siege at player's location
	 */
	@Nullable
	public static Siege getActiveSiegeAtLocation(Player player) {
		return getActiveSiegeAtLocation(player.getLocation());
	}

	/**
	 * Get the active siege at the given Location If more than one active siege is
	 * found, return the closest one If no active sieges are found, return null
	 *
	 * @param loc Location to check.
	 * @return active Siege at given Location or null.
	 */
	@Nullable
	public static Siege getActiveSiegeAtLocation(Location loc) {
		return SiegeController.getActiveSiegeAtLocation(loc);
	}

	/**
	 * This method returns true if the given location is in an active siegezone
	 *
	 * @param location the target location
	 * @return true if location is in an active siegezone
	 */
	public static boolean isLocationInActiveSiegeZone(Location location) {
		return SiegeWarDistanceUtil.isLocationInActiveSiegeZone(location);
	}

	/**
	 * Is the given TownBlock in an Acvite Siege Zone?
	 * 
	 * @param townBlock TownBlock to check.
	 * @return true if the TownBlock is in an Active Siege Zone.
	 */
	public static boolean isTownBlockInActiveSiegeZone(TownBlock townBlock) {
		return SiegeWarDistanceUtil.isTownBlockInActiveSiegeZone(townBlock);
	}

	/**
	 * Is the Location inside of the given Siege's SiegeZone?
	 * 
	 * @param location Location to check.
	 * @param siege    Siege to check.
	 * @return true if the given Location is inside of the Siege's SiegeZone.
	 */
	public static boolean isInSiegeZone(Location location, Siege siege) {
		return SiegeWarDistanceUtil.isInSiegeZone(location, siege);
	}

	/**
	 * Is the Entity inside of the given Siege's SiegeZone?
	 * 
	 * @param entity Entity to check.
	 * @param siege  Siege to check.
	 * @return true if the given Location is inside of the Siege's SiegeZone.
	 */
	public static boolean isInSiegeZone(Entity entity, Siege siege) {
		return isInSiegeZone(entity.getLocation(), siege);
	}

	/**
	 * Is the Location inside of the given Siege's TimedPointZone?
	 * 
	 * @param location Location to check.
	 * @param siege    Siege to check.
	 * @return true if the given Location is inside of the Siege's TimedPointZone.
	 */
	public static boolean isInTimedPointZone(Location location, Siege siege) {
		return SiegeWarDistanceUtil.isInTimedPointZone(location, siege);
	}

	/*
	 * BattleSession methods.
	 */

	/**
	 * @return true if there is an Active BattleSession.
	 */
	public static boolean isBattleSessionActive() {
		return BattleSession.getBattleSession().isActive();
	}

	/**
	 * @return the amount of milliseconds left in the current BattleSession or 0 if
	 *         one is not in progress.
	 */
	public static long getBattleSessionTimeRemaining() {
		return BattleSession.getBattleSession().getTimeRemainingUntilBattleSessionEnds();
	}

	/*
	 * BannerControl Sessions methods.
	 */

	/**
	 * @return a Set of Players that are in the current
	 *         {@link BannerControlSession}.
	 */
	public static Set<Player> getPlayersInBannerControlSessions() {
		return SiegeController.getPlayersInBannerControlSessions();
	}
}
