package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.goosius.siegewar.settings.ConfigNodes;
import com.gmail.goosius.siegewar.settings.Settings;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.config.migration.ConfigMigrator;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.util.Version;
import com.gmail.goosius.siegewar.command.SiegeWarAdminCommand;
import com.gmail.goosius.siegewar.command.SiegeWarCommand;
import com.gmail.goosius.siegewar.command.SiegeWarNationSetPeacefulOccupationTaxAddonCommand;
import com.gmail.goosius.siegewar.hud.SiegeHUDManager;
import com.gmail.goosius.siegewar.integration.cannons.CannonsIntegration;
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
import java.util.ArrayList;
import java.util.List;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	private final Version requiredTownyVersion = Version.fromString("0.98.6.0");
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
        
        registerAdminCommands();
        handleLegacyConfigs();
        
        if (!loadAll()) {
	        siegeWarPluginError = true;
        }

		cleanupBattleSession();
		registerPlayerCommands();
		registerListeners();
		checkIntegrations();
		cleanupLegacyMetaData();
		migrateTownOccupationData();

		if(siegeWarPluginError) {
			severe("SiegeWar did not load successfully, and is now in safe mode!");
		} else {
			info("SiegeWar loaded successfully.");
		}
    }
    
    private void handleLegacyConfigs() {
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
			if (getServer().getPluginManager().isPluginEnabled("Cannons")) {
				info("SiegeWar found Cannons plugin, enabling Cannons support.");
				cannonsIntegration = new CannonsIntegration(this);
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
			pm.registerEvents(new SiegeWarBukkitEventListener(), this);		
			pm.registerEvents(new SiegeWarTownyEventListener(this), this);
			pm.registerEvents(new SiegeWarNationEventListener(), this);
			pm.registerEvents(new SiegeWarTownEventListener(this), this);
			pm.registerEvents(new SiegeWarPlotEventListener(this), this);
			pm.registerEvents(new SiegeWarStatusScreenListener(), this);
			pm.registerEvents(new SiegeWarSelfListener(), this);
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
			new SiegeWarNationSetPeacefulOccupationTaxAddonCommand();
		}
	}

	private void printSickASCIIArt() {
		String art = System.lineSeparator() + "#2DE2E6            _________.__                     #FF6C11__      __" +
					 System.lineSeparator() + "#2DE2E6           /   _____/|__| ____   ____   ____#FF6C11/  \\    /  \\_____ _______" +
					 System.lineSeparator() + "#2DE2E6           \\_____  \\ |  |/ __ \\ / ___\\_/ __ #FF6C11\\   \\/\\/   /\\__  \\\\_  __ \\" +
					 System.lineSeparator() + "#2DE2E6           /        \\|  \\  ___// /_/  >  ___/#FF6C11\\        /  / __ \\|  | \\/" +
					 System.lineSeparator() + "#2DE2E6          /_______  /|__|\\___  >___  / \\___  >#FF6C11\\__/\\  /  (____  /__|   " +
					 System.lineSeparator() + "#2DE2E6                  \\/         \\/_____/      \\/      #FF6C11\\/        \\/" +
					 System.lineSeparator() + "#791E94                                By Goosius & LlmDl" + System.lineSeparator(); 
		Bukkit.getConsoleSender().sendMessage(Colors.translateColorCodes(art));
	}
	
	public static boolean isCannonsPluginInstalled() {
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

	/**
	 * Cleans up the battle session, if it did not exit properly when the plugin shut down.
 	 */	
	private void cleanupBattleSession() {
		if(siegeWarPluginError) {
			severe("SiegeWar is in safe mode. Battle Session Cleanup not attempted.");
		} else {
			//Find any sieges with unresolved battles
			List<Siege> siegesWithUnresolvedBattles = new ArrayList<>();
			for(Siege siege: SiegeController.getSieges()) {
				if(siege.getStatus() == SiegeStatus.IN_PROGRESS
					&& (siege.getAttackerBattlePoints() > 0 || siege.getDefenderBattlePoints() > 0)) {
					siegesWithUnresolvedBattles.add(siege);					
				}
			}
			//Resolve battles
			if(siegesWithUnresolvedBattles.size() > 0) {
				info(Translation.of("msg.battle.session.cleanup.starting"));
				int numBattlesUpdated = 0;
				for(Siege siege: siegesWithUnresolvedBattles) {
					siege.adjustSiegeBalance(siege.getAttackerBattlePoints() - siege.getDefenderBattlePoints());
					siege.setAttackerBattlePoints(0);
					siege.setDefenderBattlePoints(0);
					SiegeController.saveSiege(siege);
					numBattlesUpdated++;
				}
				
				info(Translation.of("msg.battle.session.cleanup.complete", numBattlesUpdated));
			}
		}
	}

	/**
	 * Cleanup metadata which is non longer in use
	 */
	private void cleanupLegacyMetaData() {
		for(Resident resident: TownyUniverse.getInstance().getResidents()) {
			ResidentMetaDataController.clearPlunder(resident);
		}
	}

	/**
	 * In some previous SW versions,
	 * a town could have both a regular nation, and a different occupying nation.
	 * -
	 * This method migrates the old data,
	 * so that if the older data schema is detected,
	 * and a town has an occupying nation,
	 * that town will be transferred to that occupying nation.
	 */
	public static void migrateTownOccupationData() {
		SiegeWar.info("Migrating Town Occupation Data...");
		for(Town town: new ArrayList<>(TownyAPI.getInstance().getTowns())) {
			if(TownMetaDataController.hasLegacyOccupierUUID(town)) {
				Nation occupyingNation = TownyAPI.getInstance().getNation(TownMetaDataController.getLegacyOccupierUUID(town));
				if(occupyingNation != null) {
					TownOccupationController.setTownOccupation(town, occupyingNation);
				}
				TownMetaDataController.removeLegacyOccupierUUID(town);
			}
			if(TownMetaDataController.hasLegacyPrePeacefulOccupierUUID(town)) {
				TownMetaDataController.removeLegacyPrePeacefulOccupierUUID(town);
			}
		}
	}
}
