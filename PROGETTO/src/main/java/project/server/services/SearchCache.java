package project.server.services;

import java.util.*;

/**
 * Lightweight in-memory LRU cache for search results with TTL.
 */
public class SearchCache {
    private static final int MAX_ENTRIES = 200;
    private static final long DEFAULT_TTL_MS = 60_000; // 60 seconds

    private static class CacheEntry {
        final List<Map<String,Object>> value;
        final long expiresAt;
        CacheEntry(List<Map<String,Object>> value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    // LRU map
    private static final LinkedHashMap<String, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    private static final Object LOCK = new Object();

    public static List<Map<String,Object>> get(String key) {
        synchronized (LOCK) {
            CacheEntry e = cache.get(key);
            if (e == null) return null;
            if (System.currentTimeMillis() > e.expiresAt) {
                cache.remove(key);
                return null;
            }
            // Return a shallow copy to avoid accidental modification
            return deepCopyList(e.value);
        }
    }

    public static void put(String key, List<Map<String,Object>> value) {
        put(key, value, DEFAULT_TTL_MS);
    }

    public static void put(String key, List<Map<String,Object>> value, long ttlMs) {
        if (key == null || value == null) return;
        long expiresAt = System.currentTimeMillis() + ttlMs;
        synchronized (LOCK) {
            cache.put(key, new CacheEntry(deepCopyList(value), expiresAt));
        }
    }

    private static List<Map<String,Object>> deepCopyList(List<Map<String,Object>> src) {
        List<Map<String,Object>> copy = new ArrayList<>(src.size());
        for (Map<String,Object> row : src) {
            Map<String,Object> m = new HashMap<>();
            for (Map.Entry<String,Object> e : row.entrySet()) {
                m.put(e.getKey(), e.getValue());
            }
            copy.add(m);
        }
        return copy;
    }

    public static void clear() {
        synchronized (LOCK) { cache.clear(); }
    }
}
