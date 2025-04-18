package lib.toolkit.base.managers.tools

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import lib.toolkit.base.BuildConfig
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class LocalContentTools @Inject constructor(val context: Context) {

    companion object {
        private val TAG = LocalContentTools::class.java.simpleName

        const val DOCX_EXTENSION = "docx"
        const val DOC_EXTENSION = "doc"
        const val DOTX_EXTENSION = "dotx"
        const val DOCXF_EXTENSION = "docxf"
        const val OFORM_EXTENSION = "oform"
        const val ODT_EXTENSION = "odt"
        const val OTT_EXTENSION = "ott"
        const val TXT_EXTENSION = "txt"
        const val RTF_EXTENSION = "rtf"
        const val HTML_EXTENSION = "html"
        const val EPUB_EXTENSION = "epub"
        const val PPTX_EXTENSION = "pptx"
        const val PPSX_EXTENSION = "ppsx"
        const val POTX_EXTENSION = "potx"
        const val PPT_EXTENSION = "ppt"
        const val ODP_EXTENSION = "odp"
        const val ODG_EXTENSION = "odg"
        const val OTP_EXTENSION = "otp"
        const val XLSX_EXTENSION = "xlsx"
        const val XLTX_EXTENSION = "xltx"
        const val XLS_EXTENSION = "xls"
        const val ODS_EXTENSION = "ods"
        const val OTS_EXTENSION = "ots"
        const val CSV_EXTENSION = "csv"
        const val PDF_EXTENSION = "pdf"
        const val PAGES_EXTENSION = "pages"
        const val NUMBERS_EXTENSION = "numbers"
        const val KEY_EXTENSION = "key"
        const val HWP_EXTENSION = "hwp"
        const val HWPX_EXTENSION = "hwpx"
        private const val ASSETS_TEMPLATES = "templates"

        const val MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val MIME_TYPE_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        const val MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

        private const val URI_KEY = "external"

        private const val PREFS_NAME = "sample_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"

        fun getDir(context: Context): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                "${context.filesDir.path}/${BuildConfig.ROOT_FOLDER}"
            } else {
                "${Environment.getExternalStorageDirectory().absolutePath}/${BuildConfig.ROOT_FOLDER}"
            }
        }

        fun toOOXML(ext: String): String {
            return when (ext) {
                ODT_EXTENSION, OTT_EXTENSION, DOC_EXTENSION, PAGES_EXTENSION -> DOCX_EXTENSION
                ODS_EXTENSION, OTS_EXTENSION, XLS_EXTENSION, NUMBERS_EXTENSION -> XLSX_EXTENSION
                ODP_EXTENSION, ODG_EXTENSION, OTP_EXTENSION, PPT_EXTENSION, KEY_EXTENSION -> PPTX_EXTENSION
                else -> throw IllegalArgumentException(".$ext can not be converted to OOXML extension")
            }
        }

        fun isOpenFormat(ext: String): Boolean {
            return when (ext) {
                ODT_EXTENSION, OTT_EXTENSION, ODS_EXTENSION, OTS_EXTENSION, ODP_EXTENSION, ODG_EXTENSION, OTP_EXTENSION, DOC_EXTENSION, XLS_EXTENSION, PPT_EXTENSION -> true
                else -> false
            }
        }

    }

    private val contentResolver: ContentResolver = context.contentResolver
    private val uri: Uri = MediaStore.Files.getContentUri(URI_KEY)

    private fun isFirstLaunch(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    private fun setFirstLaunchFlag() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun createRootDir(): File {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!publicDir.exists()) {
            publicDir.mkdirs()
        }

        if (!File(getDir(context)).exists()) {
            File(getDir(context)).mkdirs()
        }

        if (!isFirstLaunch() && File(getDir(context)).exists()) {
            return File(getDir(context))
        }

        if (isFirstLaunch() && File(getDir(context)).canWrite()) {
            addSamples(File(getDir(context)))
            setFirstLaunchFlag()
        }
        return File(getDir(context))
    }

    private fun addSamples(rootDir: File) {
        val samplesName = context.assets.list("samples")
        samplesName?.let {
            it.forEach { name ->
                val file = File(rootDir.absolutePath + "/" + name)
                if (file.exists()) {
                    MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
                    return@forEach
                }
                file.createNewFile()
                file.setWritable(true)
                val inputStream = context.assets.open("samples/$name")
                val outputStream = FileOutputStream(file)
                outputStream.write(inputStream.readBytes())
                inputStream.close()
                outputStream.close()
                MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
            }
        }
    }

    @JvmOverloads
    fun moveFiles(file: File, parentFile: File, isCopy: Boolean = false): Boolean {
        if (parentFile.exists()) {
            var movedFile = File(parentFile, file.name)
            if (movedFile.exists()) {
                movedFile = FileUtils.getNewFileName(movedFile)
            }
            if (file.isDirectory) {
                if (movedFile.mkdirs()) {
                    file.listFiles()?.forEach {
                        moveFiles(it, movedFile, isCopy)
                    }
                }
            } else {
                writeToFile(file, movedFile)
            }
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(movedFile)))
            if (!isCopy) {
                deleteFile(file)
            }
            return true
        } else {
            return false
        }
    }

    private fun writeToFile(oldFile: File, newFile: File) {
        val outputStream = FileOutputStream(newFile)
        val inputStream = FileInputStream(oldFile)
        try {
            outputStream.write(inputStream.readBytes())
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            outputStream.close()
            inputStream.close()
        }
    }

    fun getFiles(folder: File): List<File> {
        if (folder.exists()) {
            folder.listFiles()?.let { list ->
                return list.sortedBy { it.name }
            }
            return emptyList()
        } else {
            return emptyList()
        }
    }

    fun deleteFile(file: File): Boolean {
        var isDelete = false
        contentResolver.delete(uri, MediaStore.Files.FileColumns.DATA + " =? ", arrayOf(file.absolutePath))
        if (file.exists()) {
            isDelete = if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.canonicalFile.delete()
            }
            if (file.exists()) {
                isDelete = context.deleteFile(file.name)
            }
        }
        return isDelete
    }

    fun createFolder(name: String, parent: File): Boolean {
        val newFolder = File(parent, name)
        return if (!newFolder.exists()) {
            newFolder.mkdir()
        } else {
            false
        }
    }

    fun createFile(name: String, parent: File?, locale: String): File {
        val path = FileUtils.getTemplates(context, locale, StringUtils.getExtensionFromPath(name))
        var file = File(parent, name)

        if (file.exists()) {
            file = FileUtils.getNewFileName(file)
        } else {
            file.createNewFile()
        }

        context.assets.open("$ASSETS_TEMPLATES/$path").use { input ->
            FileOutputStream(file).use { output ->
                output.write(input.readBytes())
            }
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)

        return file
    }

    fun renameFile(oldFile: File, newName: String): Boolean {
        val newFile: File = if (oldFile.isDirectory) {
            File(oldFile.parent, newName)
        } else {
            File(oldFile.parent, newName + StringUtils.getExtensionFromPath(oldFile.name))
        }
        if (newFile.exists()) {
            return false
        }
        return if (oldFile.renameTo(newFile)) {
            MediaScannerConnection.scanFile(context, arrayOf(newFile.toString()), null, null)
            true
        } else {
            false
        }
    }

}