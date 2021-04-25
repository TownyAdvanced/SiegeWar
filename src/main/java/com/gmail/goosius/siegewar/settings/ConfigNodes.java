package com.gmail.goosius.siegewar.settings;

public enum ConfigNodes {

	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version.  Please do not edit."),
	LAST_RUN_VERSION(
			"version.last_run_version",
			"",
			"# This is for showing the changelog on updates.  Please do not edit."),
	LANGUAGE(
			"language",
			"english.yml",
			"# The language file you wish to use"),
	WAR("war","","",""),
	WAR_SIEGE(
			"war.siege",
			"",
			"############################################################",
			"# +------------------------------------------------------+ #",
			"# |                   Siege-War settings                 | #",
			"# |                                                      | #",
			"# +------------------------------------------------------+ #",
			"############################################################",
			""),
	WAR_SIEGE_ENABLED(
			"war.siege.switches.enabled",
			"true",
			"",
			"# If true, the Siege-War system is enabled.",
			"# if false, the Siege-War system is disabled."),
	WAR_SIEGE_WORLDS(
			"war.siege.switches.worlds",
			"world, world_nether, world_the_end",
			"",
			"# This list specifies the worlds in which siegewar is enabled."),
	WAR_SIEGE_CONQUEST_SIEGES_ENABLED(
			"war.siege.switches.conquest_sieges_enabled",
			"true",
			"",
			"# If true, then nations can start conquest sieges."),
	WAR_SIEGE_LIBERATION_SIEGES_ENABLED(
			"war.siege.switches.liberation_sieges_enabled",
			"true",
			"",
			"# If true, then nations can start liberation sieges."),
	WAR_SIEGE_REVOLT_SIEGES_ENABLED(
			"war.siege.switches.revolt_sieges_enabled",
			"true",
			"",
			"# If true, then towns can start revolt sieges."),
	WAR_SIEGE_SUPPRESSION_SIEGES_ENABLED(
			"war.siege.switches.suppression_sieges_enabled",
			"true",
			"",
			"# If true, then nations can start suppression sieges."),
	WAR_SIEGE_ABANDON_ENABLED(
			"war.siege.switches.abandon_enabled",
			"true",
			"",
			"# If true, then attackers can abandon sieges."),
	WAR_SIEGE_SURRENDER_ENABLED(
			"war.siege.switches.town_surrender_enabled",
			"true",
			"",
			"# If true, then defenders can surrender sieges."),
	WAR_SIEGE_INVADE_ENABLED(
			"war.siege.switches.invade_enabled",
			"true",
			"",
			"# If true, then invasions are enabled."),
	WAR_SIEGE_PLUNDER_ENABLED(
			"war.siege.switches.plunder_enabled",
			"true",
			"",
			"# If true, then plunder is enabled.",
			"# This action will steal money from the town, and transfer it to the victorious nation.",
			"# The below setting of war.siege.money.attacker_plunder_distribution_ratio determines the nation-bank/soldiers distribution",
			"# If the town does not have sufficient funds, it will be bankrupted/ruined/destroyed."),
	WAR_SIEGE_MILITARY_SALARY_ENABLED(
			"war.siege.switches.military_salary_enabled",
			"true",
			"",
			"# If true, then a king can pay their soldiers using '/sw nation paysoldiers <total amount>'.",
			"# The amount will be distributed among the soldiers according to their ranks.",
			"# The permission of siegewar.nation.siege.pay.grade.x, determines how much a rank is paid. x=100 is a normal share, x=200 is double"),
	WAR_SIEGE_PVP_ALWAYS_ON_IN_BESIEGED_TOWNS(
			"war.siege.switches.pvp_always_on_in_besieged_towns",
			"true",
			"",
			"# If true, then town pvp is always set to on during sieges.",
			"# The town pvp flag returns to its previous value when the siege ends."),
	WAR_SIEGE_CLAIMING_DISABLED_NEAR_SIEGE_ZONES(
			"war.siege.switches.claiming_disabled_near_siege_zones",
			"true",
			"",
			"# If true, then land cannot be claimed near a siege zone.",
			"# This setting is generally considered critical, otherwise one side could wall off the siege zone."),
	WAR_SIEGE_NON_RESIDENT_SPAWN_INTO_SIEGE_ZONES_OR_BESIEGED_TOWNS_DISABLED(
			"war.siege.switches.non_resident_spawn_into_siegezones_or_besieged_towns_disabled",
			"true",
			"",
			"# If this setting is true, then only town residents are permitted to spawn into siegezones OR besieged towns.",
			"# This setting is recommended to:",
			"# 1. Protect players from accidentally spawning into a warzone while unprepared.",
			"# 2. Discourage 'fake' sieges, by making the automatic siege impact harsher.",
			"# 3. Even the spawn-advantage between attacking and defender."), 
	WAR_SIEGE_REFUND_INITIAL_NATION_COST_ON_DELETE(
			"war.siege.switches.refund_initial_nation_cost_on_delete",
			"true",
			"",
			"# If this is true, then when a nation is deleted/destroyed,",
			"# a refund amount will be made available to the former king.", 
		    "# This money can then be reclaim using /n claim refund.",
			"# This prevents the new nation cost becoming a large sunken cost due to invasion."),
	WAR_SIEGE_BESIEGED_TOWN_RECRUITMENT_DISABLED(
			"war.siege.switches.besieged_town_recruitment_disabled",
			"true",
			"",
			"# If this value is true, then a town under active siege cannot recruit new residents.",
			"#  This setting is recommended because it helps discourage 'fake' sieges just for the purpose of of gifting immunity."),
	WAR_SIEGE_BESIEGED_TOWN_CLAIMING_DISABLED(
			"war.siege.switches.besieged_town_claiming_disabled",
			"true",
			"",
			"# If this value is true, then a town under active siege cannot claim new plots.",
			"#  This setting is recommended because it helps discourage 'fake' sieges just for the purpose of of gifting immunity."),
	WAR_SIEGE_BESIEGED_TOWN_UNCLAIMING_DISABLED(
			"war.siege.switches.besieged_town_unclaiming_disabled",
			"true",
			"",
			"# If this value is true, then a town under active siege cannot unclaim.",
			"#  This setting is recommended if invasion/occupation is enabled, to avoid occupation escape exploits."),
	WAR_SIEGE_COUNTERATTACK_BOOSTER_ENABLED(
			"war.siege.switches.counterattack_booster_enabled",
			"false",
			"",
			"# This feature is an essential feature for a good server experience, ",
			"# as described in detail in the the online User Guide and FAQ.",
			"#",
			"# If this setting is false, then if a player from the banner controlling side dies,",
			"# the death points are increased by a certain percentage. (see points section below)."),
	WAR_SIEGE_POPULATION_BASED_POINT_BOOSTS_ENABLED(
			"war.siege.switches.population_based_point_boosts_enabled",
			"false",
			"",
			"# If this setting is true, then the siege side with the lower population gets a boost to their siege point gains.",
			"# The attacking side population consists of the residents of the attacking nation, and allies.",
			"# The defending side population consists of the residents of the defending town, and nation + allies if applicable.",
			"# The level of the boost is configured in separate configs. See the scoring section of this file."),
	WAR_SIEGE_NATION_STATISTICS_ENABLED(
			"war.siege.switches.nation_statistics_enabled",
			"true",
			"",
			"# If this setting is true, then Siegewar statistics will be shown on nation status screens."),
	WAR_SIEGE_SWITCHES_TRAP_WARFARE_MITIGATION_ENABLED(
			"war.siege.switches.trap_warfare_mitigation_enabled",
			"false",
			"",
			"# If this setting is true, then ",
			"# 1. Players cannot build/destroy blocks in the timed point zone below the siege banner altitude, and",
			"# 2. Banner control cannot be gained if the player is below the siege banner altitude",
			"# NOTE: ",
			"# If you enable this feature, ",
			"# make sure to also have a server rule preventing traps being created in the timed-point-zone BEFORE the banner is placed"),

