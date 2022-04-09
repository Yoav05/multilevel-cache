# multilevel-cache

A two-level cache that:
* Supports two Eviction Policy LRU and LFU
* Caches from long to String (long is key, String is value)
* On the first level, it stores data in RAM; if the allocated RAM is overflowed, it flushes the objects to disk (second level of caching)
* If there are data in RAM, it reads them from the memory, if not - from the disk
* Performs disk compacting
* The interface has two methods get and put
* The speed of the get and put methods is O(L), where L is the string length (The speed from RAM is O(L), where L is the string length, the speed of reading from disk too, except when compacting disk)
* Memory counts in bytes, approximately. Only key and string size are taken into account
