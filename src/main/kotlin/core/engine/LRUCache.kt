package core.engine

class LRUCache<T>(val maxSize: Int) {

    private val internalCache: MutableMap<String, T> = object : LinkedHashMap<String, T>(0, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>?): Boolean {
            return size > maxSize
        }
    }

    fun put(key: String, value: T) {
        internalCache.put(key, value)
    }

    fun delete(key: String): Boolean {
        return internalCache.remove(key) != null
    }

    fun reset() {
        internalCache.clear()
    }

    fun get(key: String): T? {
        return internalCache.get(key)
    }


    fun size(): Long {
        return synchronized(this) {
            val snapshot = LinkedHashMap(internalCache)
            snapshot.size.toLong()
        }
    }


    fun dump() {
        println(internalCache)
    }

}