package com.gmail.goosius.siegewar.enums;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public enum GlassColor {
    WHITE(Material.WHITE_STAINED_GLASS, "White"),
    ORANGE(Material.ORANGE_STAINED_GLASS, "Orange"),
    MAGENTA(Material.MAGENTA_STAINED_GLASS, "Magenta"),
    LIGHTBLUE(Material.LIGHT_BLUE_STAINED_GLASS, "LightBlue"),
    YELLOW(Material.YELLOW_STAINED_GLASS, "Yellow"),
    LIME(Material.LIME_STAINED_GLASS, "Lime"),
    PINK(Material.PINK_STAINED_GLASS, "Pink"),
    GRAY(Material.GRAY_STAINED_GLASS, "Gray"),
    LIGHTGRAY(Material.LIGHT_GRAY_STAINED_GLASS, "LightGray"),
    CYAN(Material.CYAN_STAINED_GLASS, "Cyan"),
    PURPLE(Material.PURPLE_STAINED_GLASS, "Purple"),
    BLUE(Material.BLUE_STAINED_GLASS, "Blue"),
    BROWN(Material.BROWN_STAINED_GLASS, "Brown"),
    GREEN(Material.GREEN_STAINED_GLASS, "Green"),
    RED(Material.RED_STAINED_GLASS, "Red"),
    BLACK(Material.BLACK_STAINED_GLASS, "Black");

    private Material material;
    private String name;

    GlassColor(Material material, String name) {
        this.material = material;
        this.name = name;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public static List<String> getNamesArray() {
        List<String> names = new ArrayList<>();
        for (GlassColor color : GlassColor.values())
            names.add(color.getName().toLowerCase());
        return names;
    }
}
