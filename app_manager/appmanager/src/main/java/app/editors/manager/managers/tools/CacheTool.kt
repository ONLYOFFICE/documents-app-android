package app.editors.manager.managers.tools

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.CacheTool
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils
import javax.inject.Inject

class CacheTool @Inject constructor(context: Context) : CacheTool(context) {

    private var job: Job? = null

    fun addBitmap(key: String, bitmap: Bitmap, listener: ((isWrite: Boolean) -> Unit)? = null) {
        job = CoroutineScope(Dispatchers.Default).launch {
            val newKey = StringUtils.getMd5(key)
            addToMemoryCache(newKey ?: key, bitmap)
            listener?.invoke(addBytesToStorageCache(newKey ?: key, FileUtils.bitmapToBytes(bitmap = bitmap)))
        }
    }

    fun getBitmap(key: String, listener: (bitmap: Bitmap) -> Unit) {
        job = CoroutineScope(Dispatchers.Default).launch {
            val newKey = StringUtils.getMd5(key)

            // First, try get cache from memory
            val `object` = getFromMemoryCache(newKey ?: key)
            if (`object` is Bitmap) {
                listener(`object`)
            }

            // Else, try get cache from storage
            val bytes = getBytesFromStorageCache(newKey ?: key)
            if (bytes != null) {
                val bitmap = FileUtils.bytesToBitmap(bytes)
                addToMemoryCache(key, bitmap)
                listener(bitmap)
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }

}