package lib.toolkit.base.managers.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import lib.toolkit.base.R
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern


object StringUtils {
    @JvmField
    val TAG = StringUtils::class.java.simpleName

    val MD5 = "MD5"
    val COMMON_MIME_TYPE = "*/*"
    val URI_ALLOWED_CHARS = "@#&=*+-_.,:!?()/~'%"
    @JvmField val DIALOG_FORBIDDEN_SYMBOLS = "*+:\"<>?|\\/"
    val DIALOG_FORBIDDEN_NAMES = arrayOf(".", "..")
    val PATTERN_FORBIDDEN_SYMBOLS = ".*([$DIALOG_FORBIDDEN_SYMBOLS]*).*"
    val PATTERN_ALPHA_NUMERIC = "^[a-zA-Z0-9-]*$"
    val PATTERN_R7_CLOUDS = ".*(\\.r7-).*"
    val PATTERN_CREATE_PORTAL = "^[\\p{L}\\p{M}' \\.\\-]+$"

    private val EXT_VIDEO_SUPPORT = "3gp|mp4|ts|webm|mkv"
    private val EXT_VIDEO = "$EXT_VIDEO_SUPPORT|3g2|3gpp|asf|avi|divx|f4v|flv|h264|ifo|m2ts|m4v|mod|mov|mpeg|mpg|mswmm|mts|mxf|ogv|rm|swf|ts|vep|vob|wlmp|wmv"

    private val PATTERN_EXT_DOC = "^(doc|docx|docm|doct|dot|dotm|dotx|odt|ott|fodt|rtf|epub|txt|html|mht)$"
    private val PATTERN_EXT_FORM = "^(oform|docxf)$"
    private val PATTERN_EXT_DOCXF = "^(docxf)$"
    private val PATTERN_EXT_OFORM = "^(oform)$"
    private val PATTERN_EXT_SHEET = "^(xlst|xlsx|xlsm|xls|xlt|xltm|xltx|ods|fods|ots|csv)$"
    private val PATTERN_EXT_PRESENTATION = "^(ppt|pptt|pptx|pps|odp|fodp|otp|pot|potm|potx|ppsm|ppsx)$"
    private val PATTERN_EXT_HTML = "^(mht|html|htm)$"
    private val PATTERN_EXT_IMAGE = "^(png|jpeg|jpg|ico|bmp|heic)$"
    private val PATTERN_EXT_IMAGE_GIF = "^(gif)$"
    private val PATTERN_EXT_VIDEO_SUPPORT = "^($EXT_VIDEO_SUPPORT)$"
    private val PATTERN_EXT_VIDEO = "^($EXT_VIDEO)$"
    private val PATTERN_EXT_EBOOK = "^(epub|mobi|djvu)$"
    private val PATTERN_EXT_PDF = "^(pdf)$"
    private val PATTERN_EXT_ARCH = "^(zip|zz|zipx|rar|7z|s7z|ace|ice|tar|gz|tgz|tbz2|txz|iso)$"

    private val PATTERN_URL_HOST = "^(?:\\w+://)?((?:[^./?#]+\\.)?([^/?#]+))"

    enum class Extension {
        UNKNOWN,
        DOC,
        FORM,
        DOCXF,
        OFORM,
        SHEET,
        PRESENTATION,
        HTML,
        IMAGE,
        IMAGE_GIF,
        VIDEO,
        VIDEO_SUPPORT,
        EBOOK,
        PDF,
        ARCH
    }


    @JvmStatic
    fun getExtension(extension: String): Extension {
        val ext = extension.replace(".", "").lowercase()
        return when {
            Pattern.matches(PATTERN_EXT_FORM, ext) -> Extension.FORM
            Pattern.matches(PATTERN_EXT_DOC, ext) -> Extension.DOC
            Pattern.matches(PATTERN_EXT_SHEET, ext) -> Extension.SHEET
            Pattern.matches(PATTERN_EXT_PRESENTATION, ext) -> Extension.PRESENTATION
            Pattern.matches(PATTERN_EXT_HTML, ext) -> Extension.HTML
            Pattern.matches(PATTERN_EXT_IMAGE_GIF, ext) -> Extension.IMAGE_GIF
            Pattern.matches(PATTERN_EXT_IMAGE, ext) -> Extension.IMAGE
            Pattern.matches(PATTERN_EXT_VIDEO_SUPPORT, ext) -> Extension.VIDEO_SUPPORT
            Pattern.matches(PATTERN_EXT_VIDEO, ext) -> Extension.VIDEO
            Pattern.matches(PATTERN_EXT_EBOOK, ext) -> Extension.EBOOK
            Pattern.matches(PATTERN_EXT_PDF, ext) -> Extension.PDF
            Pattern.matches(PATTERN_EXT_ARCH, ext) -> Extension.ARCH
            else -> Extension.UNKNOWN
        }
    }

