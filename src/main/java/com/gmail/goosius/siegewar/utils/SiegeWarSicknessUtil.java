package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.TimeTools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class SiegeWarSicknessUtil {

    /**
     * This method punishes Neutral-town players who are in siege-zones
     *
     * If a player is in a Neutral town, they get minor war sickness
     * If a player is not in a Neutral town,they get major war sickness
     */
    public static void punishPeacefulPlayersInActiveSiegeZones() {
        for(final Player player: Bukkit.getOnlinePlayers()) {
            //Don't apply to OP's or towny admins
            if(player.isOp() || TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(player))
                continue;

            //Dont apply if player has the immunity perm
            if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_IMMUNE_TO_WAR_NAUSEA.getNode()))
                continue;

            //Don't apply to non-neutral players
            Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
            if(!resident.hasTown() || !resident.getTownOrNull().isNeutral())
                continue;

            //In SiegeZone...
            if(SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {

                Town townAtPlayerLocation = TownyAPI.getInstance().getTown(player.getLocation());
                if(townAtPlayerLocation != null && townAtPlayerLocation.isNeutral()) {
                    //In Peaceful town, special war sickness
                    givePlayerSpecialWarSicknessNow(player);

                } else {
                    //Otherwise full war sickness
                    int warningTimeSeconds = SiegeWarSettings.getPeacefulTownsSicknessWarningDurationSeconds();
                    givePlayerFullWarSicknessWithWarning(
                        player,
                        resident,
                        warningTimeSeconds,
                        Translatable.of("msg_war_siege_peaceful_player_warned_for_being_in_siegezone", warningTimeSeconds),
                        Translatable.of("msg_war_siege_peaceful_player_punished_for_being_in_siegezone"));
                }
            }
        }
    }

    /**
     * Punish any players who are inside siegezones, but not official participants
     *
     * Special war sickness if they are in their own town
     * Full war sickness otherwise
     */
    public static void punishNonSiegeParticipantsInSiegeZones() {
        for (Player player : Bukkit.getOnlinePlayers()) {
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
			        givePlayerSpecialWarSicknessNow(player);
			    } else {
			        int warningTimeSeconds = SiegeWarSettings.getSicknessWarningTimeInSeconds();
			        givePlayerFullWarSicknessWithWarning(
			            player,
			            resident,
			            warningTimeSeconds,
			            Translatable.of("msg_you_will_get_sickness", warningTimeSeconds),
			            Translatable.of("msg_you_received_war_sickness"));
			    }
			}
        }
    }

    /**
     * Punish any players who are inside siegezones, but over their siege attendance limit for the day
     *
     * Special war sickness if they are in their own town
     * Full war sickness otherwise
     */
    public static void punishPlayersInSiegeZonesOverSiegeAttendanceLimit() {
        for(Player player: Bukkit.getOnlinePlayers()) {
            // Players immune to war nausea won't be punished
            if (player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_IMMUNE_TO_WAR_NAUSEA.getNode()))
                continue;

            //Continue if not in a siege zone
            Location location = player.getLocation();
            if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(location))
                continue;

            //Continue if player is still within their attendance limit
            Resident resident = TownyAPI.getInstance().getResident(player);
            if(!SiegeWarBattleSessionUtil.hasResidentExceededTheirSiegeAttendanceLimit(resident))
                continue;

            //Give player war sickness
            if (isInOwnClaims(resident)) {
                givePlayerSpecialWarSicknessNow(player);
            } else {
                int warningTimeSeconds = SiegeWarSettings.getSiegeAttendanceLimiterSicknessWarningDurationSeconds();
                givePlayerFullWarSicknessWithWarning(
                    player,
                    resident,
                    warningTimeSeconds,
                    Translatable.of(
                        "msg_battle_session_attendance_limit_exceeded_warning",
                        SiegeWarSettings.getSiegeAttendanceLimiterBattleSessions(),
                        SiegeWarBattleSessionUtil.getFormattedTimeUntilPlayerBattleSessionLimitExpires(resident), 
                        warningTimeSeconds),
                    Translatable.of(
                        "msg_battle_session_attendance_limit_exceeded_punish",
                        SiegeWarSettings.getSiegeAttendanceLimiterBattleSessions(),
                        SiegeWarBattleSessionUtil.getFormattedTimeUntilPlayerBattleSessionLimitExpires(resident))); 
            }
        }
    }

    /**
     * Give player full war sickness, with a warning beforehand
     *
     * @param player player
     * @param resident resident
     * @param warningDurationInSeconds warning duration in seconds
     * @param warningTranslatable warning message
     * @param punishmentTranslatable punishment message
     */
    private static void givePlayerFullWarSicknessWithWarning(
            Player player,
            Resident resident,
            int warningDurationInSeconds,
            Translatable warningTranslatable,
            Translatable punishmentTranslatable) {

        if (warningDurationInSeconds >= 1) {
            Messaging.sendMsg(player, warningTranslatable);
        }
        Towny.getPlugin().getServer().getScheduler().runTaskLater(Towny.getPlugin(), () -> {
            if (SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
                if (isInOwnClaims(resident)) {
                    //In own claims
                    givePlayerSpecialWarSicknessNow(player);
                } else {
			        //Still in forbidden siege zone area
                    Messaging.sendMsg(player, punishmentTranslatable);
                    givePlayerFullWarSicknessNow(player);
                }
            }
        }, warningDurationInSeconds * 20);
    }

    /**
     * Give player full war sickness effects now
     * @param player the player
     */
    private static void givePlayerFullWarSicknessNow(Player player) {
        int effectDurationTicks = (int)(TimeTools.convertToTicks(TownySettings.getShortInterval() + 5));
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.CONFUSION, effectDurationTicks, 4));
        potionEffects.add(new PotionEffect(PotionEffectType.POISON, effectDurationTicks, 4));
        potionEffects.add(new PotionEffect(PotionEffectType.WEAKNESS, effectDurationTicks, 4));
        potionEffects.add(new PotionEffect(PotionEffectType.SLOW, effectDurationTicks, 2));
        potionEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING, effectDurationTicks, 2));
        player.addPotionEffects(potionEffects);
        player.setHealth(1);
    }

    /**
     * Give player special war sickness effects now
     * @param player the player
     */
    private static void givePlayerSpecialWarSicknessNow(Player player) {
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

        SiegeSide siegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(player, TownyAPI.getInstance().getResidentTownOrNull(resident), siege);

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
