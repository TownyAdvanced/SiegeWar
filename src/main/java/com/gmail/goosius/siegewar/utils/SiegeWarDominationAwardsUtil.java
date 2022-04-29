package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.objects.ArtefactOffer;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains utility functions related to domination awards
 *
 * @author Goosius
 */
public class SiegeWarDominationAwardsUtil {

    /**
     * Grant the global domination awards
     */
    public static void grantGlobalDominationAwards() {
        List<Integer> moneyToGrant = SiegeWarSettings.getDominationAwardsGlobalGrantedMoney();
        List<List<Integer>> artefactsToGrant = SiegeWarSettings.getDominationAwardsGlobalGrantedOffers();

        //Sort nations list
        List<Nation> nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
        nations.sort(SiegeWarNationUtil.BY_NUM_RESIDENTS);
        
        //The number of awardees will be as configured, or the size of the nations list, whichever is smaller, 
        int numberOfAwardees = Math.min(moneyToGrant.size(), nations.size());
                
        //Gib awards
        Nation nation = null;
        for(int nationPosition = 0; nationPosition < numberOfAwardees; nationPosition++) {
            try{
                nation = nations.get(nationPosition);            
                //Gib money
                nation.getAccount().deposit(moneyToGrant.get(nationPosition), "Global Domination Award");    
                //Gib artifacts
                grantArtefactsToNation(artefactsToGrant.get(nationPosition), nation);
            } catch(Throwable t) {
                SiegeWar.severe("Problem granting global domination award to nation " + nation.getName());
                SiegeWar.severe(t.getMessage());
                t.printStackTrace();
            }
        }                
    }

    private static void grantArtefactsToNation(List<Integer> offersToGrantFromEachTier, Nation nation) {
        List<ItemStack> artefactsToGrant = new ArrayList<>();
        for(int tier = 0; tier < offersToGrantFromEachTier.size(); tier++) {
            artefactsToGrant.addAll(generateArtefacts(tier, offersToGrantFromEachTier.get(tier)));
        }
        //Get chunk of capital homeblock
        WorldCoord homeBlockCoord = nation.getCapital().getHomeBlockOrNull().getWorldCoord();
        Location homeBlockLocation = new Location(homeBlockCoord.getBukkitWorld(), 
                                                    homeBlockCoord.getX() * TownySettings.getTownBlockSize(),
                                                    64,
                                                    homeBlockCoord.getZ()* TownySettings.getTownBlockSize());
        Chunk homeBlockChunk = homeBlockLocation.getChunk();
        try {
            homeBlockChunk.setForceLoaded(true);
            homeBlockChunk.load();
            //Find chests
            List<Chest> chests = new ArrayList<>(); 
            for(BlockState blockState: homeBlockChunk.getTileEntities()) {
                if(blockState.getBlock().getType() == Material.CHEST
                        || blockState.getBlock().getType() == Material.TRAPPED_CHEST) {
                    chests.add((Chest)blockState);
                }
            }
            //Deposit artefacts in chests
            for(Chest chest: chests) {
                 for(int i = 0; i < chest.getBlockInventory().getSize(); i++) {
                     if(chest.getBlockInventory().getItem(i) == null) {                                                
                        if(artefactsToGrant.size() == 0) {
                            return; //All artefacts granted
                        }
                        //Put artefact in chest
                        chest.getBlockInventory().setItem(i, artefactsToGrant.get(0));
                        artefactsToGrant.remove(0);
                     }
                 }
            }
        } finally {
            homeBlockChunk.setForceLoaded(false);
            homeBlockChunk.unload();
        }
    }

    private static List<ItemStack> generateArtefacts(int tier, int numOffers) {
        List<ItemStack> result = new ArrayList<>();
        List<ArtefactOffer> offersInTier = SiegeWarSettings.getDominationAwardsArtefactOffers().get(tier+1);
        for(int i = 0; i < numOffers; i++) {
            //Identify Random offer
            ArtefactOffer offer = offersInTier.get((int)(Math.random() * offersInTier.size()));
            //Generate the artefacts specified by that offer
            for(int ii = 0; ii < offer.quantity; ii++) {
                result.add(offer.artefactTemplate.clone());
            }
        }
        return result;
    }

}
