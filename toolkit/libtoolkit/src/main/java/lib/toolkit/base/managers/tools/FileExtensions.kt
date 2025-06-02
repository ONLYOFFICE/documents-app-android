package lib.toolkit.base.managers.tools

import android.webkit.MimeTypeMap

sealed class FileExtensions(
    val extension: String,
    val mimeType: String,
    val group: FileGroup = FileGroup.UNKNOWN
) {
    // Documents
    object DOCX : FileExtensions("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", FileGroup.DOCUMENT)
    object DOC : FileExtensions("doc", "application/msword", FileGroup.DOCUMENT)
    object DOTX : FileExtensions("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template", FileGroup.DOCUMENT)
    object ODT : FileExtensions("odt", "application/vnd.oasis.opendocument.text", FileGroup.DOCUMENT)
    object OTT : FileExtensions("ott", "application/vnd.oasis.opendocument.text-template", FileGroup.DOCUMENT)
    object TXT : FileExtensions("txt", "text/plain", FileGroup.DOCUMENT)
    object RTF : FileExtensions("rtf", "application/rtf", FileGroup.DOCUMENT)
    object HTML : FileExtensions("html", "text/html", FileGroup.HTML)
    object MHT : FileExtensions("mht", "message/rfc822", FileGroup.HTML)
    object HTM : FileExtensions("htm", "text/html", FileGroup.HTML)
    object EPUB : FileExtensions("epub", "application/epub+zip", FileGroup.EBOOK)
    object MOBI : FileExtensions("mobi", "application/x-mobipocket-ebook", FileGroup.EBOOK)
    object DJVU : FileExtensions("djvu", "image/vnd.djvu", FileGroup.EBOOK)
    object MD : FileExtensions("md", "text/markdown", FileGroup.DOCUMENT)
    object HWP : FileExtensions("hwp", "application/x-hwp", FileGroup.DOCUMENT)
    object HWPX : FileExtensions("hwpx", "application/x-hwpx", FileGroup.DOCUMENT)
    object PAGES : FileExtensions("pages", "application/x-iwork-pages-sffpages", FileGroup.DOCUMENT)

    // Presentations
    object PPTX : FileExtensions("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", FileGroup.PRESENTATION)
    object PPSX : FileExtensions("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow", FileGroup.PRESENTATION)
    object POTX : FileExtensions("potx", "application/vnd.openxmlformats-officedocument.presentationml.template", FileGroup.PRESENTATION)
    object PPT : FileExtensions("ppt", "application/vnd.ms-powerpoint", FileGroup.PRESENTATION)
    object ODP : FileExtensions("odp", "application/vnd.oasis.opendocument.presentation", FileGroup.PRESENTATION)
    object ODG : FileExtensions("odg", "application/vnd.oasis.opendocument.graphics", FileGroup.PRESENTATION)
    object OTP : FileExtensions("otp", "application/vnd.oasis.opendocument.presentation-template", FileGroup.PRESENTATION)
    object KEY : FileExtensions("key", "application/x-iwork-keynote-sffkey", FileGroup.PRESENTATION)

    // Spreadsheets
    object XLSX : FileExtensions("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FileGroup.SHEET)
    object XLTX : FileExtensions("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template", FileGroup.SHEET)
    object XLS : FileExtensions("xls", "application/vnd.ms-excel", FileGroup.SHEET)
    object ODS : FileExtensions("ods", "application/vnd.oasis.opendocument.spreadsheet", FileGroup.SHEET)
    object OTS : FileExtensions("ots", "application/vnd.oasis.opendocument.spreadsheet-template", FileGroup.SHEET)
    object CSV : FileExtensions("csv", "text/csv", FileGroup.SHEET)
    object NUMBERS : FileExtensions("numbers", "application/x-iwork-numbers-sffnumbers", FileGroup.SHEET)

    // PDF
    object PDF : FileExtensions("pdf", "application/pdf", FileGroup.PDF)
    object PDFA : FileExtensions("pdfa", "application/pdf", FileGroup.PDF)

    // Images
    object PNG : FileExtensions("png", "image/png", FileGroup.IMAGE)
    object JPEG : FileExtensions("jpeg", "image/jpeg", FileGroup.IMAGE)
    object JPG : FileExtensions("jpg", "image/jpeg", FileGroup.IMAGE)
    object ICO : FileExtensions("ico", "image/x-icon", FileGroup.IMAGE)
    object BMP : FileExtensions("bmp", "image/bmp", FileGroup.IMAGE)
    object HEIC : FileExtensions("heic", "image/heic", FileGroup.IMAGE)
    object WEBP : FileExtensions("webp", "image/webp", FileGroup.IMAGE)
    object GIF : FileExtensions("gif", "image/gif", FileGroup.IMAGE_GIF)

    // Video
    object MP4 : FileExtensions("mp4", "video/mp4", FileGroup.VIDEO_SUPPORT)
    object TS : FileExtensions("ts", "video/mp2t", FileGroup.VIDEO_SUPPORT)
    object WEBM : FileExtensions("webm", "video/webm", FileGroup.VIDEO_SUPPORT)
    object MKV : FileExtensions("mkv", "video/x-matroska", FileGroup.VIDEO_SUPPORT)
    object THREEGP : FileExtensions("3gp", "video/3gpp", FileGroup.VIDEO_SUPPORT)

    // Video
    object THREEG2 : FileExtensions("3g2", "video/3gpp2", FileGroup.VIDEO)
    object THREEGPP : FileExtensions("3gpp", "video/3gpp", FileGroup.VIDEO)
    object AVI : FileExtensions("avi", "video/x-msvideo", FileGroup.VIDEO)
    object MOV : FileExtensions("mov", "video/quicktime", FileGroup.VIDEO)
    object MPG : FileExtensions("mpg", "video/mpeg", FileGroup.VIDEO)
    object MPEG : FileExtensions("mpeg", "video/mpeg", FileGroup.VIDEO)

    // Archives
    object ZIP : FileExtensions("zip", "application/zip", FileGroup.ARCHIVE)
    object RAR : FileExtensions("rar", "application/x-rar-compressed", FileGroup.ARCHIVE)
    object SEVENZ : FileExtensions("7z", "application/x-7z-compressed", FileGroup.ARCHIVE)
    object TAR : FileExtensions("tar", "application/x-tar", FileGroup.ARCHIVE)
    object GZ : FileExtensions("gz", "application/gzip", FileGroup.ARCHIVE)

    object UNKNOWN : FileExtensions("", "", FileGroup.UNKNOWN)

    companion object {
        private val extensionMap by lazy {
            val values = listOf(
                DOCX, DOC, DOTX, ODT, OTT, TXT, RTF, HTML, MHT, HTM, EPUB, MOBI, DJVU, MD, HWP, HWPX, PAGES,
                PPTX, PPSX, POTX, PPT, ODP, ODG, OTP, KEY,
                XLSX, XLTX, XLS, ODS, OTS, CSV, NUMBERS,
                PDF, PDFA,
                PNG, JPEG, JPG, ICO, BMP, HEIC, WEBP, GIF,
                MP4, TS, WEBM, MKV, THREEGP,
                THREEG2, THREEGPP, AVI, MOV, MPG, MPEG,
                ZIP, RAR, SEVENZ, TAR, GZ,
                UNKNOWN
            )
            values.associateBy { it.extension.lowercase() }
        }

        @JvmStatic
        fun fromPath(input: String): FileExtensions {
            val cleanExt = FileExtensionUtils.getExtensionFromPath(input).replace(".", "")
            return extensionMap[cleanExt] ?: UNKNOWN
        }

        @JvmStatic
        fun fromExtension(ext: String): FileExtensions {
            val cleanExt = ext.replace(".", "")
            return extensionMap[cleanExt] ?: UNKNOWN
        }

        @JvmStatic
        fun toOOXML(extension: String): String {
            return when (val fileExt = fromExtension(extension)) {
                ODT, OTT, DOC, PAGES, MD -> DOCX.extension
                ODS, OTS, XLS, NUMBERS -> XLSX.extension
                ODP, ODG, OTP, PPT, KEY -> PPTX.extension
                else -> throw IllegalArgumentException(".${fileExt.extension} can not be converted to OOXML extension")
            }
        }

        @JvmStatic
        fun isOpenFormat(extension: String): Boolean {
            val fileExt = fromExtension(extension)
            return fileExt in listOf(ODT, OTT, ODS, OTS, ODP, ODG, OTP, DOC, XLS, PPT)
        }
    }

    fun getContentType(): String {
        return mimeType.takeIf { mimeType.isEmpty() } ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    fun belongsTo(group: FileGroup): Boolean {
        return this.group == group
    }
}

enum class FileGroup {
    DOCUMENT,     // Text documents
    SHEET,        // Spreadsheets
    PRESENTATION, // Presentations
    HTML,         // HTML
    IMAGE,        // Images
    IMAGE_GIF,    // Animated images
    VIDEO,        // Video
    VIDEO_SUPPORT,// Video
    EBOOK,        // E-books
    PDF,          // PDF documents
    ARCHIVE,      // Archives
    UNKNOWN       // Unknown
}


object FileExtensionUtils {

    @JvmStatic
    fun getExtensionFromPath(path: String): String {
        return try {
            path.substring(path.lastIndexOf("."))
        } catch (e: RuntimeException) {
            ""
        }
    }

    @JvmStatic
    private fun isExtensionOnly(input: String): Boolean {
        return (!input.contains("/") && !input.contains("\\")) &&
                (input.startsWith(".") || !input.contains("."))
    }

    @JvmStatic
    fun getFileExtension(input: String): FileExtensions {
        return if (isExtensionOnly(input)) {
            FileExtensions.fromExtension(input)
        } else {
            val extension = getExtensionFromPath(input).replace(".", "")
            FileExtensions.fromExtension(extension)
        }
    }

    @JvmStatic
    fun getNameWithoutExtension(path: String): String {
        return try {
            path.substring(0, path.lastIndexOf("."))
        } catch (e: RuntimeException) {
            path
        }
    }

    @JvmStatic
    fun getMimeTypeFromPath(path: String): String {
        val extension = getExtensionFromPath(path).replace(".", "")
        return FileExtensions.fromExtension(extension).getContentType()
    }

    @JvmStatic
    fun getDocumentType(input: String): FileGroup {
        return getFileExtension(input).group
    }

    @JvmStatic
    fun isDocumentFile(input: String): Boolean {
        val fileExt = getFileExtension(input)
        return fileExt.group in listOf(
            FileGroup.DOCUMENT,
            FileGroup.SHEET,
            FileGroup.PRESENTATION,
            FileGroup.PDF,
        )
    }

    @JvmStatic
    fun isTextDocument(input: String): Boolean {
        return getFileExtension(input).group == FileGroup.DOCUMENT
    }

    @JvmStatic
    fun isSpreadsheet(input: String): Boolean {
        return getFileExtension(input).group == FileGroup.SHEET
    }

    @JvmStatic
    fun isPresentation(input: String): Boolean {
        return getFileExtension(input).group == FileGroup.PRESENTATION
    }

    @JvmStatic
    fun isPdfFile(input: String): Boolean {
        return getFileExtension(input).group == FileGroup.PDF
    }

    @JvmStatic
    fun isImageFile(input: String): Boolean {
        val fileExt = getFileExtension(input)
        return fileExt.group in listOf(FileGroup.IMAGE, FileGroup.IMAGE_GIF)
    }


    @JvmStatic
    fun isImageGifFile(input: String): Boolean {
        val fileExt = getFileExtension(input)
        return fileExt.group == FileGroup.IMAGE_GIF
    }

    @JvmStatic
    fun isVideoFile(input: String): Boolean {
        val fileExt = getFileExtension(input)
        return fileExt.group in listOf(FileGroup.VIDEO, FileGroup.VIDEO_SUPPORT)
    }

    @JvmStatic
    fun getMimeType(input: String): String {
        return getFileExtension(input).getContentType()
    }
}