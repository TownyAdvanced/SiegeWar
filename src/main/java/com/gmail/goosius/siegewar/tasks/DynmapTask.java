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
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarDynmapUtil;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;

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
            String name = "Siege: " + siege.getName().replace("#", " ");
            try {
                if (siege.getStatus() == SiegeStatus.IN_PROGRESS
                    || siege.getStatus() == SiegeStatus.PENDING_DEFENDER_SURRENDER
                    || siege.getStatus() == SiegeStatus.PENDING_ATTACKER_ABANDON) {
                    MarkerIcon siegeIcon = markerapi.getMarkerIcon("fire");
                    String status = "";
                    String timeLeft = "";
                    List<String> lines = new ArrayList<>();
                    lines.add("Attacker: " + siege.getAttackingNation().getName());
                    lines.add("Defender: " + siege.getDefendingTown().getName());
                    lines.add("Points: " + siege.getSiegePoints());
                    lines.add("Banner Control: " + siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase());
                    
                    switch (siege.getStatus()) {
                        case IN_PROGRESS:
                            status = "In Progress";
                            timeLeft = TimeMgmt.getFormattedTimeValue(siege.getTimeUntilCompletionMillis());
                            break;
                        case PENDING_DEFENDER_SURRENDER:
                            status = "Pending Surrender";
                            timeLeft = siege.getFormattedTimeUntilDefenderSurrender();
                            break;
                        case PENDING_ATTACKER_ABANDON:
                            status = "Pending Abandon";
                            timeLeft = siege.getFormattedTimeUntilAttackerAbandon();
                            break;
                        default:
                            status = "Unknown";
                            timeLeft = "0";
                    }
                    lines.add("Status: " + status);
                    lines.add("Time Left: " + timeLeft);

                    if (TownySettings.isUsingEconomy() && TownyEconomyHandler.isActive())
                        lines.add("War Chest: " + TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount()));
                    String desc = "<b>" + name + "</b><hr>" + StringMgmt.join(lines, "<br>");
                    Location siegeLoc = siege.getFlagLocation();
                    double siegeX = siegeLoc.getX();
                    double siegeZ = siegeLoc.getZ();
                    String siegeMarkerId = siege.getName();
                    Marker siegeMarker = markerMap.get(siegeMarkerId);
                    if (siegeMarker == null) {
                        siegeMarker = set.createMarker(siegeMarkerId, name, siegeLoc.getWorld().getName(), siegeX, 64,
                                siegeZ, siegeIcon, false);
                        
                        siegeMarker.setLocation(siegeLoc.getWorld().getName(), siegeX, 64, siegeZ);
                        siegeMarker.setLabel(name);
                        siegeMarker.setDescription(desc);
                        siegeMarker.setMarkerIcon(siegeIcon);
                    } else {
                        siegeMarker.setLocation(siegeLoc.getWorld().getName(), siegeX, 64, siegeZ);
                        siegeMarker.setLabel(name);
                        siegeMarker.setDescription(desc);
                        siegeMarker.setMarkerIcon(siegeIcon);
                    }

                    if (siegeMarker != null)
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
