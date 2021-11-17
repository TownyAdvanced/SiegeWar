package com.gmail.goosius.siegewar.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.SiegeCamp;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;

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
			Messaging.sendErrorMsg(camp.getPlayer(), "Your Siege Camp failed to score enough points");
		}
		
	}
}
