package lib.toolkit.base.managers.tools

import android.content.Context
import android.os.Build
import com.bumptech.glide.disklrucache.DiskLruCache
import com.bumptech.glide.util.LruCache
import lib.toolkit.base.BuildConfig
import java.io.*

open class CacheTool(private val context: Context) {

    companion object {
        private val CACHE_SUB_DIR = "files_cache"
        private val MAX_MEMORY = Runtime.getRuntime().maxMemory()
        private val CACHE_MEM_SIZE: Long = 10
        private val CACHE_STORE_SIZE = (50 * 1024 * 1024).toLong()
        private val CACHE_SIZE = MAX_MEMORY / CACHE_MEM_SIZE
        private val CACHE_STORE_COUNT = 1
    }

    protected var mMemoryLruCache: LruCache<String, Any>
    protected var mStorageLruCache: DiskLruCache

    protected val storageCacheDir: File by lazy {
        File(context.externalCacheDir.toString() + File.separator + CACHE_SUB_DIR)
    }

    init {
        mMemoryLruCache = LruCache(CACHE_SIZE)

        val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }

        mStorageLruCache = DiskLruCache.open(storageCacheDir,
            version,
            CACHE_STORE_COUNT,
            CACHE_STORE_SIZE
        )
    }

    @Synchronized
    fun addToMemoryCache(key: String, obj: Any) {
        mMemoryLruCache.put(key, obj)
    }

    @Synchronized
    fun getFromMemoryCache(key: String): Any? {
        return mMemoryLruCache.get(key)
    }

    @Synchronized
    fun addObjectToStorageCache(key: String, obj: Any): Boolean {
        val editor = mStorageLruCache.edit(key)
        val file = editor.getFile(0)
        if (file.createNewFile()) {
            ObjectOutputStream(FileOutputStream(file)).use {
                it.writeObject(obj)
                it.flush()
                editor.commit()
                return true
            }
        }

        return false
    }
    
    @Synchronized
    fun getObjectFromStorageCache(key: String): Any? {
        mStorageLruCache.get(key)?.let { value ->
            value.getFile(0)?.let { file ->
                return ObjectInputStream(FileInputStream(file)).use { input ->
                    input.readObject()
                }
            }
        }

        return null
    }

    @Synchronized
    fun addBytesToStorageCache(key: String, bytes: ByteArray): Boolean {
        mStorageLruCache.edit(key)?.let { edit ->
            edit.getFile(0)?.let { file ->
                if (file.createNewFile()) {
                    BufferedOutputStream(FileOutputStream(file)).use { output ->
                        output.write(bytes)
                        output.flush()
                        edit.commit()
                        return true
                    }
                }
            }
        }

        return false
    }
    
    @Synchronized
    fun getBytesFromStorageCache(key: String): ByteArray? {
        mStorageLruCache.get(key)?.let { value ->
            value.getFile(0)?.let { file ->
                return BufferedInputStream(FileInputStream(file)).use { input ->
                    input.readBytes()
                }
            }
        }

        return null
    }

    @Synchronized
    fun addObjectToCache(key: String, obj: Any) {
        addToMemoryCache(key, obj)
        addObjectToStorageCache(key, obj)
    }

    @Synchronized
    fun getObjectFromCache(key: String): Any? {
        var obj = getFromMemoryCache(key)
        if (obj == null) {
            obj = getObjectFromStorageCache(key)

            if (obj != null) {
                addToMemoryCache(key, obj)
            }
        }

        return obj
    }

    @Synchronized
    fun addBytesToCache(key: String, bytes: ByteArray) {
        addToMemoryCache(key, bytes)
        addBytesToStorageCache(key, bytes)
    }

    @Synchronized
    fun getBytesFromCache(key: String): ByteArray? {
        var bytes = getFromMemoryCache(key) as? ByteArray
        if (bytes == null) {
            bytes = getBytesFromStorageCache(key)

            if (bytes != null) {
                addToMemoryCache(key, bytes)
            }
        }

        return bytes
    }

}
