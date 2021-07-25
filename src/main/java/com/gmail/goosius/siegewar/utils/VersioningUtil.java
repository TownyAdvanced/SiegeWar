package com.gmail.goosius.siegewar.utils;

/**
 * Utility for working with plugin versioning
 */
public class VersioningUtil {

    /**
     * This method compares plugin 2 versions, current & required
     * - If the current version is less than the required, the method returns -1
     * - If the current version is equal to the required, the method returns 0
     * - If the current version is greater than the required, the method returns 1
     * 
     * @param currentVersion the current version of a plugin
     * @param requiredVersion the required version of a plugin
     * @return -1, 0, or 1
     */
    public static int comparePluginVersions(String currentVersion, String requiredVersion) {
        int comparisonResult = 0;
        
        String[] version1Splits = currentVersion.split("\\.");
        String[] version2Splits = requiredVersion.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);
    
        for (int i = 0; i < maxLengthOfVersionSplits; i++){
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

}