    fun getFormExtension(extension: String): Extension {
        val ext = extension.replace(".", "")
        return when {
            Pattern.matches(PATTERN_EXT_DOCXF, ext) -> Extension.DOCXF
            Pattern.matches(PATTERN_EXT_OFORM, ext) -> Extension.OFORM
            else -> throw RuntimeException("Invalid form extension")
        }
    }

    @JvmStatic
    fun isDocument(extension: String): Boolean {
        return when (getExtension(extension)) {
            Extension.SHEET, Extension.DOC, Extension.FORM, Extension.PRESENTATION, Extension.PDF -> true
            else -> false
        }
    }

    @JvmStatic
    fun isCreateUserName(name: String): Boolean {
        return !name.matches(PATTERN_CREATE_PORTAL.toRegex())
    }

    @JvmStatic
    fun isBanned(portal: String): Boolean {
        return portal.matches(PATTERN_R7_CLOUDS.toRegex())
    }

    @JvmStatic
    fun isImage(extension: String): Boolean {
        return getExtension(extension).ordinal == Extension.IMAGE.ordinal || getExtension(
            extension
        ).ordinal == Extension.IMAGE_GIF.ordinal
    }

    @JvmStatic
    fun isImageGif(extension: String): Boolean {
        return getExtension(extension).ordinal == Extension.IMAGE_GIF.ordinal
    }

    @JvmStatic
    fun isVideo(extension: String): Boolean {
        return getExtension(extension).ordinal == Extension.VIDEO.ordinal
    }

    @JvmStatic
    fun isVideoSupport(extension: String): Boolean {
        return getExtension(extension).ordinal == Extension.VIDEO_SUPPORT.ordinal
    }

    @JvmStatic
    fun repeatString(string: String, length: Int): String {
        return if (length > 0) String.format(String.format("%%%ds", length), " ").replace(" ", string) else ""
    }

    @JvmStatic
    fun getEncodedString(values: String?): String? {
        return Uri.encode(values, URI_ALLOWED_CHARS)
    }

    @JvmStatic
    fun getMimeTypeFromExtension(extension: String): String {
        var mimeType: String? = null
        val ext = MimeTypeMap.getFileExtensionFromUrl(extension)
        if (ext != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        }

        return if (mimeType.isNullOrEmpty()) COMMON_MIME_TYPE else mimeType
    }

