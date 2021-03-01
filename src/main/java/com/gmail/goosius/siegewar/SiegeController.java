package com.gmail.goosius.siegewar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.SiegeMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarTownUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

/**
 * 
 * @author LlmDl
 *
 */
public class SiegeController {

	private final static Map<String, Siege> sieges = new ConcurrentHashMap<>();
	private static Map<UUID, Siege> townSiegeMap = new ConcurrentHashMap<>();
	private static List<Town> siegedTowns = new ArrayList<>();
	private static List<String> siegedTownNames = new ArrayList<>();
	
	public static void newSiege(String siegeName) {
		Siege siege = new Siege(siegeName);		

		sieges.put(siegeName.toLowerCase(), siege);
	}

	public static List<Siege> getSieges() {
		return new ArrayList<>(sieges.values());
	}

	public static Siege getSiege(String siegeName) throws NotRegisteredException {
		if(!sieges.containsKey(siegeName.toLowerCase())) {
			throw new NotRegisteredException("Siege not found");
		}
		return sieges.get(siegeName.toLowerCase());
	}
	
	public static void clearSieges() {
		sieges.clear();
		townSiegeMap.clear();
		siegedTowns.clear();
		siegedTownNames.clear();
	}
	
	public static boolean saveSieges() {
		for (Siege siege : sieges.values()) {
			saveSiege(siege);
		}
		return true;
	}
	
	public static void saveSiege(Siege siege) {
		Town town = siege.getDefendingTown();
		SiegeMetaDataController.setNationUUID(town, siege.getAttackingNation().getUUID().toString());
		SiegeMetaDataController.setTownUUID(town, siege.getDefendingTown().getUUID().toString());
		SiegeMetaDataController.setFlagLocation(town, siege.getFlagLocation().getWorld().getName()
			+ "!" + siege.getFlagLocation().getX()
			+ "!" + siege.getFlagLocation().getY()
			+ "!" + siege.getFlagLocation().getZ());
		SiegeMetaDataController.setSiegeStatus(town, siege.getStatus().toString());
		SiegeMetaDataController.setSiegeBalance(town, siege.getSiegeBalance());
		SiegeMetaDataController.setWarChestAmount(town, siege.getWarChestAmount());
		SiegeMetaDataController.setTownPlundered(town, siege.getTownPlundered());
		SiegeMetaDataController.setTownInvaded(town, siege.getTownInvaded());
		SiegeMetaDataController.setStartTime(town, siege.getStartTime());
		SiegeMetaDataController.setEndTime(town, siege.getScheduledEndTime());
		SiegeMetaDataController.setActualEndTime(town, siege.getActualEndTime());
		SiegeMetaDataController.setSiegeName(town, siege.getAttackingNation().getName() + "#vs#" + siege.getDefendingTown().getName());
		SiegeMetaDataController.setAttackerSiegeContributors(town, siege.getAttackerSiegeContributors());
	}

	public static void loadAll() {
	    System.out.println(SiegeWar.prefix + "Loading SiegeList...");
		clearSieges();
		loadSiegeList();
		loadSieges();
		System.out.println(SiegeWar.prefix + SiegeController.getSieges().size() + " siege(s) loaded.");

	}
	
	public static void loadSiegeList() {
		for (Town town : TownyUniverse.getInstance().getTowns())
			if (SiegeMetaDataController.hasSiege(town)) {
				System.out.println(SiegeWar.prefix + "Found siege in Town " + town.getName());
				String name = getSiegeName(town);
				if (name != null) {
					System.out.println(SiegeWar.prefix + "Loading siege " + name.replace("#", " "));
					newSiege(name);
					setSiege(town, true);
					townSiegeMap.put(town.getUUID(), sieges.get(name.toLowerCase()));
					siegedTowns.add(town);
					siegedTownNames.add(town.getName());
				}
			}
	}

