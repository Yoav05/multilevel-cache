package yoav;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


class LRUItem {
    private String value;
    private long key;
    private LRUItem left;
    private LRUItem right;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public LRUItem getLeft() {
        return left;
    }

    public void setLeft(LRUItem left) {
        this.left = left;
    }

    public LRUItem getRight() {
        return right;
    }

    public void setRight(LRUItem right) {
        this.right = right;
    }
}


class LRUCache implements CacheInterface {

    private final long memorySize;
    private DiskCache diskCache;
    private Map<Long, LRUItem> memoryCache;
    private long currentMemorySize = 0;
    private LRUItem begin, end;

    LRUCache(long memorySize, long diskSize, String path) throws FileNotFoundException {
        this.memorySize = memorySize;
        this.diskCache = new DiskCache(diskSize, path);
        this.memoryCache = new HashMap<Long, LRUItem>();
    }

    @Override
    public void put(long key, String value) {
        if (memoryCache.containsKey(key)) {
            // Если содержится в кеше, просто обновляем приоритет
            LRUItem item = memoryCache.get(key);
            item.setValue(value);
            removeDequeNode(item);
            addHead(item);
        } else if (value.getBytes(StandardCharsets.UTF_8).length + currentMemorySize > memorySize && value.getBytes(StandardCharsets.UTF_8).length < memorySize) {
            // Удаляем элементы из кеша, пока не сможем вставить новый элемент
            // В случае нехватки памяти в кеше, удаляемые элементы переносим на диск
            while (value.getBytes(StandardCharsets.UTF_8).length + currentMemorySize > memorySize) {
                currentMemorySize -= end.getValue().getBytes(StandardCharsets.UTF_8).length;
                diskCache.put(end.getKey(), end.getValue());
                memoryCache.remove(end.getKey());
                removeDequeNode(end);
            }
            addValueToCache(key, value);
        } else if (value.getBytes(StandardCharsets.UTF_8).length + currentMemorySize <= memorySize) {
            // Новое значение помещается в кеш, просто добавляем его
            addValueToCache(key, value);

        } else if (value.getBytes(StandardCharsets.UTF_8).length > memorySize) {
            // Кидаем исключение, значение больше, чем весь memory size
            throw new IllegalStateException("Размер данного значения, больше чем размер кеша");
        }
    }

    @Override
    public String get(long key) {
        if (memoryCache.containsKey(key)) {
            // Обновляем приоритет
            LRUItem item = memoryCache.get(key);
            removeDequeNode(item);
            addHead(item);
            return item.getValue();
        } else if (diskCache.containsKey(key)) {
            String valueFromDisk = diskCache.get(key);
            put(key, valueFromDisk);
            return valueFromDisk;
        }
        return null;
    }

    private void addValueToCache(long key, String value) {
        currentMemorySize += value.getBytes(StandardCharsets.UTF_8).length;
        LRUItem item = new LRUItem();
        item.setLeft(null);
        item.setRight(null);
        item.setValue(value);
        item.setKey(key);
        addHead(item); // Добавили на самый высокий приоритет
        memoryCache.put(key, item); // Записали в оперативную память
    }

    private void addHead(LRUItem node) {
        node.setRight(begin);
        node.setLeft(null);
        if (begin != null) {
            begin.setLeft(node);
        }
        begin = node;
        if (end == null) {
            end = begin;
        }
    }

    private void removeDequeNode(LRUItem node) {

        if (node.getLeft() != null) {
            node.getLeft().setRight(node.getRight());
        } else {
            begin = node.getRight();
        }
        if (node.getRight() != null) {
            node.getRight().setLeft(node.getLeft());
        } else {
            end = node.getLeft();
        }
    }
}