    @JvmStatic
    fun getExtensionFromPath(path: String): String {
        return try {
            path.substring(path.lastIndexOf("."))
        } catch (e: RuntimeException) {
            ""
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
        return getMimeTypeFromExtension(
            getExtensionFromPath(
                path
            )
        )
    }

    @JvmStatic
    fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @JvmStatic
    fun isWebUrl(url: CharSequence) : Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    @JvmStatic
    fun isImageMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    @JvmStatic
    fun addParameterToUrl(url: String, key: String, value: String): String {
        val uri = Uri.parse(url)
        val newUri = uri.buildUpon().appendQueryParameter(key, value).build()
        return newUri.toString()
    }

    @JvmStatic
    fun getParametersFromUrl(url: String): MutableMap<String, String> {
        val queries = url.split("&")
        val params = mutableMapOf<String, String>()
        queries.forEach { query ->
            val name = query.split("=")[0]
            val value = query.split("=")[1]
            params[name] = value
        }
        return params
    }

    @JvmStatic
    fun getAllowedString(source: String, symbols: String = DIALOG_FORBIDDEN_SYMBOLS): String? {
        val position =
            getSymbolPosition(source, symbols)
        return if (position >= 0) {
            source.substring(0, position)
        } else {
            null
        }
    }

    fun getAllowedName(source: String): Boolean {
        return source.trim() in DIALOG_FORBIDDEN_NAMES
    }

    @JvmStatic
    fun getSymbolPosition(source: String, symbols: String): Int {
        for (i in source.indices) {
            val charAt = source[i].toString()
            if (symbols.contains(charAt)) {
                return i
            }
        }

        return -1
    }

    @JvmStatic
    fun getUrlWithoutScheme(url: String): String {
        if (URLUtil.isValidUrl(url)) {
            val uri = Uri.parse(url)
            val path = if (uri.path != null) uri.path else ""
            val query = if (uri.query != null) uri.query else ""
            val port = if (uri.port > 0) ":" + uri.port.toString() else ""
            return uri.host + port + path + query
        }

        return url
    }

    fun hasScheme(url: String): Boolean {
        return url.contains(""".*://""".toRegex())
    }

    @JvmStatic
    fun equals(str1: String?, str2: String?): Boolean {
        return str1 != null && (str1 === str2 || str1.equals(str2, ignoreCase = true))
    }

    @JvmStatic
    fun isConsistOf(source: String?, pattern: String): Boolean {
        return source != null && source.matches(pattern.toRegex())
    }

    @JvmStatic
    fun isAlphaNumeric(source: String): Boolean {
        return isConsistOf(
            source,
            PATTERN_ALPHA_NUMERIC
        )
    }

    @JvmStatic
    fun isDialogForbidden(source: String): Boolean {
        return isConsistOf(
            source,
            PATTERN_FORBIDDEN_SYMBOLS
        )
    }

    @JvmStatic
    fun getHtmlSpanned(html: String): Spanned {
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    }

    @JvmStatic
    fun getHtmlSpanned(context: Context, @StringRes resource: Int, vararg args: String): Spanned {
        return getHtmlSpanned(
            context.getString(
                resource,
                *args
            )
        )
    }

    @JvmStatic
    fun getHtmlString(html: String): String {
        return getHtmlSpanned(html).toString()
    }

    @JvmStatic
    fun isValidUrl(@NonNull url: String): Boolean {
        return URLUtil.isValidUrl(url) && Patterns.WEB_URL.matcher(url).matches()
    }

    fun getUrlHost(url: String?): String {
        val groups = PATTERN_URL_HOST.toRegex().find(url.orEmpty())?.groups
        return if (!groups.isNullOrEmpty()) groups[1]?.value.orEmpty() else ""
    }

    @JvmStatic
    fun cleanJson(@NonNull json: String): String {
        return json.replace("[\\f|\\n|\\r|\\t|\\\\|]".toRegex(), "")
                .replace("\"{", "{")
                .replace("}\",", "},")
                .replace("}\"", "}")
    }

    @JvmStatic
    fun getJsonObject(@NonNull json: String): JSONObject? {
        return try {
            val newJson = cleanJson(json)
            JSONObject(newJson)
        } catch (e: Exception) {
            Log.e(TAG, "Could not parse malformed JSON: \"$json\"")
            null
        }
    }

    @JvmStatic
    fun getMd5(value: String?): String? {
        return try {
            val digest = MessageDigest.getInstance(MD5)
            val md5Data = BigInteger(1, digest.digest(value?.toByteArray() ?: byteArrayOf()))
            String.format("%032X", md5Data).lowercase(Locale.getDefault())
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }

    @SuppressLint("DefaultLocale")
    @JvmStatic
    fun getFormattedSize(context: Context, bytes: Long): String {
        val kb = 1024.0
        val b = bytes.toDouble()
        val k = bytes / kb
        val m = k / kb
        val g = m / kb
        val t = g / kb

        val resultSize: String = if (t > 1) {
            String.format("%.2f", t) + " " + context.getString(R.string.sizes_terabytes)
        } else if (g > 1) {
            String.format("%.2f", g) + " " + context.getString(R.string.sizes_gigabytes)
        } else if (m > 1) {
            String.format("%.2f", m) + " " + context.getString(R.string.sizes_megabytes)
        } else if (k > 1) {
            String.format("%.2f", k) + " " + context.getString(R.string.sizes_kilobytes)
        } else {
            String.format("%.2f", b) + " " + context.getString(R.string.sizes_bytes)
        }

        return resultSize
    }

    @JvmStatic
    fun removeExtension(fileName: String): String {
        return fileName.substringBeforeLast('.')
    }

    @JvmStatic
    fun getNewName(name: String, counter: Int = 1) : String {
        return if (name.elementAt(name.lastIndex) == ')') {
            val value = name.substring(name.lastIndexOf('(') + 1, name.lastIndexOf(')'))

            if (TextUtils.isDigitsOnly(value)) {
                StringBuilder(name.substringBeforeLast('('))
                        .append("(${value.toInt().plus(1)})")
                        .toString()
            } else {
                name
            }

        } else {
            StringBuilder(name)
                    .append("(${counter})")
                    .toString()
        }
    }

    fun getDeviceName(): String {
        return Build.MODEL + " " + Build.DEVICE
    }

    fun getAvatarName(name: String): String {
        try {
            return name.split(" ").run {
                if (size >= 2) {
                    return@run "${get(0)[0]}${get(1)[0]}".uppercase()
                } else {
                    if (get(0).length >= 2) {
                        return@run "${get(0)[0]}${get(0)[1]}".uppercase()
                    } else {
                        return@run "${get(0)[0]}".uppercase()
                    }
                }
            }
        } catch (error: IndexOutOfBoundsException) {
            return "U"
        }
    }

    @JvmStatic
    fun getHelpUrl(context: Context): String {
        return context.getString(R.string.app_url_help)
    }

    fun getDate(pattern: String, date: String): Date {
        return SimpleDateFormat(pattern, Locale.getDefault()).parse(date)
    }
}

fun String.capitalize(locale: Locale): String {
    return lowercase().replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
    }
}

fun String.capitalize(): String {
    return lowercase().replaceFirstChar(Char::titlecase)
}