	public static boolean loadSieges() {
		for (Siege siege : sieges.values()) {
			if (!loadSiege(siege)) {
				System.out.println(SiegeWar.prefix + "Loading Error: Could not read siege data '" + siege.getName() + "'.");
				return false;
			}
		}
		return true;		
	}
	
	public static boolean loadSiege(Siege siege) {
		String townName = siege.getName().split("#")[2];
		Town town = TownyUniverse.getInstance().getTown(townName);
		if (town == null)
			return false;
		siege.setDefendingTown(town);

		Nation nation = null;
		try {
			nation = TownyUniverse.getInstance().getDataSource().getNation(UUID.fromString(SiegeMetaDataController.getNationUUID(town)));
		} catch (NotRegisteredException ignored) {}
		if (nation == null)
			return false;
		siege.setAttackingNation(nation);
		
		if (SiegeMetaDataController.getFlagLocation(town).isEmpty())
			return false;
		String[] location = SiegeMetaDataController.getFlagLocation(town).split("!");
		World world = Bukkit.getWorld(location[0]);
		double x = Double.parseDouble(location[1]);
		double y = Double.parseDouble(location[2]);
		double z = Double.parseDouble(location[3]);		
		Location loc = new Location(world, x, y, z);
		siege.setFlagLocation(loc);

		if (SiegeMetaDataController.getSiegeStatus(town).isEmpty())
			return false;
		siege.setStatus(SiegeStatus.parseString(SiegeMetaDataController.getSiegeStatus(town)));

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
		if(siege.getStatus().isActive()) {
			siege.setActualEndTime(System.currentTimeMillis());
			SiegeWarTimeUtil.activateSiegeImmunityTimer(siege.getDefendingTown(), siege);

			if(refundSideIfSiegeIsActive == SiegeSide.ATTACKERS)
				SiegeWarMoneyUtil.giveWarChestToAttackingNation(siege);
			else if (refundSideIfSiegeIsActive == SiegeSide.DEFENDERS)
				SiegeWarMoneyUtil.giveWarChestToDefendingTown(siege);
		}

		Town town = siege.getDefendingTown();
		//Remove siege from town
		setSiege(town, false);
		SiegeMetaDataController.removeSiegeMeta(town);
		//Remove siege from maps
		sieges.remove(siege.getName().toLowerCase());
		townSiegeMap.remove(town.getUUID());
		removeSiegedTown(siege);

		SiegeWarTownUtil.setTownPvpFlags(town, false);
		CosmeticUtil.removeFakeBeacons(siege);

		//Save attacking nation
		siege.getAttackingNation().save();
		siege = null;
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
	
	public static void addSiegedTown(Siege siege) {
		siegedTowns.add(siege.getDefendingTown());
		siegedTownNames.add(siege.getDefendingTown().getName());
	}

	public static void removeSiegedTown(Siege siege) {
		siegedTowns.remove(siege.getDefendingTown());
		siegedTownNames.remove(siege.getDefendingTown().getName());
	}
	
	@Nullable
	public static List<Siege> getSieges(Nation nation) {
		List<Siege> siegeList = new ArrayList<>();
		for (Siege siege : sieges.values()) {
			if (siege.getAttackingNation().equals(nation))
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
	public static Siege getSiege(UUID uuid) {
		if (hasSiege(uuid))
			return townSiegeMap.get(uuid);
		return null;
	}
	
	@Nullable
	public static List<Siege> getSiegesByNationUUID(UUID uuid) {
		List<Siege> siegeList = new ArrayList<>();
		for (Siege siege : sieges.values()) {
			Town town = siege.getDefendingTown();
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
		for (Siege siege : sieges.values()) {
			result.addAll(siege.getBannerControlSessions().keySet());
		}
		return result;
	}

	public static List<Siege> getActiveSiegesAt(Location location) {
		List<Siege> siegesAtLocation = new ArrayList<>();
		for (Siege siege : sieges.values()) {
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
}
