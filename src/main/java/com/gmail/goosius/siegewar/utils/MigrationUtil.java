package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.Settings;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.exceptions.TownyException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * This util is for assisting with migrating configs, perms, metadata etc.
 */
public class MigrationUtil {

    private static Set<ConfigFileMigrationField> migrationFields = new HashSet<>();
    static {
        //Points balancing migration
        migrationFields.add(new ConfigFileMigrationField("war.siege.scoring.points_for_attacker_occupation","war.siege.points_balancing.base_points.banner_control.attacker"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.scoring.points_for_defender_occupation","war.siege.points_balancing.base_points.banner_control.defender"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.scoring.points_for_attacker_death","war.siege.points_balancing.base_points.deaths.attacker"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.scoring.points_for_defender_death","war.siege.points_balancing.base_points.deaths.defender"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.scoring.winner_takes_all_points","war.siege.points_balancing.end_of_battle_points_distribution.winner_takes_all"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.switches.counterattack_booster_enabled","war.siege.points_balancing.counterattack_booster.enabled"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.scoring.counterattack_booster_extra_death_points_per_player_percentage","war.siege.points_balancing.counterattack_booster.extra_death_points_per_player_percentage"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.banner_control_reversal_bonus.enabled","war.siege.points_balancing.banner_control_reversal_bonus.enabled"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.banner_control_reversal_bonus.multiplier","war.siege.points_balancing.banner_control_reversal_bonus.multiplier"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.battle_session.duration_minutes","war.siege.points_balancing.battle_session_timings.duration_minutes"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.battle_session.capping_limiter.weekdays","war.siege.points_balancing.capping_limiter.weekdays"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.battle_session.capping_limiter.weekend_days","war.siege.points_balancing.capping_limiter.weekend_days"));
        //Peaceful towns migration
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.enabled","neutral_towns.enabled"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.confirmation_requirement_days","neutral_towns.confirmation_requirement_days"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.new_town_confirmation_requirement_days","neutral_towns.new_town_confirmation_requirement_days"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.subvert_enabled","neutral_towns.subvert_enabled"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.revolt_enabled","neutral_towns.revolt_enabled"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.towny_influence_radius","neutral_towns.towny_influence_radius")); 
    }

    /**
     * Read in the config fields to migrate
     */
    public static boolean readInConfigFileMigrationFields() {
        try {
            String configFilePath = SiegeWar.getSiegeWar().getDataFolder().getPath() + File.separator + "config.yml";
            if (FileMgmt.checkOrCreateFile(configFilePath)) {
                File file = new File(configFilePath);
                // read the config.yml into memory
                CommentedConfiguration config = new CommentedConfiguration(file.toPath());
                if (!config.load()) {
                    throw new TownyException("Failed to load existing config file.");
                }
                //Read in migration fields
                for(ConfigFileMigrationField migrationField: migrationFields) {
                    migrationField.value = config.get(migrationField.oldKey);
                }
            }
            SiegeWar.info("Successfully read existing config file.");
            return true;
        } catch (Exception e) {
            SiegeWar.severe(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Migrate config fields
     */
    public static boolean migrateConfigFileFields() {
        try {
            int numMigratedFields = 0;
            CommentedConfiguration config = Settings.getConfig();
            //migrate fields
            for(ConfigFileMigrationField migrationField: migrationFields) {
                if(migrationField.value != null) {
                    config.set(migrationField.newKey, migrationField.value);
                    numMigratedFields++;
                }
            }
            if(numMigratedFields > 0) {
                //Save to disk
                config.save();
                SiegeWar.info("Successfully migrated " + numMigratedFields + " fields.");
            }
            return true;
        } catch (Exception e) {
            SiegeWar.severe(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inner class representing a field to migrate
     */
    private static class ConfigFileMigrationField {
        final String oldKey;
        final String newKey;
        Object value;

        private ConfigFileMigrationField(String oldKey, String newKey) {
            this.oldKey = oldKey;
            this.newKey = newKey;
            value = null;
        }
    }
}
