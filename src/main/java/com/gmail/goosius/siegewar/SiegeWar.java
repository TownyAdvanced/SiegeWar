package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.goosius.siegewar.settings.Settings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.util.Version;
import com.gmail.goosius.siegewar.command.SiegeWarAdminCommand;
import com.gmail.goosius.siegewar.command.SiegeWarCommand;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.integration.cannons.CannonsIntegration;
import com.gmail.goosius.siegewar.integration.dynmap.DynmapIntegration;
import com.gmail.goosius.siegewar.listeners.SiegeWarActionListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarBukkitEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarNationEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarPlotEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarSafeModeListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownyEventListener;

import java.io.File;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	private final Version requiredTownyVersion = Version.fromString("0.97.2.0");
	private static final SiegeHUDManager siegeHUDManager = new SiegeHUDManager();

	private static boolean siegeWarPluginError = false;
	private CannonsIntegration cannonsIntegration;

	public static SiegeWar getSiegeWar() {
		return plugin;
	}

	public File getSiegeWarJarFile() {
		return getFile();
	}

	public static SiegeHUDManager getSiegeHUDManager() {
		return siegeHUDManager;
	}
	
    @Override
    public void onEnable() {
    	
    	plugin = this;
    	
    	printSickASCIIArt();
    	
        if (!townyVersionCheck(getTownyVersion())) {
            severe("Towny version does not meet required minimum version: " + requiredTownyVersion);
            siegeWarPluginError = true;
        } else {
            info("Towny version " + getTownyVersion() + " found.");
        }
        
        if (!loadAll()) {
	        siegeWarPluginError = true;
        }

		registerCommands();
		registerListeners();
		checkIntegrations();

		if(siegeWarPluginError) {
			severe("SiegeWar did not load successfully, and is now in safe mode!");
		} else {
			info("SiegeWar loaded successfully.");
		}
    }
    
    @Override
    public void onDisable() {
    	info("Shutting down...");
    }
    
    private boolean loadAll() {
    	return !Towny.getPlugin().isError()
				&& Settings.loadSettingsAndLang()
				&& SiegeController.loadAll()
				&& TownOccupationController.loadAll();
    }

	public String getVersion() {
		return getDescription().getVersion();
	}
	
    private boolean townyVersionCheck(String version) {
        return Version.fromString(version).compareTo(requiredTownyVersion) >= 0;
    }

    private String getTownyVersion() {
        return Towny.getPlugin().getDescription().getVersion();
    }

	private void checkIntegrations() {
		if (siegeWarPluginError) {
			severe("SiegeWar is in safe mode! Plugin integrations disabled.");
		} else if (!SiegeWarSettings.getWarSiegeEnabled()) {
			info("SiegeWar is disabled in config. Plugin integrations disabled.");
		} else {
			if (SiegeWarSettings.isCannonsIntegrationEnabled()) {
				if (getServer().getPluginManager().isPluginEnabled("Cannons")) {
					info("SiegeWar found Cannons plugin, enabling Cannons support.");
					cannonsIntegration = new CannonsIntegration(this);
				} else {
					info("Cannons integration enabled in config, but Cannons plugin not found!");
				}
			}

			if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
				info("SiegeWar found Dynmap plugin, enabling Dynmap support.");
				new DynmapIntegration(this);
			}
		}
	}
	
	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		
		if (siegeWarPluginError)
			pm.registerEvents(new SiegeWarSafeModeListener(this), this);
		else {
			pm.registerEvents(new SiegeWarActionListener(this), this);
			pm.registerEvents(new SiegeWarBukkitEventListener(this), this);		
			pm.registerEvents(new SiegeWarTownyEventListener(this), this);
			pm.registerEvents(new SiegeWarNationEventListener(this), this);
			pm.registerEvents(new SiegeWarTownEventListener(this), this);
			pm.registerEvents(new SiegeWarPlotEventListener(this), this);
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
		return plugin.cannonsIntegration != null;
	}
	
	public boolean isError() {
		return siegeWarPluginError;
	}
	
	public static void info(String msg) {
		plugin.getLogger().info(msg);
	}
	
	public static void severe(String msg) {
		plugin.getLogger().severe(msg);
	}
}
