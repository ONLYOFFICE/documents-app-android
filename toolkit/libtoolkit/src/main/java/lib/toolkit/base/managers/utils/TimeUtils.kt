package lib.toolkit.base.managers.utils


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import lib.toolkit.base.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val OUTPUT_PATTERN_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss"
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

    fun parseDate(string: String?): Date? {
        val time = DEFAULT_FORMAT.parse(string ?: return null)?.time
        return if (time != null) Date(time) else null
    }

    fun showDateTimePickerDialog(context: Context, date: Date?, onDateTimeSet: (Date) -> Unit) {
        val tmp = Calendar.getInstance()
        val today = Calendar.getInstance().also { if (date != null) it.time = date }

        val timePickerDialog = TimePickerDialog(
            context,
            R.style.ThemeOverlay_Common_DateTimePicker,
            { _, hour, minute ->
                tmp.set(Calendar.HOUR, hour)
                tmp.set(Calendar.MINUTE, minute)
                tmp.set(Calendar.SECOND, 0)
                onDateTimeSet.invoke(tmp.time)
            },
            today.get(Calendar.HOUR),
            today.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(context)
        )

        DatePickerDialog(
            context,
            R.style.ThemeOverlay_Common_DateTimePicker,
            { _, year, month, day ->
                tmp.set(year, month, day)
                timePickerDialog.show()
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.minDate = Calendar.getInstance().also { it.add(Calendar.DATE, 1) }.timeInMillis }.show()

    }

    fun getCurrentLocale(context: Context): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0);
        } else {
            context.resources.configuration.locale;
        }
    }

}