	//Monetary Values

	//Plunder
	WAR_SIEGE_ATTACKER_COST_UPFRONT_PER_PLOT(
			"war.siege.money.attacker_cost_upfront_per_plot",
			"20.0",
			"",
			"# This value represents the siege deposit paid by the attacker",
			"# This is the 'warchest' (a money-account, not an actual chest object).",
			"# If the attacking nation wins the siege, the warchest is returned to the nation bank.",
			"# If the defending town wins the siege, the warchest is recovered by the defenders.",
			"# See below for more details on how the warchest is distributed among the defenders."),
	WAR_SIEGE_ATTACKER_PLUNDER_AMOUNT_PER_PLOT(
			"war.siege.money.attacker_plunder_amount_per_plot",
			"40.0",
			"",
			"# This is the amount which an attacking nation will plunder if they choose to do so after a siege victory.",
			"# See below for more details on how the plunder is distributed."),
	WAR_SIEGE_PLUNDER_DISTRIBUTION_RATIO(
			"war.siege.money.plunder_distribution_ratio",
			"1:3",
			"",
			"# This ratio affects how plunder is distributed among the winning team.",
			"# The value on the left is the ratio which will go to the government bank (town or nation).",
			"# The value on the right is the ratio which will go to the contributing soldiers",
			"# For each battle in which a soldier gained banner control at least once, that soldier receives 1 share.",
			"# The total soldiers' share is distributed accordingly among the army."),

