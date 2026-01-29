package com.technikh.evideos.util;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImagesCache {
    public static LruCache<String, Bitmap> imagesWarehouse;
    private static ImagesCache cache;

    public static ImagesCache getInstance() {
        if (cache == null) {
            cache = new ImagesCache();
        }
        return cache;
    }

    public void initializeCache() {
        if (imagesWarehouse == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;

            imagesWarehouse = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return (value.getRowBytes() * value.getHeight()) / 1024;
                }
            };
        }
    }

    // Unified Method Name to match your Activity
    public void addBitmapToCache(String key, Bitmap bitmap) {
        addImageToWarehouse(key, bitmap);
    }

    public void addImageToWarehouse(String key, Bitmap value) {
        if (imagesWarehouse != null && key != null && value != null) {
            if (imagesWarehouse.get(key) == null) {
                imagesWarehouse.put(key, value);
            }
        }
    }

    public Bitmap getImageFromWarehouse(String key) {
        if (imagesWarehouse != null && key != null) {
            return imagesWarehouse.get(key);
        }
        return null;
    }

    // Support for the SplashScreen's specific check
    public Bitmap getBitmapFromMemCache(String key) {
        return getImageFromWarehouse(key);
    }

    public void clearCache() {
        if (imagesWarehouse != null) {
            imagesWarehouse.evictAll();
        }
    }
}