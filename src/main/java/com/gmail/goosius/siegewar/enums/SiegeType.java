package com.gmail.goosius.siegewar.enums;

import com.gmail.goosius.siegewar.settings.Translation;

public enum SiegeType {

    CONQUEST(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_START,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_CONQUEST_SIEGE_SURRENDER),
    LIBERATION(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_START ,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_SURRENDER),
    REVOLT(SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_START,
            SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_REVOLT_SIEGE_SURRENDER),
    SUPPRESSION(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_START,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_SUPPRESSION_SIEGE_SURRENDER);

    private final static String langKeyTemplate_siegeType = "siege_type_%s";
    private final String siegeTypeLangKey;
    private final SiegeWarPermissionNodes permissionNodeToStartAttack;
    private final SiegeWarPermissionNodes permissionNodeToAbandonAttack;
    private final SiegeWarPermissionNodes permissionNodeToSurrenderDefence;

    SiegeType(SiegeWarPermissionNodes permissionNodeToStartAttack,
              SiegeWarPermissionNodes permissionNodeToAbandonAttack,
              SiegeWarPermissionNodes permissionNodeToAbandonDefence) {
        String siegeTypeNameLowercase = this.toString().toLowerCase();
        this.siegeTypeLangKey = String.format(langKeyTemplate_siegeType, siegeTypeNameLowercase);
        this.permissionNodeToStartAttack = permissionNodeToStartAttack;
        this.permissionNodeToAbandonAttack = permissionNodeToAbandonAttack;
        this.permissionNodeToSurrenderDefence = permissionNodeToAbandonDefence;
    }

    public String getName() {
        return Translation.of(siegeTypeLangKey);
    }

    public static SiegeType parseString(String line) {
        switch (line) {
            case "CONQUEST":
                return CONQUEST;
            case "LIBERATION":
                return LIBERATION;
            case "REVOLT":
                return REVOLT;
            case "SUPPRESSION":
                return SUPPRESSION;
            default:
                throw new RuntimeException("Unrecognized enum name");
        }
    }

    public SiegeWarPermissionNodes getPermissionNodeToAbandonAttack() {
        return permissionNodeToAbandonAttack;
    }

    public SiegeWarPermissionNodes getPermissionNodeToSurrenderDefence() {
        return permissionNodeToSurrenderDefence;
    }

    public SiegeWarPermissionNodes getPermissionNodeToAttack() {
        return permissionNodeToStartAttack;
    }
}
