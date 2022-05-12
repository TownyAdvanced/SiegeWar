package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.metadata.NationMetaDataController;
import com.gmail.goosius.siegewar.objects.ArtefactOffer;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;

import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.util.*;

/**
 * This class contains utility functions related to domination awards
 *
 * @author Goosius
 */
public class SiegeWarDominationAwardsUtil {

    public static final String GLOBAL_DOMINATION_AWARDS_LOCK = "Global Domination Awards Lock";
    private static final NamespacedKey EXPIRATION_TIME_KEY = NamespacedKey.fromString("siegewar.artefactexpirytime");
    private static final PersistentDataType<Long, Long> EXPIRATION_TIME_KEY_TYPE = PersistentDataType.LONG;

    /**
     * Grant the global domination awards
     */
    public static void grantGlobalDominationAwards() {
        if(LocalDateTime.now().getDayOfWeek() != SiegeWarSettings.getDominationAwardsGlobalGrantDayOfWeek())
            return;
        grantGlobalDominationAwardsNow(null);
    }
        
    /**
     * Grant the global domination awards now, without waiting for the correct day
     *
     * @param sender The sender. Null unless executed by SWA command
     */
    public static void grantGlobalDominationAwardsNow(CommandSender sender) {
        synchronized (GLOBAL_DOMINATION_AWARDS_LOCK) {
            List<Integer> moneyToGrant = SiegeWarSettings.getDominationAwardsGlobalGrantedMoney();
            List<List<Integer>> artefactsToGrant = SiegeWarSettings.getDominationAwardsGlobalGrantedOffers();

            //Get list of qualifying nations
            List<Nation> nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
            nations = cullNationsWithTooFewDominationRecords(nations);
            if(nations.size() == 0) {
                String message = "Global Domination Awards: No nations qualified for awards this week, due to having too few domination records.";
                if(sender == null) {
                    SiegeWar.info(message);
                } else {
                    sender.sendMessage(message);
                }
                return; 
            }

            //The number of awardees will be as configured, or the size of the nations list, whichever is smaller, 
            int numberOfAwardees = Math.min(moneyToGrant.size(), nations.size());

            //Sort nations by recorded dominance
            nations.sort(SiegeWarNationUtil.BY_GLOBAL_DOMINATION_RANKING);     
                  
            //Gib awards
            Nation nation = null;
            String moneyText;
            int numArtefactsGranted;
            String artefactText;
            Translatable globalMessageHeader = Translatable.of("msg_global_domination_awards_header");
            List<Translatable> globalMessageLines = new ArrayList<>();
            for(int nationPosition = 0; nationPosition < numberOfAwardees; nationPosition++) {
                try{
                    nation = nations.get(nationPosition);            
                    //Gib money
                    if(TownyEconomyHandler.isActive()) {
                        nation.getAccount().deposit(moneyToGrant.get(nationPosition), "Global Domination Award");
                        moneyText = TownyEconomyHandler.getFormattedBalance(moneyToGrant.get(nationPosition));
                    } else{
                        moneyText = Translatable.of("msg_na").toString();
                    }
                    //Gib artefacts
                    numArtefactsGranted = grantArtefactsToNation(artefactsToGrant.get(nationPosition), nation);
                    artefactText = Integer.toString(numArtefactsGranted);
                    //Add to Global message                     
                    globalMessageLines.add(Translatable.of("msg_global_domination_awards_line", nationPosition+1, nation.getName(), moneyText, artefactText));
                } catch(Throwable t) {
                    SiegeWar.severe("Problem granting global domination award to nation " + nation.getName());
                    SiegeWar.severe(t.getMessage());
                    t.printStackTrace();
                }
            }

            //Send Global Message now
            Messaging.sendGlobalMessage(globalMessageHeader, globalMessageLines);

            //Remove all domination records
            nations = new ArrayList<>(TownyUniverse.getInstance().getNations());
            for(Nation nationForRecordRemoval: nations) {
                NationMetaDataController.setDominationRecord(nationForRecordRemoval, Collections.emptyList());
                nationForRecordRemoval.save();
            }
        }       
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

    private static int grantArtefactsToNation(List<Integer> offersToGrantFromEachTier, Nation nation) {
        List<ItemStack> artefactsToGrant = new ArrayList<>();
        for(int tier = 0; tier < offersToGrantFromEachTier.size(); tier++) {
            artefactsToGrant.addAll(generateArtefacts(tier, offersToGrantFromEachTier.get(tier)));
        }
        //Get capital homeblock
        if(!nation.hasCapital()) {
            TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_err_nation_domination_awards_no_capital"));
            return 0;
        }
        if(!nation.getCapital().hasHomeBlock()) {
            TownyMessaging.sendPrefixedNationMessage(nation, Translatable.of("msg_err_nation_domination_awards_no_capital_homeblock"));
            return 0;
        }
        WorldCoord homeBlockWorldCoord = nation.getCapital().getHomeBlockOrNull().getWorldCoord();
        //Drop artefacts into chests               
        SiegeWar.getSiegeWar().getServer().getScheduler().runTask(SiegeWar.getSiegeWar(), ()->  dropItemsInChests(nation, homeBlockWorldCoord, artefactsToGrant));
        return artefactsToGrant.size();
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

    private static List<ItemStack> generateArtefacts(int tier, int numOffers) {
        List<ItemStack> result = new ArrayList<>();
        List<ArtefactOffer> offersInTier = SiegeWarSettings.getDominationAwardsArtefactOffers().get(tier+1);
        for(int i = 0; i < numOffers; i++) {
            //Identify Random offer
            ArtefactOffer offer = offersInTier.get((int)(Math.random() * offersInTier.size()));
            //Generate the artefacts specified by that offer
            for(int ii = 0; ii < offer.quantity; ii++) {
                ItemStack artefact = offer.artefactTemplate.clone();
                ItemMeta itemMeta =  artefact.getItemMeta();
                long expirationTime = System.currentTimeMillis() + (long)(SiegeWarSettings.getDominationAwardsArtefactExpiryLifetimeDays() * 864500000); 
                itemMeta.getPersistentDataContainer().set(EXPIRATION_TIME_KEY, EXPIRATION_TIME_KEY_TYPE, expirationTime);
                artefact.setItemMeta(itemMeta);
                result.add(artefact);
            }
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
            List<Nation> sortedNationList = getNationsListSortedNormally();            
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

    public static List<Nation> getNationsListSortedNormally() {
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

    /**
    * Determine is a given item is an artefact
    * @param item the item
    *
    * @return true if the item is a artefact
    */
    public static boolean isArtefact(ItemStack item) {
        return item.getItemMeta() != null
                && item.getItemMeta().getPersistentDataContainer().has(EXPIRATION_TIME_KEY, EXPIRATION_TIME_KEY_TYPE);
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
        if(item.getItemMeta() != null) {
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
                Bukkit.getScheduler().runTask(SiegeWar.getSiegeWar(), ()-> player.getWorld().createExplosion(player.getEyeLocation(), finalExplosionPower, true));
            }
        }
    }
}
