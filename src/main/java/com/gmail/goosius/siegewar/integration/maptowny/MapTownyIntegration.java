package com.gmail.goosius.siegewar.integration.maptowny;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.enums.SiegeSide;
import com.gmail.goosius.siegewar.objects.BattleSession;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.settings.Settings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.StringMgmt;
import me.silverwolfg11.maptowny.MapTownyPlugin;
import me.silverwolfg11.maptowny.objects.LayerOptions;
import me.silverwolfg11.maptowny.objects.MarkerOptions;
import me.silverwolfg11.maptowny.objects.Point2D;
import me.silverwolfg11.maptowny.platform.MapLayer;
import me.silverwolfg11.maptowny.platform.MapPlatform;
import me.silverwolfg11.maptowny.platform.MapWorld;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MapTownyIntegration {

    private final String SIEGE_MARKER_PREFIX = "siegewar_siege_";
    private final String SIEGE_LAYER_PREFIX = "siegewar_layer_";
    private final String PEACEFUL_BANNER_ICON_KEY = "siegewar_peaceful";
    private final String BATTLE_BANNER_ICON_KEY = "siegewar_battle";

    private final MapTownyPlugin mapTowny;
    private final Map<String, MapLayer> worldLayers = new HashMap<>();
    private boolean siegesRendered = false;

    public MapTownyIntegration(SiegeWar plugin) {
        this.mapTowny = (MapTownyPlugin) Bukkit.getPluginManager().getPlugin("MapTowny");

        // Check if map towny found a valid web-map platform
        if (mapTowny.getPlatform() == null) {
            return;
        }

        // Register replacement handler
        Bukkit.getPluginManager().registerEvents(new MapTownyReplacementsHandler(mapTowny), plugin);

        // Register repeating task that runs every 2 seconds delayed by 15 seconds on start.
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::displaySieges, 40L, 300L);

        final String pluginName = plugin.getName();
        // Run marker and layer setup on platform initialize
        this.mapTowny.getPlatform().onInitialize(() -> {
            registerIcons(plugin);
            registerMarkerLayers(pluginName);
        });
    }

    private BufferedImage readImage(final InputStream is, final String fileName) {
        try {
            return ImageIO.read(is);
        } catch (IOException ex) {
            String errorMsg = String.format("Error reading image from file '%s'...", fileName);
            SiegeWar.getSiegeWar().getLogger().log(Level.SEVERE, errorMsg, ex);
            return null;
        }
    }

    private void registerIcons(final SiegeWar plugin) {
        final MapPlatform platform = this.mapTowny.getPlatform();

        if (!platform.hasIcon(PEACEFUL_BANNER_ICON_KEY)) {
            String fileName = Settings.PEACEFUL_BANNER_FILE_NAME;
            InputStream is = plugin.getResource(fileName);

            BufferedImage img = readImage(is, fileName);
            if (img != null) {
                platform.registerIcon(PEACEFUL_BANNER_ICON_KEY, img, 16, 16);
            }
        }

        if (!platform.hasIcon(BATTLE_BANNER_ICON_KEY)) {
            String fileName = Settings.BATTLE_BANNER_FILE_NAME;
            InputStream is = plugin.getResource(fileName);

            BufferedImage img = readImage(is, fileName);
            if (img != null) {
                platform.registerIcon(BATTLE_BANNER_ICON_KEY, img, 16, 16);
            }
        }
    }

    private void registerMarkerLayers(String pluginName) {
        MapPlatform platform = this.mapTowny.getPlatform();
        LayerOptions layerOptions = new LayerOptions(pluginName, true, false, 10, 10);

        // Register SiegeWar marker layer in all available towny worlds
        for (TownyWorld townyWorld : TownyUniverse.getInstance().getTownyWorlds()) {
            if(!townyWorld.isUsingTowny())
                continue;

            String worldName = townyWorld.getName();
            World bukkitWorld = Bukkit.getWorld(worldName);

            MapWorld mapWorld = bukkitWorld != null ? platform.getWorld(bukkitWorld) : null;

            if (mapWorld == null)
                continue;

            MapLayer mapLayer = mapWorld.registerLayer(SIEGE_LAYER_PREFIX + worldName, layerOptions);
            worldLayers.put(worldName, mapLayer);
        }
    }

    private String getSiegeMarkerKey(UUID townUUID) {
        return "siegewar_siege_" + townUUID.toString();
    }

    void displaySieges() {
        // Avoid attempting to remove markers if no sieges were rendered
        if (siegesRendered) {
            // Remove all siege markers before re-rendering all of them
            for (Map.Entry<String, MapLayer> worldLayerEntry : worldLayers.entrySet()) {
                final MapLayer mapLayer = worldLayerEntry.getValue();
                mapLayer.removeMarkers(s -> s.startsWith(SIEGE_MARKER_PREFIX));
            }

            siegesRendered = false;
        }

        // Add all active siege markers
        for (Siege siege : SiegeController.getSieges()) {

            String name = Translation.of("dynmap_siege_title", siege.getAttackerNameForDisplay(), siege.getDefenderNameForDisplay());
            try {
                if (siege.getStatus().isActive()) {
                    Location siegeLoc = siege.getFlagLocation();
                    final String siegeWorldName = siegeLoc.getWorld().getName();

                    MapLayer mapLayer = worldLayers.get(siegeWorldName);

                    if (mapLayer == null)
                        continue;

                    //If siege is dormant, show the fire icon, otherwise show the crossed swords icon.
                    String iconKey;
                    if (isSiegeDormant(siege)) {
                        iconKey = PEACEFUL_BANNER_ICON_KEY;
                    } else {
                        iconKey = BATTLE_BANNER_ICON_KEY;
                    }

                    List<String> lines = new ArrayList<>();
                    lines.add(Translation.of("dynmap_siege_town", siege.getTown().getName()));
                    lines.add(Translation.of("dynmap_siege_type", siege.getSiegeType().getName()));
                    lines.add(Translation.of("dynmap_siege_balance", siege.getSiegeBalance()));
                    lines.add(Translation.of("dynmap_siege_time_left", siege.getTimeRemaining()));
                    if(TownyEconomyHandler.isActive()) {
                        lines.add(Translation.of("dynmap_siege_war_chest", TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount())));
                    }
                    lines.add(Translation.of("dynmap_siege_banner_control",
                            WordUtils.capitalizeFully(siege.getBannerControllingSide().name())
                                    + (siege.getBannerControllingSide() == SiegeSide.NOBODY ? "" :  " (" + siege.getBannerControllingResidents().size() + ")")));
                    lines.add(Translation.of("dynmap_siege_battle_points", siege.getFormattedAttackerBattlePoints(), siege.getFormattedDefenderBattlePoints()));
                    lines.add(Translation.of("dynmap_siege_battle_time_left", siege.getFormattedBattleTimeRemaining()));

                    String desc = "<b>" + name + "</b><hr>" + StringMgmt.join(lines, "<br>");

                    double siegeX = siegeLoc.getX();
                    double siegeZ = siegeLoc.getZ();
                    final UUID townUUID = siege.getTown().getUUID();
                    String siegeMarkerId = getSiegeMarkerKey(townUUID);

                    MarkerOptions markerOptions = MarkerOptions.builder()
                            .name(name)
                            .clickTooltip(desc)
                            .hoverTooltip(desc)
                            .build();

                    Point2D iconLoc = Point2D.of(siegeX, siegeZ);
                    mapLayer.addIconMarker(siegeMarkerId, iconKey, iconLoc, 16, 16, markerOptions);
                    siegesRendered = true;
                }
            } catch (Exception ex) {
                SiegeWar.severe("Problem adding siege marker for siege: " + name);
                ex.printStackTrace();
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
