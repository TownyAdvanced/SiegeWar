package com.gmail.goosius.siegewar.listeners;

import com.gmail.goosius.siegewar.SiegeWar;
import org.bukkit.event.Listener;

public class SiegeWarTownyDynmapListener implements Listener {

    @SuppressWarnings("unused")
    private final SiegeWar plugin;

    public SiegeWarTownyDynmapListener(SiegeWar instance) {
        plugin = instance;
    }

    /**

     */

    /*
    @EventHandler
    public void on(BuildTownDescriptionEvent event) {
        if (SiegeWarSettings.getWarSiegeEnabled() && SiegeWarSettings.isCannonsIntegrationEnabled()) {
            Player player = null;
            try {
                player = Towny.getPlugin().getServer().getPlayer(event.getPlayer());
                SiegeWarCannonsUtil.processPlayerCannonInteraction(player, event.getCannon(), Translation.of("msg_err_cannot_fire_no_cannon_session"));
            } catch (TownyException te) {
                event.setCancelled(true);
                if (player != null) {
                    Messaging.sendErrorMsg(player, te.getMessage());
                } else {
                    System.out.println("Problem Processing fire cannon event: " + te.getMessage());
                }
            } catch (Exception e) {
                event.setCancelled(true);
                System.out.println("Problem Processing fire cannon event: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    */

}