	WAR_SIEGE_CAPITAL_SIEGE_COST_INCREASE_PERCENTAGE(
			"war.siege.money.capital_siege_cost_increase_percentage",
			"0",
			"",
			"# The percentage to increase the cost of sieging capitals by.",
			"# Example: If set to 50, with an attack cost of 20/plot, each plot would be 30."),
	WAR_SIEGE_NATION_COST_REFUND_PERCENTAGE_ON_DELETE(
			"war.siege.money.nation_cost_refund_percentage_on_delete",
			"80.0",
			"",
			"# The value specifies what proportion of the initial nation cost is refunded," +
			"# When the nation is deleted."),
	WAR_SIEGE_EXTRA_MONEY_PERCENTAGE_PER_TOWN_LEVEL(
			"war.siege.money.extra_money_percentage_per_town_level",
			"0",
			"",
			"# This value increases the monetary costs & gains of sieges, depending on town level.",
			"# ",
			"# The value is useful to ensure that larger towns/nations will not feel unaffected by war moneys.",
			"# The value is applied to attack cost (war-chest) and plunder.",
			"# The value is appropriate in servers where town wealth does not correspond linearly to number of plots.",
			"# Example: On server x, small towns tend to have 10 plots + 100 gold, and large towns tend to have 100 plots + 999,999 gold.",
			"#          Clearly on this server, wealth is heavily concentrated in larger towns. Thus this value should be high ",
	        "# ",
			"# Example of how this value would affect attack cost (@20/plot), if the value was set to 50 : ",
			"# ",
			"# Level 1 (1 resident), 5 plots. Cost = (20 * 5) + 0% = 100",
			"# Level 2 (3 residents), 15 plots. Cost = (20 * 15) + 50% = 450",
			"# Level 3 (8 residents), 50 plots. Cost = (20 * 50) + 100% = 2000",
			"# Level 4 (12 residents), 80 plots. Cost = (20 * 80) + 150% = 4000",
			"# ",
			"# If the value is 0, then money amounts are not modified."),

	//Non-Monetary Quantities
	WAR_SIEGE_MAX_ACTIVE_SIEGE_ATTACKS_PER_NATION(
		"war.siege.quantities.max_active_siege_attacks_per_nation",
		"3",
		"",
		"# The value specifies the maximum number of active attack sieges allowed per nation." +
			"# A low setting will generally reduce the aggression level on the server.",
			"# A low setting will also rebalance the system in favour of smaller nations.",
		 	"# This is because it will prevent larger nations from conducting as many sieges as their resources would otherwise allow."),

	//Times
	WAR_SIEGE_MAX_HOLDOUT_TIME_HOURS(
			"war.siege.times.max_holdout_time_hours",
			"72",
			"",
			"# The maximum duration a town can hold out against a siege.",
			"# If the value is too high, regular players may be unsatisfied that sieges take too long.",
			"# If the value is too low, casual players may be unsatisfied that ",
		    "#    they are unable to contribute to sieges, especially those involving their own town/nation"),
	WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_SURRENDER_HOURS(
			"war.siege.times.min_siege_duration_before_surrender_hours",
			"24",
			"",
			"# The minimum duration of a siege before a town can surrender.",
			"# This setting is important because it prevents a 'quick surrender' by the defender",
			"# - which could leave the attacker with no 'aftermath' time in which to execute invade or plunder actions."),
	WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_ABANDON_HOURS(
			"war.siege.times.min_siege_duration_before_abandon_hours",
			"24",
			"",
			"# The minimum duration of a siege before an attacking nation can abandon.",
			"# This setting is important to prevent an attacker and defender colluding to establish a suitable siege immunity time."),
	WAR_SIEGE_SIEGE_IMMUNITY_TIME_NEW_TOWN_HOURS(
			"war.siege.times.siege_immunity_time_new_town_hours",
			"120",
			"",
			"# This value determines how long a town is safe from sieges, after the town is founded.",
			"# A high value allows more time to fortify new towns, but community engagement by mayors will be slower.",
			"# A low value allows less time to fortify new towns, but community engagement by mayors will be faster."),
	WAR_SIEGE_SIEGE_IMMUNITY_TIME_MODIFIER(
			"war.siege.times.siege_immunity_time_modifier",
			"3",
			"",
			"# This value determines how long a town is safe from sieges, after a siege finishes.",
			"# The actual cooldown time will be the length of the previous siege, multiplied by this modifer.",
			"# A high value makes sieges less frequent. Suitable for moderately-aggressive servers",
			"# A low value makes sieges more frequent. Suitable for highly aggressive servers."),
	WAR_SIEGE_REVOLT_IMMUNITY_TIME_MODIFIER(
			"war.siege.times.revolt_immunity_time_modifier",
			"0.75",
			"",
			"# This value determines how long a town must wait before it can revolt against an occupying nation.",
			"# Revolt immunity beings after a town is defeated in a siege.",
			"# The configured value represents a fraction of the actual immunity time.",
			"# Not that revolting can break through siege immunity",
			"# WARNING: Do not set this to any higher than about 0.8, because that could lead to towns getting trapped in an endless cycle of invasions from which they could not free themselves."),
	WAR_SIEGE_BANNER_CONTROL_SESSION_DURATION_MINUTES (
			"war.siege.times.banner_control_session_duration_minutes",
			"7",
			"",
			"# This value determines the duration of each banner control session."),

