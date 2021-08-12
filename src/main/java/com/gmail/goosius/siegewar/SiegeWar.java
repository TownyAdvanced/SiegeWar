package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import com.gmail.goosius.siegewar.settings.Settings;
import com.gmail.goosius.siegewar.tasks.DynmapTask;
import com.palmergames.bukkit.util.Version;
import com.gmail.goosius.siegewar.command.SiegeWarAdminCommand;
import com.gmail.goosius.siegewar.command.SiegeWarCommand;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.listeners.SiegeWarActionListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarBukkitEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarNationEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarPlotEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarSafeModeListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownyEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarCannonsListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownyDynmapListener;
import com.gmail.goosius.siegewar.listeners.SiegeWar_0_97_0_14_Listener;

import java.io.File;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	public static String prefix = "[SiegeWar] ";
	private static Version requiredTownyVersion = Version.fromString("0.97.0.0");
	private final static SiegeHUDManager SiegeHudManager = new SiegeHUDManager(plugin);
	private static boolean siegeWarPluginError = false;
	private static boolean cannonsPluginIntegrationEnabled = false;
	private static boolean townyDynmapPluginIntegrationEnabled = false;

	public static SiegeWar getSiegeWar() {
		return plugin;
	}

	public File getSiegeWarJarFile() {
		return getFile();
	}

	public static SiegeHUDManager getSiegeHUDManager() {
		return SiegeHudManager;
	}
	
    @Override
    public void onEnable() {
    	
    	plugin = this;
    	
    	printSickASCIIArt();
    	
        if (!townyVersionCheck(getTownyVersion())) {
            severe("Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
            siegeWarPluginError = true;
        } else {
            info("Towny version " + getTownyVersion() + " found.");
        }
        
        if (!Settings.loadSettingsAndLang())
        	siegeWarPluginError = true;

        registerCommands();
        
        if (Bukkit.getPluginManager().getPlugin("Towny").isEnabled()) {
        	if(!SiegeController.loadAll())
        		siegeWarPluginError = true;
        	if(!TownOccupationController.loadAll())
        		siegeWarPluginError = true;
		}

		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. Dynmap integration disabled.");
		} else {
			Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
			if (dynmap != null) {
				info("SiegeWar found Dynmap plugin, enabling Dynmap support.");
				DynmapTask.setupDynmapAPI((DynmapAPI) dynmap);
				townyDynmapPluginIntegrationEnabled = true;
			} else {
				info("Dynmap plugin not found.");
			}
		}

		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. Cannons integration disabled.");
		} else {
			Plugin cannons = Bukkit.getPluginManager().getPlugin("Cannons");
			if (cannons != null) {
				if (SiegeWarSettings.isCannonsIntegrationEnabled()) {
					info("SiegeWar found Cannons plugin, enabling Cannons support.");
					info("Cannons support enabled.");
					cannonsPluginIntegrationEnabled = true;
				}
			} else {
				info("Cannons plugin not found.");
			}
		}

		registerListeners();

		if(siegeWarPluginError) {
			severe("SiegeWar did not load successfully, and is now in safe mode.");
		} else {
			info("SiegeWar loaded successfully.");
		}
    }
    
    @Override
    public void onDisable() {
    	DynmapTask.endDynmapTask();
    	info("Shutting down...");
    }

	public String getVersion() {
		return this.getDescription().getVersion();
	}
	
    private boolean townyVersionCheck(String version) {
        return Version.fromString(version).compareTo(requiredTownyVersion) >= 0;
    }

	 // Checks if the current towny version matches the given required version
    private boolean doesCurrentTownyVersionMatchGivenTownyVersion(String givenTownyVersionString) {
		Version currentTownyVersion = Version.fromString(getTownyVersion());		
		Version givenTownyVersion = Version.fromString(givenTownyVersionString);    
        return currentTownyVersion.compareTo(givenTownyVersion) >= 0;
    }

    private String getTownyVersion() {
        return Bukkit.getPluginManager().getPlugin("Towny").getDescription().getVersion();
    }
	
	private void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		if (siegeWarPluginError)
			pm.registerEvents(new SiegeWarSafeModeListener(), this);
		else {
			pm.registerEvents(new SiegeWarActionListener(this), this);
			pm.registerEvents(new SiegeWarBukkitEventListener(this), this);		
			pm.registerEvents(new SiegeWarTownyEventListener(this), this);
			pm.registerEvents(new SiegeWarNationEventListener(this), this);
			pm.registerEvents(new SiegeWarTownEventListener(this), this);
			pm.registerEvents(new SiegeWarPlotEventListener(this), this);
			if(townyDynmapPluginIntegrationEnabled)
				pm.registerEvents(new SiegeWarTownyDynmapListener(this), this);
			if(cannonsPluginIntegrationEnabled)
				pm.registerEvents(new SiegeWarCannonsListener(this), this);
			if(doesCurrentTownyVersionMatchGivenTownyVersion( "0.97.0.14"))
				pm.registerEvents(new SiegeWar_0_97_0_14_Listener(this), this);				
		}
	}

	private void registerCommands() {
		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. SiegeWar commands not registered");
		} else {
			getCommand("siegewar").setExecutor(new SiegeWarCommand());
			getCommand("siegewaradmin").setExecutor(new SiegeWarAdminCommand());
		}
	}

	private void printSickASCIIArt() {
		Bukkit.getLogger().info("    _________.__                      ");
		Bukkit.getLogger().info("   /   _____/|__| ____   ____   ____  ");
		Bukkit.getLogger().info("   \\_____  \\ |  |/ __ \\ / ___\\_/ __ \\ ");
		Bukkit.getLogger().info("   /        \\|  \\  ___// /_/  >  ___/ ");
		Bukkit.getLogger().info("  /_______  /|__|\\___  >___  / \\___  >");
		Bukkit.getLogger().info("          \\/         \\/_____/      \\/ ");
		Bukkit.getLogger().info("       __      __                        ");
		Bukkit.getLogger().info("      /  \\    /  \\_____ _______          ");
		Bukkit.getLogger().info("      \\   \\/\\/   /\\__  \\\\_  __ \\         ");
		Bukkit.getLogger().info("       \\        /  / __ \\|  | \\/         ");
		Bukkit.getLogger().info("        \\__/\\  /  (____  /__|            ");
		Bukkit.getLogger().info("             \\/        \\/                ");
		Bukkit.getLogger().info("          By Goosius & LlmDl          ");
		Bukkit.getLogger().info("                                      ");
	}

	public static boolean getCannonsPluginIntegrationEnabled() {
		return cannonsPluginIntegrationEnabled;
	}
	
	public static boolean isError() {
		return siegeWarPluginError;
	}
	
	public static void info(String msg) {
		plugin.getLogger().info(msg);
	}
	
	public static void severe(String msg) {
		plugin.getLogger().severe(msg);
	}
}
