package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.utils.SiegeWarNationUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.nation.DisplayedNationsListSortEvent;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Comparator;
import java.util.List;

/**
 * This is a special listener,
 * which only registers when it detects Towny version 0.97.0.14 or greater
 * 
 * When Towny 0.98.0.0 is released, 
 * all methods here should be merged into SiegeWarNationEventListener
 * 
 * @author Goosius
 * 
 */
public class SiegeWar_0_97_0_14_Listener implements Listener {

	@SuppressWarnings("unused")
	private final SiegeWar plugin;

	private static final Comparator<Nation> BY_NUM_RESIDENTS = (n1, n2) -> {
        return SiegeWarNationUtil.getEffectiveNation(n2).getResidents().size() 
          	   - SiegeWarNationUtil.getEffectiveNation(n1).getResidents().size();
    };

    private static final Comparator<Nation> BY_TOWNS= (n1, n2) -> {
        return SiegeWarNationUtil.getEffectiveNation(n2).getNumTowns() 
          	   - SiegeWarNationUtil.getEffectiveNation(n1).getNumTowns();
    };

    private static final Comparator<Nation> BY_TOWNBLOCKS= (n1, n2) -> {
        return SiegeWarNationUtil.getEffectiveNation(n2).getNumTownblocks() 
          	   - SiegeWarNationUtil.getEffectiveNation(n1).getNumTownblocks();
    };

    private static final Comparator<Nation> BY_ONLINE= (n1, n2) -> {    
            return TownyAPI.getInstance().getOnlinePlayers(SiegeWarNationUtil.getEffectiveNation(n2)).size() 
            	   - TownyAPI.getInstance().getOnlinePlayers(SiegeWarNationUtil.getEffectiveNation(n1)).size();
    };

	public SiegeWar_0_97_0_14_Listener(SiegeWar instance) {
		plugin = instance;
	}

	/**
	 * Re-Sorts the nations list when Towny sorts it
	 * 
	 * - Towny uses this list for the /n list display
	 * 
	 * - SiegeWar re-orders it to account for town occupation
	 *   - Unoccupied towns are counted as part of their natural nation
	 *   - Occupied towns are counted as part of the occupying nation
	 */
	@EventHandler
	public void on(DisplayedNationsListSortEvent event) {
		//Get originally sorted list
		List<Nation> nationList = event.getNations();
		//Re-sort list, taking occupation into account
		switch (event.getComparatorType()) {
			case RESIDENTS:
				nationList.sort(BY_NUM_RESIDENTS);
				break;
			case TOWNBLOCKS:
				nationList.sort(BY_TOWNBLOCKS);
				break;
			case ONLINE:
				nationList.sort(BY_ONLINE);
				break;
			case TOWNS:
				nationList.sort(BY_TOWNS);
				break;
			default:
				return;	
		}
		//Give the re-sorted list to the event object
		event.setNations(nationList);
	}
}
