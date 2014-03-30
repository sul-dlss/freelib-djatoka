/*
 * Copyright (c) 2007 Los Alamos National Security, LLC.
 *
 * Los Alamos National Laboratory Research Library Digital Library Research &
 * Prototyping Team
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package gov.lanl.adore.djatoka.openurl;

import java.io.File;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an Least Recently Used (LRU) cache Manager.
 * 
 * @param max_cache the maximum cache size that will be kept in the cache.
 */
public class TileCacheManager<K, V> {

    private static Logger LOGGER = LoggerFactory.getLogger(TileCacheManager.class);

    private LinkedHashMap<K, V> cacheMap; // For fast search/remove

    private final int max_cache;

    private static final float loadFactor = 0.75F;

    private static final boolean accessOrder = true;

    /** The class constructor */
    public TileCacheManager(int max_cache) {
        this.max_cache = max_cache;
        this.cacheMap = new LinkedHashMap<K, V>(max_cache, loadFactor, accessOrder) {

            private static final long serialVersionUID = 1;

            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("cacheSize: " + size());
                }

                boolean d = size() > TileCacheManager.this.max_cache;
                if (d) {
                    File f = new File((String) eldest.getValue());

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("deletingTile: " + eldest.getValue());
                    }

                    if (f.exists()) {
                        f.delete();
                    }

                    remove(eldest.getKey());
                }

                return false;
            };
        };
    }

    /**
     * Puts the key and value into the tile cache.
     * 
     * @param key The tile cache key
     * @param val The tile cache value
     * @return The older value if any
     */
    public synchronized V put(K key, V val) {
        return cacheMap.put(key, val);
    }

    /**
     * Removes value of supplied key from tile cache.
     * 
     * @param key The key of the value to be removed
     * @return The value of the supplied key
     */
    public synchronized V remove(K key) {
        if (get(key) instanceof String) {
            File f = new File((String) get(key));
            if (!f.delete() && LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to delete file: {}", f);
            }
        }
        return cacheMap.remove(key);
    }

    /**
     * Gets the value of supplied key from tile cache.
     * 
     * @param key The key of the value to be retrieved
     * @return The value of the supplied key
     */
    public synchronized V get(K key) {
        return cacheMap.get(key);
    }

    /**
     * Returns true if the supplied key exists in the cache; else, false.
     * 
     * @param key The key to query the cache about
     * @return True if the key is found; else, false
     */
    public synchronized boolean containsKey(K key) {
        return cacheMap.containsKey(key);
    }

    /**
     * Gets the size of the tile cache.
     * 
     * @return The size of the tile cache
     */
    public synchronized int size() {
        return cacheMap.size();
    }

    /**
     * Clears the tile cache.
     */
    public synchronized void clear() {
        cacheMap.clear();
    }

}
