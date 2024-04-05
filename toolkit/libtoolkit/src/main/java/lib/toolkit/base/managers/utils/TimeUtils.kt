package lib.toolkit.base.managers.utils


import android.content.Context
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
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
    val DEFAULT_FORMAT = SimpleDateFormat(OUTPUT_PATTERN_DEFAULT)
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

    fun getDateTimeLeft(context: Context, date: String): String? {
        val time = DEFAULT_FORMAT.parse(date)?.time ?: 0
        val timeLeft = time - System.currentTimeMillis()
        val second = 1000
        val minute = 60 * second
        val hour = 60 * minute
        val day = 24 * hour

        return when {
            timeLeft / hour > 23 -> {
                val days = timeLeft / day
                "$days ${context.resources.getQuantityString(R.plurals.days, days.toInt())}"
            }
            timeLeft / hour.toFloat() in 0.1f..24f -> {
                val hours = (timeLeft / hour).coerceAtLeast(1)
                "$hours ${context.resources.getQuantityString(R.plurals.hours, hours.toInt())}"
            }
            else -> null
        }
    }

    fun showDateTimePickerDialog(context: Context, onDateTimeSet: (Date) -> Unit) {
        val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager
        val tmp = Calendar.getInstance()

        val timePickerDialog = MaterialTimePicker.Builder()
            .setTimeFormat(if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    tmp.set(Calendar.HOUR_OF_DAY, hour)
                    tmp.set(Calendar.MINUTE, minute)
                    onDateTimeSet.invoke(tmp.time)
                }
            }

        fragmentManager?.let {
            MaterialDatePicker.Builder
                .datePicker()
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.from(System.currentTimeMillis()))
                        .build()
                )
                .build()
                .apply {
                    addOnPositiveButtonClickListener { date ->
                        with(Calendar.getInstance().also { it.time = Date(date) }) {
                            tmp.set(
                                get(Calendar.YEAR),
                                get(Calendar.MONTH),
                                get(Calendar.DAY_OF_MONTH)
                            )
                            timePickerDialog.show(fragmentManager, null)
                        }
                    }
                }
                .show(fragmentManager, null)
        }
    }

    fun getCurrentLocale(context: Context): Locale? {
        return context.resources.configuration.locales.get(0);
    }

}
