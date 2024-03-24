package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.listeners.SiegeWarTownyChatEventListener;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.DataCleanupUtil;

import com.gmail.goosius.siegewar.utils.PermsCleanupUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.goosius.siegewar.settings.ConfigNodes;
import com.gmail.goosius.siegewar.settings.Settings;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.config.migration.ConfigMigrator;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.scheduling.TaskScheduler;
import com.palmergames.bukkit.towny.scheduling.impl.BukkitTaskScheduler;
import com.palmergames.bukkit.towny.scheduling.impl.FoliaTaskScheduler;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.util.Version;
import com.gmail.goosius.siegewar.command.SiegeWarAdminCommand;
import com.gmail.goosius.siegewar.command.SiegeWarCommand;
import com.gmail.goosius.siegewar.command.SiegeWarNationSetOccupationTaxAddonCommand;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.integration.dynmap.DynmapIntegration;
import com.gmail.goosius.siegewar.listeners.SiegeWarActionListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarBukkitEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarNationEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarPlotEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarSafeModeListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarSelfListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarStatusScreenListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownEventListener;
import com.gmail.goosius.siegewar.listeners.SiegeWarTownyEventListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	private final String requiredTownyVersion = "0.100.1.0";
	private static final SiegeHUDManager siegeHUDManager = new SiegeHUDManager();
	private final Object scheduler;

	private static boolean siegeWarPluginError = false;
	private static boolean listenersRegistered = false;

	public static SiegeWar getSiegeWar() {
		return plugin;
	}

	public File getSiegeWarJarFile() {
		return getFile();
	}

	public static SiegeHUDManager getSiegeHUDManager() {
		return siegeHUDManager;
	}

	public SiegeWar() {
		plugin = this;
		this.scheduler = setScheduler();
	}

	@Override
    public void onEnable() {
    	
    	printSickASCIIArt();
    	
        if (!townyVersionCheck()) {
            severe("Towny version does not meet required minimum version: " + requiredTownyVersion);
            siegeWarPluginError = true;
        } else {
            info("Towny version " + getTownyVersion() + " found.");
        }
        
        registerAdminCommands();
        handleLegacyConfigs();

		if (!loadAll()) {
			siegeWarPluginError = true;
		}

		listenersRegistered = registerListeners();
		registerPlayerCommands();
		checkIntegrations();
		DataCleanupUtil.cleanupData(siegeWarPluginError, listenersRegistered);
		PermsCleanupUtil.cleanupPerms(siegeWarPluginError);

		//Calculate estimated total money in economy. This will run async.
		SiegeWarMoneyUtil.calculateEstimatedTotalMoneyInEconomy(siegeWarPluginError);

		if(siegeWarPluginError) {
			severe("SiegeWar did not load successfully, and is now in safe mode!");
		} else {
			info("SiegeWar loaded successfully.");
		}
    }
    
    private void handleLegacyConfigs() {
		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. Legacy configs not handled");
			return;
		}

		Path configPath = getDataFolder().toPath().resolve("config.yml");
		if (!Files.exists(configPath))
			return;

		CommentedConfiguration config = new CommentedConfiguration(configPath);
		if (!config.load() || config.getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), "0.0.0.0").equals(getVersion()))
			return;

		Version lastRun = Version.fromString(config.getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), "0.0.0.0"));
		ConfigMigrator earlyMigrator = new ConfigMigrator(getSiegeWar(), config, "config-migration.json", lastRun, true);
		earlyMigrator.migrate();
	}

	@Override
    public void onDisable() {
    	info("Shutting down...");
    }
    
    private boolean loadAll() {
    	return !Towny.getPlugin().isError()
				&& Settings.loadSettingsAndLang()
				&& SiegeController.loadAll();
    }

	public String getVersion() {
		return getDescription().getVersion();
	}
	
    private boolean townyVersionCheck() {
        try {
			return Towny.isTownyVersionSupported(requiredTownyVersion);
		} catch (NoSuchMethodError e) {
			return false;
		}
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
			if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
				info("SiegeWar found Dynmap plugin, enabling Dynmap support.");
				new DynmapIntegration(this);
			}
		}
	}
	
	private boolean registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		if (siegeWarPluginError) {
			pm.registerEvents(new SiegeWarSafeModeListener(this), this);
			return false;
		} else {
			pm.registerEvents(new SiegeWarActionListener(this), this);
			pm.registerEvents(new SiegeWarBukkitEventListener(), this);		
			pm.registerEvents(new SiegeWarTownyEventListener(this), this);
			pm.registerEvents(new SiegeWarNationEventListener(), this);
			pm.registerEvents(new SiegeWarTownEventListener(this), this);
			pm.registerEvents(new SiegeWarPlotEventListener(this), this);
			pm.registerEvents(new SiegeWarStatusScreenListener(), this);
			pm.registerEvents(new SiegeWarSelfListener(), this);
			if (getServer().getPluginManager().isPluginEnabled("TownyChat")) {
				info("SiegeWar found TownyChat plugin, enabling TownyChat integration.");
				pm.registerEvents(new SiegeWarTownyChatEventListener(), this);
			}
			return true;
		}
	}

	private void registerAdminCommands() {
		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. SiegeWar admin commands not registered");
		} else {
			getCommand("siegewaradmin").setExecutor(new SiegeWarAdminCommand());
		}
	}

	private void registerPlayerCommands() {
		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. SiegeWar player commands not registered");
		} else {
			getCommand("siegewar").setExecutor(new SiegeWarCommand());
			new SiegeWarNationSetOccupationTaxAddonCommand();
		}
	}

	private void printSickASCIIArt() {
		String art = System.lineSeparator() + "<#2DE2E6>            _________.__                     <#FF6C11>__      __" +
					 System.lineSeparator() + "<#2DE2E6>           /   _____/|__| ____   ____   ____<#FF6C11>/  \\    /  \\_____ _______" +
					 System.lineSeparator() + "<#2DE2E6>           \\_____  \\ |  |/ __ \\ / ___\\_/ __ <#FF6C11>\\   \\/\\/   /\\__  \\\\_  __ \\" +
					 System.lineSeparator() + "<#2DE2E6>           /        \\|  \\  ___// /_/  >  ___/<#FF6C11>\\        /  / __ \\|  | \\/" +
					 System.lineSeparator() + "<#2DE2E6>          /_______  /|__|\\___  >___  / \\___  ><#FF6C11>\\__/\\  /  (____  /__|   " +
					 System.lineSeparator() + "<#2DE2E6>                  \\/         \\/_____/      \\/      <#FF6C11>\\/        \\/" +
					 System.lineSeparator() + "<#791E94>                                By Goosius & LlmDl" + System.lineSeparator(); 
		Bukkit.getConsoleSender().sendMessage(Colors.translateColorCodes(art));
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

	public TaskScheduler getScheduler() {
		return (TaskScheduler) this.scheduler;
	}

	private Object setScheduler() {
		if (townyVersionCheck()) {
			// We know that Towny is new enough to have the TaskSchedulers available.
			if (isFoliaClassPresent())
				return new FoliaTaskScheduler(this);
			else
				return new BukkitTaskScheduler(this);
		}
		return null; // Doesn't matter because SiegeWar will not pass the onEnable phase.
	}

	public static boolean isFoliaClassPresent() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
