package lib.toolkit.base.managers.tools

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat.getExternalFilesDirs
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
        const val POTX_EXTENSION = "potx"
        const val PPT_EXTENSION = "ppt"
        const val ODP_EXTENSION = "odp"
        const val OTP_EXTENSION = "otp"
        const val XLSX_EXTENSION = "xlsx"
        const val XLTX_EXTENSION = "xltx"
        const val XLS_EXTENSION = "xls"
        const val ODS_EXTENSION = "ods"
        const val OTS_EXTENSION = "ots"
        const val CSV_EXTENSION = "csv"
        const val PDF_EXTENSION = "pdf"

        private const val ASSETS_TEMPLATES = "templates"

        const val MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val MIME_TYPE_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        const val MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

        private const val URI_KEY = "external"

        private val EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE")

        @SuppressLint("SdCardPath")
        private val KNOWN_PHYSICAL_PATHS = arrayOf(
            "/storage/sdcard0", "/storage/sdcard1",                                    //Motorola Xoom
            "/storage/extsdcard",                                                      //Samsung SGS3
            "/storage/sdcard0/external_sdcard",                                        //User request
            "/mnt/extsdcard", "/mnt/sdcard/external_sd",                               //Samsung galaxy family
            "/mnt/sdcard/ext_sd", "/mnt/external_sd", "/mnt/media_rw/sdcard1",         //4.4.2 on CyanogenMod S3
            "/removable/microsd",                                                      //Asus transformer prime
            "/mnt/emmc", "/storage/external_SD",                                       //LG
            "/storage/ext_sd",                                                         //HTC One Max
            "/storage/removable/sdcard1",                                              //Sony Xperia Z1
            "/data/sdext", "/data/sdext2", "/data/sdext3", "/data/sdext4", "/sdcard1", //Sony Xperia Z
            "/sdcard2",                                                                //HTC One M8s
            "/storage/microsd"                                                         //ASUS ZenFone 2
        )

        fun getDir(context: Context): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                "${context.filesDir.path}/${BuildConfig.ROOT_FOLDER}"
            } else {
                "${Environment.getExternalStorageDirectory().absolutePath}/${BuildConfig.ROOT_FOLDER}"
            }
        }

        fun toOOXML(ext: String): String {
            return when (ext) {
                ODT_EXTENSION -> DOCX_EXTENSION
                ODS_EXTENSION -> XLSX_EXTENSION
                ODP_EXTENSION -> PPTX_EXTENSION
                else -> throw IllegalArgumentException(".$ext can not be converted to OOXML extension")
            }
        }

    }

    private val contentResolver: ContentResolver = context.contentResolver
    private val uri: Uri = MediaStore.Files.getContentUri(URI_KEY)
    private lateinit var rootDir: File

    fun createRootDir(): File {
        val rootDir = File(getDir(context))
        if (rootDir.exists()) {
            this.rootDir = rootDir
            return rootDir
        }
        if (!rootDir.exists() && rootDir.mkdirs()) {
            addSamples(rootDir)
        }
        this.rootDir = rootDir
        return rootDir
    }

    fun getRootDir(): File = rootDir

    private fun addSamples(rootDir: File) {
        val samplesName = context.assets.list("samples")
        samplesName?.let {
            it.forEach { name ->
                val file = File(rootDir.absolutePath + "/" + name)
                val inputStream = context.assets.open("samples/$name")
                val outputStream = FileOutputStream(file)
                outputStream.write(inputStream.readBytes())
                inputStream.close()
                outputStream.close()
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
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

    fun search(value: String): List<File> {
        val files = getFiles()
        val result = ArrayList<File>()
        files.forEach {
            if (it.name.contains(value)) {
                result.add(it)
            }
        }
        return result
    }

    private fun moveFile(oldFile: File, newFile: File) {
        if (oldFile.exists()) {
            oldFile.renameTo(newFile)
        }
        Log.d(TAG, "${oldFile.exists()}")
    }

    fun getFiles(): List<File> {
        rootDir.listFiles()?.let { list ->
            return list.asList().sortedBy { it.name }
        }
        return emptyList()
    }

    private fun checkForFiles(folder: File): File? {
        folder.listFiles()?.forEach {
            if (it.isDirectory && it.name != "data" && isContainFiles(it)) {
                return folder
            } else if (it.isFile) {
                if (it.extension == DOCX_EXTENSION || it.extension == PPTX_EXTENSION || it.extension == XLSX_EXTENSION) {
                    return folder
                }
            }
        }
        return null
    }

    private fun isContainFiles(folder: File): Boolean {
        val list = folder.listFiles()
        if (list != null) {
            if (list.isEmpty()) {
                return false
            }
            list.forEach {
                if (it.isDirectory) {
                    isContainFiles(it)
                } else if (it.isFile) {
                    if (it.extension == DOCX_EXTENSION || it.extension == PPTX_EXTENSION || it.extension == XLSX_EXTENSION) {
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

    private fun getFiles(rootList: List<File>): List<File> {
        val files = ArrayList<File>()
        rootList.forEach {
            if (it.isFile) {
                if (it.extension == DOCX_EXTENSION || it.extension == PPTX_EXTENSION || it.extension == XLSX_EXTENSION) {
                    files.add(it)
                }
            }
        }
        return files.sortedBy { it.extension }
    }

    private fun getAllFiles(): List<File> {
        val files = ArrayList<File>()
        contentResolver.query(uri,
            arrayOf(MediaStore.Files.FileColumns.DATA),
            MediaStore.Files.FileColumns.MIME_TYPE + " =? OR " + MediaStore.Files.FileColumns.MIME_TYPE + " =? OR " + MediaStore.Files.FileColumns.MIME_TYPE + " =?",
            arrayOf(
                MIME_TYPE_DOCX,
                MIME_TYPE_PPTX,
                MIME_TYPE_XLSX
            ),
            MediaStore.Files.FileColumns.MIME_TYPE)?.use {
            val dataIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            while (it.moveToNext()) {
                files.add(File(it.getString(dataIndex)))
            }
        }

        return files
    }

    fun getLastModifiedFiles(): List<File> {
        return getAllFiles().sortedBy { it.lastModified() }
    }

    @SuppressLint("Range")
    fun getIdByPath(path: String): Int {
        var id = 0
        contentResolver.query(uri, arrayOf(MediaStore.Files.FileColumns._ID),
            MediaStore.Files.FileColumns.DATA + " =? ",
            arrayOf(path), null)?.use {
            while (it.moveToNext()) {
                id = it.getInt(it.getColumnIndex(MediaStore.Files.FileColumns._ID))
                return@use
            }
        }

        return id
    }

    private fun getSdCard(): String {
        val fileList = File("/storage/").listFiles()
        for (file in fileList) {
            if (!file.absolutePath.equals(Environment.getExternalStorageDirectory().absolutePath,
                    ignoreCase = true) && file.isDirectory && file.canRead()
            )
                if (getExternalStorage(context).size >= 2) {
                    return file.absolutePath
                }
            return ""
        }
        return ""
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

    private fun getExternalStorage(context: Context): Set<String> {
        val availableDirectoriesSet = HashSet<String>()
        // /Storage/????-????
        val files = getExternalFilesDirs(context, null)
        for (file in files) {
            if (file != null) {
                val applicationSpecificAbsolutePath = file.absolutePath
                val rootPath = applicationSpecificAbsolutePath.substring(
                    0,
                    applicationSpecificAbsolutePath.indexOf("Android/data")
                )
                availableDirectoriesSet.add(rootPath)
            }
        }
        return availableDirectoriesSet
    }

    private fun getAvailablePhysicalPaths(): List<String> {
        val availablePhysicalPaths = ArrayList<String>()
        KNOWN_PHYSICAL_PATHS.forEach {
            if (File(it).exists()) {
                availablePhysicalPaths.add(it)
            }
        }
        return availablePhysicalPaths
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
        }

        context.assets.open("$ASSETS_TEMPLATES/$path").use { input ->
            FileOutputStream(file).use { output ->
                output.write(input.readBytes())
            }
        }

        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

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
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)))
            true
        } else {
            false
        }
    }

}