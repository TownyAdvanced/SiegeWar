package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class SiegeWarAllegianceUtil {

    public static SiegeSide calculateSiegePlayerSide(Player player, Town playerTown, Siege siege) {

        //Look for defender
        Government besiegedTown = siege.getDefender();
        if (isTownGuard(player, playerTown, besiegedTown)
            || isNationSoldierOrAlliedSoldier(player, playerTown, besiegedTown)) {
            return SiegeSide.DEFENDERS;
        }

        //Look for attacker
        Government attackingNation = siege.getAttacker();
        if (isNationSoldierOrAlliedSoldier(player, playerTown, attackingNation)) {
            return SiegeSide.ATTACKERS;
        }
        return SiegeSide.NOBODY;
    }

    private static boolean isTownGuard(Player player, Town residentTown, Government governmentToCheck) {
        return residentTown == governmentToCheck
                && TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS.getNode());
    }

    private static boolean isNationSoldierOrAlliedSoldier(Player player, Town residentTown, Government governmentToCheck) {
        if(!residentTown.hasNation())
            return false;
        Nation nation = TownyAPI.getInstance().getTownNationOrNull(residentTown);

        if(!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS.getNode()))
            return false;

        if(governmentToCheck instanceof Nation) {
            //The government-to-check is a nation
            return nation == governmentToCheck
                    || nation.hasMutualAlly((Nation) governmentToCheck);
        } else if (((Town)governmentToCheck).hasNation()) {
            //The government-to-check is a nation town
            return nation == TownyAPI.getInstance().getTownNationOrNull((Town) governmentToCheck)
                    || nation.hasMutualAlly(TownyAPI.getInstance().getTownNationOrNull((Town) governmentToCheck));
        } else {
            //The government-to-check is a non-nation town. Nation soldiers cannot contribute
            return false;
        }
    }

    public static boolean isSideHostileToTown(SiegeSide gunnerSiegeSide, Siege siege) {
        switch (gunnerSiegeSide) {
            case ATTACKERS:
                return siege.isConquestSiege();
            case DEFENDERS:
                return siege.isRevoltSiege();
            default:
                return false;
        }
    }

    /**
     * Determine if a player is on the town-friendly side of a given siege
     */
    public static boolean isPlayerOnTownFriendlySide(Player player, Resident resident, Siege siege) {
        Town gunnerResidentTown = resident.getTownOrNull();
        SiegeSide playerSiegeSide = calculateSiegePlayerSide(player, gunnerResidentTown, siege);
        switch(playerSiegeSide) {
            case DEFENDERS:
                return siege.isConquestSiege();
            case ATTACKERS:
                return siege.isRevoltSiege();
            default:
                return false;
        }
    }

    /**
     * Determine if a player is on the town-hostile side of a given siege
     */
    public static boolean isPlayerOnTownHostileSide(Player player, Resident resident, Siege siege) {
        Town gunnerResidentTown = resident.getTownOrNull();
        SiegeSide playerSiegeSide = calculateSiegePlayerSide(player, gunnerResidentTown, siege);
        switch(playerSiegeSide) {
            case ATTACKERS:
                return siege.isConquestSiege();
            case DEFENDERS:
                return siege.isRevoltSiege();
            default:
                return false;
        }
    }
}