	//Distances
	WAR_SIEGE_MAX_ALLOWED_BANNER_TO_TOWN_DOWNWARD_ELEVATION_DIFFERENCE(
			"war.siege.distances.max_allowed_banner_to_town_downward_elevation_difference",
			"15",
			"",
			"# This is the max allowed elevation difference downward from siege banner to town.",
			 "# There is no limit on the upward difference.",
		     "# This setting prevents the banner being placed on a platform high in the air."),
	WAR_SIEGE_ZONE_RADIUS_BLOCKS(
			"war.siege.distances.zone_radius_blocks",
			"150",
			"",
			"# The radius of the 'siege zone'.",
			"# This radius applies only horizontally, so players can never get above a siegezone (e.g. to place lava there or something).",
			"# Various siege related effects can apply in this zone e.g. lose points on death, keep inv on death, cannot claim here."),
	WAR_SIEGE_BANNER_CONTROL_VERTICAL_DISTANCE_BLOCKS(
			"war.siege.distances.banner_control_vertical_distance_blocks",
			"16",
			"",
			"# This is the vertical distance a soldier must be from the banner to get banner control.",
			"# Note that the horizontal distance is always the same as the Towny townblock size."),

	//Battle points
	WAR_BATTLE_POINTS_FOR_ATTACKER_OCCUPATION(
			"war.siege.scoring.points_for_attacker_occupation",
			"10",
			"",
			"# This setting determines the number of battle points awarded to an occupying attacker.",
			"# The points are awarded if a player remains within a town-block length of the siege banner for: ",
			"# 7 minutes (default configuration)."),
	WAR_BATTLE_POINTS_FOR_DEFENDER_OCCUPATION(
			"war.siege.scoring.points_for_defender_occupation",
			"10",
			"",
			"# This setting determines the number of battle points awarded to an occupying defender.",
			"# The points are awarded if a player remains within a town-block length of the siege banner for ",
			"# 7 minutes (default configuration)."),
	WAR_BATTLE_POINTS_FOR_ATTACKER_DEATH(
			"war.siege.scoring.points_for_attacker_death",
			"150",
			"",
			"# This setting determines the number of battle points awarded if an attacker dies.",
			"# The points are awarded if the player dies within the configured siege zone death radius.",
			"# The points are given to the defending town.",
			"# ",
			"# Background:",
			"# To configure correctly, you must understand how this setting affects a siege:",
			"# 1. Attacker stronger than defender - A stronger attacker is NOT AFFECTED by this value, because regardless of the value, they will expect to score more death points than the opponent.",
			"# 2. Attacker weaker than defender - A weaker attacker IS AFFECTED by this value, because with every attack they will likely suffer a higher death points loss than the opponent.",
			"# ",
			"# Configuration Outcomes:",
		    "# Value HIGH --> If the value is high, then PVP will be DISCOURAGED",
			"# Value LOW --> If the value is low, then PVP will be ENCOURAGED"),
	WAR_BATTLE_POINTS_FOR_DEFENDER_DEATH(
			"war.siege.scoring.points_for_defender_death",
			"150",
			"",
			"# This setting determines the number of battle points awarded if a defender dies.",
			"# The points are awarded if the player dies within the configured siege zone death radius.",
			"# The points are given to the attacking nation.",
			"# ",	
			"# Background:",
			"# To configure correctly, you must understand how this setting affects a siege:",
			"# 1. Defender strong than attacker - A stronger defender is NOT AFFECTED by this value, because regardless of the value, they will expect to score more death points than the opponent.",
			"# 2. Defender weaker than attacker - A weaker defender IS AFFECTED by this value, because with every attack they will likely suffer a higher death points loss than the opponent.",
			"# ",
			"# Configuration Outcomes:",
			"# Value HIGH --> If the value is high, then PVP will be DISCOURAGED",
			"# Value LOW --> If the value is low, then PVP will be ENCOURAGED"),
	WAR_SIEGE_COUNTERATTACK_BOOSTER_EXTRA_DEATH_POINTS_PER_PLAYER_PERCENTAGE(
			"war.siege.scoring.counterattack_booster_extra_death_points_per_player_percentage",
			"10.0",
			"",
			"# As long as the counterattack booster feature is not disabled, this setting determines the strength of the boost.",
			"# Example: If this setting is 10.0, and there are 3 players on the banner control list, and a player from the banner-controlling side dies,",
			"# then the death points awarded to the attacker will be increased by +30%."),
	WAR_SIEGE_POPULATION_QUOTIENT_FOR_MAX_POINTS_BOOST(
			"war.siege.scoring.population_quotient_for_max_points_boost",
			"3.0",
			"",
			"# This setting determines the population quotient which results in max points boots.",
			"# Example:",
			"# 1. Assume this value is set to 3.0.",
		    "# 2. Assume a siege attacker has 3 times the population of a siege defender (counting allied populations too).",
			"# 3. In this example, if the siege defender scores any battle points, the points will be boosted by the (separately configured) maximum.",
            "# 4. In this example, the siege attacker will not get any points boosts."),
	WAR_SIEGE_MAX_POPULATION_BASED_POINTS_BOOST(
			"war.siege.scoring.max_population_based_points_boost",
			"2.0",
			"",
			"# This setting determines the maximum points boost which a siege side can get due to population.",
			"# Example:",
			"# 1. Assume this value is set to 2.0.",
			"# 2. Assume that a siege attacker greatly outnumbers a siege defender in population. (also counting allies)",
			"# 3. In this example, if the siege defender scores any battle points, the points will be multiplied by 2.",
			"# 4. In this example, the siege attacker will not get any points boosts."),

