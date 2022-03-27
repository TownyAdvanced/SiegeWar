package com.gmail.goosius.siegewar.utils;
import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.Map;
import java.util.HashMap;

public class SiegeWarTeleportUtil {

    private static Map<Player, AbstractHorse> scheduledHorseTeleports = new HashMap<>();

    /**
     * Teleport the player's horse, if there is one
     * @param event player teleport event
     */
    public static void scheduleMountTeleport(PlayerTeleportEvent event) {
        if(SiegeWarSettings.isTeleportMountWithPlayer() && scheduledHorseTeleports.containsKey(event.getPlayer())) {
            AbstractHorse horse = scheduledHorseTeleports.get(event.getPlayer());
            //Protect horse until it rejoins player
            double preTeleportAbsorbtion = horse.getAbsorptionAmount();
            horse.setAbsorptionAmount(999999);
            horse.setInvisible(true);
            //Schedule Teleport
            Towny.getPlugin().getServer().getScheduler().runTaskLater(Towny.getPlugin(), () -> {
                //Teleport Mount to wherever the player is
                horse.teleport(event.getPlayer());
                //Remove from map (allowing us to mount)
                scheduledHorseTeleports.remove(event.getPlayer());
                //Mount player on horse
                horse.addPassenger(event.getPlayer());
                //Remove horse health protection
                horse.setAbsorptionAmount(preTeleportAbsorbtion);
                horse.setInvisible(false);
            },100);
        }
    }

    public static void registerPlayerMount(Player player, AbstractHorse mount) {
        scheduledHorseTeleports.put(player, mount);
    }

    public static void deregisterPlayerMount(Player player) {
        scheduledHorseTeleports.remove(player);
    }

    public static boolean isHorseTeleportScheduled(AbstractHorse horse) {
        return scheduledHorseTeleports.containsValue(horse);
    }

    public static boolean isPlayerTeleportBlocked(PlayerTeleportEvent event) {
		 // Don't stop admins/ops. towny.admin.spawn is part of towny.admin.
		if (event.getPlayer().hasPermission("towny.admin.spawn") || event.getPlayer().isOp())
			return false;

        // Block most teleports into the siegezone
        if(SiegeWarSettings.getWarSiegeNonResidentSpawnIntoSiegeZonesOrBesiegedTownsDisabled()) {
            if (TownyAPI.getInstance().isWilderness(event.getTo())) { // The teleport destination is in the wilderness.
                
                //Player cannot TP into siegezone wilderness
                return SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo());
                
            } else {
                Town destinationTown = TownyAPI.getInstance().getTown(event.getTo());
                Resident resident = TownyUniverse.getInstance().getResident(event.getPlayer().getUniqueId());
                
                //Player can always TP to their own town.
                if (destinationTown.hasResident(resident))
                return false;
                
                //Player cannot TP to a town which is besieged.
                if(SiegeController.hasActiveSiege(destinationTown))
                return true;
                
                //Player cannot TP to an in-town location which is in a Siege-Zone
                if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(event.getTo()))
                return true;
            }
        }
        //TP not blocked
        return false;
    }
}


