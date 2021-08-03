package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.util.TimeTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

	/**
	 * Freeze battlefield reporters in siegezones,
	 * if they are carrying any non-tools (anything except shovel, axe, pick)
	 */
	public static void freezeUnfreezeBattlefieldReporters() {
	    boolean freezePlayer;
	
		for(Player player: Bukkit.getOnlinePlayers()) {
			if(player.hasPermission(SiegeWarPermissionNodes.SIEGEWAR_SIEGEZONE_CANNOT_CARRY_NON_TOOL_ITEMS.getNode())) {
			    freezePlayer = false;
			    
			    //Check if reporter in in siegezone with forbidden items
			    if(SiegeWarDistanceUtil.isLocationInActiveSiegeZone(player.getLocation())) {
                     for(ItemStack itemStack: player.getInventory().getStorageContents()) {
                        if(!itemStack.getType().toString().endsWith("AXE")
                            && !itemStack.getType().toString().endsWith("SHOVEL")) {
                            //Forbidden item found
                            freezePlayer = true;
                            break;
                        }
                     }
                } 
                
                if(freezePlayer) {
                    if(player.getWalkSpeed() != 0) 
                        player.setWalkSpeed(0f);			        
                } else {
                    if(player.getWalkSpeed() == 0) 
                        player.setWalkSpeed(0.2f);			                            
                }			   
            }
		}
	}
}