	//Battle Sessions
	WAR_SIEGE_BATTLE_SESSIONS_START_TIMES_UTC(
			"war.siege.battle_session.start_times_utc",
			"0:10,1:10,2:10,3:10,4:10,5:10,6:10,7:10,8:10,9:10,10:10,11:10,12:10,13:10,14:10,15:10,16:10,17:10,18:10,19:10,20:10,21:10,22:10,23:10",
			"# This value determines the times (in UTC) when each battle session will start.",
			"# Integers can be used to signify hours, and minutes are used as follows: 4:20,5:20,6:35 etc.",
			"# The default is every hour, at ten past the hour.",
			"# The ten-past is so the critical point of the battle (the final minutes), will fall on the hour.",
			"# In addition to controlling routine battle session start times,",
			"# this config can also be used to prevent afk sieging at unusual times e.g. night-time sieges.",
			"# But be careful with this, as it also restricts sieging by cross-timezone players."),
	WAR_SIEGE_BATTLE_SESSIONS_DURATION_MINUTES(
			"war.siege.battle_session.duration_minutes",
			"50",
			"",
			"# This value determines the duration of each battle session.",
			"# After a battle session ends,",
			"# the time period until the next battle session starts, is defined as a 'break'"),

	//Banner Control Reversal Bonus
	WAR_SIEGE_BANNER_CONTROL_REVERSAL_BONUS_ENABLED(
			"war.siege.banner_control_reversal_bonus.enabled",
			"true",
			"# This setting determines if the banner control reversal bonus is enabled.",
			"# If enabled, then whenever a team reverses banner control during a battle,",
			"# that team receives a bonus, equal to a multiplier of the timed points which that control had previously granted to the opposing team.",
            "# This feature is particularly important to prevent 'AFK sieging',",
			"# because the more timed points a team gains while its opponent is away,",
			"# the more that team stands to lose if its opponent reappears and reverses banner control."),
	WAR_SIEGE_BANNER_CONTROL_REVERSAL_BONUS_MULTIPLIER(
			"war.siege.banner_control_reversal_bonus.multiplier",
			"2",
			"# This setting determines the strength of the bonus multiplier.",
			"# Example: Assuming this value is 2,",
			"# then if team A has gained 420 battle points from banner control,",
			"# and banner control is then reversed by Team B,",
			"# then Team B will get an instant bonus of 840 battle points."),

	//Siege zone block/use restrictions
	WAR_SIEGE_ZONE_BLOCK_PLACEMENT_RESTRICTIONS_ENABLED(
			"war.siege.zone_block_placement_restrictions.enabled",
			"true",
			"",
			"# If this setting is true, then certain blocks cannot be placed in the wilderness area of the siegezone.",
			"# This setting is useful to help prevent extreme siege-zone grief such as obsidian forts."),
	WAR_SIEGE_ZONE_BLOCK_PLACEMENT_RESTRICTIONS_MATERIALS(
			"war.siege.zone_block_placement_restrictions.materials",
			"obsidian",
			"",
			"# This setting is used to indicate the list of forbidden materials",
			"# WARNING: Avoid putting 'common' blocks on this list as that may cause lag."),
	WAR_SIEGE_ZONE_BUCKET_EMPTYING_RESTRICTIONS_ENABLED(
			"war.siege.zone_bucket_usage_restrictions.enabled",
			"true",
			"",
			"# If this setting is true, then certain buckets cannot be emptied in the wilderness area of the siegezone.",
			"# This setting is useful to help prevent extreme siege-zone grief such as obsidian forts or cobble monsters."),
	WAR_SIEGE_ZONE_BUCKET_EMPTYING_RESTRICTIONS_MATERIALS(
			"war.siege.zone_bucket_usage_restrictions.materials",
			"lava_bucket, water_bucket, cod_bucket, pufferfish_bucket, salmon_bucket, tropical_fish_bucket",
			"",
			"# This setting is used to indicate the list of forbidden buckets"),

