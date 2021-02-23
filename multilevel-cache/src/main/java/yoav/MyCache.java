package yoav;

import java.io.FileNotFoundException;

public class MyCache implements CacheInterface {

    private long memorySize;
    private long diskSize;
    private String path;

    private CacheInterface cache;

    public MyCache(long memorySize, long diskSize, String path, EvictionPolicy policy) throws FileNotFoundException {
        switch (policy) {
            case LRU:
                this.cache = new LRUCache(memorySize, diskSize, path);
                break;
            case LFU:
                this.cache = new LFUCache(memorySize, diskSize, path);
            default:
                break;
        }
    }


    @Override
    public void put(long key, String value) {
        cache.put(key, value);
    }

    @Override
    public String get(long key) {
        return cache.get(key);
    }
}
