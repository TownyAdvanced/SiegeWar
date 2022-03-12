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
        migrationFields.add(new ConfigFileMigrationField("war.siege.battle_session.capping_limiter.weekdays","war.siege.points_balancing.capping_limiter.weekdays"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.battle_session.capping_limiter.weekend_days","war.siege.points_balancing.capping_limiter.weekend_days"));
        //Peaceful towns to Neutral towns migration
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.enabled","neutral_towns.enabled"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.confirmation_requirement_days","neutral_towns.confirmation_requirement_days"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.new_town_confirmation_requirement_days","neutral_towns.new_town_confirmation_requirement_days"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.subvert_enabled","neutral_towns.subvert_enabled"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.revolt_enabled","neutral_towns.revolt_enabled"));
        migrationFields.add(new ConfigFileMigrationField("peaceful_towns.towny_influence_radius","neutral_towns.towny_influence_radius"));
        //SiegeCamps to Siege-Assemblies migration
        migrationFields.add(new ConfigFileMigrationField("war.siege.siegecamps.enabled","war.siege.siege_assemblies.enabled"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.siegecamps.failed_siegecamp_cooldown","war.siege.siege_assemblies.failed_siege_assembly_cooldown"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.siegecamps.points_required_to_succeed","war.siege.siege_assemblies.points_required_to_succeed"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.siegecamps.points_per_minute","war.siege.siege_assemblies.points_per_minute"));
        migrationFields.add(new ConfigFileMigrationField("war.siege.siegecamps_assemblies.duration_in_minutes","war.siege.siege_assemblies.duration_in_minutes"));
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
                    //Read if old field exists AND is not dummy
                    if(config.contains(migrationField.oldKey) && config.getComments(migrationField.oldKey).size() >0 ) {
                        migrationField.value = config.get(migrationField.oldKey);
                    }
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