	//Map Sneaking
	//Todo - Eventually move this to another location as it works regardless of war system, or without.
	WAR_SIEGE_MAP_SNEAKING_ENABLED(
			"war.siege.switches.map_sneaking_enabled",
			"true",
			"",
			"# If this setting is true, then the map sneaking feature is enabled",
			"# PREREQUISITES: ",
			"# * You must have deployed a standard dynmap jar.",
			"# ",
			"# DESCRIPTION",
			"# * This feature is critical to enable normal military tactics such as ambushing.",
			"# * The feature works as follows:",	
			"# * if a player wishes to 'map sneak', they equip a specific combination of items in their hands (configured below).",
			"# * Then in a few seconds they will disappear from the dynmap, and are then considered to be 'map sneaking'.",
			"# * Player's in banner control sessions cannot map-sneak"),
	WAR_SIEGE_MAP_SNEAKING_ITEMS(
			"war.siege.items.map_sneaking_items",
			"diamond_sword|diamond_sword, diamond_axe|diamond_axe, diamond_shovel|diamond_shovel, bow|bow",
			"",
			"# This list specifies the item combinations which allow players to map-sneak.",
			"# Each list entry is in the form of <off-hand>|<main-hand>.",
			"# ",
			"# To specify that both items are required - e.g. 'compass|painting'" + 
			"# To specify that only one item is required - e.g. 'compass|any'",
			"# To specify that one hand must be empty - e.g. 'compass|empty'",
			"# ",
			"# This list allows a server to grant usage of the feature to different categories of players.",
			"# Example 1:  An entry with 'shield|diamond_sword' grants the feature to soldiers.",
			"# Example 2:  An entry with 'compass|diamond_sword' grants the feature to scouts/explorers.",
			"# Example 3:  An entry with 'compass|air' grants the feature to very peaceful explorers.",
			"# Example 4:  An entry with 'compass|any' grants the feature to many players including builders/miners/lumberjacks."),

	//SIEGE DEATH PENALTIES
	WAR_SIEGE_DEATH_PENALTY_KEEP_INVENTORY_ENABLED(
			"war.siege.switches.keep_inventory_on_siege_death",
			"true",
			"",
			"# This is an essential feature for a good server experience, ",
			"# as described in detail in the the online User Guide and FAQ.",
			"# ",
			"# If the setting is true then military ranked players keep inventory on siege-zone death."),
	WAR_SIEGE_DEATH_PENALTY_DEGRADE_INVENTORY_ENABLED(
			"war.siege.death_penalty.degrade_inventory.enabled",
			"true",
			"",
			"# This feature is recommended for use with keep_inventory_on_siege_death.",
			"# If this setting is true, battle will be more 'rpg-like', with a little more death cost.",
			"# If this setting is false, battles will be more 'arcade-like', with low death cost."),
	WAR_SIEGE_DEATH_PENALTY_DEGRADE_INVENTORY_PERCENTAGE(
			"war.siege.death_penalty.degrade_inventory.percentage",
			"20.0",
			"",
			"# This values specifies the percentage equipment degradation that occurs to a soldiers equipment,",
			"# when they die in a siege zone.",
			"# A low value will keep battles fast paced.",
			"# A high value will slow down battles (as players take time to repair items),",
			"# and will also exclude some casual players from war (as they cannot afford to keep repairing/replacing/mending degraded items)"),
	WAR_SIEGE_DEATH_SPAWN_FIREWORK(
			"war.siege.death.spawn_firework",
			"true",
			"",
			"# If enabled, a firework will get spawned whenever a player dies inside a siege zone."),

