package com.gmail.goosius.siegewar.enums;

import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;

public enum SiegeType {
    CONQUEST,
    REVOLT;

    private final static String langKeyTemplate_siegeType = "siege_type_%s";
    private final String siegeTypeLangKey;

    SiegeType() {
        String siegeTypeNameLowercase = this.toString().toLowerCase();
        this.siegeTypeLangKey = String.format(langKeyTemplate_siegeType, siegeTypeNameLowercase);
    }

    public String getName() {
        return Translation.of(siegeTypeLangKey);
    }
    
    public Translatable getTranslatedName() {
    	return Translatable.of(siegeTypeLangKey);
    }

    public static SiegeType parseString(String line) {
        switch (line) {
            case "CONQUEST":
                return CONQUEST;
            case "REVOLT":
                return REVOLT;
            default:
                throw new RuntimeException("Unrecognized enum name");
        }
    }
}
