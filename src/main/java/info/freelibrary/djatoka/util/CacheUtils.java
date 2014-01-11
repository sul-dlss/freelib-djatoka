
package info.freelibrary.djatoka.util;

import java.util.ArrayList;
import java.util.List;

public class CacheUtils {

    /**
     * Return a file name for the cached file based on its characteristics.
     * 
     * @param aLevel A level to be cached
     * @param aScale A scale to be cached
     * @param aRegion A region to be cached
     * @return The file name for the cached file
     */
    public static final String getFileName(String aLevel, String aScale,
            String aRegion) {
        StringBuilder cfName = new StringBuilder("image_");
        String region = isEmpty(aRegion) ? "all" : aRegion.replace(',', '-');

        /*
         * TODO: Right now this assumes if it gets passed a level that it's not
         * doing a region... what possibilities do we exclude by doing this?
         */
        if (aLevel != null && !aLevel.equals("") && !aLevel.equals("-1")) {
            cfName.append(aLevel);
        } else {
            cfName.append(aScale).append('_').append(region);
        }

        return cfName.append(".jpg").toString();
    }

    /**
     * Gets the max level for the supplied height and width.
     * 
     * @param aHeight A height of the image
     * @param aWidth A width of the image
     * @return The maximum level using the supplied height and width
     */
    public static final int getMaxLevel(int aHeight, int aWidth) {
        return (int) Math.ceil(Math.log(Math.max(aHeight, aWidth)) /
                Math.log(2));
    }

    /**
     * Returns the scale for the supplied level.
     * 
     * @param aLevel A supplied image level
     * @return The scale for the supplied level
     */
    public static final int getScale(int aLevel) {
        return (int) Math.pow(2, aLevel);
    }

    /**
     * Get a list of tile queries based on the supplied height and width.
     * 
     * @param aHeight A supplied image height
     * @param aWidth A supplied image width
     * @return The list of tile queries based on the supplied height and width
     */
    public static final List<String> getCachingQueries(int aHeight, int aWidth) {
        int maxLevel = getMaxLevel(aHeight, aWidth);
        List<String> list = new ArrayList<String>();

        for (int level = 0; level <= maxLevel; level++) {
            String scale = Integer.toString(getScale(level));

            if (level <= 8) {
                list.add("/all/" + scale);
                continue; // We don't need to get regions for these
            }

            int tileSize = getTileSize(level, maxLevel);
            int x = 0;

            /* x is left point and y is top point */
            for (int xSize = 0, y = 0; xSize <= aWidth; xSize += tileSize) {
                if (x * tileSize > (aWidth + tileSize)) {
                    break;
                }

                for (int ySize = 0; ySize <= aHeight; ySize += tileSize) {
                    String region = getRegion(level, aWidth, aHeight, x, y++);

                    if (y * tileSize > (aHeight + tileSize)) {
                        break;
                    }

                    list.add("/" + region + "/" + scale);
                }

                x += 1;
            }
        }

        return list;
    }

    /**
     * Gets a string representation of the region for the supplied
     * characteristics.
     * 
     * @param aLevel A image level
     * @param aWidth An image width
     * @param aHeight An image height
     * @param aX An X coordinate
     * @param aY A Y coordinate
     */
    public static final String getRegion(int aLevel, int aWidth, int aHeight,
            int aX, int aY) {
        // All the other code uses width, height (rather than height, width); I
        // should probably change to match their use pattern/order for
        // consistency

        int tileSize = getTileSize(aLevel, getMaxLevel(aHeight, aWidth));
        int startX, startY, tileSizeX, tileSizeY;

        /* startX is left point and startY is top point */
        if (aLevel > 8) {
            if (aX == 0) {
                tileSizeX = tileSize - 1;
            } else {
                tileSizeX = tileSize;
            }

            if (aY == 0) {
                tileSizeY = tileSize - 1;
            } else {
                tileSizeY = tileSize;
            }

            startX = aX * tileSize;
            startY = aY * tileSize;

            return startY + "," + startX + "," + tileSizeY + "," + tileSizeX;
        }

        return "all";
    }

    private static boolean isEmpty(String aString) {
        return aString == null || aString.equals("");
    }

    private static int getTileSize(int aLevel, int aMaxLevel) {
        return (int) (Math.pow(2, aMaxLevel) / Math.pow(2, aLevel)) * 256;
    }
}
