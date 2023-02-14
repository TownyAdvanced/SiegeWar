package com.gmail.goosius.siegewar;

import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.metadata.TownMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.MoneyUtil;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TownOccupationController {

    private static final Map<Nation, Set<Town>> nationTownsOccupationMap = new HashMap<>();
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
			else {
                Set<Town> towns = new HashSet<>();
                towns.add(town);

                nationTownsOccupationMap.put(occupyingNation, towns);
            }
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
                Set<Town> towns = new HashSet<>();
                towns.add(occupiedTown);

                nationTownsOccupationMap.put(occupyingNation, towns);
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

	public static void chargeNationOccupationTax() {
		if (!TownyEconomyHandler.isActive())
			return;
		for (Nation nation : nationTownsOccupationMap.keySet()) {
			double tax = NationMetaDataController.getNationOccupationTax(nation); 
			if (tax > 0)
				collectNationOccupationTax(nation, tax);
		}
	}

	private static void collectNationOccupationTax(Nation nation, double tax) {
		for (Town town : new ArrayList<>(nationTownsOccupationMap.get(nation)))
			collectNationOccupationTax(nation, tax, town);
	}

	private static void collectNationOccupationTax(Nation nation, double tax, Town town) {
		if (town.getAccount().canPayFromHoldings(tax)) {
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_occupation_tax_payed", getMoney(tax)));
			town.getAccount().payTo(tax, nation.getAccount(), "Nation Occupation Tax");
			return;
		}

		if (TownySettings.isTownBankruptcyEnabled()) {
			// Set the Town's debtcap fresh.
			town.getAccount().setDebtCap(MoneyUtil.getEstimatedValueOfTown(town));
			double debtCap = town.getAccount().getDebtCap();

			if (town.getAccount().getHoldingBalance() - tax < debtCap * -1) {
				// The Town cannot afford to pay the nation occupation tax.
				Messaging.sendGlobalMessage(Translatable.of("msg_occupation_tax_cannot_be_payed", town.getName()));
				removeTownOccupation(town);
				TownyUniverse.getInstance().getDataSource().removeTown(town);
				return;
			}

			// Charge the town (using .withdraw() which will allow for going into bankruptcy.)
			TownyMessaging.sendPrefixedTownMessage(town, Translatable.of("msg_occupation_tax_payed_bankrupt", getMoney(tax)));
			town.getAccount().payTo(tax, nation.getAccount(), "Nation Occupation Tax");

		} else {
			Messaging.sendGlobalMessage(Translatable.of("msg_occupation_tax_cannot_be_payed", town.getName()));
			removeTownOccupation(town);
			TownyUniverse.getInstance().getDataSource().removeTown(town);
		}
	}

	private static String getMoney(double amount) {
		return TownyEconomyHandler.getFormattedBalance(amount);
	}
}

