package yoav;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

class LFUItem {

    private String value;
    private long frequency = 0;

    LFUItem(String value, long frequency) {
        this.setValue(value);
        this.setFrequency(frequency);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }
}

class LFUCache implements CacheInterface {

    private long memorySize;
    private Map<Long, LFUItem> memoryCache;
    private Map<Long, LinkedHashSet<Long>> frequencyLists;
    private DiskCache diskCache;
    private long currentMemorySize = 0;
    private long minimalCurrentList = 0;

    LFUCache(long memorySize, long diskSize, String path) throws FileNotFoundException {
        this.memorySize = memorySize;
        this.memoryCache = new HashMap<Long, LFUItem>();
        this.frequencyLists = new HashMap<Long, LinkedHashSet<Long>>();
        this.diskCache = new DiskCache(diskSize, path);
        frequencyLists.put((long) 1, new LinkedHashSet<Long>());
    }

    @Override
    public void put(long key, String value) {
        if (memoryCache.containsKey(key)) {
            // Если содержится в кеше, увеличиваем счетчик элемента
            get(key);
        } else if (value.getBytes(StandardCharsets.UTF_8).length + currentMemorySize > memorySize && value.getBytes(StandardCharsets.UTF_8).length < memorySize) {
            while (value.getBytes(StandardCharsets.UTF_8).length + currentMemorySize > memorySize) {
                if (frequencyLists.get(minimalCurrentList).size() == 0) {
                    minimalCurrentList += 1;
                    continue;
                }
                long oldValue = frequencyLists.get(minimalCurrentList).iterator().next();
                currentMemorySize -= memoryCache.get(oldValue).getValue().getBytes(StandardCharsets.UTF_8).length;
                diskCache.put(oldValue, memoryCache.get(oldValue).getValue());
                frequencyLists.get(minimalCurrentList).remove(oldValue);
                memoryCache.remove(oldValue);
            }
            addValueToCache(key, value);
        } else if (value.getBytes(StandardCharsets.UTF_8).length + currentMemorySize <= memorySize) {
            addValueToCache(key, value);
        } else {
            // Кидаем исключение, значение больше, чем весь memory size
            throw new IllegalStateException("Размер данного значения, больше чем размер кеша");
        }
    }

    @Override
    public String get(long key) {
        if (memoryCache.containsKey(key)) {
            long freq = memoryCache.get(key).getFrequency();
            memoryCache.get(key).setFrequency(memoryCache.get(key).getFrequency() + 1);
            frequencyLists.get(freq).remove(key);
            if (freq == minimalCurrentList && frequencyLists.get(freq).size() == 0) {
                minimalCurrentList += 1;
            }

            if (!frequencyLists.containsKey(freq + 1)) {
                frequencyLists.put(freq + 1, new LinkedHashSet<Long>());
            }
            frequencyLists.get(freq + 1).add(key);
            return memoryCache.get(key).getValue();
        } else if (diskCache.containsKey(key)) {
            String valueFromDisk = diskCache.get(key);
            put(key, valueFromDisk);
            return valueFromDisk;
        }
        return null;
    }

    private void addValueToCache(long key, String value) {
        LFUItem item = new LFUItem(value, 1);
        minimalCurrentList = 1;
        memoryCache.put(key, item);
        currentMemorySize += item.getValue().getBytes(StandardCharsets.UTF_8).length;
        frequencyLists.get((long) 1).add(key);
    }
}
