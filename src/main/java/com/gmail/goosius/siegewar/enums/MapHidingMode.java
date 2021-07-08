package com.gmail.goosius.siegewar.enums;

public enum MapHidingMode {
    AUTOMATIC, MANUAL;

    public static MapHidingMode parseString(String mode) {
        switch (mode.toUpperCase()) {
            case "AUTOMATIC":
                return AUTOMATIC;
            case "MANUAL":
                return MANUAL;
            default:
                throw new RuntimeException("Unknown Map Hiding Mode");
        }
    }
}
