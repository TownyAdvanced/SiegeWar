package com.gmail.goosius.siegewar.enums;

import com.gmail.goosius.siegewar.settings.Translation;

public enum SiegeType {

    CONQUEST("siege_type_conquest"),
    LIBERATION("siege_type_liberation"),
    REVOLT("siege_type_revolt"),
    SUPPRESSION("siege_type_suppression");

    private String langStringKey;

    SiegeType(String langStringKey) {
        this.langStringKey = langStringKey;
    }

    public String getName() {
        return Translation.of(langStringKey);
    }

    public static SiegeType parseString(String line) {
        switch (line) {
            case "LIBERATION":
                return LIBERATION;
            case "REVOLT":
                return REVOLT;
            case "SUPPRESSION":
                return SUPPRESSION;
            default:
                return CONQUEST;
        }
    }
}
