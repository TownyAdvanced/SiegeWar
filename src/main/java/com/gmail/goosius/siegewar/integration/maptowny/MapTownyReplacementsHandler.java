package com.gmail.goosius.siegewar.integration.maptowny;

import com.gmail.goosius.siegewar.TownOccupationController;
import me.silverwolfg11.maptowny.MapTownyPlugin;
import me.silverwolfg11.maptowny.events.MapReloadEvent;
import me.silverwolfg11.maptowny.managers.LayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapTownyReplacementsHandler implements Listener {

    private final MapTownyPlugin mapTowny;

    public MapTownyReplacementsHandler(MapTownyPlugin mapTowny) {
        this.mapTowny = mapTowny;
        registerReplacements();
    }

    private void registerReplacements() {
        LayerManager layerManager = mapTowny.getLayerManager();

        if (layerManager == null)
            return;

        layerManager.registerReplacement("%occupier%", town ->
                TownOccupationController.isTownOccupied(town) ?
                        TownOccupationController.getTownOccupier(town).getName() : ""
        );
    }

    // Re-register replacements when maptowny reloads
    @EventHandler
    void onMapTownyReload(MapReloadEvent event) {
        registerReplacements();
    }

}
