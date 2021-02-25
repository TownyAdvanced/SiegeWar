package com.gmail.goosius.siegewar.tasks;

import java.io.InputStream;
import java.util.*;

import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.settings.Settings;
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
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.util.StringMgmt;

public class DynmapTask {

    static DynmapAPI api;
    static MarkerAPI markerapi;
    static boolean stop;
    static MarkerSet set;
    static Map<String, Marker> markerMap = new HashMap<String, Marker>();
    final static String PEACEFUL_BANNER_ICON_ID = "fire";
    final static String BATTLE_BANNER_ICON_ID = "siegewar.battle";

    public static void setupDynmapAPI(DynmapAPI _api) {
        api = _api;
        markerapi = api.getMarkerAPI();
        if (markerapi == null) {
            System.err.println(SiegeWar.prefix + "Error loading dynmap marker API!");
            return;
        }

        set = markerapi.getMarkerSet("siegewar.markerset");
        if (set == null) {
            set = markerapi.createMarkerSet("siegewar.markerset", "SiegeWar", null, false);
        } else
            set.setMarkerSetLabel("SiegeWar");

        if (set == null) {
            System.err.println(SiegeWar.prefix + "Error creating dynmap marker set");
            return;
        }

        //Create battle banner marker icon
        InputStream png = SiegeWar.getSiegeWar().getResource(Settings.BATTLE_BANNER_FILE_NAME);
        markerapi.createMarkerIcon(BATTLE_BANNER_ICON_ID, "BattleBanner", png);

        startDynmapTask();
        System.out.println(SiegeWar.prefix + "Dynmap support enabled.");
    }

    public static void startDynmapTask() {
        stop = false;
        Bukkit.getScheduler().runTaskTimerAsynchronously(SiegeWar.getSiegeWar(), () -> {
            if (!stop) {
                hideMapSneakingPlayers();
                displaySieges();
            }
        }, 40l, 300l);
    }

    public static void endDynmapTask() {
        stop = true;
    }

    /**
     * Remove markers belonging to sieges that have ended
     * Also change any icons if required (between peaceful icon & battle icon)
     */
    private static void displaySieges() {
        for (Marker marker : new ArrayList<Marker>(markerMap.values())) {
            try {
                Siege siege = SiegeController.getSiege(marker.getLabel().replaceAll(".+: ", "").replaceAll(" ", "#"));

                if (!SiegeController.hasActiveSiege(siege.getDefendingTown())) {
                    //Delete marker if siege is over
                    marker.deleteMarker();
                    markerMap.remove(marker.getMarkerID());

                } else if (marker.getMarkerIcon().getMarkerIconID().equals(PEACEFUL_BANNER_ICON_ID)) {
                    //Change to battle icon if players are fighting
                    if (siege.getStatus() == SiegeStatus.IN_PROGRESS
                            && (siege.getBannerControllingSide() != SiegeSide.NOBODY || siege.getBannerControlSessions().size() > 0)) {
                        marker.setMarkerIcon(markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID));
                    }

                } else if (marker.getMarkerIcon().getMarkerIconID().equals(BATTLE_BANNER_ICON_ID)) {
                    //Change to peaceful icon if nobody is fighting
                    if (siege.getStatus() != SiegeStatus.IN_PROGRESS
                            || (siege.getBannerControllingSide() == SiegeSide.NOBODY && siege.getBannerControlSessions().size() == 0)) {
                        marker.setMarkerIcon(markerapi.getMarkerIcon(PEACEFUL_BANNER_ICON_ID));
                    }
                }
            } catch (NotRegisteredException e) {
                marker.deleteMarker();
                markerMap.remove(marker.getMarkerID());
            }
        }

        for (Siege siege : SiegeController.getSieges()) {
            String name = Translation.of("dynmap_siege_title", siege.getName().replace("#", " "));
            try {
                if (siege.getStatus().isActive()) {
                    //If anyone is in a BC session or on the BC list, it is a fire & swords icon
                    //otherwise just fire
                    MarkerIcon siegeIcon;
                    if(siege.getBannerControllingSide() == SiegeSide.NOBODY
                            && siege.getBannerControlSessions().size() == 0) {
                        siegeIcon = markerapi.getMarkerIcon(PEACEFUL_BANNER_ICON_ID);
                    } else {
                        siegeIcon = markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID);
                    }
                    List<String> lines = new ArrayList<>();
                    lines.add(Translation.of("dynmap_siege_attacker", siege.getAttackingNation().getName()));
                    lines.add(Translation.of("dynmap_siege_defender", siege.getDefendingTown().getName()));
                    lines.add(Translation.of("dynmap_siege_status", siege.getStatus().getName()));
                    lines.add(Translation.of("dynmap_siege_points", siege.getSiegePoints()));
                    lines.add(Translation.of("dynmap_siege_time_left", siege.getTimeRemaining()));
                    lines.add(Translation.of("dynmap_siege_banner_control", siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase()));
                    lines.add(Translation.of("dynmap_siege_battle_score", siege.getDefenderBattleScore(), siege.getAttackerBattleScore()));
                    lines.add(Translation.of("dynmap_siege_battle_time_left", BattleSession.getBattleSession().getFormattedTimeRemainingUntilBattleSessionEnds()));


                    if (TownyEconomyHandler.isActive())
                        lines.add(Translation.of("dynmap_siege_war_chest", TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount())));
                    String desc = "<b>" + name + "</b><hr>" + StringMgmt.join(lines, "<br>");
                    Location siegeLoc = siege.getFlagLocation();
                    double siegeX = siegeLoc.getX();
                    double siegeZ = siegeLoc.getZ();
                    String siegeMarkerId = siege.getName();
                    Marker siegeMarker = set.findMarker(siegeMarkerId);
                    if (siegeMarker == null) {
                        set.createMarker(siegeMarkerId, name, siegeLoc.getWorld().getName(), siegeX, 64,
                                siegeZ, siegeIcon, false);
                        
                        siegeMarker = set.findMarker(siegeMarkerId);
                        siegeMarker.setLabel(name);
                        siegeMarker.setDescription(desc);
                    } else {
                        siegeMarker.setLabel(name);
                        siegeMarker.setDescription(desc);
                    }
                    markerMap.put(siegeMarkerId, siegeMarker);
                }
            } catch (Exception ex) {
                System.err.println(SiegeWar.prefix + "Problem adding siege marker for siege: " + name);
                ex.printStackTrace();
            }
        }

    }

    /**
     * This method hides players who are 'map sneaking'.
     * It also un-hides players who are not.
     */
    private static void hideMapSneakingPlayers() {
        if (!SiegeWarSettings.getWarSiegeMapSneakingEnabled())
            return;

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player player : onlinePlayers) {
            if (player.hasMetadata(SiegeWarDynmapUtil.MAP_SNEAK_METADATA_ID)) {
                // Hide from dynmap if map sneaking
                api.assertPlayerInvisibility(player, true, SiegeWar.getSiegeWar());
            } else {
                // Otherwise don't hide
                api.assertPlayerInvisibility(player, false, SiegeWar.getSiegeWar());
            }
        }
    }
}
