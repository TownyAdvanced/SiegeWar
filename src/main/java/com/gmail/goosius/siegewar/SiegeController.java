package com.gmail.goosius.siegewar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.CosmeticUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.metadata.SiegeMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarImmunityUtil;
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
		SiegeMetaDataController.setResidentTimedPointContributors(town, siege.getResidentTimedPointContributors());
		SiegeMetaDataController.setPrimaryTownGovernments(town, siege.getPrimaryTownGovernments());
		town.save();
	}

	public static boolean loadAll() {
		try {
			System.out.println(SiegeWar.prefix + "Loading Siege Data...");
			clearSieges();
			System.out.println(SiegeWar.prefix + "Loading Siege List Data...");
			loadSiegeList();
			System.out.println(SiegeWar.prefix + "Loading Siege Detail Data...");
			if(!loadSieges())
				return false;
			System.out.println(SiegeWar.prefix + "Siege Data Loaded Successfully.");
			System.out.println(SiegeWar.prefix + SiegeController.getSieges().size() + " siege(s) loaded.");
			return true;
		} catch (Exception e) {
			System.out.println(SiegeWar.prefix + "Problem Loading Siege Data...");
			e.printStackTrace();
			return false;
		}
	}

	public static void loadSiegeList() {
		for (Town town : TownyUniverse.getInstance().getTowns())
			if (SiegeMetaDataController.hasSiege(town)) {
				System.out.println(SiegeWar.prefix + "Siege List Data: Found siege in Town " + town.getName());
				newSiege(town);

				setSiege(town, true);

			}
	}

	public static boolean loadSieges() {
		for (Siege siege : townSiegeMap.values()) {
			System.out.println(SiegeWar.prefix + "Siege Detail Data: Loading siege data for town '" + siege.getTown().getName() + "'");
			if (!loadSiege(siege)) {
				System.out.println(SiegeWar.prefix + "Siege Detail Data: Loading Error: Could not read siege data for town '" + siege.getTown().getName() + "'.");
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
				Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(SiegeMetaDataController.getNationUUID(town)));
				if (nation == null)
					return false;
				siege.setAttacker(nation);
				siege.setDefender(town);
			} else {
				System.err.print("Neither attackerUUID nor nationUUID were found");
				return false;
			}

		} else {
			switch (siege.getSiegeType()) {
				case CONQUEST:
				case LIBERATION:
				case SUPPRESSION:
					String uuid = SiegeMetaDataController.getAttackerUUID(town);
					if (uuid == null)
						return false;
					Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(uuid));
					if (nation == null)
						return false;
					siege.setAttacker(nation);
					break;
				case REVOLT:
					siege.setAttacker(town);
					break;
			}

			switch (siege.getSiegeType()) {
				case CONQUEST:
				case SUPPRESSION:
					siege.setDefender(town);
					break;
				case LIBERATION:
				case REVOLT:
					String uuid = SiegeMetaDataController.getDefenderUUID(town);
					if (uuid == null)
						return false;
					Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(uuid));
					if (nation == null)
						return false;
					siege.setDefender(nation);
					break;
			}
		}

		if (SiegeMetaDataController.getSiegeStatus(town).isEmpty())
			return false;
		siege.setStatus(SiegeStatus.parseString(SiegeMetaDataController.getSiegeStatus(town)));

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

		siege.setResidentTimedPointContributors(SiegeMetaDataController.getResidentTimedPointContributors(town));

		siege.setPrimaryTownGovernments(SiegeMetaDataController.getPrimaryTownGovernments(town));

		return true;
	}

	//Remove a particular siege, and all associated data
	public static void removeSiege(Siege siege, SiegeSide refundSideIfSiegeIsActive) {
		//If siege is active, initiate siege immunity for town, and return war chest
		if(siege.getStatus().isActive()) {
			siege.setActualEndTime(System.currentTimeMillis());
			SiegeWarImmunityUtil.grantSiegeImmunityAfterEndedSiege(siege.getTown(), siege);

			//Return warchest only if siege is not revolt
			if(siege.getSiegeType() != SiegeType.REVOLT) {
				if (refundSideIfSiegeIsActive == SiegeSide.ATTACKERS)
					SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getAttacker());
				else if (refundSideIfSiegeIsActive == SiegeSide.DEFENDERS)
					SiegeWarMoneyUtil.giveWarChestTo(siege, siege.getDefender());
			}
		}

		Town town = siege.getTown();
		//Remove siege from town
		setSiege(town, false);
		SiegeMetaDataController.removeSiegeMeta(town);
		//Remove siege from collections
		townSiegeMap.remove(town.getUUID());
		siegedTowns.remove(siege.getTown());
		siegedTownNames.remove(siege.getTown().getName());

		SiegeWarTownUtil.setPvpFlag(town, false);
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
			if(siege.getAttacker().getUUID().equals(uuid)
				|| siege.getDefender().getUUID().equals(uuid)) {
				siegeList.add(siege);
			}
		}
		return siegeList;
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

	public static Map<Siege, Town> getActiveOffensiveSieges(Nation nation) {
		Map<Siege, Town> result = new HashMap<>();
		for(Siege siege : SiegeController.getSieges()) {
			if(siege.getStatus().isActive()
				&& siege.getAttacker() == nation) {
					result.put(siege, siege.getTown());
			}
		}
		return result;
	}

	public static Map<Siege, Town> getActiveDefensiveSieges(Nation nation) {
		Map<Siege, Town> result = new HashMap<>();
		for(Siege siege : SiegeController.getSieges()) {
			if(siege.getStatus().isActive()) {
				if(siege.getDefender() instanceof Nation) {
					if(siege.getDefender() == nation) {
						//Defender is the given nation
						result.put(siege, siege.getTown());
					}
				} else {
					if(siege.getTown().hasNation()
						&& TownyAPI.getInstance().getTownNationOrNull(siege.getTown()) == nation) {
						//Defender is a town belonging to the given nation
						result.put(siege, siege.getTown());
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param town the town to check
	 * @return true if the town has a nation & that nation is fighting a home-defence war
	 *
	 * Note: A home defence war is when one or more of the nation's natural towns (ie not occupied foreign towns) is under siege.
	 */
	public static boolean isTownsNationFightingAHomeDefenceWar(Town town) {
		if(town.hasNation()) {
			return isNationFightingAHomeDefenceWar(TownyAPI.getInstance().getTownNationOrNull(town));
		}
		return false;
	}

	/**
	 * @param nation the nation to check
	 * @return true if the given nation is fighting a home-defence war
	 *
	 * Note: A home defence war is when one or more of the nation's natural towns (ie not occupied foreign towns) is under siege.
	 */
	public static boolean isNationFightingAHomeDefenceWar(Nation nation) {
		for(Siege siege: SiegeController.getSieges()) {
			if(siege.getStatus().isActive()
				&& siege.getTown().hasNation()
				&& TownyAPI.getInstance().getTownNationOrNull(siege.getTown()) == nation) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Start a siege
	 *
	 * @param bannerBlock banner block
	 * @param siegeType the siege type
	 * @param targetTown the target town
	 * @param attacker the attacking government
	 * @param defender the defending government
	 * @param townOfSiegeStarter the town of the siege starter
	 * @param useWarchest true if warchest should be used
	 */
	public static void startSiege(Block bannerBlock,
								   SiegeType siegeType,
								   Town targetTown,
								   Government attacker,
								   Government defender,
								   Town townOfSiegeStarter,
								   boolean useWarchest) {
		//Create Siege
		SiegeController.newSiege(targetTown);
		Siege siege = SiegeController.getSiege(targetTown);

		//Set values in siege object
		siege.setSiegeType(siegeType);
		siege.setTown(targetTown);
		siege.setAttacker(attacker);
		siege.setDefender(defender);
		siege.setStatus(SiegeStatus.IN_PROGRESS);
		siege.setTownPlundered(false);
		siege.setTownInvaded(false);
		siege.setStartTime(System.currentTimeMillis());
		siege.setScheduledEndTime(
				(System.currentTimeMillis() +
						((long) (SiegeWarSettings.getWarSiegeMaxHoldoutTimeHours() * TimeMgmt.ONE_HOUR_IN_MILLIS))));
		siege.setActualEndTime(0);
		siege.setFlagLocation(bannerBlock.getLocation());

		SiegeController.setSiege(targetTown, true);
		SiegeController.putTownInSiegeMap(targetTown, siege);

		//Set pvp to true in the besieged town
		SiegeWarTownUtil.setPvpFlag(targetTown, true);

		sendGlobalSiegeStartMessage(siege);

		//Pay into warchest
		if (useWarchest) {
			siege.setWarChestAmount(SiegeWarMoneyUtil.calculateSiegeCost(targetTown));
			if (TownyEconomyHandler.isActive()) {
				//Pay upfront cost into warchest now
				attacker.getAccount().withdraw(siege.getWarChestAmount(), "Cost of starting a siege.");
				String moneyMessage =
						Translation.of("msg_siege_war_attack_pay_war_chest",
								attacker.getName(),
								TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));

				TownyMessaging.sendPrefixedNationMessage((Nation)attacker, moneyMessage);
				if(defender instanceof Nation) {
					TownyMessaging.sendPrefixedNationMessage((Nation)defender, moneyMessage);
				} else {
					TownyMessaging.sendPrefixedTownMessage((Town)defender, moneyMessage);
				}
			}
	 	} else {
			siege.setWarChestAmount(0);
		}

		//Save to DB
		SiegeController.saveSiege(siege);

		//Call event
		Bukkit.getPluginManager().callEvent(new SiegeWarStartEvent(siege, townOfSiegeStarter, bannerBlock));
	}

	private static void sendGlobalSiegeStartMessage(Siege siege) {
		switch (siege.getSiegeType()) {

			case CONQUEST:
				if (siege.getTown().hasNation()) {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_conquest_siege_started_nation_town"),
							siege.getAttacker().getName(),
							TownyAPI.getInstance().getTownNationOrNull(siege.getTown()).getName(),
							siege.getTown().getName()
					));
				} else {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_conquest_siege_started_neutral_town"),
							siege.getAttacker().getName(),
							siege.getTown().getName()
					));
				}
				break;
			case LIBERATION:
				if (siege.getTown().hasNation()) {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_liberation_siege_started_nation_town"),
							siege.getAttacker().getName(),
							siege.getDefender().getName(),
							siege.getTown().getName()
					));
				} else {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_liberation_siege_started_neutral_town"),
							siege.getAttacker().getName(),
							siege.getDefender().getName(),
							siege.getTown().getName()
					));
				}
				break;
			case REVOLT:
				if (siege.getTown().hasNation()) {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_revolt_siege_started_nation_town"),
							siege.getTown().getName(),
							TownyAPI.getInstance().getTownNationOrNull(siege.getTown()).getName(),
							TownOccupationController.getTownOccupier(siege.getTown()).getName()
					));
				} else {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_revolt_siege_started_neutral_town"),
							siege.getTown().getName(),
							TownOccupationController.getTownOccupier(siege.getTown()).getName()
					));
				}
				break;
			case SUPPRESSION:
				if (siege.getTown().hasNation()) {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_suppression_siege_started_nation_town"),
							siege.getAttacker().getName(),
							TownyAPI.getInstance().getTownNationOrNull(siege.getTown()).getName(),
							siege.getTown().getName()
					));
				} else {
					Messaging.sendGlobalMessage(String.format(
							Translation.of("msg_suppression_siege_started_neutral_town"),
							siege.getAttacker().getName(),
							siege.getTown().getName()
					));
				}
				break;
		}
	}

	public static boolean doesNationHaveAnyHomeDefenceContributionsInActiveSieges(Nation nation) {
		for(Siege siege: townSiegeMap.values()) {
			if(siege.getStatus().isActive()
				&& siege.getPrimaryTownGovernments().containsKey(nation.getUUID())) {
				return true;
			}
		}
		return false;
	}
}
