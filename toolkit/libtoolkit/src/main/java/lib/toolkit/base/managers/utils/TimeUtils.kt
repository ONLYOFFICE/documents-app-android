package lib.toolkit.base.managers.utils


import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    val TAG: String = TimeUtils::class.java.simpleName

    /*
    * Time constant
    * */
    private val ONE_MINUT_MS = 60000L
    private val ONE_HOUR_MS = 3600000L
    private val ONE_DAY_MS = 86400000L

    /*
    * Patterns
    * */
    val OUTPUT_PATTERN_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ"
    private val OUTPUT_PATTERN_DATE = "dd MMM yyyy"
    private val OUTPUT_PATTERN_TIME = "dd MMM yyyy HH:mm"
    private val OUTPUT_PATTERN_FILE = "yyyyMMdd_HHmmssSSS"

    /*
    * Objects
    * */
    private val DEFAULT_FORMAT = SimpleDateFormat(OUTPUT_PATTERN_DEFAULT)
    private val OUTPUT_TIME_FORMAT = SimpleDateFormat(OUTPUT_PATTERN_TIME)
    private val OUTPUT_DATE_FORMAT = SimpleDateFormat(OUTPUT_PATTERN_DATE)


    /*
    * Get time from day begin
    * */
    @JvmStatic
    val todayMs: Long
        get() = getModMs(ONE_DAY_MS)

    /*
     * Get time from hour begin
     * */
    @JvmStatic
    val hourMs: Long
        get() = getModMs(ONE_HOUR_MS)

    @JvmStatic
    val yesterdayMs: Long
        get() = getDaysMs(-1L)

    @JvmStatic
    val weekMs: Long
        get() = getDaysMs(-7L)

    @JvmStatic
    val monthMs: Long
        get() = getDaysMs(-30L)

    @JvmStatic
    val yearMs: Long
        get() = getDaysMs(-365L)

    @JvmStatic
    val fileTimeStamp: String
        get() = SimpleDateFormat(OUTPUT_PATTERN_FILE).format(Date())

    @JvmStatic
    fun formatDate(defaultStr: String): String {
        return try {
            val date = DEFAULT_FORMAT.parse(defaultStr)
            OUTPUT_TIME_FORMAT.format(date)
        } catch (e: ParseException) {
            "-"
        }
    }

    @JvmStatic
    fun formatDate(date: Date?): String {
        return if (date != null) OUTPUT_TIME_FORMAT.format(date) else "-"
    }

    @JvmStatic
    fun getWeekDate(date: Date?): String {
        return if (date != null) {
            if (date.time <= weekMs) {
                OUTPUT_DATE_FORMAT.format(date)
            } else {
                OUTPUT_TIME_FORMAT.format(date)
            }
        } else {
            "-"
        }
    }

    @JvmStatic
    fun getModMs(value: Long): Long {
        val currentTimeMillis = System.currentTimeMillis()
        return currentTimeMillis - currentTimeMillis % value
    }

    /*
     * Get time before/after hours count
     * */
    @JvmStatic
    fun getHoursMs(hours: Long): Long {
        return hourMs + hours * ONE_HOUR_MS
    }

    /*
    * Get time before/after day count
    * */
    @JvmStatic
    fun getDaysMs(days: Long): Long {
        return todayMs + days * ONE_DAY_MS
    }

    @JvmStatic
    fun isJustCreated(ms: Long): Boolean {
        return Calendar.getInstance().timeInMillis - ms <= ONE_MINUT_MS
    }

}
