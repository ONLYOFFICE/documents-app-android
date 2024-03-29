package lib.toolkit.base.managers.utils


import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.*
import java.io.File


/*
* TODO refactoring after core merge
* */
object ContentResolverUtils {

    @JvmField
    val TAG = ContentResolverUtils::class.java.simpleName

    interface OnWebVideoListener {
        fun onGetFrame(bitmap: Bitmap)
    }

    interface OnUriListener {
        fun onGetUri(uri: Uri)
    }

    @JvmStatic
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".asc.provider", file)
    }

    //TODO Как варинт для получения URI, проверить на android 11
    @Deprecated("Not work to android 11?")
    fun getUriFromFile(context: Context, file: File): Uri {
        val filePath = file.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), "$id")
        } else {
            Uri.EMPTY
        }
    }

    @JvmStatic
    fun grandUriPermission(context: Context, intent: Intent) {
        intent.data?.let { uri ->
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach {
                context.grantUriPermission(
                        it?.activityInfo?.packageName,
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun getImageUri(context: Context, name: String): Uri? {
        FileUtils.createImageFile(context, name)?.let {
            return getFileUri(context, it)
        }
        return null
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE])
    @JvmStatic
    fun rename(context: Context, uri: Uri, newName: String): Boolean {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return File(uri.path ?: "").let {
                    it.renameTo(File(it.parent, newName))
                }
            }

            ContentResolver.SCHEME_CONTENT -> {
                return DocumentsContract.renameDocument(context.contentResolver, uri, newName) != null
            }
        }

        return false
    }

    @JvmStatic
    fun checkExported(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.close()
            true
        } catch (exception: SecurityException) {
            Log.e(TAG, "${exception.message}" )
            false
        }
    }

    @JvmStatic
    fun getName(context: Context, uri: Uri): String {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return File(uri.path ?: "").name
            }

            ContentResolver.SCHEME_CONTENT -> {
                return DocumentFile.fromSingleUri(context, uri)?.name ?: ""
            }
        }
        return ""
    }

    @JvmStatic
    fun getSize(context: Context, uri: Uri): Long {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return File(uri.path ?: "").length()
            }

            ContentResolver.SCHEME_CONTENT -> {
                return DocumentFile.fromSingleUri(context, uri)?.length() ?: 0
            }
        }
        return 0
    }

    @JvmStatic
    fun getMimeType(context: Context, uri: Uri): String {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return StringUtils.getMimeTypeFromPath(uri.path!!)
            }

            ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.getType(uri)?.let {
                    return it
                }

               return StringUtils.getMimeTypeFromPath(DocumentFile.fromSingleUri(context, uri)?.name ?: "")
            }
        }
        return StringUtils.COMMON_MIME_TYPE
    }

    @JvmStatic
    fun getFrameFromWebVideoSync(path: String, headers: Map<String, String>): Bitmap? {
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        return try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(path, headers)
            mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            null
        } finally {
            mediaMetadataRetriever?.release()
        }
    }

    @JvmStatic
    fun getFrameFromWebVideoAsync(path: String, headers: Map<String, String>,
                                  size: Point?, onFrameFromWebVideo: OnWebVideoListener,
                                  scope: CoroutineScope
    ): Job {
        val job = scope.launch(Dispatchers.Main) {
            val image = async(Dispatchers.IO) {
                val bitmap = getFrameFromWebVideoSync(path, headers)
                if (size != null && bitmap != null) {
                    return@async FileUtils.getResizedBitmap(
                        bitmap,
                        size.x,
                        size.y
                    )
                } else {
                    return@async null
                }
            }
            image.await()?.let { bitmap ->
                onFrameFromWebVideo.onGetFrame(bitmap)
            }

        }
        return job
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun getBitmapUriAsync(context: Context, bitmap: Bitmap, onUriListener: OnUriListener, name: String, scope: CoroutineScope): Job {
        val job = scope.launch(Dispatchers.Main) {
            val image = async(Dispatchers.IO) {
                val file = FileUtils.createBitmapFile(
                    context,
                    bitmap,
                    name
                )
                if (file != null) {
                    return@async getFileUri(
                        context,
                        file
                    )
                } else return@async null
            }
            image.await()?.let { uri ->
                onUriListener.onGetUri(uri)
            }
        }
        return job
    }

}
