package app.documents.core.manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import lib.toolkit.base.managers.utils.ContentResolverUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException

class ProgressRequestBody(context: Context, val uri: Uri) : RequestBody() {

    @Volatile
    private var isCanceled = false

    private val contentResolver: ContentResolver = context.contentResolver
    private val contentType: String = ContentResolverUtils.getMimeType(context, uri)
    private val totalSize: Long = ContentResolverUtils.getSize(context, uri)
    private var onUploadCallbacks: ((total: Long, progress: Long) -> Unit)?  = null

    override fun contentType(): MediaType? {
        return MediaType.parse(contentType)
    }

    override fun contentLength(): Long {
        return totalSize
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        contentResolver.openInputStream(uri).use { inputStream ->
            var countBytes: Int
            var totalBytes: Long = 0
            while (inputStream!!.read(buffer).also { countBytes = it } != -1 && !isCanceled) {
                totalBytes += countBytes.toLong()
                sink.write(buffer, 0, countBytes)
                onUploadCallbacks?.invoke(totalSize, totalBytes)
            }
        }
    }

    fun cancel() {
        isCanceled = true
    }

    fun setOnUploadCallbacks(listener: ((total: Long, progress: Long) -> Unit)?  = null) {
        this.onUploadCallbacks = listener
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }


}