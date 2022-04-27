package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
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

        //Sort nations list
        List<Nation> nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
        nations.sort(SiegeWarNationUtil.BY_NUM_RESIDENTS);
        
        //The number of awardees will be as configured, or the size of the nations list, whichever is smaller, 
        int numberOfAwardees = Math.min(moneyToGrant.size(), nations.size());
                
        //Gib awards
        Nation nation;
        for(int nationPosition = 0; nationPosition < numberOfAwardees; nationPosition++) {
            nation = nations.get(nationPosition);
            //Gib money
            nation.getAccount().deposit(moneyToGrant.get(nationPosition), "Global Domination Award");    
            //Gib artifacts
            grantArtifactsToNation(generateArtifacts(nationPosition), nation);
        }                
    }

    private static List<ItemStack> generateArtifacts(int nationPosition) {
        List<ItemStack> result = new ArrayList<>();
        List<List<Integer>> artifactGenerationNumbersByTier = SiegeWarSettings.getDominationAwardsGlobalGrantedArtifacts();

        for(int artifactTier = 0; artifactTier < artifactGenerationNumbersByTier.size(); artifactTier++) {
            int numArtifactsToGenerate = artifactGenerationNumbersByTier.get(artifactTier).get(nationPosition);
            result.addAll(generateArtifacts(artifactTier, numArtifactsToGenerate));
        }
        return result;
    }

    private static List<ItemStack> generateArtifacts(int artifactTier, int numArtifactsToGenerate) {
        List<ItemStack> result = new ArrayList<>();
        for(int i = 0; i < numArtifactsToGenerate; i++) {
            result.add(generateArtifact(SiegeWarSettings.getDominationAwardsGlobalArtifactSpecifications().get(artifactTier)));
        }
        return result;
    }
    
    public static ItemStack generateArtifact(List<String> artifactSpecifications) {
        //Pick 1 random artifact specification
        String artifactSpecification = artifactSpecifications.get((int)(Math.random() * artifactSpecifications.size()));
        
        ItemStack result;
        
        BLAHBLAH
        
        return result;
    }

    private static void grantArtifactsToNation(List<ItemStack> artifacts, Nation nation) {
        BLAH
    }
    /**
     * Get nations list, sorted by num residents
     * @return nations list, sorted by num residents
     */
    public List<Nation> getSortedNationsList() {
        List<Nation> nationsList = new ArrayList<>(TownyUniverse.getInstance().getNations());
        nationsList.sort(SiegeWarNationUtil.BY_NUM_RESIDENTS);
        return nationsList;
    }
}
