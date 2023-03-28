package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.enums.GlassColor;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Util class for everything fancy.
 * 
 * @author Warriorrrr
 */
public class CosmeticUtil {
	public static void evaluateBeacons() {
		for (Player player : Bukkit.getOnlinePlayers()) {
            for (Siege siege : SiegeController.getSieges()) {
				if (!siege.getStatus().isActive())
					continue;
				
                if (SiegeWarDistanceUtil.isInSiegeZone((Entity) player, siege))
                    evaluateBeacon(player, siege);
            }
        }
	}

	public static void removeFakeBeacons(Siege siege) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (SiegeWarDistanceUtil.isInSiegeZone((Entity) player, siege))
                removeFakeBeacon(player, siege.getFlagLocation());
		}
	}

	public static void removeFakeBeacons(Player player) {
		for (Siege siege : SiegeController.getSieges()) {
			if (SiegeWarDistanceUtil.isInSiegeZone((Entity) player, siege))
				removeFakeBeacon(player, siege.getFlagLocation());
		}
	}

    public static void evaluateBeacon(Player player, Siege siege) {
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (SiegeWarSettings.getBeaconsEnabled() && !ResidentMetaDataController.getBeaconsDisabled(resident))
			createFakeBeacon(player, siege.getFlagLocation(), getGlassColor(player, siege));
    }

	/**
	 * Creates a fake beacon at the specified location. Only the specified player will be able to see it.
	 * @param loc The location to create the beacon at.
	 * @param player The player to show the beacon for.
     * @param glassColor The glass block that will be above the beacon, to change the color.
	 */
	public static void createFakeBeacon(Player player, Location loc, Material glassColor) {
		player.sendBlockChange(loc.clone().subtract(0, 1, 0), Bukkit.createBlockData(glassColor));
		player.sendBlockChange(loc.clone().subtract(0, 2, 0), Bukkit.createBlockData(Material.BEACON));

		int[][] ironBlockLocations = {{1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, 0}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}};
		for (int i = 0; i < 9; i++) {
			player.sendBlockChange(loc.clone().add(ironBlockLocations[i][0], -3, ironBlockLocations[i][1]), Bukkit.createBlockData(Material.IRON_BLOCK));
		}

		// Set any non-transparent blocks above the banner to glass.
		for (int i = loc.getBlockY(); i < loc.getWorld().getMaxHeight(); i++) {
			Block block = loc.getWorld().getBlockAt(loc.getBlockX(), i, loc.getBlockZ());
			if (block.getType().isBlock() && block.getType().isOccluding())
				player.sendBlockChange(block.getLocation(), Bukkit.createBlockData(Material.GLASS));
		}
	}

	public static void removeFakeBeacon(Player player, Location loc) {
		player.sendBlockChange(loc.clone().subtract(0, 1, 0), Bukkit.createBlockData(loc.clone().subtract(0, 1, 0).getBlock().getType()));
		player.sendBlockChange(loc.clone().subtract(0, 2, 0), Bukkit.createBlockData(loc.clone().subtract(0, 2, 0).getBlock().getType()));

		int[][] ironBlockLocations = {{1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, 0}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}};
		for (int i = 0; i < 9; i++) {
			player.sendBlockChange(loc.clone().add(ironBlockLocations[i][0], -3, ironBlockLocations[i][1]), Bukkit.createBlockData(loc.clone().add(ironBlockLocations[i][0], -3, ironBlockLocations[i][1]).getBlock().getType()));
		}

		for (int i = loc.getBlockY(); i < loc.getWorld().getMaxHeight(); i++) {
			Block block = loc.getWorld().getBlockAt(loc.getBlockX(), i, loc.getBlockZ());
			if (block.getType().isBlock() && block.getType().isOccluding())
				player.sendBlockChange(block.getLocation(), Bukkit.createBlockData(block.getType()));
		}
	}
	
	/**
	 * @param player The player to get the glass color for.
	 * @param siege The siege
	 * @return The material for the colour of glass.
	 */
    public static Material getGlassColor(Player player, Siege siege) {
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (siege.getBannerControlSessions().containsKey(player))
			return getCaptureColor();

		SiegeSide siegeSide = SiegeSide.NOBODY;
		if(resident.hasTown())
			siegeSide = SiegeWarAllegianceUtil.calculateCandidateSiegePlayerSide(player, TownyAPI.getInstance().getResidentTownOrNull(resident), siege);


		if (siegeSide == SiegeSide.NOBODY || siege.getBannerControllingSide() == SiegeSide.NOBODY)
			return Material.GLASS;
		
		if (siege.getBannerControllingSide() != siegeSide)
			return getEnemyColor();
		else
			return getCapturedColor();
    }
    
    /**
     * Spawns a firework at the specified location.
     * 
     * @param location The location to spawn the firework at.
     * @param primaryColor The primary color.
     * @param fadeColor The fade color.
     * @param instantDetonate Whether the firework should detonate instantly.
     */
    public static void spawnFirework(Location location, Color primaryColor, Color fadeColor, boolean instantDetonate) {
        location.getWorld().spawn(location, Firework.class, fw -> {
			FireworkMeta fireworkMeta = (FireworkMeta) fw.getFireworkMeta();
			fireworkMeta.addEffects(FireworkEffect.builder().withColor(primaryColor).withFade(fadeColor).build());
			fw.setFireworkMeta(fireworkMeta);
			if (instantDetonate)
				fw.detonate();
		});
    }

    /*
	public static SiegeSide getSiegeSide(Resident resident, Siege siege) {
		if (!resident.hasTown())
			return SiegeSide.NOBODY;
		
		Town town = TownyAPI.getInstance().getResidentTownOrNull(resident);

		if (siege.getTown().hasResident(resident))
			return SiegeSide.DEFENDERS;
		
		if (town.isAlliedWith(siege.getTown()) && town.isAlliedWith(siege.getNation().getCapital()))
			return SiegeSide.NOBODY;
		
		if (town.isAlliedWith(siege.getTown()))
			return SiegeSide.DEFENDERS;
		else if (town.isAlliedWith(siege.getNation().getCapital()))
			return SiegeSide.ATTACKERS;
		else
			return SiegeSide.NOBODY;
	}

     */
	public static Material getCaptureColor() {
		String materialName = SiegeWarSettings.getBeaconCaptureColor();	
		Material material;
		try {
			material = GlassColor.valueOf(materialName.toUpperCase()).getMaterial();
		} catch (IllegalArgumentException e) {
			material = Material.YELLOW_STAINED_GLASS;
		}
		return material;
	}

	public static Material getCapturedColor() {
		String materialName = SiegeWarSettings.getBeaconCapturedColor();
		Material material;
		try {
			material = GlassColor.valueOf(materialName.toUpperCase()).getMaterial();
		} catch (IllegalArgumentException e) {
			material = Material.GREEN_STAINED_GLASS;
		}
		return material;
	}

	public static Material getEnemyColor() {
		String materialName = SiegeWarSettings.getBeaconEnemyColor();
		Material material;
		try {
			material = GlassColor.valueOf(materialName.toUpperCase()).getMaterial();
		} catch (IllegalArgumentException e) {
			material = Material.RED_STAINED_GLASS;
		}
		return material;
	}
}
