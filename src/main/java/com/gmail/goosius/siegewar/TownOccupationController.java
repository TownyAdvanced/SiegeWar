package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TownOccupationController {

    private static Map<Nation, Set<Town>> nationTownsOccupationMap = new HashMap<>();
    private final static String NATION_TOWNS_OCCUPATION_MAP_LOCK = "";

    public static void clearTownOccupations() {
        nationTownsOccupationMap.clear();
    }

    public static boolean loadAll() {
        try {
            SiegeWar.info("Loading town occupation data...");
            loadTownOccupationData();
            SiegeWar.info("Town occupation data loaded.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	public static void loadTownOccupationData() {
		Nation occupyingNation = null;
		for (Town town : TownyUniverse.getInstance().getTowns()) {
			occupyingNation = loadOccupationData(town);
			if (occupyingNation == null)
				continue;
			// Populate the map
			if (nationTownsOccupationMap.containsKey(occupyingNation))
				nationTownsOccupationMap.get(occupyingNation).add(town);
			else
				nationTownsOccupationMap.put(occupyingNation, Collections.singleton(town));
		}
	}

	private static Nation loadOccupationData(Town town) {
		String occupyingNationUUID = TownMetaDataController.getOccupyingNationUUID(town);
		// Occupier uuid not found.
		if (occupyingNationUUID == null) {
			if (town.isConquered() && SiegeWarSettings.getWarCommonOccupiedTownCorrectConquerStatus()) // Fixed data if required.
				setConqueredInTowny(town, false);
			return null;
		}

		// Occupier uuid found in MetaData DB, get the Nation or null from TownyUniverse.
		Nation occupyingNation = TownyUniverse.getInstance().getNation(UUID.fromString(occupyingNationUUID));

		// Nation is not known to Towny.
		if (occupyingNation == null) {
			if (town.isConquered()) // Fixed data if required.
				setConqueredInTowny(town, false);
			return null;
		}

		// Nation is known to towny. Return discovered occupying nation.
		if (!town.isConquered()) // Fixed data if required. 
			setConqueredInTowny(town, true);

		return occupyingNation;
	}

	public static List<Town> getOccupiedForeignTowns(Nation nation) {
		// Get either a Set of (Occupied) Towns or an emptySet, then parse out any towns
		// which are home towns (natural towns in the nation proper,) then turn it into
		// a List.
		return new HashMap<>(nationTownsOccupationMap).getOrDefault(nation, Collections.emptySet()).stream()
				.filter(town -> !town.hasNation() || town.getNationOrNull() != nation)
				.collect(Collectors.toList());
	}
	
	public static List<Town> getOccupiedHomeTowns(Nation nation) {
		// Get either a Set of (Occupied) Towns or an emptySet, then parse out any towns
		// which are foreign towns (not properly part of the nation,) then turn it into
		// a List.
		return new HashMap<>(nationTownsOccupationMap).getOrDefault(nation, Collections.emptySet()).stream()
				.filter(town -> town.hasNation() && town.getNationOrNull() == nation)
				.collect(Collectors.toList());
	}

    /**
     * Determine if a town is occupied.
     *
     * In the SW codebase, do NOT use town.isConquered() for that purpose.
     *
     * Because the logic behind town.conquered is not controlled by siegewar.
     * For example, Towny may potentially set it to false if a town leaves a nation.
     *
     * Thus, town.isConquered() is never used within siegewar (except to correct errors).
     * It is used within Towny, to display whether a town is occupied.
     *
     */
     public static boolean isTownOccupied(Town occupiedTown) {
         if (TownMetaDataController.getOccupyingNationUUID(occupiedTown) == null) {
             // This town isn't occupied according to SiegeWar.
             if(occupiedTown.isConquered() && SiegeWarSettings.getWarCommonOccupiedTownCorrectConquerStatus())
                 setConqueredInTowny(occupiedTown, false); //Fix data if required and set up to do so in the config.
             return false;
         }
         // This town is occupied. 
         if (!occupiedTown.isConquered())
             setConqueredInTowny(occupiedTown, true); //Fix data if required
         return true;
    }

	/**
	 * Get the occupying nation
	 *
	 * If there is any uncertainty whether the town is actually occupied, make sure
	 * to call isTownOccupied(town) before calling this method
	 *
	 * @param occupiedTown the occupied town
	 * @return the occupying nation.
	 * @throws RuntimeException if no occupier is found
	 */
	public static Nation getTownOccupier(Town occupiedTown) {
		String occupierUUID = TownMetaDataController.getOccupyingNationUUID(occupiedTown);
		if (occupierUUID == null)
			throw new RuntimeException("Occupier not found");

		Nation nation = TownyUniverse.getInstance().getNation(UUID.fromString(occupierUUID));
		if (nation == null) {
			// Nation could not be loaded. Fix data
			TownMetaDataController.removeOccupationMetadata(occupiedTown);
			if (SiegeWarSettings.getWarCommonOccupiedTownCorrectConquerStatus()) // Fix data if required
				setConqueredInTowny(occupiedTown, false);
			throw new RuntimeException("Error loading occupier data for " + occupiedTown.getName() + " Data fixed automatically by de-occupying town");
		}

		if (!occupiedTown.isConquered())
			setConqueredInTowny(occupiedTown, true); // Fix data if required

		return nation;
	}

    public static void removeTownOccupation(Town occupiedTown) {
        //Remove occupation
        TownMetaDataController.removeOccupationMetadata(occupiedTown);
        setConqueredInTowny(occupiedTown, false);
        //Adjust occupation map
        synchronized (NATION_TOWNS_OCCUPATION_MAP_LOCK) {
            //Remove town if it appears anywhere in the occupation map
            for(Map.Entry<Nation, Set<Town>> mapEntry: new HashMap<>(nationTownsOccupationMap).entrySet()) {
                if(mapEntry.getValue().contains(occupiedTown)) {
                    if(mapEntry.getValue().size() == 1) {
                        //This it the only town on the list
                        nationTownsOccupationMap.remove(mapEntry.getKey());
                    } else {
                        //There are more towns on the list
                        nationTownsOccupationMap.get(mapEntry.getKey()).remove(occupiedTown);
                    }
                    return; //Set operation finished
                }
            }
        }
    }

    public static void setTownOccupation(Town occupiedTown, @NotNull Nation occupyingNation) {
        //Remove any existing occupation
        removeTownOccupation(occupiedTown);
        //Add occupation
        TownMetaDataController.setOccupyingNationUUID(occupiedTown, occupyingNation.getUUID().toString());
        setConqueredInTowny(occupiedTown, true);
        //Adjust occupation map
        synchronized (NATION_TOWNS_OCCUPATION_MAP_LOCK) {
            if (nationTownsOccupationMap.containsKey(occupyingNation)) {
                //Nation already on map
                nationTownsOccupationMap.get(occupyingNation).add(occupiedTown);
            } else {
                //Nation not yet on map
                nationTownsOccupationMap.put(occupyingNation, Collections.singleton(occupiedTown));
            }
        }
    }

    public static void removeForeignTownOccupations(Nation nation) {
        synchronized (NATION_TOWNS_OCCUPATION_MAP_LOCK) {
            if(nationTownsOccupationMap.containsKey(nation)) {
                for(Town occupiedTown: nationTownsOccupationMap.get(nation)) {
                    TownMetaDataController.removeOccupationMetadata(occupiedTown);
                    setConqueredInTowny(occupiedTown, false);
                }
                nationTownsOccupationMap.remove(nation);
            }
        }
    }

    public static Set<String> getAllOccupiedTownNames() {
        Set<String> result = new HashSet<>();
        for (Set<Town> occupiedTowns : new HashMap<>(nationTownsOccupationMap).values()) {
            for(Town occupiedTown: occupiedTowns) {
                result.add(occupiedTown.getName());
            }
        }
        return result;
    }
    
	private static void setConqueredInTowny(Town town, boolean conquered) {
		town.setConquered(conquered);
		town.save();
	}
}

