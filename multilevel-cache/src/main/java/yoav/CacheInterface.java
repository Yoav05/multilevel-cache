package yoav;

public interface CacheInterface {

    void put(long key, String value);

    String get(long key);

}
