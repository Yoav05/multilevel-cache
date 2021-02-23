package ru.hse.cs.java2020.task02;

import yoav.CacheInterface;
import yoav.MyCache;
import yoav.EvictionPolicy;
import java.io.FileNotFoundException;


public class Main {
    public static void main(String[] args) {
        String path = "/Users/yoav/Desktop/javaFileDisk/"; // Путь к директории, где будет хранится кеш
        final int n = 50;
        final long memorySize = 150;
        final long diskSize = 1000;
        try {
            CacheInterface cache = new MyCache(memorySize, diskSize, path, EvictionPolicy.LFU);
            cache.put(0, "JavaHSE0");
            for (int i = 1; i < n; ++i) {
                cache.get(0);
            }
            for (int i = 1; i < n; ++i) {
                cache.put(i, "Русские" + i);
            }
            for (int i = 1; i < n; ++i) {
                System.out.println(cache.get(i));
            }
            System.out.println(cache.get(0));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
