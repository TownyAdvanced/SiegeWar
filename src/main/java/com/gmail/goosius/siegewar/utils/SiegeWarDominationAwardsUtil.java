package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.ArtefactOffer;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.WorldCoord;

import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.Material;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
        Map<String, Comparator<Nation>> availableComparators = new HashMap<>();
        availableComparators.put("num_residents", SiegeWarNationUtil.BY_NUM_RESIDENTS);
        availableComparators.put("num_towns", SiegeWarNationUtil.BY_NUM_TOWNS);
        availableComparators.put("num_townblocks", SiegeWarNationUtil.BY_NUM_TOWNBLOCKS);
        availableComparators.put("num_online_players", SiegeWarNationUtil.BY_NUM_RESIDENTS);               
        Comparator<Nation> nationSortComparator = availableComparators.get(SiegeWarSettings.getDominationAwardsGlobalAssessmentCriterion().toLowerCase());
        if(nationSortComparator == null) {
            SiegeWar.severe("Problem Granting Global Domination Awards. Unknown criterion: " + SiegeWarSettings.getDominationAwardsGlobalAssessmentCriterion());
            return;
        } else {
            nations.sort(SiegeWarNationUtil.BY_NUM_RESIDENTS);     
        }

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
        
        //Global message
        System.out.println("Global Domination Awards Granted");       
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
            
            //Identify Chests
            BlockState[] tileEntities = homeBlockChunk.getTileEntities();
            List<Chest> signedChests = identifySignedChests(tileEntities);            
            List<Chest> generalChests = identifyChests(tileEntities, signedChests);
            
            //Deposit artefacts
            depositArtefactsIntoChests(artefactsToGrant, signedChests);
            if(artefactsToGrant.size() > 0) 
                depositArtefactsIntoChests(artefactsToGrant, generalChests);
            if(artefactsToGrant.size() > 0)         
                System.out.println("Could not deposit not enough chests");
                //TODO - add propor message to above
                
            //TODO here --- Propor success message
            System.out.println("Artefacts granted to nation");  
        } finally {
            homeBlockChunk.setForceLoaded(false);
            homeBlockChunk.unload();
        }
    }

    /**
     * Identify chests signed with "Artefacts"
     *
     * @param tileEntities list of tile entities to look at
     * @return the list of signed chests
     */    
    private static List<Chest> identifySignedChests(BlockState[] tileEntities) {
        List<Chest> result = new ArrayList<>();      
        for(BlockState blockState: tileEntities) {
            if(blockState.getBlockData() instanceof WallSign) {
                WallSign wallSign = (WallSign) blockState.getBlockData();
                BlockFace directionOfSignedBlock = wallSign.getFacing().getOppositeFace(); 
                Block signedBlock = blockState.getBlock().getRelative(directionOfSignedBlock);
                //Sign must be attached to be chest or trapped chest
                if(signedBlock.getType() != Material.CHEST && signedBlock.getType() != Material.TRAPPED_CHEST) 
                    continue;
                //Sign must have the text "Artefacts"                    
                Sign sign = (Sign)blockState;
                StringBuilder builder = new StringBuilder();
                for(String s: sign.getLines()) {
                    builder.append(s);
                }
                String signTextLowercase = builder.toString().toLowerCase();
                List<String> artefactChestSignsLowercase = SiegeWarSettings.getDominationAwardsArtefactChestSignsLowercase();
                if(!artefactChestSignsLowercase.contains(signTextLowercase.trim()))
                    continue; 
                //Here we know the chest qualifies as signed
                result.add((Chest)signedBlock.getState());
            }
        }
        return result;
    }

    /**
     * Identify chests
     *
     * @param tileEntities list of tile entities to look at
     * @param chestsToIgnore Ignore these chests
     *
     * @return list of chests
     */    
    private static List<Chest> identifyChests(BlockState[] tileEntities, List<Chest> chestsToIgnore) {
        List<Chest> result = new ArrayList<>();      
        for(BlockState blockState: tileEntities) {
            if(blockState.getBlock().getType() == Material.CHEST
                    || blockState.getBlock().getType() == Material.TRAPPED_CHEST) {
                Chest chest = (Chest)blockState;        
                if(!chestsToIgnore.contains(chest))                        
                    result.add((Chest)blockState);
            }
        }
        return result;
    }
    
    private static void depositArtefactsIntoChests(List<ItemStack> artefactsToDeposit, List<Chest> chests) {
        for(Chest chest: chests) {
            for(int i = 0; i < chest.getBlockInventory().getSize(); i++) {
                if(artefactsToDeposit.size() == 0)
                    return; //All artefacts granted
                if(chest.getBlockInventory().getItem(i) != null)
                    continue; //Slot is not free                                                
                //Put artefact in chest
                chest.getBlockInventory().setItem(i, artefactsToDeposit.get(0));
                artefactsToDeposit.remove(0);
            }
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

    public static void evaluateDominationAwards() {
        if(!SiegeWarSettings.isDominationAwardsGlobalEnabled())
            return;
        //Increase domination records for all nations
        List<Nation> sortedNationList = getSortedNationsList();            
        List<String> dominationRecord;
        for(int i = 0; i < sortedNationList.size(); i++) {
            dominationRecord = NationMetaDataController.getDominationRecord(sortedNationList.get(i));
            dominationRecord.add(Integer.toString(i);
        }
    }
    
    public static List<Nation> getSortedNationsList() {
        List<Nation> nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
        Map<String, Comparator<Nation>> availableComparators = new HashMap<>();
        availableComparators.put("num_residents", SiegeWarNationUtil.BY_NUM_RESIDENTS);
        availableComparators.put("num_towns", SiegeWarNationUtil.BY_NUM_TOWNS);
        availableComparators.put("num_townblocks", SiegeWarNationUtil.BY_NUM_TOWNBLOCKS);
        availableComparators.put("num_online_players", SiegeWarNationUtil.BY_NUM_RESIDENTS);               
        Comparator<Nation> nationSortComparator = availableComparators.get(SiegeWarSettings.getDominationAwardsGlobalAssessmentCriterion().toLowerCase());
        if(nationSortComparator == null) {
            throw new RuntimeException("Problem Granting Global Domination Awards. Unknown criterion: " + SiegeWarSettings.getDominationAwardsGlobalAssessmentCriterion());
        } else {
            nations.sort(nationSortComparator);     
            return nations;      
        }
    }
}
