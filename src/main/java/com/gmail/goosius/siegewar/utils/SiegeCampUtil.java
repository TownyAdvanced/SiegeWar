package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SiegeCampUtil {

	/**
	 * Evaluates a {@link SiegeCamp}.
	 * @param camp SiegeCamp to evaluate.
	 * @param firstRun true causes it to skip scoring points. 
	 */
	public static void evaluateCamp(SiegeCamp camp, boolean firstRun) {
		if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(camp.getBannerBlock().getLocation())) {
			// Stop if a Siege has begun in the area, shouldn't happen but you never know.
			SiegeController.removeSiegeCamp(camp);
		} else if (camp.getEndTime() < System.currentTimeMillis()) {
			// If the time duration of a SiegeCamp has passed, finish the SiegeCamp by evaluating the attacker's points.
			finishSiegeCamp(camp);
		} else {
			// SiegeCamp is ongoing, evaluate players around the SiegeCamp and reschedule the next evaluation in one minute.
			if (!firstRun)
				evaluatePlayers(camp);
			Bukkit.getScheduler().runTaskLater(SiegeWar.getSiegeWar(), ()-> evaluateCamp(camp, false), 1200l);
		}	
	}

	/**
	 * Evaluate the player surrounding the SiegeCamp.
	 * 
	 * If there are players present belonging to the attackers town or nation,
	 * the attackerpoints are increased.
	 * 
	 * @param camp {@link SiegeCamp} to evaluate. 
	 */
	private static void evaluatePlayers(SiegeCamp camp) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Resident res = TownyAPI.getInstance().getResident(player);
			// We only care about residents which are part of the attacking town or nation.
			if (res == null 
				|| !res.hasTown()
				|| !camp.getTownOfSiegeStarter().hasResident(res.getName()))
				continue;

			// Various things which would prevent a player from otherwise being part of a real Siege.
			if (!player.isOnline()
				|| player.isDead()
				|| !player.getWorld().equals(camp.getBannerBlock().getWorld())
				|| player.isGliding() 
				|| player.isFlying() 
				|| player.getGameMode().equals(GameMode.SPECTATOR))
				continue;
			
			// Weed out the player who aren't in the area of the SiegeCamp.
			if (!TownyAPI.getInstance().isWilderness(player.getLocation()) || !SiegeWarDistanceUtil.isInSiegeCampZone(player.getLocation(), camp))
				continue;

			// At least one attacker is present, give attacker points and break out of the loop.
			camp.setAttackerPoints(camp.getAttackerPoints() + SiegeWarSettings.getSiegeCampPointsPerMinute());
			Messaging.sendGlobalMessage(Translatable.of("attackers_scored_points_towards_siege_camp_x_of_x", camp.getTownOfSiegeStarter(), camp.getAttackerPoints(), SiegeWarSettings.getSiegeCampPointsForSuccess()));
			break;
		}
	}

	/**
	 * Finish a SiegeCamp and determine whether the SiegeCamp was successful.
	 * Penalize failed SiegeCamps by making the attacking Town unable to begin another SiegeCamp for a time period.
	 * @param camp {@link SiegeCamp} to finish.
	 */
	private static void finishSiegeCamp(SiegeCamp camp) {

		if (camp.getAttackerPoints() >= SiegeWarSettings.getSiegeCampPointsForSuccess()) {
			// Attackers scored enough points to start the Siege in ernest.
			camp.startSiege();

		} else {
			// Attackers were thwarted, they are penalized with a cooldown on making another SiegeCamp on this town.
			Government attacker = camp.getAttacker();
			if (attacker instanceof Town)
				TownyMessaging.sendPrefixedTownMessage((Town) attacker, Translatable.of("msg_err_your_siegecamp_failed_you_must_wait_x", TimeMgmt.formatCountdownTime(SiegeWarSettings.getFailedSiegeCampCooldown())));
			else if (attacker instanceof Nation)
				TownyMessaging.sendPrefixedNationMessage((Nation) attacker, Translatable.of("msg_err_your_siegecamp_failed_you_must_wait_x", TimeMgmt.formatCountdownTime(SiegeWarSettings.getFailedSiegeCampCooldown())));

			String failedCamps = TownMetaDataController.getFailedSiegeCampList(camp.getTargetTown());
			long endTime = (System.currentTimeMillis() + (SiegeWarSettings.getFailedSiegeCampCooldown() * 1000));
			if (failedCamps == null || failedCamps.isEmpty()) {
				// No metadata, start a new failedCamps string.
				failedCamps = camp.getTargetTown().getUUID() + ":" + endTime;
			} else {
				// This target town already had at least one failed siegecamp.
				failedCamps += "," + camp.getTargetTown().getUUID() + ":" + endTime;
			}

			// Set the metadata on the target town.
			TownMetaDataController.setFailedCampSiegeList(camp.getTargetTown(), failedCamps);
			// Remove the SiegeCamp.
			SiegeController.removeSiegeCamp(camp);
		}
	}
	
	/**
	 * Does this {@link Town} have a failed {@link SiegeCamp} from the given
	 * siegeCandidate town?
	 * 
	 * Evaluates a town's metadata for failed SiegeCamps, creating new metadata
	 * during the evaluation (clearing out any out-dated entries as it goes.)
	 * 
	 * @param town           {@link Town} to check for failed SiegeCamp metas.
	 * @param siegeCandidate {@link Town} which is trying to start a Siege via a
	 *                       SiegeCamp.
	 */
	public static boolean hasFailedCamp(Town town, Town siegeCandidate) {
		String failedSiegeCamps = TownMetaDataController.getFailedSiegeCampList(town);
		// No metadata, so no failed camps.
		if (failedSiegeCamps == null || failedSiegeCamps.isEmpty())
			return false;
		boolean hasFailedCamp = false;
		List<String> validFailedSiegeCampList = new ArrayList<>();
		// meta data is stored like so: townUUID:time,townUUID:time,townUUID:time 
		String[] failedCamps = failedSiegeCamps.split(",");
		for (String campString : failedCamps) {
			String[] UUIDAndTime = campString.split(":");
			UUID uuid = getUUID(UUIDAndTime[0]);
			long time = Long.parseLong(UUIDAndTime[1]);
			// This campString is not expired, add it to the validFailedSiegeCampList and check if our siegeCandidate is here.
			if (time > System.currentTimeMillis()) {
				validFailedSiegeCampList.add(campString);
				if (uuid.equals(siegeCandidate.getUUID()))
					hasFailedCamp = true;
			}
		}

		// Set the meta if we dont have an empty list, otherwise remove the now expired meta.
		if (validFailedSiegeCampList.size() > 0)
			TownMetaDataController.setFailedCampSiegeList(town, StringMgmt.join(validFailedSiegeCampList, ","));
		else 
			TownMetaDataController.removeFailedCampSiegeList(town);

		// Return true if we found siegeCandidate in the metadata at any point.
		return hasFailedCamp;
	}

	// A bug in SiegeWar 1.2.0 and earlier resulted in the uuid's being prefixed
	// with null, making SiegeWar unable to load.
	private static UUID getUUID(String string) {
		String uuidString = string;
		if (uuidString.startsWith("null"))
			uuidString = uuidString.replaceFirst("null", "");
		UUID uuid = UUID.fromString(uuidString);
		return uuid;
	}
}
