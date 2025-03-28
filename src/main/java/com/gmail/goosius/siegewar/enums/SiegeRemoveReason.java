package com.gmail.goosius.siegewar.enums;

public enum SiegeRemoveReason {
    ADMIN_COMMAND("msg_swa_remove_siege_admin_command"),
    NATION_DELETE("msg_swa_remove_siege_nation_delete"),
    TOWN_DELETE("msg_swa_remove_siege_town_delete"),
    TOWN_RUIN("msg_swa_remove_siege_town_ruin"),
    IMMUNITY_EXPIRED("msg_swa_remove_siege_immunity_expired");

    private final String translatable;

    SiegeRemoveReason(String translatable) {
        this.translatable = translatable;
    }

    public String getTranslatable() {
        return translatable;
    }
}
