package com.gmail.goosius.siegewar.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.goosius.siegewar.metadata.ResidentMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Resident;

public class SiegeWarResidentEventListener implements Listener {

	@EventHandler
	public void onResStatusScreen(ResidentStatusScreenEvent event) {
		if (SiegeWarSettings.getWarSiegeEnabled()) {
			List<String> out = new ArrayList<>();
			Resident resident = event.getResident();
			if (ResidentMetaDataController.getNationRefundAmount(resident) != 0)
				out.add("Nation Refund: " + TownyEconomyHandler.getFormattedBalance(ResidentMetaDataController.getNationRefundAmount(resident)));
			
			if (ResidentMetaDataController.getPlunderAmount(resident) != 0)
				out.add("Plunder: " + TownyEconomyHandler.getFormattedBalance(ResidentMetaDataController.getPlunderAmount(resident)));
			
			if (ResidentMetaDataController.getMilitarySalaryAmount(resident) != 0)
				out.add("Military Salary: " + TownyEconomyHandler.getFormattedBalance(ResidentMetaDataController.getMilitarySalaryAmount(resident)));
			
			if (!out.isEmpty())
				event.addLines(out);
		}
			
	}
}
