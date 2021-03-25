package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.util.TimeTools;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class SiegeWarSicknessUtil {

    public static void punishNonSiegeParticipantsInSiegeZone() {

        for (Player player : BukkitTools.getOnlinePlayers()) {
            Location location = player.getLocation();

            // Players immune to war nausea won't be punished
            if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_IMMUNE_TO_WAR_NAUSEA.getNode()))
                continue;

            List<Siege> sieges = SiegeController.getActiveSiegesAt(location);

            // not in a siege zone
            if (sieges.isEmpty())
                continue;

            Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());

            if (resident == null)
                continue;

            boolean allowedInAnyOverlappingSiege = false;
            for (Siege siege : sieges) {
			    if (isSiegeParticipant(player, resident, siege)) {
			        allowedInAnyOverlappingSiege = true;
			        break;
			    }
			}

			if (!allowedInAnyOverlappingSiege) {
			    if (isInOwnClaims(resident)) {
			        punishWithSpecialWarSickness(player);
			    } else {
			        punishWithFullWarSickness(player);
			    }

			}

        }

    }

    public static void punishWithFullWarSickness(Player player) {
        final int effectDurationTicks = (int)(TimeTools.convertToTicks(TownySettings.getShortInterval() + 5));
        if (SiegeWarSettings.getSicknessWarningTimeInTicks() / 20 >= 1) {
            player.sendMessage(Translation.of("plugin_prefix") + Translation.of("msg_you_will_get_sickness",
                    SiegeWarSettings.getSicknessWarningTimeInTicks() / 20));
        }
        Towny.getPlugin().getServer().getScheduler().runTaskLater(Towny.getPlugin(), () -> {
            Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
			List<Siege> sieges = SiegeController.getActiveSiegesAt(player.getLocation());
			boolean allowedInAnyOverlappingSiege = false;
			for (Siege siege : sieges) {
			    if (isSiegeParticipant(player, resident, siege)) {
			        allowedInAnyOverlappingSiege = true;
			        break;
			    }
			}

			if (!allowedInAnyOverlappingSiege && SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
			    // still in siege zone
			    List<PotionEffect> potionEffects = new ArrayList<>();
			    potionEffects.add(new PotionEffect(PotionEffectType.CONFUSION, effectDurationTicks, 4));
			    potionEffects.add(new PotionEffect(PotionEffectType.POISON, effectDurationTicks, 4));
			    potionEffects.add(new PotionEffect(PotionEffectType.WEAKNESS, effectDurationTicks, 4));
			    potionEffects.add(new PotionEffect(PotionEffectType.SLOW, effectDurationTicks, 2));
			    potionEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING, effectDurationTicks, 2));
			    player.addPotionEffects(potionEffects);
			    player.sendMessage(Translation.of("plugin_prefix") + Translation.of("msg_you_received_war_sickness"));
			}
        }, SiegeWarSettings.getSicknessWarningTimeInTicks());
    }

    public static void punishWithSpecialWarSickness(Player player) {
        final int effectDurationTicks = (int)(TimeTools.convertToTicks(TownySettings.getShortInterval() + 5));
        Towny.getPlugin().getServer().getScheduler().runTask(Towny.getPlugin(), new Runnable() {
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, effectDurationTicks, 4));
            }
        });
    }

    public static boolean isSiegeParticipant(Player player, Resident resident, Siege siege) {
        if (!resident.hasTown())
            return false;

        SiegeSide siegeSide;
        try {
            siegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(player, resident.getTown(), siege);
        } catch (NotRegisteredException e) {
            return false;
        }

        return siegeSide != SiegeSide.NOBODY;
    }

    private static boolean isInOwnClaims(Resident resident) {
        Location location = resident.getPlayer().getLocation();
        if (!resident.hasTown())
            return false;

        if (TownyAPI.getInstance().isWilderness(location))
            return false;

        return TownyAPI.getInstance().getTown(location).equals(TownyAPI.getInstance().getResidentTownOrNull(resident));
    }

}
