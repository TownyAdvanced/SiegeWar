package com.gmail.goosius.siegewar.enums;

import com.gmail.goosius.siegewar.settings.Translation;

public enum BattleResult {
	ATTACKER_WIN("msg_attackers"), DEFENDER_WIN("msg_defenders"), DRAW("msg_draw");

	BattleResult(String langStringId) {
		this.langStringId = langStringId;
	}

	private String langStringId;

	public String getFormattedName() {
		return Translation.of(langStringId);
	}
}
