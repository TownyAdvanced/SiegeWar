package com.gmail.goosius.siegewar.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.gmail.goosius.siegewar.utils.SiegeWarDynmapUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.util.StringMgmt;

public class DynmapTask {

    static DynmapAPI api;
    static MarkerAPI markerapi;
    static boolean stop;
    static MarkerSet set;
    static Map<String, Marker> markerMap = new HashMap<String, Marker>();

    public static void setDynmapAPI(DynmapAPI _api) {
        api = _api;
        markerapi = api.getMarkerAPI();
        if (markerapi == null) {
            System.err.println(SiegeWar.prefix + "Error loading dynmap marker API!");
            return;
        }

        set = markerapi.getMarkerSet("siegewar.markerset");
        if (set == null)
            set = markerapi.createMarkerSet("siegewar.markerset", "SiegeWar", null, false);
        else
            set.setMarkerSetLabel("SiegeWar");
        if (set == null) {
            System.err.println(SiegeWar.prefix + "Error creating dynmap marker set");
            return;
        }

        startDynmapTask();
        System.out.println(SiegeWar.prefix + "Dynmap support enabled.");
    }

    public static void startDynmapTask() {
        stop = false;
        Bukkit.getScheduler().runTaskTimerAsynchronously(SiegeWar.getSiegeWar(), () -> {
            if (!stop) {
                hideTacticallyInvisiblePlayers();
                displaySieges();
            }
        }, 40l, 300l);
    }

    public static void endDynmapTask() {
        stop = true;
    }

    private static void displaySieges() {
        markerMap.clear();
        for (Siege siege : SiegeController.getSieges()) {
            String name = Translation.of("dynmap_siege_title", siege.getName().replace("#", " "));
            try {
                if (siege.getStatus().isActive()) {
                    MarkerIcon siegeIcon = markerapi.getMarkerIcon("fire");
                    List<String> lines = new ArrayList<>();
                    lines.add(Translation.of("dynmap_siege_attacker", siege.getAttackingNation().getName()));
                    lines.add(Translation.of("dynmap_siege_defender", siege.getDefendingTown().getName()));
                    lines.add(Translation.of("dynmap_siege_points", siege.getSiegePoints()));
                    lines.add(Translation.of("dynmap_siege_banner_control", siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase()));
                    lines.add(Translation.of("dynmap_siege_status", siege.getStatus().getName()));
                    lines.add(Translation.of("dynmap_siege_time_left", siege.getTimeRemaining()));

                    if (TownySettings.isUsingEconomy() && TownyEconomyHandler.isActive())
                        lines.add(Translation.of("dynmap_siege_war_chest", TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount())));
                    String desc = "<b>" + name + "</b><hr>" + StringMgmt.join(lines, "<br>");
                    Location siegeLoc = siege.getFlagLocation();
                    double siegeX = siegeLoc.getX();
                    double siegeZ = siegeLoc.getZ();
                    String siegeMarkerId = siege.getName();
                    set.createMarker(siegeMarkerId, name, siegeLoc.getWorld().getName(), siegeX, 64,
                            siegeZ, siegeIcon, false);
                    
                    Marker siegeMarker = set.findMarker(siegeMarkerId);
                    siegeMarker.setLabel(name);
                    siegeMarker.setDescription(desc);

                    markerMap.put(siegeMarkerId, siegeMarker);
                }
            } catch (Exception ex) {
                System.err.println(SiegeWar.prefix + "Problem adding siege marker for siege: " + name);
                ex.printStackTrace();
            }
        }

    }

    /**
     * This method hides players who have 'tactical invisibility. It also un-hides
     * players who do not.
     */
    private static void hideTacticallyInvisiblePlayers() {
        if (!SiegeWarSettings.getWarSiegeTacticalVisibilityEnabled())
            return;

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player player : onlinePlayers) {
            if (player.hasMetadata(SiegeWarDynmapUtil.TACTICAL_INVISIBILITY_METADATA_ID)) {
                // Hide from dynmap if tactically invis
                api.assertPlayerInvisibility(player, true, SiegeWar.getSiegeWar());
            } else {
                // Otherwise don't hide
                api.assertPlayerInvisibility(player, false, SiegeWar.getSiegeWar());
            }
        }
    }
}
