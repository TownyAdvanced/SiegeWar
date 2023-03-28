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
import com.palmergames.util.TimeTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SiegeWarSicknessUtil {

    public static Set<Player> playersWithFullWarSickness = new HashSet<>();

    /**
     * Evaluate all war sickness:
     * - Peaceful town effects
     * - Siege Attendance Limiter effects
     * - Unofficial Siege-Participant effects
     */
    public static void evaluateWarSickness() {
        boolean neutralTownsEnabled = SiegeWarSettings.getWarCommonPeacefulTownsEnabled();
        boolean nonOfficialLimiterEnabled = SiegeWarSettings.getPunishingNonSiegeParticipantsInSiegeZone();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();

            // Players immune to war nausea won't be punished
            if (player.isOp() || player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_IMMUNE_TO_WAR_NAUSEA.getNode()))
                continue;

            // check if in a siege zone
            Siege siege = SiegeController.getActiveSiegeAtLocation(location);
            if (siege == null)
                continue;

            Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
            if (resident == null)
                continue;

            if(neutralTownsEnabled && resident.hasTown() && resident.getTownOrNull().isNeutral()) {

                //Give war sickness to neutral town residents in Siege-Zones
                if (isInOwnClaims(resident)) {
                    givePlayerSpecialWarSicknessNow(player);
                } else {
                    givePlayerFullWarSicknessWithWarning(
                        player,
                        resident,
                        siege,
                        SiegeWarSettings.getPeacefulTownsSicknessWarningDurationSeconds(),
                        Translatable.of("msg_war_siege_peaceful_player_warned_for_being_in_siegezone"),
                        Translatable.of("msg_war_siege_peaceful_player_punished_for_being_in_siegezone"));
                }

            } else if (nonOfficialLimiterEnabled && !isSiegeParticipant(player, resident, siege)) {

                //Give war sickness to players who are not official participants in the SiegeZone
                if (isInOwnClaims(resident)) {
                    givePlayerSpecialWarSicknessNow(player);
                } else {
                    int warningDurationInSeconds = SiegeWarSettings.getNonResidentSicknessWarningTimeSeconds();
                    givePlayerFullWarSicknessWithWarning(
                        player,
                        resident,
                        siege,
                        warningDurationInSeconds,
                        Translatable.of("msg_you_will_get_sickness", warningDurationInSeconds),
                        Translatable.of("msg_you_received_war_sickness"));
                }
            }
        }
    }

    /**
     * Give player full war sickness, with a warning beforehand
     *
     * @param player player
     * @param resident resident
     * @param warningDurationInSeconds warning duration in seconds
     * @param siege the siege causing the war sickness
     * @param warningTranslatable warning message
     * @param punishmentTranslatable punishment message
     */
    private static void givePlayerFullWarSicknessWithWarning(
            Player player,
            Resident resident,
            Siege siege,
            int warningDurationInSeconds,
            Translatable warningTranslatable,
            Translatable punishmentTranslatable) {

        if(!playersWithFullWarSickness.contains(player)) {
            //Send warning
            if (warningDurationInSeconds >= 1)
                Messaging.sendMsg(player, warningTranslatable);
            //Mark player as having full war sickness
            playersWithFullWarSickness.add(player);
        }

        Towny.getPlugin().getServer().getScheduler().runTaskLater(Towny.getPlugin(), () -> {
            if (SiegeWarDistanceUtil.isInSiegeZone(player, siege)) {
                if (isInOwnClaims(resident)) {
                    //In own claims
                    givePlayerSpecialWarSicknessNow(player);
                    playersWithFullWarSickness.remove(player);
                } else {
			        //Still in forbidden siege zone area
                    Messaging.sendMsg(player, punishmentTranslatable);
                    givePlayerFullWarSicknessNow(player);
                }
            } else {
                playersWithFullWarSickness.remove(player);
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
