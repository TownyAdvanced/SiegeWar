package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.events.GlobalDominationAwardsEvent;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.*;

/**
 * This class contains utility functions related to domination awards
 *
 * @author Goosius
 */
public class SiegeWarDominationAwardsUtil {

    public static final String GLOBAL_DOMINATION_AWARDS_LOCK = "Global Domination Awards Lock";
    public static final NamespacedKey EXPIRATION_TIME_KEY = NamespacedKey.fromString("siegewar.artefactexpirytime");
    public static final PersistentDataType<Long, Long> EXPIRATION_TIME_KEY_TYPE = PersistentDataType.LONG;
    public static final NamespacedKey CUSTOM_EFFECTS_KEY = NamespacedKey.fromString("siegewar.customeffects");
    public static final PersistentDataType<String, String> CUSTOM_EFFECTS_KEY_TYPE = PersistentDataType.STRING;
    /**
     * Grant the global domination awards
     */
    public static void grantGlobalDominationAwards() {
        //Grant only on a particular day of the week
        if(LocalDateTime.now().getDayOfWeek() != SiegeWarSettings.getDominationAwardsGlobalGrantDayOfWeek())
            return;
        //Get list of qualifying nations
        List<Nation> nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
        nations = cullNationsWithTooFewDominationRecords(nations);
        // Grant awards now
        grantGlobalDominationAwardsNow(nations);
    }

    /**
     * Grant the global domination awards now, to the top nations on the given list
     *
     * @param nations The nations to award
     */
    public static void grantGlobalDominationAwardsNow(List<Nation> nations) {
        List<Integer> moneyToGrant = SiegeWarSettings.getDominationAwardsGlobalGrantedMoney();
        List<List<Integer>> numberOfArtefactsToGrant = SiegeWarSettings.getDominationAwardsGlobalGrantedOffers();

        synchronized (GLOBAL_DOMINATION_AWARDS_LOCK) {
            //The number of awardees will be as configured, or the size of the nations list, whichever is smaller, 
            int numberOfAwardees = Math.min(moneyToGrant.size(), nations.size());
            //Sort the nations by their domination records
            nations.sort(SiegeWarNationUtil.BY_GLOBAL_DOMINATION_RANKING);     
            //Calculate awards
            List<Nation> awardees = new ArrayList<>();
            Map<Nation, Integer> nationMoneyMap = new HashMap<>();
            Map<Nation, List<ItemStack>> nationArtefactMap = new HashMap<>();
            Nation nation;
            for(int nationPosition = 0; nationPosition < numberOfAwardees; nationPosition++) {
                nation = nations.get(nationPosition);
                //Record awardee
                awardees.add(nation);
                //Record money
                nationMoneyMap.put(nation, moneyToGrant.get(nationPosition));
                //Record artefacts
                nationArtefactMap.put(nation, generateArtefacts(numberOfArtefactsToGrant.get(nationPosition)));
            }

            //Fire event so other plugins can read/modify the awards
            GlobalDominationAwardsEvent event = new GlobalDominationAwardsEvent(awardees, nationMoneyMap, nationArtefactMap);
            Bukkit.getPluginManager().callEvent(event);

            //Grant all awards now
            grantGlobalDominationAwardsNow(event);

            //Remove all domination records
            nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
            for(Nation nationForRecordRemoval: nations) {
                NationMetaDataController.setDominationRecord(nationForRecordRemoval, Collections.emptyList());
                nationForRecordRemoval.save();
            }
        }       
    }

