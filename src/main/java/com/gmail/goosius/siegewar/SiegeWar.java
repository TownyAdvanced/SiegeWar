package com.gmail.goosius.siegewar;

import java.io.IOException;
import java.util.List;

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
import com.gmail.goosius.siegewar.listeners.SiegeWarActionListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarBukkitEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarNationEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownyEventListener;

import io.github.townyadvanced.util.JavaUtil;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	public static String prefix = "[SiegeWar] ";
	private static Version requiredTownyVersion = Version.fromString("0.96.5.12");
	
	public static SiegeWar getSiegeWar() {
		return plugin;
	}
	
    @Override
    public void onEnable() {
    	
    	plugin = this;
    	
    	printSickASCIIArt();
    	
        if (!townyVersionCheck(getTownyVersion())) {
            System.err.println(prefix + "Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
            System.err.println(prefix + "Shutting down....");
            onDisable();
            return;
        } else {
            System.out.println(prefix + "Towny version " + getTownyVersion() + " found.");
        }
        
        if (!Settings.loadSettingsAndLang()) {
        	System.err.println(SiegeWar.prefix + "Shutting down....");
        	onDisable();
        }

        if (Settings.isUpdating(getVersion()))
        	update();
        
        registerListeners();
        
        registerCommands();
        
        if (Bukkit.getPluginManager().getPlugin("Towny").isEnabled())
        	SiegeController.loadAll();
        
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmap != null) {
        	System.out.println(prefix + "SiegeWar found Dynmap, enabling Dynmap support.");
        	DynmapTask.setDynmapAPI((DynmapAPI) dynmap);
        } else {
        	System.out.println(prefix + "Dynmap not found.");
        }
        
        System.out.println(prefix + "SiegeWar loaded successfully.");
    }
    
    @Override
    public void onDisable() {
    	DynmapTask.endDynmapTask();
    	System.err.println(prefix + "Shutting down....");
    }

	public String getVersion() {
		return this.getDescription().getVersion();
	}
	
    private boolean townyVersionCheck(String version) {
        return Version.fromString(version).compareTo(requiredTownyVersion) >= 0;
    }

    private String getTownyVersion() {
        return Bukkit.getPluginManager().getPlugin("Towny").getDescription().getVersion();
    }

	private void update() {

		try {
			List<String> changeLog = JavaUtil.readTextFromJar("/ChangeLog.txt");
			boolean display = false;
			System.out.println("------------------------------------");
			System.out.println(prefix + " ChangeLog up until v" + getVersion());
			String lastVersion = Settings.getLastRunVersion(getVersion()).split("_")[0];
			for (String line : changeLog) {
				if (line.startsWith(lastVersion)) {
					display = true;
				}
				if (display && line.replaceAll(" ", "").replaceAll("\t", "").length() > 0) {
					System.out.println(line);
				}
			}
			System.out.println("------------------------------------");
		} catch (IOException e) {
			System.err.println("Could not read ChangeLog.txt");
		} catch (NullPointerException e) {
			System.err.println("Could not read ChangeLog.txt");
		}
		Settings.setLastRunVersion(getVersion());
	}
	
	private void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new SiegeWarActionListener(this), this);
		pm.registerEvents(new SiegeWarBukkitEventListener(this), this);		
		pm.registerEvents(new SiegeWarTownyEventListener(this), this);
		pm.registerEvents(new SiegeWarNationEventListener(this), this);
		pm.registerEvents(new SiegeWarTownEventListener(this), this);
	}
	
	private void registerCommands() {
		getCommand("siegewar").setExecutor(new SiegeWarCommand());
		getCommand("siegewaradmin").setExecutor(new SiegeWarAdminCommand());
	}

	private void printSickASCIIArt() {
		System.out.println("    _________.__                      ");
		System.out.println("   /   _____/|__| ____   ____   ____  ");
		System.out.println("   \\_____  \\ |  |/ __ \\ / ___\\_/ __ \\ ");
		System.out.println("   /        \\|  \\  ___// /_/  >  ___/ ");
		System.out.println("  /_______  /|__|\\___  >___  / \\___  >");
		System.out.println("          \\/         \\/_____/      \\/ ");
		System.out.println("       __      __                        ");
		System.out.println("      /  \\    /  \\_____ _______          ");
		System.out.println("      \\   \\/\\/   /\\__  \\\\_  __ \\         ");
		System.out.println("       \\        /  / __ \\|  | \\/         ");
		System.out.println("        \\__/\\  /  (____  /__|            ");
		System.out.println("             \\/        \\/                ");
		System.out.println("          By Goosius & LlmDl          ");
		System.out.println("                                      ");
	}
}
