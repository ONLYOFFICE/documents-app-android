package lib.toolkit.base.managers.utils

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import java.io.File


object PathUtils {

    @JvmStatic
    private val CONTENT_DOWNLOAD = arrayOf(
            "content://downloads/all_downloads",
            "content://downloads/public_downloads",
            "content://downloads/my_downloads"
    )

    @JvmStatic
    fun getFolderPath(context: Context, uri: Uri): String? {
        return getPath(
                context,
                DocumentsContract.buildDocumentUriUsingTree(
                        uri,
                        DocumentsContract.getTreeDocumentId(uri)
                )
        )
    }

    @JvmStatic
    fun getPath(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    getPathFromExternal(split)?.let {
                        return it
                    }
                }

                isDownloadsDocument(uri) -> {
                    context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                            (Environment.getExternalStorageDirectory().toString() + "/Download/" + name)?.let { path ->
                                if (File(path).exists()) {
                                    return path
                                }
                            }
                            (Environment.getExternalStorageDirectory().toString() + "/OnlyOffice/" + name)?.let { path ->
                                if (File(path).exists()) {
                                    return path
                                }
                            }
                        }
                    }

                    DocumentsContract.getDocumentId(uri)?.let { id ->
                        if (id.isNotEmpty()) {
                            if (id.startsWith("raw:")) {
                                return id.replaceFirst("raw:".toRegex(), "")
                            }

                            id.toLongOrNull()?.let { number ->
                                CONTENT_DOWNLOAD.forEach { content ->
                                    ContentUris.withAppendedId(Uri.parse(content), number)?.let { documentUri ->
                                        getDataColumn(
                                                context,
                                                documentUri,
                                                null,
                                                null
                                        )?.let {
                                            if (File(it).exists()) {
                                                return it
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                isMediaDocument(uri) -> {
                    DocumentsContract.getDocumentId(uri)?.let { id ->
                        val split = id.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]

                        when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> null
                        }?.let { mediaUri ->
                            getDataColumn(
                                    context,
                                    mediaUri,
                                    "_id=?",
                                    arrayOf(split[1])
                            )?.let {
                                if (File(it).exists()) {
                                    return it
                                }
                            }
                        }
                    }
                }
            }

        } else {
            when (uri.scheme) {
                "content" -> {
                    context.contentResolver.query(uri, arrayOf(MediaStore.Files.FileColumns.DATA), null, null, null)?.use {
                        if (it.moveToFirst()) {
                            return it.getString(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                        }
                    }
                }

                "file" -> {
                    return uri.path
                }
            }
        }

        return null
    }

    @JvmStatic
    private fun getPathFromExternal(pathData: Array<String>): String? {
        val type = pathData[0]
        val path = if (pathData.size == 1) {
            "/"
        } else {
            "/" + pathData[1]
        }


        if ("primary".equals(type, ignoreCase = true)) {
            (Environment.getExternalStorageDirectory().path + path)?.let {
                if (File(it).exists()) {
                    return it
                }
            }
        }

        if ("home".equals(type, ignoreCase = true)) {
            (Environment.getExternalStorageDirectory().toString() + "/Documents" + path)?.let {
                if (File(it).exists()) {
                    return it
                }
            }
        }

        (System.getenv("EXTERNAL_STORAGE") + path)?.let {
            if (File(it).exists()) {
                return it
            }
        }

        (System.getenv("SECONDARY_STORAGE") + path)?.let {
            if (File(it).exists()) {
                return it
            }
        }

        return null
    }

    @JvmStatic
    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        val column = "_data"
        val projection = arrayOf(column)

        try {
            context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndexOrThrow(column)
                    return it.getString(index)
                }
            }
        } catch (exception: IllegalArgumentException) {
            val state = context.packageManager.getApplicationEnabledSetting("com.android.providers.downloads")
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                startDownloadManager(context)
            }
        }
        return null
    }

    @JvmStatic
    private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority

    @JvmStatic
    private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority

    @JvmStatic
    private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority

    @JvmStatic
    private fun isGoogleDocument(uri: Uri) = "com.google.android.apps.docs.storage" == uri.authority

    @JvmStatic
    private fun startDownloadManager(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:" + "com.android.providers.downloads")
            })
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            context.startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
        }
    }

}