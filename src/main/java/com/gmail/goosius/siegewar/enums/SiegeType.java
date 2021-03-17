package com.gmail.goosius.siegewar.enums;

import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

public enum SiegeType {

    CONQUEST(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_CONQUEST_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_CONQUEST_SIEGE_SURRENDER),
    LIBERATION(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_LIBERATION_SIEGE_SURRENDER),
    REVOLT(SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_REVOLT_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_REVOLT_SIEGE_SURRENDER),
    SUPPRESSION(SiegeWarPermissionNodes.SIEGEWAR_NATION_SIEGE_SUPPRESSION_SIEGE_ABANDON,
            SiegeWarPermissionNodes.SIEGEWAR_TOWN_SIEGE_SUPPRESSION_SIEGE_SURRENDER);

    private final static String langKeyTemplate_siegeType = "siege_type_%s";
    private final static String langKeyTemplate_siegeStartedNeutralTown = "msg_%s_siege_started_neutral_town";
    private final static String langKeyTemplate_siegeStartedNationTown = "msg_%s_siege_started_nation_town";
    private final static String langKeyTemplate_attackerAbandon= "msg_%s_siege_attacker_abandon";
    private final static String langKeyTemplate_townSurrender= "msg_%s_siege_town_surrender";
    private final static String langKeyTemplate_pendingAttackerAbandon= "msg_%s_siege_pending_attacker_abandon";
    private final static String langKeyTemplate_pendingTownSurrender= "msg_%s_siege_pending__town_surrender";

    private final String siegeTypeLangKey;
    private final SiegeWarPermissionNodes permissionNodeToAbandonAttack;
    private final SiegeWarPermissionNodes permissionNodeToSurrenderDefence;

    SiegeType(SiegeWarPermissionNodes permissionNodeToAbandonAttack,
              SiegeWarPermissionNodes permissionNodeToAbandonDefence) {
        String siegeTypeNameLowercase = this.toString().toLowerCase();
        this.siegeTypeLangKey = String.format(langKeyTemplate_siegeType, siegeTypeNameLowercase);
        this.permissionNodeToAbandonAttack = permissionNodeToAbandonAttack;
        this.permissionNodeToSurrenderDefence = permissionNodeToAbandonDefence;
    }

    public String getName() {
        return Translation.of(siegeTypeLangKey);
    }

    public String getMsgSiegeStartedNeutralTown(Government attacker, Government defender) {
        String key = String.format(langKeyTemplate_siegeStartedNeutralTown, this.toString().toLowerCase());
        return Translation.of(key, attacker.getFormattedName(), defender.getFormattedName());
    }

    public String getMsgSiegeStartedNationTown(Government attacker, Government defender) {
        String key = String.format(langKeyTemplate_siegeStartedNationTown, this.toString().toLowerCase());
        return Translation.of(key, attacker.getFormattedName(), defender.getFormattedName());
    }

    public String getMsgSiegeAttackerAbandon(Town town, Government attacker) {
        String key = String.format(langKeyTemplate_siegeStartedNeutralTown, this.toString().toLowerCase());
        return Translation.of(key, town.getFormattedName(), attacker.getFormattedName());
    }

    public String getMsgSiegePendingAttackerAbandon(Town town, Government attacker) {
        String key = String.format(langKeyTemplate_siegeStartedNeutralTown, this.toString().toLowerCase());
        return Translation.of(key, town.getFormattedName(), attacker.getFormattedName());
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

    public String getMsgKeyTownSurrender() {
        return String.format(langKeyTemplate_townSurrender, this.toString().toLowerCase());
    }

    public SiegeWarPermissionNodes getPermissionNodeToAttack() {
        //TODO - FIXME
        return null;
    }
}
