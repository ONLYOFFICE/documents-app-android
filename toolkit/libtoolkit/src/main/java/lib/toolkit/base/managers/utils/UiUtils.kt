package lib.toolkit.base.managers.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import lib.toolkit.base.BuildConfig
import lib.toolkit.base.R
import java.lang.ref.WeakReference
import java.nio.IntBuffer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.ceil

object UiUtils {

    /*
    * Helpers methods
    * */

    private fun getDeviceInfo(context: Context? = null) : Map<String, String> {
        val deviceInfo = TreeMap<String, String>()
        deviceInfo["OS version"] = System.getProperty("os.version")!!
        deviceInfo["Api level"] = Build.VERSION.SDK_INT.toString()
        deviceInfo["App code"] = BuildConfig.VERSION_CODE.toString()
        deviceInfo["App name"] = BuildConfig.VERSION_NAME
        deviceInfo["Device"] = Build.DEVICE
        deviceInfo["Model"] = Build.MODEL
        deviceInfo["Brand"] = Build.BRAND
        deviceInfo["Version release"] = Build.VERSION.RELEASE
        deviceInfo["Device language"] = Locale.getDefault().displayLanguage

        context?.let {
            val info = it.packageManager.getPackageInfo(it.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                deviceInfo["App code"] = info.longVersionCode.toString()
            } else {
                deviceInfo["App code"] = info.versionCode.toString()
            }
            deviceInfo["App name"] = info.versionName
        }
        return deviceInfo
    }

    @JvmStatic
    fun isTablet(context: Context): Boolean {
        return context.resources.getInteger(R.integer.screen_size) > 0
    }

