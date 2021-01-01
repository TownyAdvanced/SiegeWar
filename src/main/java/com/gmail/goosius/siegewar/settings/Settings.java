package com.gmail.goosius.siegewar.settings;

import java.io.File;
import java.io.IOException;

import com.gmail.goosius.siegewar.settings.CommentedConfiguration;
import com.gmail.goosius.siegewar.settings.ConfigNodes;

import io.github.townyadvanced.util.FileMgmt;

public class Settings {
	private static CommentedConfiguration config, newConfig;

	public static void loadConfig(String filepath, String version) throws IOException {
		if (FileMgmt.checkOrCreateFile(filepath)) {
			File file = new File(filepath);

			// read the config.yml into memory
			config = new CommentedConfiguration(file);
			if (!config.load())
				System.out.print("Failed to load Config!");

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

		newConfig = new CommentedConfiguration(file);
		newConfig.load();

		for (ConfigNodes root : ConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());
			if (root.getRoot() == ConfigNodes.VERSION.getRoot())
				setNewProperty(root.getRoot(), version);
			else if (root.getRoot() == ConfigNodes.LAST_RUN_VERSION.getRoot())
				setNewProperty(root.getRoot(), getLastRunVersion(version));
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

		System.out.println("Error could not read " + msg);
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

	public static boolean isUpdating(String currentVersion) {

		if (isUpToDate(currentVersion))
			return false;
		else
			return true; // Assume
	}

	private static boolean isUpToDate(String currentVersion) {

		return currentVersion.equals(getLastRunVersion(currentVersion));
	}

	public static void setLastRunVersion(String currentVersion) {

		setProperty(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
		config.save();
	}

}
