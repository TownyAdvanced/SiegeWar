package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.metadata.SiegeMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.*;

public class TownOccupationController {

    private static Map<Nation, List<Town>> nationTownsOccupationMap = new HashMap<>();
    private final static String NATION_TOWNS_OCCUPATION_MAP_LOCK = "";

    public static void clearTownOccupations() {
        nationTownsOccupationMap.clear();
    }

    public static boolean loadAll() {
        try {
            System.out.println(SiegeWar.prefix + "Loading town occupation data...");
            loadTownOccupationData();
            System.out.println(SiegeWar.prefix + "Town occupation data loaded.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void loadTownOccupationData() {
        String occupyingNationUUID;
        Nation occupyingNation = null;
        List<Town> occupiedTowns;

        for (Town town : TownyUniverse.getInstance().getTowns()) {
            //Load occupying nation data
            occupyingNationUUID = TownMetaDataController.getOccupyingNationUUID(town);
            if (occupyingNationUUID != null) {
                try {
                    occupyingNation = TownyUniverse.getInstance().getDataSource().getNation(UUID.fromString(SiegeMetaDataController.getNationUUID(town)));
                } catch (NotRegisteredException e) {
                    //Fix data - no occupier, so conquered must be false
                    town.setConquered(false);
                    town.save();
                    continue; //Next town
                }
            } else {
                if (town.isConquered()) {
                    //Fix data - no occupier, so conquered must be false
                    town.setConquered(false);
                    town.save();
                    continue; //Next town
                }
            }

            //At this point, valid occupier data has been found
            //Populate the map
            if (nationTownsOccupationMap.containsKey(occupyingNation)) {
                occupiedTowns = new ArrayList<>();
                occupiedTowns.add(town);
            } else {
                occupiedTowns = nationTownsOccupationMap.get(occupyingNation);
                occupiedTowns.add(town);
            }
            nationTownsOccupationMap.put(occupyingNation, occupiedTowns);
        }
    }

    public static List<Town> getTownsOccupiedByNation(Nation nation) {
        Map<Nation, List<Town>> nationTownsOccupationMapCopy = new HashMap<>(nationTownsOccupationMap);

        if (nationTownsOccupationMapCopy.containsKey(nation)) {
            return new ArrayList<>(nationTownsOccupationMapCopy.get(nation));
        } else {
            return new ArrayList<>();
        }
    }

    public static Nation getTownOccupier(Town occupiedTown) {
        Nation occupier = null;
        if(occupiedTown.isConquered()) {
            String occupierUUID = TownMetaDataController.getOccupyingNationUUID(occupiedTown);
            if (occupierUUID == null)
                return null;

            occupier = TownyUniverse.getInstance().getNation(occupierUUID);
        }
        return occupier;
    }

    public static void setTownOccupier(Town occupiedTown, Nation occupyingNation) {
        if (occupyingNation == null) {
            //Remove occupation
            occupiedTown.setConquered(false);
            TownMetaDataController.removeOccupationMetadata(occupiedTown);
            occupiedTown.save();
            //Adjust occupation map
            synchronized (NATION_TOWNS_OCCUPATION_MAP_LOCK) {
                //Remove town if it appears anywhere in the occupation map
                for(Map.Entry<Nation, List<Town>> mapEntry: new HashMap<>(nationTownsOccupationMap).entrySet()) {
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
        } else {
            //Add occupation
            occupiedTown.setConquered(true);
            TownMetaDataController.setOccupyingNationUUID(occupiedTown, occupyingNation.getUUID().toString());
            occupiedTown.save();
            //Adjust occupation map
            synchronized (NATION_TOWNS_OCCUPATION_MAP_LOCK) {
                if (nationTownsOccupationMap.containsKey(occupyingNation)) {
                    //Nation already on map
                    List<Town> occupiedTownsList = nationTownsOccupationMap.get(occupyingNation);
                    occupiedTownsList.add(occupiedTown);
                } else {
                    //Nation not yet on map
                    List<Town> occupiedTownsList = new ArrayList<>();
                    occupiedTownsList.add(occupiedTown);
                    nationTownsOccupationMap.put(occupyingNation, occupiedTownsList);
                }
            }
        }
    }

    public static void removeTownOccupations(Nation nation) {
        synchronized (NATION_TOWNS_OCCUPATION_MAP_LOCK) {
            if(nationTownsOccupationMap.containsKey(nation)) {
                for(Town occupiedTown: nationTownsOccupationMap.get(nation)) {
                    occupiedTown.setConquered(false);
                    TownMetaDataController.removeOccupationMetadata(occupiedTown);
                    occupiedTown.save();
                }
                nationTownsOccupationMap.remove(nation);
            }
        }
    }
}