    @JvmStatic
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    @JvmStatic
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    @JvmStatic
    fun getScreenSize(context: Context): Point {
        return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).let { manager ->
            Point().apply {
                manager.defaultDisplay.getRealSize(this)
            }
        }
    }

    @JvmStatic
    fun getScreenRect(context: Context): Rect {
        return getScreenSize(context).let {
            Rect().apply {
                left = 0
                top = 0
                right = it.x
                bottom = it.y
            }
        }
    }

    @JvmStatic
    fun getViewRectOnScreen(view: View): Rect {
        val viewPosition = IntArray(2)
        view.getLocationOnScreen(viewPosition)
        return Rect().apply {
            left = viewPosition[0]
            top = viewPosition[1]
            right = viewPosition[0] + view.width
            bottom = viewPosition[1] + view.height
        }
    }

    @JvmStatic
    fun getViewRectInWindow(view: View): Rect {
        val viewPosition = IntArray(2)
        view.getLocationInWindow(viewPosition)
        return Rect().apply {
            left = viewPosition[0]
            top = viewPosition[1]
            right = viewPosition[0] + view.width
            bottom = viewPosition[1] + view.height
        }
    }

    @JvmStatic
    fun getWindowVisibleRect(view: View): Rect {
        return Rect().apply {
            view.getWindowVisibleDisplayFrame(this)
        }
    }

    @JvmStatic
    fun getActivityVisibleRect(activity: Activity): Rect {
        val view = activity.window.decorView
        val viewRect = getViewRectOnScreen(view)
        val visibleRect = getWindowVisibleRect(view)
        return if (visibleRect.contains(viewRect)) {
            viewRect
        } else {
            visibleRect
        }
    }

    @JvmStatic
    fun getOverlapViewRect(anchor: Rect, view: Rect, restrict: Rect, offset: Point): Rect {
        val position = Rect().apply {
            left = anchor.left
            top = anchor.top
            right = anchor.left + view.width()
            bottom = anchor.top + view.height()
        }

        position.offset(offset.x, offset.y)

        if (position.right > restrict.right) {
            var dX = anchor.right - view.width() - abs(offset.x)
            if (dX + view.width() > restrict.right) {
                dX = restrict.right - view.width() - abs(offset.x)
            }
            position.left = dX
            position.right = dX + view.width()
        }

        if (position.left < restrict.left) {
            var dX = anchor.left + abs(offset.y)
            if (dX < restrict.left) {
                dX = restrict.left + abs(offset.x)
            }
            position.left = dX
            position.right = dX + view.width()
        }

        if (position.bottom > restrict.bottom) {
            var dY = anchor.bottom - view.height() - abs(offset.y)
            if (dY + view.height() > restrict.bottom) {
                dY = restrict.bottom - view.height() - abs(offset.y)
            }
            position.top = dY
            position.bottom = dY + view.height()
        }

        if (position.top < restrict.top) {
            var dY = anchor.top + abs(offset.y)
            if (dY < restrict.top) {
                dY = restrict.top + abs(offset.y)
            }
            position.top = dY
            position.bottom = dY + view.height()
        }

        return position
    }

    @JvmStatic
    fun getDropViewRect(anchor: Rect, view: Rect, restrict: Rect, offset: Point, isDown: Boolean = true, isCentered: Boolean = false): Rect {
        val position = Rect().apply {
            left = anchor.left
            top = anchor.top
            right = anchor.left + view.width()
            bottom = anchor.top + view.height()
        }

        if (isCentered) {
            position.offset(((Math.abs(anchor.width()) - view.width()) * 0.5f).toInt(), 0)
        }

        if (isDown) {
            position.offset(offset.x, offset.y)
            position.offset(0, anchor.height())
        } else {
            position.offset(offset.x, -offset.y)
            position.offset(0, -view.height())
        }

        if (position.right > restrict.right) {
            var dX = anchor.right - view.width() - Math.abs(offset.x)
            if (dX + view.width() > restrict.right) {
                dX = restrict.right - view.width() - Math.abs(offset.x)
            }
            position.left = dX
            position.right = dX + view.width()
        }

        if (position.left < restrict.left) {
            var dX = anchor.left + Math.abs(offset.x)
            if (dX < restrict.left) {
                dX = restrict.left + Math.abs(offset.x)
            }
            position.left = dX
            position.right = dX + view.width()
        }

        if (position.bottom > restrict.bottom) {
            var dY = anchor.top - view.height() - Math.abs(offset.y)
            if (dY + view.height() > restrict.bottom) {
                dY = restrict.bottom - view.height() - Math.abs(offset.y)
            }
            position.top = dY
            position.bottom = dY + view.height()
        }

        if (position.top < restrict.top) {
            var dY = anchor.bottom + Math.abs(offset.y)
            if (dY + view.height() < restrict.top) {
                dY = restrict.top + Math.abs(offset.y)
            }
            position.top = dY
            position.bottom = dY + view.height()
        }

        return position
    }

    /*
    * Show info message
    * */
    @JvmStatic
    fun getSnackBar(rootView: View, duration: Int = BaseTransientBottomBar.LENGTH_LONG, @ColorRes colorId: Int = R.color.colorBlack): Snackbar {
        val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)

        snackbar.duration = duration

        val view = snackbar.view
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 3

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else {
            textView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        view.setBackgroundColor(ContextCompat.getColor(rootView.context, colorId))
        return snackbar
    }

    @JvmStatic
    fun getShortSnackBar(rootView: View): Snackbar {
        return getSnackBar(
                rootView,
                Snackbar.LENGTH_SHORT,
                R.color.colorBlack
        )
    }

    @JvmStatic
    fun getSnackBar(activity: Activity): Snackbar {
        return getSnackBar(
                activity.findViewById(android.R.id.content),
                Snackbar.LENGTH_SHORT,
                R.color.colorBlack
        )
    }

    @JvmStatic
    fun hideSnackBarOnOutsideTouch(snackbar: Snackbar?, motionEvent: MotionEvent?): Boolean {
        if (snackbar?.isShown == true && motionEvent?.action == MotionEvent.ACTION_DOWN) {
            val rect = Rect()
            snackbar.view.getHitRect(rect)
            if (!rect.contains(motionEvent.x.toInt(), motionEvent.y.toInt())) {
                snackbar.dismiss()
                return true
            }
        }

        return false
    }

    @JvmStatic
    fun getToast(activity: Activity): Toast {
        return Toast.makeText(activity, "", Toast.LENGTH_LONG).apply {
            duration = Toast.LENGTH_SHORT
        }
    }

    @JvmStatic
    fun getFloatResource(context: Context, @DimenRes resId: Int): Float {
        val typedValue = TypedValue()
        context.resources.getValue(resId, typedValue, true)
        return typedValue.float
    }

    @JvmStatic
    fun getFilteredDrawable(context: Context, @DrawableRes resId: Int, @ColorRes color: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, resId)!!.constantState!!.newDrawable().mutate()
        drawable.setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN)
        return drawable
    }

    @JvmStatic
    fun setImageTint(imageView: ImageView, @ColorRes colorRes: Int): ImageView {
        imageView.setColorFilter(ContextCompat.getColor(imageView.context, colorRes))
        return imageView
    }

    @JvmStatic
    fun setImageTint(imageView: ImageView, @DrawableRes drawableRes: Int, @ColorRes colorRes: Int): ImageView {
        imageView.setImageResource(drawableRes)
        setImageTint(imageView, colorRes)
        return imageView
    }

    @JvmStatic
    fun setMenuItemTint(context: Context, menuTint: MenuItem, @ColorRes colorRes: Int): MenuItem {
        menuTint.icon.setColorFilter(ContextCompat.getColor(context, colorRes),
                PorterDuff.Mode.SRC_IN)
        return menuTint
    }

    @JvmStatic
    fun imageCropTop(imageView: AppCompatImageView) {
        val matrix = imageView.imageMatrix
        val imageWidth = imageView.drawable.intrinsicWidth.toFloat()
        val sizes = getScreenSize(imageView.context)
        val screenSide = if (isLandscape(imageView.context)) sizes.y else sizes.x
        val scaleRatio = screenSide / imageWidth
        matrix.postScale(scaleRatio, scaleRatio)
        imageView.imageMatrix = matrix
    }

    @JvmStatic
    fun getDeviceInfoString(context: Context? = null, isIndents: Boolean): String {
        val stringBuilder = StringBuilder()
        val deviceInfo = getDeviceInfo(context)

        stringBuilder.append(if (isIndents) "\n\n\n\n\n\n\n\n\n\n" else "\n\n")
        stringBuilder.append("______________________________").append("\n\n")
        for (key in deviceInfo.keys) {
            stringBuilder.append(key).append(": ").append(deviceInfo[key]).append("\n")
        }
        stringBuilder.append("\n")
        return stringBuilder.toString()
    }

    @JvmStatic
    fun getAppPackageVersionCode(context: Context, packageId: String): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageId, PackageManager.GET_META_DATA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

    @JvmStatic
    fun measureTextSizes(text: String, size: Int): Point {
        val paint = Paint()
        val bounds = Rect()
        paint.typeface = Typeface.DEFAULT
        paint.textSize = size.toFloat()
        paint.getTextBounds(text, 0, text.length, bounds)
        return Point(bounds.width(), bounds.height())
    }

    @JvmStatic
    fun setStatusBarColor(activity: Activity, @ColorRes color: Int) {
        activity.window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_FULLSCREEN)
            statusBarColor = ContextCompat.getColor(activity, color)
        }
    }

    @JvmStatic
    fun recyclerAutoColumns(context: Context, width: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val columns = (dpWidth / width).toInt()
        return columns
    }

    @JvmStatic
    fun setLayoutParams(view: View, height: Int, width: Int) {
        val layoutParams = view.layoutParams
        layoutParams.height = height
        layoutParams.width = width
        view.layoutParams = layoutParams
    }

    @JvmStatic
    fun setProgressBarColorDrawable(progressBar: ProgressBar, @ColorRes colorId: Int) {
        progressBar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(progressBar.context, colorId), PorterDuff.Mode.SRC_ATOP)
    }

    @JvmStatic
    fun setProgressBarColorTint(progressBar: ProgressBar, @ColorRes colorId: Int) {
        progressBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(progressBar.context, colorId))
    }

    @JvmStatic
    fun dimEffect(context: Context, window: Window, dim: Float = 0.8f) {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).let { manager ->
            (window.decorView.layoutParams as WindowManager.LayoutParams).let { params ->
                params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                params.dimAmount = dim
                manager.updateViewLayout(window.decorView, params)
            }
        }
    }

    @JvmStatic
    fun getStatusBarHeightMeasure(activity: Activity): Int {
        return getWindowVisibleRect(activity.window.decorView).top
    }

    @JvmStatic
    fun getStatusBarHeightResource(context: Context): Int {
        return ceil(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            24
        } else {
            25
        } * context.resources.displayMetrics.density).toInt()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun getStatusBarHeightInsets(activity: Activity): Int {
        return activity.window.decorView.rootWindowInsets.systemWindowInsetTop
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun getNavigationBarHeightInsets(activity: Activity): Int {
        return activity.window.decorView.rootWindowInsets.systemWindowInsetBottom
    }

    @JvmStatic
    fun dpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @JvmStatic
    fun pixelsToDp(pixel: Int, context: Context): Float {
        return pixel.toFloat() / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT.toFloat())
    }

    @JvmStatic
    fun removePaddingFromNavigationItem(view: BottomNavigationView) {
        val menuView = view.getChildAt(0) as BottomNavigationMenuView

        for (i in 0 until menuView.childCount) {
            val item = menuView.getChildAt(i) as BottomNavigationItemView
            val activeLabel = item.findViewById<View>(R.id.largeLabel)
            if (activeLabel is TextView) {
                activeLabel.setPadding(0, 0, 0, 0)
            }
        }
    }

    @JvmStatic
    fun checkDeXEnabled(configuration: Configuration): Boolean {
        val enabled: Boolean
        try {
            val configClass = configuration.javaClass
            enabled = configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass) == configClass.getField("semDesktopModeEnabled").getInt(configuration)
            return enabled
        } catch (ignored: NoSuchFieldException) {
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: IllegalArgumentException) {
        }

        return false
    }

    @JvmStatic
    fun getParentDisplay(activity: Activity): Display {
        return activity.windowManager.defaultDisplay
    }

    @JvmStatic
    fun getSecondDisplayIds(context: Context): Array<Int> {
        val displays = (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays
        return if (displays.size > 1) {
            val secondDisplays = ArrayList<Int>()
            for (i in 1..displays.lastIndex) {
                secondDisplays.add(displays[i].displayId)
            }
            secondDisplays.toTypedArray()
        } else {
            arrayOf()
        }
    }

    @JvmStatic
    fun isHuaweiDesktopMode(configuration: Configuration): Boolean {
        val brand = getDeviceInfo()["Brand"]
        return if (brand.equals("HUAWEI")) {
            configuration.densityDpi in 160..240
        } else {
            false
        }
    }

    @JvmStatic
    fun pixelsToBitmap(width: Int, height: Int, pixels: IntArray): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixels))
        return bitmap
    }

    @JvmStatic
    fun isMultiWindow(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.isInMultiWindowMode
        } else {
            false
        }
    }

    @JvmStatic
    fun getButtonTint(context: Context, color: Int): ColorStateList? {
        return ContextCompat.getColorStateList(context, color)
    }

    fun getTabStateList(context: Context): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.colorWhite),
            ContextCompat.getColor(context, R.color.colorLight),
        )

        return ColorStateList(states, colors)
    }

    @JvmStatic
    fun getBoundCoordinate(size: Int, start: Int, end: Int, delta: Float): Float {
        val newStart = start + delta
        val newEnd = end + delta

        val boundStart = 0 - newStart
        val boundEnd = newEnd - size

        return when {
            boundStart > 0 -> delta + boundStart
            boundEnd > 0 -> delta - boundEnd
            else -> delta
        }

    }

}


