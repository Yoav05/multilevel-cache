package yoav;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

class ItemInfo {
    private long offset = 0;
    private long len = 0;

    ItemInfo(long offset, long len) {
        this.setOffset(offset);
        this.setLen(len);
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLen() {
        return len;
    }

    public void setLen(long len) {
        this.len = len;
    }
}

class DiskCache implements CacheInterface {

    private final long diskSize;
    private HashMap<Long, ItemInfo> diskMap;
    private String path;
    private RandomAccessFile file;
    private Boolean nameFlag = false;

    DiskCache(long diskSize, String path) throws FileNotFoundException {
        this.diskSize = diskSize;
        this.path = path;
        this.file = new RandomAccessFile(path + "diskCache.txt", "rw");
        this.diskMap = new HashMap<Long, ItemInfo>();
    }

    @Override
    public void put(long key, String value) {
        try {
            if (file.length() > diskSize / 2) {
                diskCompact();
            }
            if (!diskMap.containsKey(key) && (file.length() + value.getBytes(StandardCharsets.UTF_8).length) <= diskSize) {
                // Пишем в файл
                ItemInfo item = new ItemInfo(file.length(), value.getBytes(StandardCharsets.UTF_8).length);
                file.seek(item.getOffset());
                diskMap.put(key, item);
                file.write(value.getBytes(StandardCharsets.UTF_8));
            } else if (!diskMap.containsKey(key)) {
                throw new IllegalStateException("Нет места на диске для записи нового значения");
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get(long key) {
        // Читаем из диска, удаляем ключ из диск мапы, возвращаем значение в оперативку
        try {
            byte[] buf = new byte[(int) diskMap.get(key).getLen()];
            file.seek(diskMap.get(key).getOffset());
            file.read(buf, 0, (int) diskMap.get(key).getLen());
            diskMap.remove(key);
            return new String(buf, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean containsKey(long key) {
        return diskMap.containsKey(key);
    }

    private void diskCompact() {
        try {
            if (file.length() >= diskSize / 2) {
                String newDiskCacheName = "diskCache1.txt";
                RandomAccessFile newFileDisk = new RandomAccessFile(path + newDiskCacheName, "rw");

                for (Long key : diskMap.keySet()) {
                    byte[] buf = new byte[(int) diskMap.get(key).getLen()];
                    file.seek(diskMap.get(key).getOffset());
                    file.read(buf, 0, (int) diskMap.get(key).getLen());
                    diskMap.get(key).setOffset(newFileDisk.length());
                    newFileDisk.seek(diskMap.get(key).getOffset());
                    newFileDisk.write(buf);
                }
                file.close();
                newFileDisk.close();
                File oldFile = new File(path + "diskCache.txt");
                oldFile.delete();
                File newFile = new File(path + "diskCache1.txt");
                newFile.renameTo(new File(path + "diskCache.txt"));
                this.file = new RandomAccessFile(path + "diskCache.txt", "rw");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
