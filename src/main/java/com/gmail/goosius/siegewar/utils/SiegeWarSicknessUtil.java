package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.util.TimeTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
import java.util.List;

public class SiegeWarSicknessUtil {

    public static Set<Player> playersWithFullWarSickness = new HashSet<>();

    /**
     * Evaluate all war sickness:
     * - Unofficial Siege-Participant effects
     */
    public static void evaluateWarSickness() throws TownyException {
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

            Nation attackerNation = TownyAPI.getInstance().getNation(siege.getAttackerName());
            Nation defenderNation = TownyAPI.getInstance().getNation(siege.getDefenderName());

            boolean isAllyOfAttacker = attackerNation != null && attackerNation.getAllies().contains(resident.getNation());
            boolean isAllyOfDefender = defenderNation != null && defenderNation.getAllies().contains(resident.getNation());

            if (nonOfficialLimiterEnabled && !isOfficialSiegeParticipant(player, resident, siege) ) {
                //Give war sickness to players who are not official participants in the SiegeZone
                if (TownyAPI.getInstance().isWilderness(location)) {
                    //In Wilderness - Full war sickness
                    int warningDurationInSeconds = SiegeWarSettings.getNonResidentSicknessWarningTimeSeconds();
                    givePlayerFullWarSicknessWithWarning(
                        player,
                        resident,
                        siege,
                        warningDurationInSeconds,
                        Translatable.of("msg_you_will_get_sickness", warningDurationInSeconds),
                        Translatable.of("msg_you_received_war_sickness"));
                } else {
                    //In a town - Special war sickness
                    givePlayerSpecialWarSicknessNow(player);
                }
            } else if(isAllyOfAttacker && isAllyOfDefender){
                //Give war sickness to players who are allies to both Attacker and Defender.
                if (TownyAPI.getInstance().isWilderness(location)) {
                    int warningDurationInSeconds = SiegeWarSettings.getNonResidentSicknessWarningTimeSeconds();
                    givePlayerFullWarSicknessWithWarning(
                            player,
                            resident,
                            siege,
                            warningDurationInSeconds,
                            Translatable.of("msg_you_will_get_sickness", warningDurationInSeconds),
                            Translatable.of("msg_you_received_war_sickness"));
                    Objects.requireNonNull(resident.getPlayer()).sendMessage(ChatColor.RED + "You have been given war sickness because you have an Alliance with BOTH the Attackers & Defenders!");
                } else {
                    //In a town - Special war sickness
                    givePlayerSpecialWarSicknessNow(player);
                    Objects.requireNonNull(resident.getPlayer()).sendMessage(ChatColor.RED + "You have been given war sickness because you have an Alliance with BOTH the Attackers & Defenders!");
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

        SiegeWar.getSiegeWar().getScheduler().runLater(player, () -> {
            if (SiegeWarDistanceUtil.isInSiegeZone(player, siege)) {
                if (SiegeWarDistanceUtil.isInANonBesiegedTown(player.getLocation())) {
                    //Special War Sickness
                    givePlayerSpecialWarSicknessNow(player);
                    playersWithFullWarSickness.remove(player);
                } else {
                    //Full war sickness
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
        SiegeWar.getSiegeWar().getScheduler().run(player, ()->
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, effectDurationTicks, 4)));
    }

    public static boolean isOfficialSiegeParticipant(Player player, Resident resident, Siege siege) {
        return SiegeSide.getPlayerSiegeSide(siege, player) != SiegeSide.NOBODY;
    }

}
