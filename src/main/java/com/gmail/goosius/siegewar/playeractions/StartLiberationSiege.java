package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.enums.SiegeType;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class StartLiberationSiege {

    public static void processStartSiegeRequest(Player player, Town town, Nation liberation, Nation townOccupier) throws TownyException {
        if (!SiegeWarSettings.getLiberationSiegesEnabled())
            throw new TownyException(Translation.of("msg_err_action_disable"));

        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, SiegeType.LIBERATION.getPermissionNodeToAttack().getNode()))
            throw new TownyException(Translation.of("msg_err_action_disable"));


        startSiege();
    }

    private static void startSiege() {

    }
}
