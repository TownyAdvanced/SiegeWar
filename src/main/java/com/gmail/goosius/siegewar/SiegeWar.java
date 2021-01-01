package com.gmail.goosius.siegewar;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.goosius.siegewar.settings.Settings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.util.Version;
import com.gmail.goosius.siegewar.command.SiegeWarAdminCommand;
import com.gmail.goosius.siegewar.command.SiegeWarCommand;
import com.gmail.goosius.siegewar.listeners.SiegeWarActionListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarNationEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownEventListener;

import io.github.townyadvanced.util.JavaUtil;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	private static Version requiredTownyVersion = Version.fromString("0.96.5.11");
	private final SiegeWarActionListener siegeWarActionListener = new SiegeWarActionListener(this);
	private final SiegeWarEventListener siegeWarEventListener = new SiegeWarEventListener(this);
	private final SiegeWarNationEventListener siegeWarNationListener = new SiegeWarNationEventListener(this);
	private final SiegeWarTownEventListener siegeWarTownListener = new SiegeWarTownEventListener(this);
	
	public SiegeWar getSiegeWar() {
		return plugin;
	}
	
    @Override
    public void onEnable() {
        if (!townyVersionCheck(Bukkit.getPluginManager().getPlugin("Towny").getDescription().getVersion())) {
            getLogger().severe("Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
            onDisable();
            return;
        } else {
            getLogger().info("Towny version " + Bukkit.getPluginManager().getPlugin("Towny").getDescription().getVersion() + " found.");
        }
        
        if (!loadSettings())
        	onDisable();

        if (Settings.isUpdating(getVersion()))
        	update();
        
        registerListeners();
        
        registerCommands();
        
        if (Bukkit.getPluginManager().getPlugin("Towny").isEnabled())
        	loadSieges();
    }

	public String getVersion() {
		return this.getDescription().getVersion();
	}

	public String getPrefix() {
		return "["+this.getDescription().getPrefix()+"] ";
	}
	
    private boolean townyVersionCheck(String version) {
        Version ver = Version.fromString(version);

        return ver.compareTo(requiredTownyVersion) >= 0;
    }

	private boolean loadSettings() {
		try {
			Settings.loadConfig(this.getDataFolder().getPath() + File.separator + "config.yml", getVersion());
		} catch (IOException e) {
            e.printStackTrace();
            System.err.println(getPrefix() + "Config.yml failed to load! Disabling!");
            return false;
        }
		System.out.println(getPrefix() + "Config.yml loaded successfully.");

		try {
			Translation.loadLanguage(this.getDataFolder().getPath() + File.separator, "english.yml");
		} catch (IOException e) {
	        e.printStackTrace();
	        System.err.println(getPrefix() + "Language file failed to load! Disabling!");
	        return false;
	    }
		System.out.println(getPrefix() + "Language file loaded successfully.");
		return true;
	}

	private void update() {

		try {
			List<String> changeLog = JavaUtil.readTextFromJar("/ChangeLog.txt");
			boolean display = false;
			System.out.println("------------------------------------");
			System.out.println(getPrefix() + " ChangeLog up until v" + getVersion());
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
		}
		Settings.setLastRunVersion(getVersion());
	}
	
	private void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(siegeWarActionListener, this);
		pm.registerEvents(siegeWarEventListener, this);		
		pm.registerEvents(siegeWarNationListener, this);
		pm.registerEvents(siegeWarTownListener, this);
	}
	
	private void registerCommands() {
		getCommand("siegewar").setExecutor(new SiegeWarCommand());
		getCommand("siegewaradmin").setExecutor(new SiegeWarAdminCommand());
	}
	
	public void loadSieges() {
		SiegeController.clearSieges();
		SiegeController.loadSiegeList();
		SiegeController.loadSieges();
		System.out.println("SiegeWar: " + SiegeController.getSieges().size() + " siege(s) loaded.");
		
	}
}
