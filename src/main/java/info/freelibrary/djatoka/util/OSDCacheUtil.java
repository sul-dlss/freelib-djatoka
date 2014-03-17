
package info.freelibrary.djatoka.util;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.freelibrary.util.StringUtils;

public class OSDCacheUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OSDCacheUtil.class);

    private static final String REGION = "{},{},{},{}";

    /**
     * Utility used to generate regions for derivatives images prior to the point where they are requested by a user.
     */
    public OSDCacheUtil() {

    }

    /**
     * Return a list of derivatives to be generated.
     * 
     * @return
     */
    public String[] getPaths(final int aMaxLevel, final int aTileSize, final int aWidth, final int aHeight) {
        final int longDim = Math.max(aWidth, aHeight);
        final ArrayList<String> list = new ArrayList<String>();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generating OSD paths [Levels: {}; Tile Size: {}; Width: {}; Height: {} ]", aMaxLevel,
                    aTileSize, aWidth, aHeight);
        }

        for (int multiplier = 1; multiplier * aTileSize < longDim; multiplier *= 2) {
            final int tileSize = multiplier * aTileSize;

            int x = 0, y = 0, xTileSize, yTileSize;
            String region;

            for (x = 0; x < aWidth + tileSize; x += tileSize) {
                xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                yTileSize = tileSize < aHeight ? tileSize : aHeight;

                if (xTileSize > 0 && yTileSize > 0) {
                    region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                    list.add(region);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("OSD tile path region: {}", region);
                    }
                }

                for (y = tileSize; y < aHeight + tileSize; y += tileSize) {
                    xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                    yTileSize = y + tileSize < aHeight ? tileSize : aHeight - y;

                    if (xTileSize > 0 && yTileSize > 0) {
                        region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                        list.add(region);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("OSD tile path region: {}", region);
                        }
                    }
                }

                y = 0;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'{}' OSD tile path regions created", list.size());
        }

        return list.toArray(new String[list.size()]);
    }

}
