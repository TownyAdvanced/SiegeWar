package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.events.PreSiegeCampEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.gmail.goosius.siegewar.metadata.SiegeMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.*;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
	private static List<SiegeCamp> siegeCamps = new ArrayList<>();

	public static void newSiege(Town town) {
		Siege siege = new Siege(town);
		townSiegeMap.put(town.getUUID(), siege);
		siegedTowns.add(town);
	}

	public static List<Siege> getSieges() {
		return new ArrayList<>(townSiegeMap.values());
	}

	public static void clearSieges() {
		townSiegeMap.clear();
		siegedTowns.clear();
	}

	public static void saveSiege(Siege siege) {
		Town town = siege.getTown();
		SiegeMetaDataController.setTownUUID(town, siege.getTown().getUUID().toString());
		SiegeMetaDataController.setAttackerUUID(town, siege.getAttacker().getUUID().toString());
		SiegeMetaDataController.setDefenderUUID(town, siege.getDefender().getUUID().toString());
		if(siege.getAttackerName() != null)
			SiegeMetaDataController.setAttackerName(town, siege.getAttackerName());
		if(siege.getDefenderName() != null)
			SiegeMetaDataController.setDefenderName(town, siege.getDefenderName());
		SiegeMetaDataController.setFlagLocation(town, siege.getFlagLocation().getWorld().getName()
				+ "!" + siege.getFlagLocation().getX()
				+ "!" + siege.getFlagLocation().getY()
				+ "!" + siege.getFlagLocation().getZ());
		SiegeMetaDataController.setSiegeType(town, siege.getSiegeType().toString());
		SiegeMetaDataController.setSiegeStatus(town, siege.getStatus().toString());
		SiegeMetaDataController.setSiegeBalance(town, siege.getSiegeBalance());
		SiegeMetaDataController.setAttackerBattlePoints(town, siege.getAttackerBattlePoints());
		SiegeMetaDataController.setDefenderBattlePoints(town, siege.getDefenderBattlePoints());
		SiegeMetaDataController.setWarChestAmount(town, siege.getWarChestAmount());
		SiegeMetaDataController.setTownPlundered(town, siege.getTownPlundered());
		SiegeMetaDataController.setTownInvaded(town, siege.getTownInvaded());
		SiegeMetaDataController.setStartTime(town, siege.getStartTime());
		SiegeMetaDataController.setEndTime(town, siege.getScheduledEndTime());
		SiegeMetaDataController.setActualEndTime(town, siege.getActualEndTime());
		SiegeMetaDataController.setPrimaryTownGovernments(town, siege.getPrimaryTownGovernments());
		town.save();
	}

	public static boolean loadAll() {
		try {

			SiegeWar.info("Loading Siege Data...");
			clearSieges();
			SiegeWar.info("Loading Siege List Data...");
			loadSiegeList();
			SiegeWar.info("Loading Siege Detail Data...");
			if(!loadSieges())
				return false;
			SiegeWar.info("Siege Data Loaded Successfully.");
			SiegeWar.info(SiegeController.getSieges().size() + " siege(s) loaded.");
			return true;
		} catch (Exception e) {
			SiegeWar.severe("Problem Loading Siege Data...");
			e.printStackTrace();
			return false;
		}
	}

	public static void loadSiegeList() {
		for (Town town : TownyUniverse.getInstance().getTowns())
			if (SiegeMetaDataController.hasSiege(town)) {
				SiegeWar.info("Siege List Data: Found siege in Town " + town.getName());
				newSiege(town);
				setSiege(town, true);
			}
	}

	public static boolean loadSieges() {
		for (Siege siege : townSiegeMap.values()) {
			SiegeWar.info("Siege Detail Data: Loading siege data for town '" + siege.getTown().getName() + "'");
			if (!loadSiege(siege)) {
				SiegeWar.severe("Siege Detail Data: Loading Error: Could not read siege data for town '" + siege.getTown().getName() + "'.");
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

		//Load Attacker
		switch (siege.getSiegeType()) {
			case CONQUEST:
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

		//Load defender
		switch (siege.getSiegeType()) {
			case CONQUEST:
				siege.setDefender(town);
				break;
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

		//Load Status
		if (SiegeMetaDataController.getSiegeStatus(town).isEmpty())
			return false;
		siege.setStatus(SiegeStatus.parseString(SiegeMetaDataController.getSiegeStatus(town)));

		//Load attacker & defender name
		if(!siege.getStatus().isActive() && SiegeMetaDataController.getAttackerName(town) == null) {
			/* 
			 * Migrate old data:
			 * 1. If the siege is over but there is no attacker & defender name, 
			 * then the data must be unavailable, 
			 * likely due to a pre-0.10.0 data source.
			 * 2. Thus, populate the fields now using the names of the current attacker and defender.
			 */
			siege.setAttackerName(siege.getAttacker().getName());
			siege.setDefenderName(siege.getDefender().getName());
		} else {
			siege.setAttackerName(SiegeMetaDataController.getAttackerName(town));
			siege.setDefenderName(SiegeMetaDataController.getDefenderName(town));
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
		siege.setAttackerBattlePoints(SiegeMetaDataController.getAttackerBattlePoints(town));
		siege.setDefenderBattlePoints(SiegeMetaDataController.getDefenderBattlePoints(town));
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
			if(!siege.isRevoltSiege()) {
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

	public static Collection<String> getNamesOfSiegedTowns() {
		Set<String> result = new HashSet<>();
		for(Siege siege: getSieges()) {
			result.add(siege.getTown().getName());
		}
		return result;
	}

	public static Collection<String> getNamesOfActivelySiegedTowns() {
		Set<String> result = new HashSet<>();
		for(Siege siege: getSieges()) {
			if(siege.getStatus().isActive())
				result.add(siege.getTown().getName());
		}
		return result;
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

	/**
	 * Get the active siege at the given location
	 * If more than one active siege is found, return the closest one
	 * If no active sieges are found, return null
	 *
	 * @param loc Given location
	 * @return active siege at given location
	 */
	@Nullable
	public static Siege getActiveSiegeAtLocation(Location loc) {
		Siege resultSiege = null;
		int distanceToResultSiege = 0;
		int distanceToCandidateSiege = 0;
		for (Siege candidateSiege : getSieges()) {
			if (candidateSiege.getStatus().isActive()) {
				if (!candidateSiege.getFlagBlock().getWorld().getName().equals(loc.getWorld().getName()))
					continue;
				distanceToCandidateSiege = SiegeWarDistanceUtil.getDistanceToSiege(loc, candidateSiege);
				if(distanceToCandidateSiege < SiegeWarSettings.getWarSiegeZoneRadiusBlocks()) {
					if(resultSiege == null || distanceToCandidateSiege < distanceToResultSiege) {
						resultSiege = candidateSiege;
						distanceToResultSiege = distanceToCandidateSiege;
					}
				}
			}
		}
		return resultSiege;
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
		Bukkit.getPluginManager().callEvent(new SiegeWarStartEvent(siege, townOfSiegeStarter));
	}

	private static void sendGlobalSiegeStartMessage(Siege siege) {
		switch (siege.getSiegeType()) {

			case CONQUEST:
				if (siege.getTown().hasNation()) {
					Messaging.sendGlobalMessage(
							Translatable.of("msg_conquest_siege_started_nation_town",
							siege.getAttacker().getName(),
							TownyAPI.getInstance().getTownNationOrNull(siege.getTown()).getName(),
							siege.getTown().getName()
					));
				} else {
					Messaging.sendGlobalMessage(
							Translatable.of("msg_conquest_siege_started_neutral_town",
							siege.getAttacker().getName(),
							siege.getTown().getName()
					));
				}
				break;

			case REVOLT:
				Messaging.sendGlobalMessage(
						Translatable.of("msg_revolt_siege_started",
						siege.getTown().getName(),
						siege.getDefender().getName()
				));
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
	
	/**
	 * List of {@link SiegeCamp}s, objects which precede a Siege.
	 */
	public static List<SiegeCamp> getSiegeCamps() {
		return Collections.unmodifiableList(siegeCamps);
	}
	
	/**
	 * Add a {@link SiegeCamp} to the SiegeCamp list.
	 * @param camp {@link SiegeCamp} to add.
	 */
	public static void addSiegeCamp(SiegeCamp camp) {
		siegeCamps.add(camp);
	}
	
	/**
	 * Remove a {@link SiegeCamp} from the SiegeCamp list.
	 * @param camp {@link SiegeCamp} to remove.
	 */
	public static void removeSiegeCamp(SiegeCamp camp) {
		siegeCamps.remove(camp);
	}
	
	/**
	 * Called internally from the StartTYPESiege classes, to begin a Siege,
	 * potentially starting with the SiegeCamp minigame.
	 * 
	 * Note: SiegeCamps are called SiegeAssemblies in the config and ingame lingo.
	 * 
	 * @param player             Player starting the siege using a Banner.
	 * @param bannerBlock        Block which is the Banner.
	 * @param siegeType          SiegeType that the Siege will be.
	 * @param targetTown         Town about to be Sieged.
	 * @param attacker           Government which is attacking the town.
	 * @param defender           Government which is defending the town.
	 * @param townOfSiegeStarter Town of the player who started the siege.
	 * @param townBlock          TownBlock of the Town which is adjacent to the
	 *                           banner.
	 * @throws TownyException if the PreSiegeCampEvent is cancelled or if the
	 *                        SiegeCamp is unable to start.
	 */
	public static void startSiegeCampProcess (Player player,
											  Block bannerBlock,
											  SiegeType siegeType,
											  Town targetTown,
											  Government attacker,
											  Government defender,
											  Town townOfSiegeStarter,
											  TownBlock townBlock) throws TownyException {
		SiegeCamp camp = new SiegeCamp(player, bannerBlock, siegeType, targetTown, attacker, defender, townOfSiegeStarter, townBlock);
		
		PreSiegeCampEvent event = new PreSiegeCampEvent(camp);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			throw new TownyException(event.getCancellationMsg());
		
		if (SiegeWarSettings.areSiegeCampsEnabled()) {
			if (hasSiegeCamp(targetTown))
				throw new TownyException(Translatable.of("msg_err_town_already_has_a_siege_assembly"));
			// Launch a SiegeCamp, a (by default) 10 minute minigame. If successful the Siege will be initiated in ernest. 
			SiegeController.beginSiegeCamp(camp);
		} else 
			// SiegeCamps are disabled, just do the Siege.
			camp.startSiege();
	}
	
	private static boolean hasSiegeCamp(Town targetTown) {
		for (SiegeCamp camp : getSiegeCamps())
			if (camp.getTargetTown().equals(targetTown))
				return true;
		return false;
	}

	/**
	 * Initiates a {@link SiegeCamp}, leads to a {@link Siege} if successfully camped.
	 * @param camp {@link SiegeCamp} to begin.
	 * @throws TownyException if SiegeCamp is denied.
	 */
	public static void beginSiegeCamp(SiegeCamp camp) throws TownyException {
		// Another SiegeCamp is already present.
		if (SiegeWarDistanceUtil.campTooClose(camp.getBannerBlock().getLocation()))
			throw new TownyException(Translation.of("msg_err_siegecamp_too_close_to_another_siegecamp"));
		
		// Town initiating the SiegeCamp has a failed SiegeCamp on this 
		// town and not enough time has passed. 
		if (SiegeCampUtil.hasFailedCamp(camp.getTargetTown(), camp.getTownOfSiegeStarter()))
			throw new TownyException(Translation.of("msg_err_too_soon_since_your_last_siegecamp"));

		// Broadcast a message
		Messaging.sendGlobalMessage(Translatable.of("attacker_has_begun_a_siegecamp_session", camp.getTownOfSiegeStarter(), camp.getTargetTown(), SiegeWarSettings.getSiegeCampPointsForSuccess(), SiegeWarSettings.getSiegeCampDurationInMinutes()));
		// Add to SiegeCamp list and begin Evaluating this SiegeCamp for success.
		addSiegeCamp(camp);
		Bukkit.getScheduler().runTask(SiegeWar.getSiegeWar(), ()-> SiegeCampUtil.evaluateCamp(camp, true));
	}


}
