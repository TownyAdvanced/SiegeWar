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
			"# This action will steal money from the town.",
			"# If the town does not have sufficient funds, it will be bankrupted/ruined/destroyed."),
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
	WAR_SIEGE_EXPLOSIONS_ALWAYS_ON_IN_BESIEGED_TOWNS(
			"war.siege.switches.explosions_always_on_in_besieged_towns",
			"false",
			"",
			"# If true, then town explosions are always set to on during sieges.",
			"# The town explosions flag returns to its previous value when the siege ends.",
			"# The setting is false by default, because SiegeWar is designed to be minimally-destructive.",
			"# The setting is only recommended for use in combination with a block regeneration feature."),
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
	WAR_SIEGE_POPULATION_BASED_POINT_BOOSTS_ENABLED(
			"war.siege.switches.population_based_point_boosts_enabled",
			"false",
			"",
			"# If this setting is true, then the siege side with the lower population gets a boost to their siege point gains.",
			"# The attacking side population consists of the residents of the attacking nation, and allies.",
			"# The defending side population consists of the residents of the defending town, and nation + allies if applicable.",
			"# The level of the boost is configured in separate configs. See the scoring section of this file."),
	WAR_SIEGE_COUNTERATTACK_BOOSTER_ENABLED(
			"war.siege.switches.counterattack_booster_enabled",
			"false",
			"",
			"# If this setting is true, and a player from the banner controlling side dies,",
			"# then the death points are increased by a certain percentage. (see points section below)",
			"# This setting gives smaller and weaker towns/nations a better chance, as they will tend to be the counter-attackers."),

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
		"999",
		"",
		"# The value specifies the maximum number of active/in-progress sieges allowed per nation." +
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
	WAR_SIEGE_BANNER_CONTROL_HORIZONTAL_DISTANCE_BLOCKS(
			"war.siege.distances.banner_control_horizontal_distance_blocks",
			"16",
			"",
			"# This is the horizontal distance a soldier must be from the banner to get banner control."),
	WAR_SIEGE_BANNER_CONTROL_VERTICAL_DISTANCE_BLOCKS(
			"war.siege.distances.banner_control_vertical_distance_blocks",
			"16",
			"",
			"# This is the vertical distance a soldier must be from the banner to get banner control."),
	WAR_SIEGE_LEADERSHIP_AURA_RADIUS_BLOCKS(
		"war.siege.distances.leadership_aura_radius_blocks",
			"50",
			"",
			"# This setting determines the size of the 'Military Leadership Aura'.",
			"# The aura emanates from kings, generals, and captains.",
			"# The aura decreases death point losses for nearby nation/allied soldiers in a siege.",
			"# The aura increases death point gains for nearby enemy soldiers in a siege."),

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
	WAR_SIEGE_POINTS_PERCENTAGE_ADJUSTMENT_FOR_LEADER_PROXIMITY(
			"war.siege.scoring.percentage_adjustment_for_leader_proximity",
			"10",
			"",
			"# If a friendly military leader is nearby when a soldier dies in a siege, then points loss is reduced by this percentage.",
			"# If an enemy military leader is nearby when a soldier dies in a siege, then points loss is increased by this percentage."),
	WAR_SIEGE_POINTS_PERCENTAGE_ADJUSTMENT_FOR_LEADER_DEATH(
			"war.siege.scoring.percentage_adjustment_for_leader_death",
			"50",
			"",
			"# If a military leader dies in a siege, then points loss in increased by this percentage."),
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
	WAR_SIEGE_COUNTERATTACK_BOOSTER_EXTRA_DEATH_POINTS_PER_PLAYER_PERCENT(
			"war.siege.scoring.counterattack_booster_extra_death_points_per_player_percent",
			"5.0",
			"",
			"# If the counterattack booster feature is enabled, then this setting determines the strength of the boost.",
			"# Example: If this setting is 5.0, and there are 3 players on the banner control list, and a player from the controlling side dies,",
			"# then the death points will be increased by 15%."),

	//Siege-war specific peaceful towns
	WAR_SIEGE_PEACEFUL_TOWNS_GUARDIAN_TOWN_PLOTS_REQUIREMENT(
			"war.siege.peaceful_towns.guardian_town_plots_requirement",
			"30",
			"",
			"# This value determines how many plots a town has to have,",
			"# to be considered a guardian town."),
	WAR_SIEGE_PEACEFUL_TOWNS_GUARDIAN_TOWN_MIN_DISTANCE_REQUIREMENT_TOWNBLOCKS(
			"war.siege.peaceful_towns.guardian_town_min_distance_requirement_townblocks",
			"75",
			"",
			"# This value determines how close a town has to be to a peaceful town,",
			"# to be considered a guardian town."),

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

	//Tactical Visibility
	//Todo - Eventually move this to another location as it works regardless of war system, or without.
	WAR_SIEGE_TACTICAL_VISIBILITY_ENABLED(
			"war.siege.switches.tactical_visibility_enabled",
			"true",
			"",
			"# If this setting is true, then the tactical invisibility feature is enabled",
			"# PREREQUISITES: ",
			"# * You must have deployed a dynmap jar containing support for tactical invisibility.",
			"# * In your dynmap config, tactical-invisibility must be enabled.",
			"# ",
			"# DESCRIPTION",
			"# * This feature is critical to enable normal military tactics such as ambushing.",
			"# * The feature works as follows:",	
			"# * Player in a banner control session - Always visible on map.",
			"# * Player with certain items in their hands (configured below) - Invisible on map.",
			"# * ",
			"# * NOTE: Any additional dynmap config settings for map invisibility will override the 'always visible' scenarios above."),
	WAR_SIEGE_TACTICAL_VISIBILITY_ITEMS(
			"war.siege.items.tactical_visibility_items",
			"compass|diamond_sword, compass|bow",
			"",
			"# This list specifies the items which make players tactically invisible. ",
			"# Each list entry is in the format of <off-hand>|<main-hand>.",
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
			"# If this setting is true then military ranked players keep inventory on siege-zone death.",
			"# ",
			"# This keeps the game fun for new and casual players.",
			"# The alternative of removing all equipment when a player dies in battle, and handing it to the player's enemy,",
			"# is a poor design generally, rarely found in modern online war games:",
			"# e.g.",
			"# * World of Warcraft, Neverwinter, LOTR Online: Player keeps inventory.",
			"# * Battlefield, Call-of-duty: Player keeps 'grinded/unlocked' equipment e.g. gun types, special grenades etc.",
			"# * League of Legends, Dota: Player keeps 'grinded/unlocked' skills/equipment",
			"# ",
			"# Configurations:",
			"# True - Expect small/casual nations and players to have an influence on world affairs.",
			"# False - Expect small/casual nations and players to have little influence on world affairs, with large/dedicated nations & players dominating."),
	WAR_SIEGE_DEATH_PENALTY_DEGRADE_INVENTORY_ENABLED(
			"war.siege.death_penalty.degrade_inventory.enabled",
			"true",
			"",
			"# If this setting is true then when military ranked players die in a siege zone, their equipment degrades a little. (e.g. 10%)",
			"# This setting is commonly used in combination with 'WAR_SIEGE_KEEP_INVENTORY_ON_SIEGE_DEATH',",
			"# which thus sets up the death-penalty to be similar to online RPG's such as WOW, or LOTR"),
	WAR_SIEGE_DEATH_PENALTY_DEGRADE_INVENTORY_PERCENTAGE(
			"war.siege.death_penalty.degrade_inventory.percentage",
			"10.0",
			"",
			"# This values specifies the percentage equipment degradation that occurs to a soldiers equipment,",
			"# when they die in a siege zone."),

	PEACEFUL_TOWNS(
		"peaceful_towns",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |               Peacful Towns settings                 | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	PEACEFUL_TOWNS_ENABLED(
			"peaceful_towns.enabled",
			"true",
			"",
			"# If this is true, then a town can toggle peacefulness,",
			"# After a countdown (usually in days), the new town status is confirmed.",
			"# The status has different effects depending on the war-system",
			"# ",
			"# COMMON:",
			"# 1. PVP is forced off in the town (configurable)",
			"# 2. A resident who leaves such a town cannot inflict PVP for a (configured) number of hours.",
			"# 3. (optional) T spawn is enabled, even if it is normally off via config.",
			"# 4. (optional) Nationality is disregarded for spawn purposes (for residents and visitors to the town).",
			"# ",
			"# SIEGEWAR:", 
			"# 1. Town is immune to attack ",
			"# 2. Town is immune to most power-diplomacy (e.g. 'join my nation pls pls pls').",
			"#    This second benefit is achieved by hitching the political status of the town to a nearby 'Guardian Town' (and removing the choice from the town).",
			"# 	  Thus if the guardian town joins/leaves a nation, the peaceful town automatically does the same (on new day).",
			"#    The following are required for a guardian town",
			"#    - Close to the peaceful town (configurable) ",
			"#    - Min population (configurable) ",
			"#    - Min number of plots (configurable) ",
			"#    - Not under siege ",
			"#    - Not affected by a recent siege (i.e. immunity finished) ",
			"#    - The largest qualifying town (size is determined by pop * plots) ",
			"# 3. Residents cannot get nation military ranks.",
			"# 4. Residents get 'war nausea' if they enter siege zones.",
			"# ",
			"# PLAYER TIPS:",
			"# If a town is in any of the following scenarios, it may find the feature useful:",
			"# 1. Town is building strength and preparing for war, but not yet ready to handle war costs.",
			"# 2. Town is currently in a hopeless geo-political position e.g. completely surrounded by much more powerful enemies,",
			"# 3. Town is interested in politics, but prefers to influence world events via diplomatic/economic methods rather than military strength.",
			"# 4. Town is not currently interested much in war/politics, and just wants to build/trade/explore in peace.",
		    "# ",
			"# SERVER TIPS",
			"# This option is recommended as a 'safety valve'",
			"# E.g. if something has been badly coded or misconfigured,", 
			"# this option gives players an off-ramp from the system without flooding staff w/ tickets,"),
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
			"#  This setting is recommended, to avoid occupation escape exploits.");	

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
