package com.gmail.goosius.siegewar.integration.dynmap;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.Settings;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.StringMgmt;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DynmapIntegration {
    
    private static final String PEACEFUL_BANNER_ICON_ID = "fire";
    private static final String BATTLE_BANNER_ICON_ID = "siegewar.battle";

    private final SiegeWar plugin;
    private final DynmapAPI dynmapAPI;
    private final MarkerAPI markerapi;
    private BukkitTask dynmapTask;
    private final Map<UUID, Marker> townUUIDToSiegeMarkerMap = new HashMap<>();
    private MarkerSet siegeWarMarkerSet;

    public DynmapIntegration(SiegeWar plugin) {
        this.plugin = plugin;
        dynmapAPI = (DynmapAPI) plugin.getServer().getPluginManager().getPlugin("dynmap");
        markerapi = dynmapAPI.getMarkerAPI();

        addMarkerSet();
        registerDynmapTownyListener();
        startDynmapTask();
        SiegeWar.info("Dynmap support enabled.");
    }

    private void addMarkerSet() {
        if (markerapi == null) {
            SiegeWar.severe("Error loading Dynmap marker API!");
            return;
        }

        //Create siegewar marker set
        siegeWarMarkerSet = markerapi.getMarkerSet("siegewar.markerset");
        if (siegeWarMarkerSet == null) {
            siegeWarMarkerSet = markerapi.createMarkerSet("siegewar.markerset", SiegeWarSettings.getDynmapLayerName(), null, false);
        } else {
            siegeWarMarkerSet.setMarkerSetLabel(plugin.getName());
        }

        if (siegeWarMarkerSet == null) {
            SiegeWar.severe("Error creating Dynmap marker set!");
            return;
        }

        //Create battle banner marker icon
        markerapi.createMarkerIcon(BATTLE_BANNER_ICON_ID, "BattleBanner", plugin.getResource(Settings.BATTLE_BANNER_FILE_NAME));
    }

    private void registerDynmapTownyListener() {
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.isPluginEnabled("Dynmap-Towny")) {
            SiegeWar.info("SiegeWar found Dynmap-Towny plugin, enabling Dynmap-Towny support.");
            pm.registerEvents(new DynmapTownyListener(), plugin);
        } else {
            SiegeWar.info("Dynmap-Towny plugin not found.");
        }
    }

    public void startDynmapTask() {
        dynmapTask = new DynmapTask(this).runTaskTimerAsynchronously(plugin, 40, 300);
    }

    public void endDynmapTask() {
        dynmapTask.cancel();
    }

    /**
     * Remove markers belonging to sieges that have ended
     * Also change any icons if required (between peaceful icon & battle icon)
     */
    void displaySieges() {
        Map<UUID, Marker> townUUIDToSiegeMarkerMapCopy = new HashMap<>(townUUIDToSiegeMarkerMap);

        {
            //Cleanup current siege markers
            UUID townUUID = null;
            Marker marker = null;
            for (Map.Entry<UUID, Marker> mapEntry : townUUIDToSiegeMarkerMapCopy.entrySet()) {
                try {
                    marker = null;
                    townUUID = null;
                    townUUID = mapEntry.getKey();
                    marker = mapEntry.getValue();
                    Siege siege = SiegeController.getSiegeByTownUUID(townUUID);

                    if (siege == null || siege.getStatus() != SiegeStatus.IN_PROGRESS) {
                        //Delete marker if siege is not in progress
                        marker.deleteMarker();
                        townUUIDToSiegeMarkerMap.remove(townUUID);

                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(PEACEFUL_BANNER_ICON_ID)) {
                        /*
                         * Change to battle icon if siege is active.
                         */
                        if(!isSiegeDormant(siege))
                            marker.setMarkerIcon(markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID));                             
                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(BATTLE_BANNER_ICON_ID)) {
                        /*
                         * Change to peaceful icon if siege is dormant
                         */
                        if (isSiegeDormant(siege))
                            marker.setMarkerIcon(markerapi.getMarkerIcon(PEACEFUL_BANNER_ICON_ID));                      
                    }
                } catch (Exception e) {
                    if (marker != null)
                        marker.deleteMarker();
                    townUUIDToSiegeMarkerMap.remove(townUUID);
                }
            }
        }

        {
            //Add siege marker if required
            for (Siege siege : SiegeController.getSieges()) {

                String name = Translation.of("dynmap_siege_title", siege.getAttackerNameForDisplay(), siege.getDefenderNameForDisplay());
                try {
                    if (siege.getStatus().isActive()) {
                        //If siege is dormant, show the fire icon, otherwise show the crossed swords icon.
                        MarkerIcon siegeIcon;
                        if (isSiegeDormant(siege)) {
                            siegeIcon = markerapi.getMarkerIcon(PEACEFUL_BANNER_ICON_ID);
                        } else {
                            siegeIcon = markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID);
                        }
                        List<String> lines = new ArrayList<>();
                        lines.add(Translation.of("dynmap_siege_town", siege.getTown().getName()));
                        lines.add(Translation.of("dynmap_siege_type", siege.getSiegeType().getName()));
                        if(TownyEconomyHandler.isActive()) {
                            lines.add(Translation.of("dynmap_siege_war_chest", TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount())));
                        }
                        lines.add(Translation.of("dynmap_siege_progress", siege.getNumBattleSessionsCompleted(), SiegeWarSettings.getSiegeDurationBattleSessions()));
                        lines.add(Translation.of("dynmap_siege_status", siege.getStatus().getName()));
                        lines.add(Translation.of("dynmap_siege_balance", siege.getSiegeBalance()));
                        lines.add(Translation.of("dynmap_siege_banner_control",
                            WordUtils.capitalizeFully(siege.getBannerControllingSide().name())
                            + (siege.getBannerControllingSide() == SiegeSide.NOBODY ? "" :  " (" + siege.getBannerControllingResidents().size() + ")")));
                        lines.add(Translation.of("dynmap_siege_battle_points", siege.getFormattedAttackerBattlePoints(), siege.getFormattedDefenderBattlePoints()));
                        lines.add(Translation.of("dynmap_siege_battle_time_left", siege.getFormattedBattleTimeRemaining()));

                        String desc = "<b>" + name + "</b><hr>" + StringMgmt.join(lines, "<br>");
                        Location siegeLoc = siege.getFlagLocation();
                        double siegeX = siegeLoc.getX();
                        double siegeZ = siegeLoc.getZ();
                        String siegeMarkerId = siege.getTown().getUUID().toString();
                        Marker siegeMarker = siegeWarMarkerSet.findMarker(siegeMarkerId);
                        if (siegeMarker == null) {
                            siegeMarker = siegeWarMarkerSet.createMarker(siegeMarkerId, name, siegeLoc.getWorld().getName(), siegeX, 64,
                                    siegeZ, siegeIcon, false);
                        }
                        siegeMarker.setLabel(name);
                        siegeMarker.setDescription(desc);
                        townUUIDToSiegeMarkerMap.put(siege.getTown().getUUID(), siegeMarker);
                    }
                } catch (Exception ex) {
                	SiegeWar.severe("Problem adding siege marker for siege: " + name);
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * A siege is dormant if there is no significant activity there (e.g. kills, banner control).
     * 
     * This state is represented by a fire icon on the map.
     * If the battle becomes active, the icon changes to crossed-swords.
     * 
     * @return true if siege is dormant
     */
    private boolean isSiegeDormant(Siege siege) {
        return !BattleSession.getBattleSession().isActive()
                || (siege.getAttackerBattlePoints() == 0
                && siege.getDefenderBattlePoints() == 0
                && siege.getBannerControllingSide() == SiegeSide.NOBODY
                && siege.getBannerControlSessions().size() == 0);
    }
    
}
