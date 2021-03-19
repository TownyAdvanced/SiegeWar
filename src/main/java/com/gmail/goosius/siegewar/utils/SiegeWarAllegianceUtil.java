package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeWarPermissionNodes;
import com.gmail.goosius.siegewar.objects.Siege;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class SiegeWarAllegianceUtil {

    public static SiegeSide calculateCandidateSiegePlayerSide(Player deadPlayer, Town deadResidentTown, Siege candidateSiege) throws NotRegisteredException {

        //Look for defender
        Government defendingGovernment = candidateSiege.getDefender();
        switch (candidateSiege.getSiegeType()) {
            case CONQUEST:
            case SUPPRESSION:
                //In the above sieges, defenders can be town guards
                if (isTownGuard(deadPlayer, deadResidentTown, defendingGovernment))
                    return SiegeSide.DEFENDERS;
            case REVOLT:
            case LIBERATION:
                //In the above sieges, defenders can be nation/allied soldiers
                if (isNationSoldierOrAlliedSoldier(deadPlayer, deadResidentTown, defendingGovernment))
                    return SiegeSide.DEFENDERS;
        }

        //Look for attacker
        Government attackingGovernment = candidateSiege.getAttacker();
        switch (candidateSiege.getSiegeType()) {
            case REVOLT:
                //In the above sieges, attackers can be town guards
                if (isTownGuard(deadPlayer, deadResidentTown, attackingGovernment))
                    return SiegeSide.ATTACKERS;
            case CONQUEST:
            case SUPPRESSION:
            case LIBERATION:
                //In the above sieges, attackers can be nation/allied soldiers
                if (isNationSoldierOrAlliedSoldier(deadPlayer, deadResidentTown, attackingGovernment))
                    return SiegeSide.ATTACKERS;
        }
        return SiegeSide.NOBODY;
    }

    private static boolean isTownGuard(Player player, Town residentTown, Government governmentToCheck) {
        return residentTown == governmentToCheck
                && TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_BATTLE_POINTS.getNode());
    }

    private static boolean isNationSoldierOrAlliedSoldier(Player player, Town residentTown, Government governmentToCheck) throws NotRegisteredException {
        if (residentTown.hasNation() && governmentToCheck instanceof Nation) {

            if(!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_BATTLE_POINTS.getNode()))
                return false;

            return residentTown.getNation() == governmentToCheck
                    || residentTown.getNation().hasMutualAlly((Nation) governmentToCheck);
        } else {
            return false;
        }
    }

}
