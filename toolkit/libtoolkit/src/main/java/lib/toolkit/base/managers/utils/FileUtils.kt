package lib.toolkit.base.managers.utils

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import okhttp3.ResponseBody
import java.io.*
import java.net.URL
import java.util.*

object FileUtils {

    class Cache {
        @JvmField
        var to: String? = null

        @JvmField
        var temp: String? = null

        @JvmField
        var root: String? = null
    }

    const val LOAD_PROGRESS_UPDATE = 200
    const val LOAD_MAX_PROGRESS = 100
    const val MEGA_BYTES = 1048576.0
    const val STRICT_SIZE = 100 * 1024 * 1024.toLong()

    private const val DOCX_PATTERN_EXT = ".docx"
    private const val ASSETS_TEMPLATES = "templates"

    @JvmStatic
    @JvmOverloads
    fun getCacheDir(context: Context, isExternal: Boolean = true): File {
        return if (isExternal) context.externalCacheDir!! else context.cacheDir
    }

    @JvmStatic
    @JvmOverloads
    fun getCachePath(context: Context, isExternal: Boolean = true): String {
        return getCacheDir(context, isExternal).absolutePath
    }

    @JvmStatic
    fun getUuid(value: String = ""): String {
        return UUID.nameUUIDFromBytes(value.toByteArray()).toString()
    }

    @JvmStatic
    fun isDirectoryExist(path: String): Boolean {
        return File(path).let { it.isDirectory && it.exists() }
    }

    @JvmStatic
    fun isFileExist(path: String): Boolean {
        return File(path).let { it.isFile && it.exists() }
    }

