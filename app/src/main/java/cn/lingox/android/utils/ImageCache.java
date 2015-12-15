package cn.lingox.android.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * 图片缓存工具类
 */
public class ImageCache {

    private static ImageCache imageCache = null;
    private LruCache<String, Bitmap> cache = null;

    private ImageCache() {
        // use 1/8 of available heap size
        cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public static synchronized ImageCache getInstance() {
        if (imageCache == null) {
            imageCache = new ImageCache();
        }
        return imageCache;
    }

    /**
     * put bitmap to image cache
     *
     * @param key 图片url
     * @param value 图片的Bitmap实例
     * @return the puts bitmap
     */
    public Bitmap put(String key, Bitmap value) {
        return cache.put(key, value);
    }

    /**
     * return the bitmap
     *
     * @param key 图片url
     * @return the puts bitmap
     */
    public Bitmap get(String key) {
        return cache.get(key);
    }
}