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

	public static void evaluateCamp(SiegeCamp camp) {
		if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(camp.getBannerBlock().getLocation())) {
			SiegeController.removeSiegeCamp(camp);
		} else if (camp.getEndTime() < System.currentTimeMillis()) {
			finishSiegeCamp(camp);
		} else {
			evaluatePlayers(camp);
			Bukkit.getScheduler().runTaskLater(SiegeWar.getSiegeWar(), ()-> evaluateCamp(camp), 20*60);
		}	
	}

	private static void evaluatePlayers(SiegeCamp camp) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Resident res = TownyAPI.getInstance().getResident(player);
			if (res == null 
				|| !res.hasTown()
				|| !camp.getAttacker().hasResident(res.getName()))
				continue;
			
			if (!player.isOnline()
				|| player.isDead()
				|| !player.getWorld().equals(camp.getBannerBlock().getWorld())
				|| player.isGliding() 
				|| player.isFlying() 
				|| player.getGameMode().equals(GameMode.SPECTATOR))
				continue;
			
			if (!TownyAPI.getInstance().isWilderness(player.getLocation()) || !SiegeWarDistanceUtil.isInSiegeCampZone(player.getLocation(), camp))
				continue;
			
			// The attackers are present, give 7 points and break out of the loop.
			camp.setAttackerPoints(camp.getAttackerPoints() + 7);
			break;
		}
	}

	private static void finishSiegeCamp(SiegeCamp camp) {
		if (camp.getAttackerPoints() >= 50) {
			camp.startSiege();
		} else {
			Messaging.sendErrorMsg(camp.getPlayer(), "Your Siege Camp failed to score enough points, you may not attempt another siege for 12 hours.");
			
			String failedCamps = TownMetaDataController.getFailedSiegeCampList(camp.getTargetTown());
			if (failedCamps == null)
				failedCamps = camp.getTargetTown().getUUID() + ":" + System.currentTimeMillis();
			else 
				failedCamps += "," + camp.getTargetTown().getUUID() + ":" + System.currentTimeMillis() + TimeTools.getMillis("12h");

			TownMetaDataController.setFailedCampSiegeList(camp.getTargetTown(), failedCamps);
		}
	}
	
	public static boolean hasFailedCamp(Town town, Town siegeCandidate) {
		String failedSiegeCamps = TownMetaDataController.getFailedSiegeCampList(town); 
		if (failedSiegeCamps == null)
			return false;
		boolean hasFailedCamp = false;
		String newFailedSiegeCampsString = null;
		String[] failedCamps = failedSiegeCamps.split(",");
		for (String campString : failedCamps) {
			String[] keyValue = campString.split(":");
			UUID uuid = UUID.fromString(keyValue[0]);
			long time = Long.parseLong(keyValue[1]);
			if (time > System.currentTimeMillis()) {
				newFailedSiegeCampsString += (newFailedSiegeCampsString != null ? "," : "") + uuid + ":" + time;
				if (uuid.equals(siegeCandidate.getUUID()))
					hasFailedCamp = true;
			}
		}

		if (newFailedSiegeCampsString != null)
			TownMetaDataController.setFailedCampSiegeList(town, newFailedSiegeCampsString);
		else 
			TownMetaDataController.removeFailedCampSiegeList(town);
			
		return hasFailedCamp;
	}
}
