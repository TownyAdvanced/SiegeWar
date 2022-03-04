package com.gmail.goosius.siegewar.enums;

import com.palmergames.bukkit.towny.object.Translatable;

public enum SiegeSide {
	ATTACKERS("msg_attackers"), DEFENDERS("msg_defenders"), NOBODY("msg_nobody");

	SiegeSide(String langStringId) {
		this.langStringId = langStringId;
	}

	private String langStringId;

	public Translatable getFormattedName() {
		return Translatable.of(langStringId);
	}
}
