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
	WAR_SIEGE_ATTACK_ENABLED(
			"war.siege.switches.attack_enabled",
			"true",
			"",
			"# If true, then nations can start sieges."),
	WAR_SIEGE_ABANDON_ENABLED(
			"war.siege.switches.abandon_enabled",
			"true",
			"",
			"# If true, then nations can abandon sieges."),
	WAR_SIEGE_TOWN_SURRENDER_ENABLED(
			"war.siege.switches.town_surrender_enabled",
			"true",
			"",
			"# If true, then a town can surrender."),
	WAR_SIEGE_INVADE_ENABLED(
			"war.siege.switches.invade_enabled",
			"true",
			"",
			"# If true, then a nation siege winner can invade the defeated town.",
			"# This action will add the town to the nation"),
	WAR_SIEGE_PLUNDER_ENABLED(
			"war.siege.switches.plunder_enabled",
			"true",
			"",
			"# If true, then a nation siege winner can plunder the defeated town.",
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
	WAR_SIEGE_TOWN_LEAVE_DISABLED(
			"war.siege.switches.nation_leave_disabled",
			"true",
			"",
			"#. If true, then a town cannot leave a nation of its own accord. ",
			"# However the nation can always kick."),
	WAR_SIEGE_REVOLT_ENABLED(
			"war.siege.switches.revolt_enabled",
			"true",
			"",
			"#. If true, then a town can 'revolt' against the nation and leave",
			 "# Usually enabled in combination with WAR_SIEGE_TOWN_LEAVE_DISABLED"),
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
	WAR_SIEGE_COUNTERATTACK_BOOSTER_DISABLED(
			"war.siege.switches.counterattack_booster_disabled",
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

	//Monetary Values
	WAR_SIEGE_ATTACKER_COST_UPFRONT_PER_PLOT(
			"war.siege.money.attacker_cost_upfront_per_plot",
			"20.0",
			"",
			"# This value represents the siege deposit paid by the attacker (aka warchest).",
			"# This value is recovered by the siege winner, whether attack or defender."),
	WAR_SIEGE_ATTACKER_PLUNDER_AMOUNT_PER_PLOT(
			"war.siege.money.attacker_plunder_amount_per_plot",
			"40.0",
			"",
			"# This is the amount plundered by the attacker is a siege is successful.",
			"# It is recommended that the 'attack-cost:plunder-reward' ratio be about 1:2 ."),
	WAR_SIEGE_ATTACKER_PLUNDER_DISTRIBUTION_RATIO(
			"war.siege.money.attacker_plunder_distribution_ratio",
			"1:3",
			"",
			"# This ratio affects how plunder is distributed within the plundering nation",
			"# The value on the left is the ratio which will go to the nation bank.",
			"# The value on the right is the ratio which will go to the nation soldiers",
			"# The share each soldier receives depends on their rank.",
			"# Plunder can be collected later using /sw collect.",
			"# The rank permission which affects is towny.nation.siege.pay.grade.x",
			"# X can be any integer value.",
			"# Usually a value of 100 is a normal share, and a value of 200 is a double share"),
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
			"# When the nation is deleted or defeated."),
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
	WAR_SIEGE_REVOLT_IMMUNITY_TIME_HOURS(
			"war.siege.times.revolt_immunity_time_hours",
			"240",
			"",
			"# This value determines how long a town must wait before it can revolt against an occupying nation nation. The immunity time gets set to the given value if a town is captured, or if it revolts.",
			"# If the value is too high, towns will be frustrated that it is too difficult to revolt against an occupier.",
			"# If the value is too low, nations will find it difficult to hold territory due to constant revolts."),
	WAR_SIEGE_BANNER_CONTROL_SESSION_DURATION_MINUTES (
			"war.siege.times.banner_control_session_duration_minutes",
			"10",
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

	//Siege points
	WAR_SIEGE_POINTS_FOR_ATTACKER_OCCUPATION(
			"war.siege.scoring.points_for_attacker_occupation",
			"10",
			"",
			"# This setting determines the number of siege points awarded to an occupying attacker.",
			"# The points are awarded if a player remains within a town-block length of the siege banner for: ",
			"# 10 minutes (default configuration)."),
	WAR_SIEGE_POINTS_FOR_DEFENDER_OCCUPATION(
			"war.siege.scoring.points_for_defender_occupation",
			"10",
			"",
			"# This setting determines the number of siege points awarded to an occupying defender.",
			"# The points are awarded if a player remains within a town-block length of the siege banner for ",
			"# 10 minutes (default configuration)."),
	WAR_SIEGE_POINTS_FOR_ATTACKER_DEATH(
			"war.siege.scoring.points_for_attacker_death",
			"150",
			"",
			"# This setting determines the number of siege points awarded if an attacker dies.",
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
	WAR_SIEGE_POINTS_FOR_DEFENDER_DEATH(
			"war.siege.scoring.points_for_defender_death",
			"150",
			"",
			"# This setting determines the number of siege points awarded if a defender dies.",
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
			"# 3. In this example, if the siege defender scores any siege points, the points will be boosted by the (separately configured) maximum.",
            "# 4. In this example, the siege attacker will not get any points boosts."),
	WAR_SIEGE_MAX_POPULATION_BASED_POINTS_BOOST(
			"war.siege.scoring.max_population_based_points_boost",
			"2.0",
			"",
			"# This setting determines the maximum points boost which a siege side can get due to population.",
			"# Example:",
			"# 1. Assume this value is set to 2.0.",
			"# 2. Assume that a siege attacker greatly outnumbers a siege defender in population. (also counting allies)",
			"# 3. In this example, if the siege defender scores any siege points, the points will be multiplied by 2.",
			"# 4. In this example, the siege attacker will not get any points boosts."),

	//Battle Sessions
	WAR_SIEGE_BATTLE_SESSIONS_ENABLED(
			"war.siege.battle_sessions.enabled",
			"true",
			"",
			"# If this setting is true, then battle sessions are enabled.",
			"# Battle sessions have 2 functions:",
			"# 1. They automatically moderate the time each player spends siege-fighting.",
			"# 2. They prevent certain blocks (e.g. obsidian) being placed while siege-fighting.",
			"# ",
			"# This feature is recommended to reduce stress and combat-fatigue.",
			"# ",
			"# A battle session starts when a player steps into a siege zone (unless they are in their own town).",
			"# A battle session has 2 main phases:",
			"# * phase 1 - active - In this phase, the player can attend any siege.",
			"# * phase 2 - expired - In this phase the player cannot attend any siege (without getting 'battle fatigue' - poisoned/slowed/weakened).",
			"# The durations of these phases are configured below."),
	WAR_SIEGE_BATTLE_SESSIONS_ACTIVE_PHASE_DURATION_MINUTES(
			"war.siege.battle_sessions.active_phase_duration_minutes",
			"60",
			"",
			"# This value determines the duration of the battle session active phase."),
	WAR_SIEGE_BATTLE_SESSIONS_EXPIRED_PHASE_DURATION_MINUTES(
			"war.siege.battle_sessions.expired_phase_duration_minutes",
			"10",
			"",
			"# This value determines the duration of the battle session expired phase."),
	WAR_SIEGE_BATTLE_SESSIONS_FIRST_WARNING_MINUTES_TO_EXPIRY(
			"war.siege.battle_sessions.first_warning_minutes_to_expiry",
			"5",
			"",
			"# This value determines the number of minutes between first warning and expiry."),
	WAR_SIEGE_BATTLE_SESSIONS_SECOND_WARNING_MINUTES_TO_EXPIRY(
			"war.siege.battle_sessions.second_warning_minutes_to_expiry",
			"1",
			"",
			"# This value determines the number of minutes between second warning and expiry."),

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
			"# - Town cannot be plundered. ",
			"# - Town cannot be taxed. ",
			"# - PVP is forced off in the town (configurable)",
			"# - T spawn is enabled, even if it is normally disabled for towns. (configurable)",
			"# - Nationality is disregarded for spawn purposes (for residents and visitors to the town).",
			"# - Residents cannot get nation military ranks.",
			"# - Residents get 'war nausea' if they enter siege zones.",
			"# - Nation Choice is limited as follows: ",
			"#   1. The town can only join a nation if there is a 'Guardian Town' of that nation nearby: ",
			"#   2. If the nearby set of Guardian Towns changes, the town's nation may automatically change also: ",
			"# ",
			"# GUARDIAN TOWN QUALIFICATION REQUIREMENTS:",
			"# - Min number of plots (configurable) ",
			"# - Close to the peaceful town (distance configurable) ",
			"# - Not under active siege ",
			"# ",
			"# WARNING:",
			"# Peaceful towns must be very careful if they want to be nation capitals, to avoid automatic nation disbands.",
			"# If your players are not careful and are known for not reading instruction manuals,",
			"# then consider enabling the 'allowed_to_make_nation' setting below."),
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
	PEACEFUL_TOWNS_ALLOWED_TO_MAKE_NATION(
			"peaceful_towns.allowed_to_make_nation",
			"false",
			"",
			"# If this setting is true, then peaceful towns can found nations."),
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
	PEACEFUL_TOWNS_PUBLIC_SPAWNING(
			"peaceful_towns.public_spawning",
			"true",
			"",
			"# This setting is essential to allow peaceful players to play in their preferred style.",
			"# For example, in servers with no war, a peaceful town can create and maintain a nation.",
			"# This (usually) gives the town a public /n spawn, an important asset which delivers visitors and shop customers directly to the town.",
			"# Also the nation can change allies/enemies, affecting visitors to the public /t spawn of the town (if there is one).",
			"# However with SiegeWar, peaceful towns cannot (and must not) be able to maintain a nation",
			"# Thus, without this setting, peaceful players would be denied control of a key asset (public spawn in the town) which they could get on other servers, and would be more likely to quit or never join."),
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
			"# ",
			"# WARNING: ",
			"# Do not enable this feature unless the following issue is resolved",
			"# (Either in the Cannons plugin, Towny plugin, or your own custom branch of either) - ",
			"# https://github.com/DerPavlov/Cannons/pull/37."),
	CANNONS_INTEGRATION_MAX_CANNON_SESSION_DURATION(
			"cannons_integration.max_cannon_session_duration",
			"9",
			"# This value determines the max duration of each cannon session,",
			"# The duration is 'in short ticks', typically a short tick is 20 seconds."
	);

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