    private static void grantGlobalDominationAwardsNow(GlobalDominationAwardsEvent event) {
        List<Nation> awardees = event.getAwardees();
        Map<Nation, Integer> moneyAwards = event.getMoneyAwards();
        Map<Nation, List<ItemStack>> artefactAwards = event.getArtefactAwards();
        int moneyToGrant;
        int numArtefacts;
        String moneyText;
        String artefactText;
        List<ItemStack> artefactsToGrant;
        List<Translatable> globalMessageLines = new ArrayList<>();
        int readableNationPosition = 0;

        for(Nation nation: awardees) {
            try {
                //Gib Money
                moneyToGrant = moneyAwards.get(nation);
                nation.getAccount().deposit(moneyToGrant, "Global Domination Award");
                if(TownyEconomyHandler.isActive()) {
                    moneyText = TownyEconomyHandler.getFormattedBalance(moneyToGrant);
                } else{
                    moneyText = Translatable.of("msg_na").toString();
                }
                //Gib Artefacts
                artefactsToGrant = artefactAwards.get(nation);
                grantArtefactsToNation(nation, artefactsToGrant);
                numArtefacts = 0;
                for(ItemStack artefact: artefactsToGrant) {
                    numArtefacts += artefact.getAmount();
                }
                artefactText = Integer.toString(numArtefacts);
                //Add to Global message
                readableNationPosition++;
                globalMessageLines.add(Translatable.of("msg_global_domination_awards_line", readableNationPosition, nation.getName(), moneyText, artefactText));
            } catch (Throwable t) {
                SiegeWar.severe("Problem granting global domination award to nation " + nation.getName());
                SiegeWar.severe(t.getMessage());
                t.printStackTrace();
            }
        }

        //Send Global Message
        Translatable globalMessageHeader = Translatable.of("msg_global_domination_awards_header");
        Messaging.sendGlobalMessage(globalMessageHeader, globalMessageLines);
    }

    private static List<Nation> cullNationsWithTooFewDominationRecords(List<Nation> nations) {
        int requiredRecords = SiegeWarSettings.getDominationAwardsGlobalMinimumAssessmentPeriodHours();
        List<Nation> result = new ArrayList<>();
        for(Nation nation: nations) {
            if(NationMetaDataController.getDominationRecord(nation).size() >= requiredRecords)
                result.add(nation);
        }
        return result;
    }

    private static void grantArtefactsToNation(Nation nation, List<ItemStack> artefacts) {
        //Get capital homeblock
        if(!nation.hasCapital()) {
            TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_err_nation_domination_awards_no_capital"));
            return;
        }
        if(!nation.getCapital().hasHomeBlock()) {
            TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_err_nation_domination_awards_no_capital_homeblock"));
            return;
        }
        WorldCoord homeBlockWorldCoord = nation.getCapital().getHomeBlockOrNull().getWorldCoord();
        //Drop artefacts into chests               
        SiegeWar.getSiegeWar().getServer().getScheduler().runTask(SiegeWar.getSiegeWar(), ()->  dropItemsInChests(nation, homeBlockWorldCoord, artefacts));
    }