class ActivityLayoutListener : ViewTreeObserver.OnGlobalLayoutListener {

    interface OnActivityChangeListener {
        fun onActivityChangeSize(
                totalHeight: Int,
                visibleHeight: Int,
                topPadding: Int,
                bottomPadding: Int
        )
        fun onFinishDrawingActivity()
    }

    var activityTotalHeight: Int = 0
        private set

    var activityVisibleHeight: Int = 0
        private set

    var activityTopPadding: Int = 0
        private set

    var activityBottomPadding: Int = 0
        private set

    private var mWeakActivity: WeakReference<Activity>? = null
    private var mWeakListener: WeakReference<OnActivityChangeListener>? = null

    override fun onGlobalLayout() {
        mWeakActivity?.get()?.let { activity ->
            val screenSize = Point()
            activity.windowManager.defaultDisplay.getRealSize(screenSize)

            val activitySize = Point()
            activity.windowManager.defaultDisplay.getSize(activitySize)

            val activityVisibleRect = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(activityVisibleRect)

            activityTotalHeight = activitySize.y
            activityVisibleHeight = activityVisibleRect.height()
            activityTopPadding = activityVisibleRect.top
            activityBottomPadding = screenSize.y - activityVisibleRect.bottom

            mWeakListener?.get()?.onActivityChangeSize(
                    activityTotalHeight,
                    activityVisibleHeight,
                    activityTopPadding,
                    activityBottomPadding
            )
            mWeakListener?.get()?.onFinishDrawingActivity()
        }
    }

    fun setActivity(activity: Activity) {
        mWeakActivity = WeakReference(activity)
        activity.window.decorView.apply {
            viewTreeObserver.addOnGlobalLayoutListener(this@ActivityLayoutListener)
        }
    }

    fun removeActivity() {
        mWeakActivity?.get()?.apply {
            window.decorView.apply {
                viewTreeObserver.removeOnGlobalLayoutListener(this@ActivityLayoutListener)
            }
        }
        mWeakActivity = null
    }

    fun setListener(listener: OnActivityChangeListener) {
        mWeakListener = WeakReference(listener)
    }

    fun removeListener() {
        mWeakListener = null
    }

}