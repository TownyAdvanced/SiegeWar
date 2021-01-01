package com.gmail.goosius.siegewar.enums;

import com.gmail.goosius.siegewar.settings.Translation;

public enum SiegeSide {
	ATTACKERS("msg_attackers"), DEFENDERS("msg_defenders"), NOBODY("msg_nobody");

	SiegeSide(String langStringId) {
		this.langStringId = langStringId;
	}

	private String langStringId;

	public String getFormattedName() {
		return Translation.of(langStringId);
	}
}
