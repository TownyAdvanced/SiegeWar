package com.gmail.goosius.siegewar;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.metadata.SiegeMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeController {

	////The key of this map is the town UUID
	//private final static Map<String, Siege> sieges = new ConcurrentHashMap<>();
	private static Map<UUID, Siege> townSiegeMap = new ConcurrentHashMap<>();
	private static List<Town> siegedTowns = new ArrayList<>();
	private static List<String> siegedTownNames = new ArrayList<>();

	public static void newSiege(Town town) {
		Siege siege = new Siege(town);
		townSiegeMap.put(town.getUUID(), siege);
		siegedTowns.add(town);
		siegedTownNames.add(town.getName());
	}

	public static List<Siege> getSieges() {
		return new ArrayList<>(townSiegeMap.values());
	}

	public static void clearSieges() {
		townSiegeMap.clear();
		siegedTowns.clear();
		siegedTownNames.clear();
	}

	public static void saveSiege(Siege siege) {
		Town town = siege.getTown();
		SiegeMetaDataController.setTownUUID(town, siege.getTown().getUUID().toString());
		SiegeMetaDataController.setAttackerUUID(town, siege.getAttacker().getUUID().toString());
		SiegeMetaDataController.setDefenderUUID(town, siege.getDefender().getUUID().toString());
		SiegeMetaDataController.setFlagLocation(town, siege.getFlagLocation().getWorld().getName()
				+ "!" + siege.getFlagLocation().getX()
				+ "!" + siege.getFlagLocation().getY()
				+ "!" + siege.getFlagLocation().getZ());
		SiegeMetaDataController.setSiegeType(town, siege.getSiegeType().toString());
		SiegeMetaDataController.setSiegeStatus(town, siege.getStatus().toString());
		SiegeMetaDataController.setSiegeBalance(town, siege.getSiegeBalance());
		SiegeMetaDataController.setWarChestAmount(town, siege.getWarChestAmount());
		SiegeMetaDataController.setTownPlundered(town, siege.getTownPlundered());
		SiegeMetaDataController.setTownInvaded(town, siege.getTownInvaded());
		SiegeMetaDataController.setStartTime(town, siege.getStartTime());
		SiegeMetaDataController.setEndTime(town, siege.getScheduledEndTime());
		SiegeMetaDataController.setActualEndTime(town, siege.getActualEndTime());
		SiegeMetaDataController.setSiegeName(town, siege.getAttacker().getName() + "#vs#" + siege.getDefender().getName());
		SiegeMetaDataController.setAttackerSiegeContributors(town, siege.getAttackerSiegeContributors());
	}

	public static boolean loadAll() {
		try {
			System.out.println(SiegeWar.prefix + "Loading sieges...");
			clearSieges();
			loadSiegeList();
			loadSieges();
			System.out.println(SiegeWar.prefix + SiegeController.getSieges().size() + " siege(s) loaded.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void loadSiegeList() {
		for (Town town : TownyUniverse.getInstance().getTowns())
			if (SiegeMetaDataController.hasSiege(town)) {
				System.out.println(SiegeWar.prefix + "Found siege in Town " + town.getName());
				System.out.println(SiegeWar.prefix + "Loading siege of town " + town.getName());
				newSiege(town);

				setSiege(town, true);

			}
	}

	public static boolean loadSieges() {
		for (Siege siege : townSiegeMap.values()) {
			if (!loadSiege(siege)) {
				System.out.println(SiegeWar.prefix + "Loading Error: Could not read data for siege on '" + siege.getTown().getName() + "'.");
				return false;
			}
		}
		return true;
	}

	public static boolean loadSiege(Siege siege) {
		//Town will be already loaded
		Town town = siege.getTown();

		//Load siege type
		String siegeTypeString = SiegeMetaDataController.getSiegeType(town);
		if (siegeTypeString== null || siegeTypeString.isEmpty())
			siege.setSiegeType(SiegeType.CONQUEST);
		else
			siege.setSiegeType(SiegeType.parseString(siegeTypeString));

		//Load Attacker & Defender
		if (SiegeMetaDataController.getAttackerUUID(town) == null) {
			//Attacker data not found. Look for old data schema
			if (SiegeMetaDataController.getNationUUID(town) != null) {
				//Old data scheme found, hook up to new values
				Nation nation;
				try {
					nation = TownyUniverse.getInstance().getDataSource().getNation(UUID.fromString(SiegeMetaDataController.getNationUUID(town)));
				} catch (NotRegisteredException e) {
					return false;
				}
				siege.setAttacker(nation);
				siege.setDefender(town);
			} else {
				System.err.print("Neither attackerUUID nor nationUUID were found");
				return false;
			}

		} else {
			//Load Attacker as normal
			try {
				switch (siege.getSiegeType()) {
					case CONQUEST:
					case LIBERATION:
					case SUPPRESSION:
						Nation nation = TownyUniverse.getInstance().getDataSource().getNation(UUID.fromString(SiegeMetaDataController.getAttackerUUID(town)));
						siege.setAttacker(nation);
						break;
					case REVOLT:
						siege.setAttacker(town);
						break;
				}
			} catch (NotRegisteredException e) {
				e.printStackTrace();
				return false;
			}

			//Load Defender as normal
			try {
				switch (siege.getSiegeType()) {
					case CONQUEST:
					case SUPPRESSION:
						siege.setDefender(town);
						break;
					case LIBERATION:
					case REVOLT:
						Nation nation = TownyUniverse.getInstance().getDataSource().getNation(UUID.fromString(SiegeMetaDataController.getDefenderUUID(town)));
						siege.setDefender(nation);
						break;
				}
			} catch (NotRegisteredException e) {
				e.printStackTrace();
				return false;
			}
		}

		//Load flag location
		if(SiegeMetaDataController.getFlagLocation(town).isEmpty())
			return false;
		String[] location = SiegeMetaDataController.getFlagLocation(town).split("!");
		World world = Bukkit.getWorld(location[0]);
		double x = Double.parseDouble(location[1]);
		double y = Double.parseDouble(location[2]);
		double z = Double.parseDouble(location[3]);
		Location loc = new Location(world, x, y, z);
		siege.setFlagLocation(loc);

		siege.setSiegeBalance(SiegeMetaDataController.getSiegeBalance(town));
		siege.setWarChestAmount(SiegeMetaDataController.getWarChestAmount(town));
		siege.setTownPlundered(SiegeMetaDataController.townPlundered(town));
		siege.setTownInvaded(SiegeMetaDataController.townInvaded(town));

		if (SiegeMetaDataController.getStartTime(town) == 0l)
			return false;
		siege.setStartTime(SiegeMetaDataController.getStartTime(town));

		if (SiegeMetaDataController.getEndTime(town) == 0l)
			return false;
		siege.setScheduledEndTime(SiegeMetaDataController.getEndTime(town));

		siege.setActualEndTime(SiegeMetaDataController.getActualEndTime(town));

		siege.setAttackerSiegeContributors(SiegeMetaDataController.getAttackerSiegeContributors(town));
		return true;
	}

	//Remove a particular siege, and all associated data
	public static void removeSiege(Siege siege, SiegeSide refundSideIfSiegeIsActive) {
		//If siege is active, initiate siege immunity for town, and return war chest
		if (siege.getStatus().isActive()) {
			siege.setActualEndTime(System.currentTimeMillis());
			SiegeWarTimeUtil.activateSiegeImmunityTimer(siege.getTown(), siege);

			if (refundSideIfSiegeIsActive == SiegeSide.ATTACKERS)
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getAttacker());
			else if (refundSideIfSiegeIsActive == SiegeSide.DEFENDERS)
				SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefender());
		}

		Town town = siege.getTown();
		//Remove siege from town
		setSiege(town, false);
		SiegeMetaDataController.removeSiegeMeta(town);
		//Remove siege from collections
		//sieges.remove(siege.getName().toLowerCase());
		townSiegeMap.remove(town.getUUID());
		siegedTowns.remove(siege.getTown());
		siegedTownNames.remove(siege.getTown().getName());

		SiegeWarTownUtil.setTownPvpFlags(town, false);
		CosmeticUtil.removeFakeBeacons(siege);

		//Save town
		town.save();
	}

	public static void putTownInSiegeMap(Town town, Siege siege) {
		townSiegeMap.put(town.getUUID(), siege);
	}

	public static boolean hasSiege(Town town) {
		return hasSiege(town.getUUID());
	}

	public static boolean hasSiege(UUID uuid) {
		return townSiegeMap.containsKey(uuid);
	}

	public static boolean hasActiveSiege(Town town) {
		return hasSiege(town) && getSiege(town).getStatus().isActive();
	}

	public static boolean hasSieges(Nation nation) {
		return !getSieges(nation).isEmpty();
	}

	public static Collection<Town> getSiegedTowns() {
		return Collections.unmodifiableCollection(siegedTowns);
	}

	public static Collection<String> getSiegedTownNames() {
		return Collections.unmodifiableCollection(siegedTownNames);
	}

	public static void renameSiegedTownName(String oldname, String newname) {
		siegedTownNames.remove(oldname);
		siegedTownNames.add(newname);
	}

	//Get all the sieges a nation is involved in
	@Nullable
	public static List<Siege> getSieges(Nation nation) {
		List<Siege> siegeList = new ArrayList<>();
		for (Siege siege : townSiegeMap.values()) {
			if (siege.getAttacker().equals(nation) || siege.getDefender().equals(nation))
				siegeList.add(siege);
		}
		return siegeList;
	}

	@Nullable
	public static Siege getSiege(Town town) {
		if (hasSiege(town.getUUID()))
			return townSiegeMap.get(town.getUUID());
		return null;
	}

	@Nullable
	public static Siege getSiegeByTownUUID(UUID townUUID) {
		if (hasSiege(townUUID))
			return townSiegeMap.get(townUUID);
		return null;
	}

	@Nullable
	public static List<Siege> getSiegesByNationUUID(UUID uuid) {
		List<Siege> siegeList = new ArrayList<>();
		for (Siege siege : townSiegeMap.values()) {
			Town town = siege.getTown();
			if (UUID.fromString(SiegeMetaDataController.getNationUUID(town)).equals(uuid))
				siegeList.add(siege);
		}
		return siegeList;
	}

	@Nullable
	public static String getSiegeName(Town town) {
		return SiegeMetaDataController.getSiegeName(town);
	}

	public static void setSiege(Town town, boolean bool) {
		SiegeMetaDataController.setSiege(town, bool);
	}

	public static Set<Player> getPlayersInBannerControlSessions() {
		Set<Player> result = new HashSet<>();
		for (Siege siege : townSiegeMap.values()) {
			result.addAll(siege.getBannerControlSessions().keySet());
		}
		return result;
	}

	public static List<Siege> getActiveSiegesAt(Location location) {
		List<Siege> siegesAtLocation = new ArrayList<>();
		for (Siege siege : townSiegeMap.values()) {
			if (SiegeWarDistanceUtil.isInSiegeZone(location, siege) && siege.getStatus().isActive()) {
				siegesAtLocation.add(siege);
			}
		}
		return siegesAtLocation;
	}

	/**
	 * Gets a list of towns with an active siege that have a certain nation
	 *
	 * @param nation The nation that the town must be in.
	 * @return The list of towns that are under siege in that nation.
	 */
	public static List<Town> getSiegedTowns(Nation nation) {
		return getSiegedTowns().stream()
				.filter(t -> t.hasNation())
				.filter(t -> getSiege(t).getStatus().isActive())
				.filter(t -> TownyAPI.getInstance().getTownNationOrNull(t).equals(nation))
				.collect(Collectors.toList());

	}

	public static int getNumActiveAttackSieges(Nation nation) {
		int num = 0;
		for(Siege siege: getSieges(nation)) {
			if(siege.getStatus().isActive() && siege.getAttacker() == nation) {
				num++;
			}
		}
		return num;
	}
}