	PEACEFUL_TOWNS(
		"peaceful_towns",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |               Peaceful Towns settings                 | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	PEACEFUL_TOWNS_ENABLED(
			"peaceful_towns.enabled",
			"true",
			"",
			"# If this is true, then a town can toggle peacefulness using /t toggle peaceful,",
			"# After a countdown (usually in days - configurable), the new town status is confirmed.",
			"# ",
			"# EFFECTS:",
			"# - Town is immune to siege attack.",
			"# - PVP is forced off in the town (configurable)",
			"# - Residents cannot get nation military ranks.",
			"# - Residents get 'war nausea' if they enter siege zones.",
            "# - Town will get automatically occupied if there is an unsieged 'guardian town' from another nation nearby",
			"# ",
			"# GUARDIAN TOWN QUALIFICATION REQUIREMENTS:",
			"# - Min number of plots (configurable) ",
			"# - Close to the peaceful town (distance configurable) ",
			"#"),
	PEACEFUL_TOWNS_CONFIRMATION_REQUIREMENT_DAYS(
			"peaceful_towns.confirmation_requirement_days",
			"5",
			"",
			"# This value determines how long it takes to confirm a town peacefulness status change.",
			"# It is recommended to be high, for use by genuinely peaceful towns, not just for war cost avoidance."),
	PEACEFUL_TOWNS_NEW_TOWN_PEACEFULNESS_ENABLED(
			"peaceful_towns.new_town_peacefulness_enabled",
			"false",
			"",
			"# If this setting is true, then new towns start peaceful"),
	PEACEFUL_TOWNS_NEW_TOWN_CONFIRMATION_REQUIREMENT_DAYS(
			"peaceful_towns.new_town_confirmation_requirement_days",
			"2",
			"",
			"# This setting applies only in the first week after a town is founded.",
			"# The value determines the countdown duration to be applied to any peacefulness switch."),
	PEACEFUL_TOWNS_ALLOWED_TO_TOGGLE_PVP(
			"peaceful_towns.allowed_to_toggle_pvp",
			"false",
			"",
			"# If this setting is true, then peaceful towns can toggle PVP on and off."),
	PEACEFUL_TOWNS_GUARDIAN_TOWN_PLOTS_REQUIREMENT(
			"war.siege.peaceful_towns.guardian_town_plots_requirement",
			"30",
			"",
			"# This value determines how many plots a town has to have,",
			"# to be considered a guardian town."),
	PEACEFUL_TOWNS_GUARDIAN_TOWN_MIN_DISTANCE_REQUIREMENT_TOWNBLOCKS(
			"war.siege.peaceful_towns.guardian_town_min_distance_requirement_townblocks",
			"75",
			"",
			"# This value determines how close a town has to be to a peaceful town,",
			"# to be considered a guardian town."),

	OCCUPIED_TOWNS("occupied_towns",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |              Occupied Towns settings                 | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	//Occupied town unclaiming
	OCCUPIED_TOWN_UNCLAIMING_DISABLED(
			"occupied_towns.occupied_town_unclaiming_disabled",
			"true",
			"",
			"# If this value is true, then a town under occupation cannot unclaim.",
			"#  This setting is recommended, to avoid occupation escape exploits."),

	PUNISH_NON_SIEGE_PARTICIPANTS_IN_SIEGE_ZONE(
			"punish_non_siege_participants_in_siege_zone",
			"",
			"############################################################",
			"# +------------------------------------------------------+ #",
			"# |                     War Sickness                     | #",
			"# +------------------------------------------------------+ #",
			"############################################################",
			""),

	ENABLE_SICKNESS(
			"punish_non_siege_participants_in_siege_zone.enable_sickness",
			"false",
			"",
			"# If true, players that are not participating in a siege will receive war sickness",
			"# A non-participant is a player who does not have a military rank, is not allied to either the attacker or the defender, or is peaceful.",
			"# There are two types of war sickness, full and special.",
			"# Special war sickness is only given if a non-participant is at his town that happened to be in a siege zone",
			"#   - Effects: Weakness V",
			"# Full sickness is given to all players that are not allied to either side, do not have a military rank, or is peaceful, and are not in their own town.",
			"#   - Effects: Nausea V, Poison V, Weakness V, Slowness III, Mining Fatigue III"
	),

	SECONDS_BEFORE_SICKNESS(
			"punish_non_siege_participants_in_siege_zone.seconds_warning",
			"5",
			"",
			"# This is how many seconds a player has to leave the siege zone before he gets war sickness",
			"# If this is set to 0, no warn will be given and non-participants will receive war sickness instantly, if enabled"
	),

	CANNONS_INTEGRATION("cannons_integration",
			"",
			"############################################################",
			"# +------------------------------------------------------+ #",
			"# |              Cannons Integration                     | #",
			"# +------------------------------------------------------+ #",
			"############################################################",
			""),
	CANNONS_INTEGRATION_ENABLED (
			"cannons_integration.enabled",
			"false",
			"",
			"# If this value is true, then the integration with the Cannons plugin is enabled.",
			"# The integration works as follows:",
			"# 1. While a town is not under siege: Normal Towny mechanics apply.",
			"# 2. While a town is under siege:",
			"#    - Town cannons cannot be fired unless there is a 'cannon session' in effect.",
			"#    - A cannon session starts/refreshes when a town resident with the 'siegewar.town.siege.startcannonsession' fires a town cannon.",
			"#    - While a cannon session is in effect, town cannons can be fired, and town explosion perm protections are forced off.",
			"#    - The cannon session usually lasts just a few minutes (don't make it too long or the defender will often be too scared to fire).",
			"#    - The max duration is configured below.",
			"# "),
	CANNONS_INTEGRATION_MAX_CANNON_SESSION_DURATION(
			"cannons_integration.max_cannon_session_duration",
			"9",
			"# This value determines the max duration of each cannon session,",
			"# The duration is 'in short ticks', typically a short tick is 20 seconds."),
	
	BEACON_MARKERS(
			"beacon_markers",
			"",
			"############################################################",
			"# +------------------------------------------------------+ #",
			"# |                   Beacon Markers                     | #",
			"# +------------------------------------------------------+ #",
			"############################################################",
			""),
	BEACON_MARKERS_ENABLED(
			"beacon_markers.enabled",
			"true",
			"",
			"# If enabled, client-side beacons will be shown for players at the siege banner while they are in a siege zone."),
	BEACON_MARKERS_CAPTURE_COLOR(
			"beacon_markers.capture_color",
			"yellow",
			"",
			"# The color that the beacon will be while a player is capturing it. Only visible to the player.",
			"# Accepts any of the following colors (case insensitive): White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown, Green, Red & Black.",
			"# Defaults to yellow if no valid color is entered."),
	BEACON_MARKERS_CAPTURED_COLOR(
			"beacon_markers.captured_color",
			"green",
			"",
			"# The color that the beacon will be for a player when their side has control of the banner.",
			"# See above for valid colors.",
			"# Defaults to green if no valid color is entered."),
	BEACON_MARKERS_ENEMY_COLOR(
			"beacon_markers.enemy_color",
			"red",
			"",
			"# The color that the beacon will be for a player when the enemy side has control of the banner.",
			"# See above for valid colors.",
			"# Defaults to red if no valid color is entered."),

	BANNER_XYZ_TEXT(
			"banner_xyz_text",
			"",
			"############################################################",
			"# +------------------------------------------------------+ #",
			"# |                   Banner XYZ Text                    | #",
			"# +------------------------------------------------------+ #",
			"############################################################",
			""),
	BANNER_XYZ_TEXT_ENABLED(
			"banner_xyz_text.enabled",
			"false",
			"# The banner xyz text is an alternative to beacon markers for siege banners (but they can also be used together).",
			"# If enabled, besieged towns will show the XYZ of the siege banner on their town screens."),

	POST_WAR_NATION_IMMUNITY(
		"post_war_nation_immunity",
			"",
			"############################################################",
			"# +------------------------------------------------------------+ #",
			"# |                  Post-War Nation Immunity                  | #",
			"# +------------------------------------------------------------+ #",
			"#######################################################################",
			""),
	POST_WAR_NATION_IMMUNITY_ENABLED(
			"post_war_nation_immunity.enabled",
			"true",
			"",
			"# If this setting is true, then post-war nation immunity is enabled.",
			"# ",
			"# This feature gives each nation regular full-breaks from war, ",
			"# by granting siege-immunity to all their towns after a home-defence war.",
			"#  (A home-defence war occurs when one or ore of a nation's towns are under non-revolt sieges)",
			"# ",
			"# NOTES:",
			"# 1. To mitigate exploits, nations suffer some mild negative effects while fighting a home-defence war - cannot claim/unclaim/recruit.",
			"# 2. To mitigate annoyance-sieges, the cost to attack a nation's towns increases as that nation increases in size.",
			"# 3. If a town switches home nation during a home-defence war",
			"#     then when the home-defence war ends, the post-war immunity from that town,",
			"#     is distributed between all the home nations which the town had during the siege."),
	POST_WAR_NATION_IMMUNITY_DURATION_MODIFIER(
			"post_war_nation_immunity.duration_modifier",
			"0.75",
			"",
			"# This setting determines the duration ofsiege immunity which each nation town receives after a war period,",
			"# ",
			"# EXAMPLE:",
			"# If this value is 0.75, and a nation fights continuously in home defence for 4 days,",
			"# then at the end of the fighting, all nation hometowns will receive siege immunity of 3 days."),
	POST_WAR_NATION_IMMUNITY_HOME_TOWN_CONTRIBUTION_TO_ATTACK_COST(
			"post_war_nation_immunity.home_town_contribution_to_attack_cost",
			"0.1",
			"",
			"# If this setting is higher than 0,",
			"# then the larger a nation is, the higher the cost to attack one of its towns.",
			"# ",
			"# This setting is important to mitigate 'annoyance sieges', ",
			"# because large nations are disproportionately affected by the mild nation-under-siege costs.",
			"# Thus without mitigation, small nations could attack larger ones,",
			"# with no intention of fighting, but just to cheaply disrupt them.",
			"# ",
			"# EXAMPLE:",
			"# If this setting is 0.1,",
			"# and an attack is attempted on a nation town,",
			"# then for every home town in that nation (including the attacked one),",
			"# the attack-cost (i.e. warchest) requirement is increased by 10% of the amount it would take to attack that town.");

	private final String Root;
	private final String Default;
	private String[] comments;

	ConfigNodes(String root, String def, String... comments) {

		this.Root = root;
		this.Default = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return Root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return Default;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}

}
