package com.gmail.goosius.siegewar.utils;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.TimeTools;

public class SiegeCampUtil {

	/**
	 * Evaluates a {@link SiegeCamp}.
	 * @param camp SiegeCamp to evaluate. 
	 */
	public static void evaluateCamp(SiegeCamp camp) {
		
		// Stop if a Siege has begun in the area, shouldn't happen.
		if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(camp.getBannerBlock().getLocation())) {
			SiegeController.removeSiegeCamp(camp);
		// If the time duration of a SiegeCamp has passed, finish the SiegeCamp by evaluating the attacker's points.
		} else if (camp.getEndTime() < System.currentTimeMillis()) {
			finishSiegeCamp(camp);
		// SiegeCamp is ongoing, evaluate players around the SiegeCamp and reschedule the next evaluation.
		} else {
			evaluatePlayers(camp);
			Bukkit.getScheduler().runTaskLater(SiegeWar.getSiegeWar(), ()-> evaluateCamp(camp), 20*60);
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
				|| !camp.getAttacker().hasResident(res.getName()))
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
			camp.setAttackerPoints(camp.getAttackerPoints() + 7);
			break;
		}
	}

	/**
	 * Finish a SiegeCamp and determine whether the SiegeCamp was successful.
	 * Penalize failed SiegeCamps by making the attacking Town unable to begin another SiegeCamp for a time period.
	 * @param camp {@link SiegeCamp} to finish.
	 */
	private static void finishSiegeCamp(SiegeCamp camp) {
		// Attackers scored enough points to start the Siege in ernest.
		if (camp.getAttackerPoints() >= 50) {
			camp.startSiege();
		// Attackers were thwarted, they are penalized with a cooldown on making another SiegeCamp on this town.
		} else {
			Messaging.sendErrorMsg(camp.getPlayer(), "Your Siege Camp failed to score enough points, you may not attempt another siege for 12 hours.");
			
			String failedCamps = TownMetaDataController.getFailedSiegeCampList(camp.getTargetTown());
			// No metadata, start a new failedCamps string.
			if (failedCamps == null)
				failedCamps = camp.getTargetTown().getUUID() + ":" + System.currentTimeMillis();
			// This target town already had at least one failed siegecamp.
			else 
				failedCamps += "," + camp.getTargetTown().getUUID() + ":" + System.currentTimeMillis() + TimeTools.getMillis("12h");

			// Set the metadata on the target town.
			TownMetaDataController.setFailedCampSiegeList(camp.getTargetTown(), failedCamps);
		}
	}
	
	/**
	 * Does this {@link Town} a failed {@link SiegeCamp} from the given siegeCandidate town?
	 * 
	 * Evaluates a town's metadata for failed SiegeCamps, creating new metadata during
	 * the evaluation (clearing out any out-dated entries as it goes.)
	 * 
	 * @param town {@link Town} to check for failed SiegeCamp metas.
	 * @param siegeCandidate {@link Town} which is trying to start a Siege via a SiegeCamp.
	 */
	public static boolean hasFailedCamp(Town town, Town siegeCandidate) {
		String failedSiegeCamps = TownMetaDataController.getFailedSiegeCampList(town);
		// No metadata, so no failed camps.
		if (failedSiegeCamps == null)
			return false;
		boolean hasFailedCamp = false;
		String newFailedSiegeCampsString = null;
		// meta data is stored like so: townUUID:time,townUUID:time,townUUID:time 
		String[] failedCamps = failedSiegeCamps.split(",");
		for (String campString : failedCamps) {
			String[] UUIDAndTime = campString.split(":");
			UUID uuid = UUID.fromString(UUIDAndTime[0]);
			long time = Long.parseLong(UUIDAndTime[1]);
			// This campString is not expired, add it to the newFailedSiegeCampsString and check if our siegeCandidate is here. 
			if (time > System.currentTimeMillis()) {
				newFailedSiegeCampsString += (newFailedSiegeCampsString != null ? "," : "") + uuid + ":" + time;
				if (uuid.equals(siegeCandidate.getUUID()))
					hasFailedCamp = true;
			}
		}

		// Set the meta if we dont have an empty list, otherwise remove the now expired meta.
		if (newFailedSiegeCampsString != null)
			TownMetaDataController.setFailedCampSiegeList(town, newFailedSiegeCampsString);
		else 
			TownMetaDataController.removeFailedCampSiegeList(town);
			
		// Return true if we found siegeCandidate in the metadata at any point.
		return hasFailedCamp;
	}
}