    @JvmStatic
    fun createPath(path: String): Boolean {
        return File(path).let { it.isDirectory && it.exists() || it.mkdirs() }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun createPathThrow(path: String) {
        if (!createPath(path)) {
            throw IOException("Can't create path: $path")
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun deletePath(path: File): Int {
        var count = 0
        if (path.isDirectory) {
            path.listFiles()?.forEach {
                count += deletePath(it)
            }
        }

        count += if (path.delete()) 1 else 0
        return count
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun deletePath(path: String): Int {
        return deletePath(File(path))
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun asyncDeletePath(path: String) {
        AsyncRoutines().run({ foreground, instance ->
            deletePath(path)
        })
    }

    @JvmStatic
    @JvmOverloads
    fun getCache(
        context: Context,
        name: String? = null,
        folder: String? = null,
        isExternal: Boolean = true
    ): Cache? {
        val newFolder = folder ?: getUuid(System.currentTimeMillis().toString())
        val newName = name ?: getUuid(System.currentTimeMillis().toString())

        val rootDir = "${getCachePath(context, isExternal)}/data/$newFolder"
        val toFile = "$rootDir/$newName"
        val tempDir = "$rootDir/temp"

        if (createPath(tempDir)) {
            return Cache().apply {
                this.to = toFile
                this.temp = tempDir
                this.root = rootDir
            }
        }

        return null
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    @JvmOverloads
    fun clearCacheDir(context: Context, isExternal: Boolean = true): Int {
        return deletePath(getCachePath(context, isExternal))
    }

    /*
    * Assets
    * */
    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    @JvmOverloads
    fun assetUnpack(context: Context, from: List<String>, isExternal: Boolean = true, delete: Boolean = false): String? {
        val path = "${getCachePath(context, isExternal)}/assets"

        val packVersion = readSdkVersion(context, "sdk.version")

        val localFile = File("$path/sdk.version")
        if (localFile.exists()) {
            val localVersion = FileInputStream(localFile).reader(Charsets.UTF_8).use {
                it.readText()
            }

            if (packVersion == localVersion && !delete) {
                return path
            }
        }

        deletePath(path)

        return if (assetUnpack(context, from, path)) {
            path
        } else {
            null
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun assetUnpack(context: Context, from: List<String>, to: String): Boolean {
        if (!createPath(to)) {
            return false
        }

        var count = 0
        from.forEach {
            count += copyAssets(context, it, to)
        }

        return count > 0
    }

    @JvmStatic
    fun readSdkVersion(context: Context, path: String) = context.assets.open(path).use { input ->
        try {
            input.reader(Charsets.UTF_8).use { reader ->
                reader.readText()
            }
        } catch (e: FileNotFoundException) {
            return ""
        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun copyAssets(context: Context, asset: String, to: String): Int {
        var count = 0
        val assets = context.assets.list(asset)
        if (assets!!.isEmpty()) { // if asset is single file
            copyAsset(context, asset, to)
            count += 1
        } else { // else asset is tree files
            createPathThrow("$to/$asset")
            assets.forEach {
                count += copyAssets(context, "$asset/$it", to)
            }
        }

        return count
    }

    @JvmStatic
    fun copyAsset(context: Context, asset: String, to: String) {
        BufferedInputStream(context.assets.open(asset))?.use { input ->
            BufferedOutputStream(FileOutputStream("$to/$asset"))?.use { output ->
                output.write(input.readBytes())
            }
        }
    }

    /*
    * Work with path
    * */
    @JvmStatic
    fun getExtension(path: String): String {
        var extension = ""
        val index = path.lastIndexOf(".") + 1
        if (index >= 0 && index < path.length) {
            extension = path.substring(index)
        }
        return extension
    }

    @JvmStatic
    fun addExtension(extension: String, path: String): String {
        if (extension.isNotEmpty()) {
            return "$path.$extension"
        }

        return path
    }

    @JvmStatic
    fun getFileName(path: String, isExtension: Boolean = false): String {
        var name = File(path).name
        val index = name.lastIndexOf(".")
        if (!isExtension && index >= 0 && index < name.length) {
            name = name.substring(0, index)
        }
        return name
    }

    @JvmStatic
    fun getNewFileName(file: File): File {
        var newFile: File = file
        var number = 0
        while (newFile.exists()) {
            number++
            newFile = if (file.isDirectory) {
                File(file.parent, file.name + "($number)")
            } else {
                val newFileName = StringBuilder(file.name)
                    .insert(file.name.indexOf('.'), "($number)")
                    .toString()
                File(file.parent, newFileName)
            }
        }
        return newFile
    }

    /*
    * Create/delete
    * */
    @RequiresPermission(allOf = [WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE])
    @JvmStatic
    fun copyFile(context: Context, from: Uri, to: String): Boolean {
        BufferedInputStream(context.contentResolver.openInputStream(from))?.use { input ->
            BufferedOutputStream(FileOutputStream(to))?.use { output ->
                output.write(input.readBytes())
                output.flush()
                return true
            }
        }

        return false
    }

    @RequiresPermission(allOf = [WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE])
    @JvmStatic
    fun copyFile(from: String, to: String): Boolean {
        BufferedInputStream(FileInputStream(from)).use { input ->
            BufferedOutputStream(FileOutputStream(to)).use { output ->
                output.write(input.readBytes())
                output.flush()
                return true
            }
        }

        return false
    }

    @RequiresPermission(allOf = [WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE])
    @JvmStatic
    fun replaceFile(context: Context, item: Uri, replace: String): Boolean {
        BufferedInputStream(FileInputStream(replace)).use { input ->
            BufferedOutputStream(context.contentResolver.openOutputStream(item)).use { output ->
                output.write(input.readBytes())
                output.flush()
                return true
            }
        }

        return false
    }

    @RequiresPermission(allOf = [WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE])
    @JvmStatic
    @JvmOverloads
    fun copyFile(
        context: Context,
        uri: Uri,
        dstFileName: String? = null,
        dstFolderName: String? = null,
        isExternal: Boolean = true
    ): Cache? {
        return getCache(context, dstFileName, dstFolderName, isExternal)?.apply {
            to = addExtension(getExtension(StringUtils.getExtensionFromPath(uri.path ?: "")), to!!)
            copyFile(context, uri, to!!)
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun createFile(dir: File, name: String): File? {
        return try {
            File(dir, name.replace("/", "_")).apply {
                createNewFile()
            }
        } catch (e: IOException) {
            null
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun createFile(dir: File, name: String, extension: String): File? {
        return createFile(dir, "$name.$extension")
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    @JvmOverloads
    fun createCacheFile(context: Context, name: String, isExternal: Boolean = true): File? {
        return createFile(getCacheDir(context, isExternal), name)
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    @JvmOverloads
    fun createImageFile(
        context: Context,
        name: String?,
        extension: String = "png",
        isExternal: Boolean = true
    ): File? {
        val dir = if (name == null) {
            getCacheDir(context, isExternal)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        }

        return createFile(dir, name ?: "temp", extension)
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    @JvmOverloads
    fun createTempAssetsFile(
        context: Context,
        from: String,
        name: String,
        ext: String,
        isExternal: Boolean = true
    ): File? {
        return createCacheFile(context, name + ext, isExternal).also { file ->
            context.assets.open(from).use { input ->
                FileOutputStream(file).use { output ->
                    output.write(input.readBytes())
                }
            }
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun createGifFile(context: Context, name: String): File? {
        return createImageFile(context, name, "gif")
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun writeBitmapToFile(into: File, bitmap: Bitmap): File? {
        return into.also { file ->
            BufferedOutputStream(FileOutputStream(file))?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun writeBytesToFile(into: File, bytes: ByteArray): File? {
        return into.also { file ->
            BufferedOutputStream(FileOutputStream(into))?.use { output ->
                output.write(bytes)
            }
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun createBitmapFile(context: Context, bitmap: Bitmap, name: String): File? {
        return createImageFile(context, name)?.let {
            writeBitmapToFile(it, bitmap)
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    @JvmOverloads
    fun createCachedBitmap(
        context: Context,
        bitmap: Bitmap,
        name: String,
        isExternal: Boolean = true
    ): File? {
        return createCacheFile(context, name, isExternal)?.let {
            writeBitmapToFile(it, bitmap)
        }
    }

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    @JvmStatic
    @JvmOverloads
    fun getCachedBitmap(context: Context, name: String, isExternal: Boolean = true): Bitmap? {
        val file = File(getCachePath(context, isExternal), name)
        return if (file.exists()) {
            readBitmap(file.path)
        } else {
            null
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmStatic
    fun createGifFile(context: Context, bytes: ByteArray, name: String): File? {
        return createGifFile(context, name)?.let {
            writeBytesToFile(it, bytes)
        }
    }

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    @JvmStatic
    fun readBitmap(path: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(path, options)
    }

    @JvmStatic
    fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.toByteArray()
        }
    }

    @JvmStatic
    fun bytesToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @JvmStatic
    fun getResizedBitmap(image: Bitmap, bitmapWidth: Int, bitmapHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true)
    }

    @JvmStatic
    fun getBitmap(context: Context, @DrawableRes resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @JvmStatic
    fun getTemplates(context: Context, locale: String, extension: String): String? {
        val templates = context.assets.list(ASSETS_TEMPLATES)
        templates?.forEach { template ->
            if (extension == DOCX_PATTERN_EXT && template.endsWith(DOCX_PATTERN_EXT, true)) {
                return template
            }
            if (template.endsWith(extension, true) && template.contains(locale)) {
                return template
            }
        }
        return getDefault(templates, extension)
    }


    private fun getDefault(templates: Array<String>?, extension: String): String? {
        val default = "empty-en$extension"
        templates?.forEach { template ->
            if (template == default) {
                return template
            }
        }
        return ""
    }

    fun getFontsDir(context: Context): String {
        val path = "${context.filesDir.path}/Fonts"

        val folder = File(path)
        if (!folder.exists()) {
            folder.mkdir()
        }
        return path

    }

    @JvmStatic
    fun getSize(file: File?): Long {
        return if (file != null && file.exists()) {
            var result: Long = 0
            val files = file.listFiles()
            files?.forEach {
                result += if (it.isDirectory) {
                    getSize(it)
                } else {
                    it.length()
                }
            }
            result
        } else {
            0
        }
    }

    fun interface Finish {
        fun onFinish()
    }

    fun interface Error {
        fun onError(error: Throwable)
    }

    fun interface Progress {
        fun onProgress(total: Long, progress: Long, isArchiving: Boolean): Boolean
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @JvmStatic
    fun downloadFromUrl(
        context: Context,
        to: Uri,
        from: String,
        finish: Finish? = null,
        error: Error? = null
    ) {
        AsyncRoutines().run({ _, _ ->
            context.contentResolver.openOutputStream(to)?.use { out ->
                URL(from).openStream()?.use {
                    out.write(it.readBytes())
                    out.flush()
                }
            }
        }, {
            finish?.onFinish()
        }, {
            error?.onError(it)
        })

    }

    @JvmStatic
    fun writeToUri(
        context: Context,
        to: Uri,
        from: String,
        notFound: ((error: Throwable) -> Unit)? = null,
        success: (() -> Unit)? = null
    ): Boolean {
        try {
            BufferedInputStream(FileInputStream(from)).use { input ->
                BufferedOutputStream(
                    context.contentResolver.openOutputStream(
                        to,
                        "rwt"
                    )
                ).use { output ->
                    output.write(input.readBytes())
                    output.flush()
                    success?.invoke()
                    return true
                }
            }
        } catch (error: Throwable) {
            notFound?.invoke(error)
            return false
        }

    }

    @JvmStatic
    fun writeFromResponseBody(
        response: ResponseBody?,
        to: Uri,
        context: Context,
        progress: Progress?,
        finish: Finish?,
        error: Error?
    ) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = BufferedInputStream(response?.byteStream(), 1024 * 8)
            outputStream = BufferedOutputStream(context.contentResolver.openOutputStream(to))
            val buffer = ByteArray(1024 * 4)
            // Downloading with progress
            var countBytes: Int
            var totalBytes: Long = 0
            while (inputStream.read(buffer).also { countBytes = it } != -1) {
                totalBytes += countBytes.toLong()
                outputStream.write(buffer, 0, countBytes)

                if (progress?.onProgress(response?.contentLength() ?: 0, totalBytes, false) == true) {
                    throw Exception()
                }
            }
            finish?.onFinish()
            outputStream.flush()
        } catch (e: Exception) {
            error?.onError(e)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    @JvmStatic
    fun getPercentOfLoading(total: Long, progress: Long): Int {
        return if (total > 0) (progress * LOAD_MAX_PROGRESS / total).toInt() else 0
    }

    @JvmStatic
    fun getMbFromBytes(bytes: Long): Float {
        return (bytes.toDouble() / MEGA_BYTES).toFloat()
    }

    fun isEnoughFreeSpace(itemSize: Long?): Boolean {
        val availableBytes = StatFs(Environment.getExternalStorageDirectory().path).availableBytes
        return (itemSize ?: 0) < availableBytes
    }

}