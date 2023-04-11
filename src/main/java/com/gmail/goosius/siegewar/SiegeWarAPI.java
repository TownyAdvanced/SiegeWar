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

	public static List<Siege> getSieges() {
		return SiegeController.getSieges();
	}

	public static Collection<Town> getSiegedTowns() {
		return SiegeController.getSiegedTowns();
	}

	public static Collection<String> getNamesOfSiegedTowns() {
		return getSieges().stream().map(s -> s.getTown().getName()).collect(Collectors.toSet());
	}

	public static Collection<String> getNamesOfActivelySiegedTowns() {
		return getSieges().stream().filter(s -> isActive(s)).map(s -> s.getTown().getName())
				.collect(Collectors.toSet());
	}

	/*
	 * Siege Stat methods
	 */
	public static boolean isActive(Siege siege) {
		return siege.getStatus().isActive();
	}

	/*
	 * Has Siege
	 */

	public static boolean hasSiege(Player player) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		return resident != null && hasSiege(resident);
	}

	public static boolean hasSiege(Resident resident) {
		return resident.hasTown() && hasSiege(resident.getTownOrNull());
	}

	public static boolean hasSiege(Town town) {
		return townHasSiege(town.getUUID());
	}

	public static boolean townHasSiege(UUID uuid) {
		return SiegeController.hasSiege(uuid);
	}

	public static boolean hasActiveSiege(Town town) {
		return hasSiege(town) && getSiegeOrNull(town).getStatus().isActive();
	}

	/*
	 * Get Siege by Player.
	 */

	@Nullable
	public static Siege getSiegeOrNull(Player player) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		return resident != null ? getSiegeOrNull(resident) : null;
	}

	public static Siege getSiegeOrThrow(Player player) throws TownyException {
		Siege siege = getSiegeOrNull(player);
		if (siege == null)
			throw new TownyException(Translatable.of("msg_err_player_town_is_not_sieged"));
		return siege;
	}

	public static Optional<Siege> getSiege(Player player) {
		return Optional.ofNullable(getSiegeOrNull(player));
	}

	/*
	 * Get Siege by Resident.
	 */

	@Nullable
	public static Siege getSiegeOrNull(Resident resident) {
		return resident.hasTown() ? getSiegeOrNull(resident.getTownOrNull()) : null;
	}

	public static Siege getSiegeOrThrow(Resident resident) throws TownyException {
		Siege siege = getSiegeOrNull(resident);
		if (siege == null)
			throw new TownyException(Translatable.of("msg_err_resident_town_is_not_sieged"));
		return siege;
	}

	public static Optional<Siege> getSiege(Resident resident) {
		return Optional.ofNullable(getSiegeOrNull(resident));
	}

	/*
	 * Get Siege by Town.
	 */

	@Nullable
	public static Siege getSiegeOrNull(Town town) {
		return SiegeController.getSiege(town);
	}

	public static Siege getSiegeOrThrow(Town town) throws TownyException {
		Siege siege = getSiegeOrNull(town);
		if (siege == null)
			throw new TownyException(Translatable.of("msg_err_town_is_not_sieged"));
		return siege;
	}

	public static Optional<Siege> getSiege(Town town) {
		return Optional.ofNullable(getSiegeOrNull(town));
	}

	/*
	 * Get Sieges by Nation
	 */

	/**
	 * Gets a list of towns with an active siege that have a certain nation
	 *
	 * @param nation The nation that the town must be in.
	 * @return The list of towns that are under siege in that nation.
	 */
	public static List<Town> getActivelySiegedTowns(Nation nation) {
		return SiegeController.getSiegedTowns(nation);
	}

	public static Map<Siege, Town> getActiveOffensiveSieges(Nation nation) {
		return SiegeController.getActiveOffensiveSieges(nation);
	}

	public static Map<Siege, Town> getActiveDefensiveSieges(Nation nation) {
		return SiegeController.getActiveDefensiveSieges(nation);
	}

	/*
	 * Get Siege by Location.
	 */

	/**
	 * Get the active siege at the given player's location If more than one active
	 * siege is found, return the closest one If no active sieges are found, return
	 * null
	 *
	 * @param player Given player
	 * @return active siege at player's location
	 */
	@Nullable
	public static Siege getActiveSiegeAtLocation(Player player) {
		return getActiveSiegeAtLocation(player.getLocation());
	}

	/**
	 * Get the active siege at the given location If more than one active siege is
	 * found, return the closest one If no active sieges are found, return null
	 *
	 * @param loc Given location
	 * @return active siege at given location
	 */
	@Nullable
	public static Siege getActiveSiegeAtLocation(Location loc) {
		return SiegeController.getActiveSiegeAtLocation(loc);
	}

	public static Set<Player> getPlayersInBannerControlSessions() {
		return SiegeController.getPlayersInBannerControlSessions();
	}

	/*
	 * Are Locations in SiegeZones
	 */

	/**
	 * This method returns true if the given location is in an active siegezone
	 *
	 * @param location the target location
	 * @return true if location is in an active siegezone
	 */
	public static boolean isLocationInActiveSiegeZone(Location location) {
		return SiegeWarDistanceUtil.isLocationInActiveSiegeZone(location);
	}

	public static boolean isTownBlockInActiveSiegeZone(TownBlock townBlock) {
		return SiegeWarDistanceUtil.isTownBlockInActiveSiegeZone(townBlock);
	}

	public static boolean isInSiegeZone(Location location, Siege siege) {
		return SiegeWarDistanceUtil.isInSiegeZone(location, siege);
	}

	public static boolean isInSiegeZone(Entity entity, Siege siege) {
		return isInSiegeZone(entity.getLocation(), siege);
	}

	public static boolean isInTimedPointZone(Location location, Siege siege) {
		return SiegeWarDistanceUtil.isInTimedPointZone(location, siege);
	}

}
