package com.gmail.goosius.siegewar.objects;

import org.bukkit.inventory.ItemStack;

public class ArtefactOffer {
    public final ItemStack artefactTemplate;
    public final int quantity;
    
    public ArtefactOffer(ItemStack artefactTemplate, int quantity) {
        this.artefactTemplate = artefactTemplate;
        this.quantity = quantity;
    }
}