    private static void dropItemsInChests(Nation nation, WorldCoord chunkWorldCoord, List<ItemStack> itemsToGrant) {
        try {
            chunkWorldCoord.loadChunks();
            Location homeBlockLocation = new Location(chunkWorldCoord.getBukkitWorld(), 
                                                    chunkWorldCoord.getX() * TownySettings.getTownBlockSize(),
                                                    64,
                                                    chunkWorldCoord.getZ()* TownySettings.getTownBlockSize());
            Chunk chunk = homeBlockLocation.getChunk();
            BlockState[] tileEntities = chunk.getTileEntities();
            List<Chest> signedChests = identifySignedChests(tileEntities);            
            List<Chest> generalChests = identifyChests(tileEntities, signedChests);
            
            //Deposit artefacts
            depositArtefactsIntoChests(itemsToGrant, signedChests);
            if(itemsToGrant.size() > 0) 
                depositArtefactsIntoChests(itemsToGrant, generalChests);
            if(itemsToGrant.size() > 0)
                TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_err_nation_domination_awards_not_enough_chests"));

        } finally {
            chunkWorldCoord.unloadChunks();
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

    private static List<ItemStack> generateArtefacts(List<Integer> numberOfArtefactsPerTier) {
        List<ItemStack> artefacts = new ArrayList<>();
        for(int tier = 0; tier < numberOfArtefactsPerTier.size(); tier++) {
            artefacts.addAll(generateArtefacts(tier, numberOfArtefactsPerTier.get(tier)));
        }
        return artefacts;
    }

    private static List<ItemStack> generateArtefacts(int tier, int numOffers) {
        List<ItemStack> result = new ArrayList<>();
        List<ItemStack> offersInTier = SiegeWarSettings.getDominationAwardsArtefactOffers().get(tier);
        for(int i = 0; i < numOffers; i++) {
            //Identify Random offer
            ItemStack offer = offersInTier.get((int)(Math.random() * offersInTier.size()));
            //Generate the artefact(s) specified by that offer
            ItemStack artefact = offer.clone();
            ItemMeta itemMeta =  artefact.getItemMeta();
            //Set expiration time
            long expirationTime = System.currentTimeMillis() + (long)(SiegeWarSettings.getDominationAwardsArtefactExpiryLifetimeDays() * 86400000); 
            itemMeta.getPersistentDataContainer().set(EXPIRATION_TIME_KEY, EXPIRATION_TIME_KEY_TYPE, expirationTime);
            artefact.setItemMeta(itemMeta);
            //Add to result
            result.add(artefact);
        }
        return result;
    }

    /**
     * Add a domination record to each nation, based on their current rank on the /n list.
     */
    public static void addDominationRecords() {
        synchronized (GLOBAL_DOMINATION_AWARDS_LOCK) {
            if(!SiegeWarSettings.isDominationAwardsGlobalEnabled())
                return;
            //Add a domination record for each nation
            List<Nation> sortedNationList = getNationsListSortedForGlobalDominationAwards();
            List<String> dominationRecord;
            Nation nation;
            for(int i = 0; i < sortedNationList.size(); i++) {
                nation = sortedNationList.get(i);
                dominationRecord = NationMetaDataController.getDominationRecord(nation);
                dominationRecord.add(Integer.toString(i));
                NationMetaDataController.setDominationRecord(nation, dominationRecord);
                nation.save();
            }
        }
    }

    public static List<Nation> getNationsListSortedForGlobalDominationAwards() {
        List<Nation> nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
        Map<String, Comparator<Nation>> availableComparators = new HashMap<>();
        availableComparators.put("num_residents", SiegeWarNationUtil.BY_NUM_RESIDENTS);
        availableComparators.put("num_towns", SiegeWarNationUtil.BY_NUM_TOWNS);
        availableComparators.put("num_townblocks", SiegeWarNationUtil.BY_NUM_TOWNBLOCKS);
        availableComparators.put("num_online_players", SiegeWarNationUtil.BY_NUM_RESIDENTS);               
        Comparator<Nation> nationSortComparator = availableComparators.get(SiegeWarSettings.getDominationAwardsGlobalAssessmentCriterion().toLowerCase());
        if(nationSortComparator == null) {
            throw new RuntimeException("Problem adding global domination record. Unknown sorting criterion: " + SiegeWarSettings.getDominationAwardsGlobalAssessmentCriterion());
        } else {
            nations.sort(nationSortComparator);     
            return nations;      
        }
    }


    /**
    * Determine is a given candidate is an artefact
    * @param artefact the candidate
    *
    * @return true if the candidate is an artefact
    */
    public static boolean isArtefact(Object artefact) {
        //Get persistent data container
        PersistentDataContainer persistentDataContainer;
        if(artefact instanceof ItemStack && ((ItemStack) artefact).hasItemMeta()) {
            persistentDataContainer = ((ItemStack) artefact).getItemMeta().getPersistentDataContainer();
        } else if (artefact instanceof Projectile) {
            persistentDataContainer = ((Projectile) artefact).getPersistentDataContainer();
        } else {
            return false;
        }
        //Determine if artefact
        return persistentDataContainer.has(EXPIRATION_TIME_KEY, EXPIRATION_TIME_KEY_TYPE);
    }

    /**
    * Determine is a given item is an expired artefact
    * @param item the item
    *
    * @return true if the item is an expired artefact
    */
    public static boolean isExpiredArtefact(ItemStack item) {
        PersistentDataContainer persistentDataContainer;
        Long expiryTime;
        if (item.hasItemMeta()) {
            persistentDataContainer = item.getItemMeta().getPersistentDataContainer();
            expiryTime = persistentDataContainer.get(EXPIRATION_TIME_KEY, EXPIRATION_TIME_KEY_TYPE);
            return expiryTime != null && System.currentTimeMillis() > expiryTime;
        } else {
            return false;
        }
   }

    /**
     * This method scans a certain number of the online players
     * 
     * If any of those online players are carrying expired artefacts:
     * 1. All their artefacts are deleted.
     * 2. All their artefacts explode.
     */
    public static void evaluateArtefactExpiries() {
        if(!SiegeWarSettings.isDominationAwardsGlobalEnabled())
            return;
        //Decide which players to scan
        List<Player> playersToScan = new ArrayList<>();
        double normalizedScanChance;
        for(Player player: Bukkit.getOnlinePlayers()) {
            normalizedScanChance = SiegeWarSettings.getDominationAwardsArtefactExpiryPercentageChancePerShortTick() / 100;
            if(Math.random() < normalizedScanChance) {
                playersToScan.add(player);
            }
        }
        if(playersToScan.size() == 0)
            return;
        //Scan the players
        int numExpiredArtefacts;
        int explosionPower;
        for(Player player: playersToScan) {
            //Delete Artefacts
            numExpiredArtefacts = 0;
            for(ItemStack item: player.getInventory().getContents()) {
                if(item != null && isExpiredArtefact(item)) {
                    item.setAmount(0);
                    numExpiredArtefacts++;
                }
            }
            //Create explosion
            if(numExpiredArtefacts > 0 && SiegeWarSettings.getDominationAwardsArtefactExpiryExplosionsEnabled()) {
                explosionPower = SiegeWarSettings.getDominationAwardsArtefactExpiryExplosionsBasePower()
                                    + (SiegeWarSettings.getDominationAwardsArtefactExpiryExplosionsExtraPowerPerExpiredArtefact() * numExpiredArtefacts);
                int finalExplosionPower = Math.min(explosionPower, SiegeWarSettings.getDominationAwardsArtefactExpiryExplosionsMaxPower());                
                Bukkit.getScheduler().runTask(SiegeWar.getSiegeWar(), ()-> player.getWorld().createExplosion(player.getLocation(), finalExplosionPower, true));
            }
        }
    }

    /**
     * Get the custom effects given an item meta
     * @param itemMeta the item meta
     *
     * @return list of custom effects
     */
    public static List<String> getCustomEffects(ItemMeta itemMeta) {
        //Get persistent data container
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        //Get custom effects
        if(persistentDataContainer.has(CUSTOM_EFFECTS_KEY, CUSTOM_EFFECTS_KEY_TYPE)) {
            return new ArrayList<>(Arrays.asList(persistentDataContainer.get(CUSTOM_EFFECTS_KEY, CUSTOM_EFFECTS_KEY_TYPE).replaceAll(" ","").split(",")));
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Set the custom effects given an item meta
     *
     * @param itemMeta the item meta
     * @param customEffects the custom effects
     */
    public static void setCustomEffects(ItemMeta itemMeta, List<String> customEffects) {
        //Get persistent data container
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        //Set custom effects
        persistentDataContainer.set(CUSTOM_EFFECTS_KEY, CUSTOM_EFFECTS_KEY_TYPE, customEffects.toString().replace("[","").replace("]",""));
    }

    /**
     * Get the custom effects of an artefact
     * @param artefact the artefact
     *
     * @return list of custom effects
     */
    public static List<String> getCustomEffects(Object artefact) {
        //Get persistent data container
        PersistentDataContainer persistentDataContainer;
        if(artefact instanceof ItemStack) {
            persistentDataContainer = ((ItemStack) artefact).getItemMeta().getPersistentDataContainer();
        } else if (artefact instanceof Projectile) {
            persistentDataContainer = ((Projectile) artefact).getPersistentDataContainer();
        } else {
            throw new RuntimeException("Unknown artefact class");
        }
        //Get custom effects
        if(persistentDataContainer.has(CUSTOM_EFFECTS_KEY, CUSTOM_EFFECTS_KEY_TYPE)) {
            return Arrays.asList(persistentDataContainer.get(CUSTOM_EFFECTS_KEY, CUSTOM_EFFECTS_KEY_TYPE).replaceAll(" ","").split(","));
        } else {
            return new ArrayList<>();
        }
    }
}
