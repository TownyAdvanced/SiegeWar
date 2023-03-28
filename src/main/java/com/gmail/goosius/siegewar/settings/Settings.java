package com.gmail.goosius.siegewar.settings;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.utils.FileMgmt;
import com.gmail.goosius.siegewar.utils.SiegeWarBattleSessionUtil;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.config.migration.ConfigMigrator;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.util.Version;
import com.palmergames.util.TimeTools;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Settings {
	private static CommentedConfiguration config, newConfig;
	private static File battleIconFile;
	public static final String BATTLE_BANNER_FILE_NAME = "crossedswords.png";

	public static boolean loadSettingsAndLang() {
		SiegeWar sw = SiegeWar.getSiegeWar();
		boolean loadSuccessFlag = true;

		try {
			Settings.loadConfig(sw.getDataFolder().getPath() + File.separator + "config.yml", sw.getVersion());
		} catch (Exception e) {
			SiegeWar.severe("Config.yml failed to load! Disabling!");
			loadSuccessFlag = false;
        }

		if (Settings.getLastRunVersion(SiegeWar.getSiegeWar().getVersion()).equals(SiegeWar.getSiegeWar().getVersion())) {
			ConfigMigrator migrator = new ConfigMigrator(SiegeWar.getSiegeWar(), config, "config-migration.json", getLastRunVersion(), false);
			migrator.migrate();
		}
		
		// Some list variables do not reload upon loadConfig.
		SiegeWarSettings.resetCachedSettings();
		
		try {
			Plugin plugin = SiegeWar.getSiegeWar(); 
			Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
			TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, SiegeWar.class);
			loader.load();
			TownyAPI.getInstance().addTranslations(plugin, loader.getTranslations());
		} catch (Exception e) {
			SiegeWar.severe("Language file failed to load! Disabling!");
			loadSuccessFlag = false;
	    }

		//Extract images
		try {
			battleIconFile = FileMgmt.extractImageFile(BATTLE_BANNER_FILE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			SiegeWar.severe("Could not load images! Disabling!");
			loadSuccessFlag = false;
		}

		//Schedule next battle session
		try {
			SiegeWarBattleSessionUtil.attemptToScheduleNextBattleSession();
		} catch (Exception e) {
			e.printStackTrace();
			SiegeWar.severe("Problem Scheduling Battle Session! Disabling!");
			loadSuccessFlag = false;
		}

		return loadSuccessFlag;
	}
	
	public static void loadConfig(String filepath, String version) throws Exception {
		if (FileMgmt.checkOrCreateFile(filepath)) {
			File file = new File(filepath);

			// read the config.yml into memory
			config = new CommentedConfiguration(file.toPath());
			if (!config.load())
				throw new IOException("Failed to load Config!");

			setDefaults(version, file);
			config.save();
		}
	}

	public static void addComment(String root, String... comments) {

		newConfig.addComment(root.toLowerCase(), comments);
	}

	private static void setNewProperty(String root, Object value) {

		if (value == null) {
			value = "";
		}
		newConfig.set(root.toLowerCase(), value.toString());
	}

	private static void setProperty(String root, Object value) {

		config.set(root.toLowerCase(), value.toString());
	}

	public static String getLastRunVersion(String currentVersion) {

		return getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
	}

	/**
	 * Builds a new config reading old config data.
	 */
	private static void setDefaults(String version, File file) {

		newConfig = new CommentedConfiguration(file.toPath());
		newConfig.load();

		for (ConfigNodes root : ConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());
			if (root.getRoot() == ConfigNodes.VERSION.getRoot())
				setNewProperty(root.getRoot(), version);
			else if (root.getRoot() == ConfigNodes.LAST_RUN_VERSION.getRoot())
				setNewProperty(root.getRoot(), SiegeWar.getSiegeWar().getVersion());
			else
				setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());
		}

		config = newConfig;
		newConfig = null;
	}

	public static String getString(String root, String def) {

		String data = config.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from config.yml");
			return "";
		}
		return data;
	}

	private static void sendError(String msg) {

		SiegeWar.severe("Error could not read " + msg);
	}

	public static boolean getBoolean(ConfigNodes node) {

		return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	public static double getDouble(ConfigNodes node) {

		try {
			return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0.0;
		}
	}

	public static int getInt(ConfigNodes node) {

		try {
			return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0;
		}
	}

	public static String getString(ConfigNodes node) {

		return config.getString(node.getRoot().toLowerCase(), node.getDefault());
	}
	
	public static long getSeconds(ConfigNodes  node) {

		try {
			return TimeTools.getSeconds(getString(node));
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 1;
		}
	}

	public static void setLastRunVersion(String currentVersion) {

		setProperty(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
		config.save();
	}

	public static Version getLastRunVersion() {
		return Version.fromString(config.getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), "0.0.0.0"));
	}

	public static File getBattleIconFile() {
		return battleIconFile;
	}

	public static CommentedConfiguration getConfig() {
		return config;
	}

	/**
	 * Get list of items, where each item is surrounded by curly brackets e.g. {a}{b}{c}
	 * @param node the node where the list is configured
	 * @return list of items
	 */
	public static List<String> getListOfCurlyBracketedItems(ConfigNodes node) {
		return getListOfCurlyBracketedItems(getString(node));
	}

	/**
	 * Get list of items, where each item is surrounded by curly brackets e.g. {a}{b}{c}
	 * @param inputText the text in which the list is contained
	 * @return list of items
	 */
	public static List<String> getListOfCurlyBracketedItems(String inputText) {
		return getListOfItems(inputText, "\\{([^}]+)}");
	}

	public static List<String> getListOfItems(String inputText, String itemIdentifierRegex) {
		List<String> result = new ArrayList<>();
		Pattern pattern = Pattern.compile(itemIdentifierRegex);
		Matcher matcher = pattern.matcher(inputText);
		while(matcher.find()) {
			result.add(matcher.group(1));	
		}
		return result;
	}	
}
