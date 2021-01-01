package com.gmail.goosius.siegewar;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.goosius.siegewar.settings.Settings;

import io.github.townyadvanced.util.JavaUtil;

public class SiegeWar extends JavaPlugin {
	
	private static SiegeWar plugin;
	
	public SiegeWar getSiegeWar() {
		return plugin;
	}
	
    @Override
    public void onEnable() {
        if (!loadSettings())
        	onDisable();

        if (Settings.isUpdating(getVersion()))
        	update();
    }

	public String getVersion() {
		return this.getDescription().getVersion();
	}

	public String getPrefix() {
		return "["+this.getDescription().getPrefix()+"] ";
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
}
