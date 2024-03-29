package lib.toolkit.base.ui.popup

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils

abstract class BasePopup(protected val context: Context, @LayoutRes protected var layoutId: Int) {

    companion object {
        val TAG: String = BasePopup::class.java.simpleName
    }

    var margin: Int = context.resources.getDimensionPixelSize(R.dimen.elevation_height_micro)
    var bottomMargin = 0
    var elevation: Float = context.resources.getDimension(R.dimen.elevation_height_micro)

    var isDropDown: Boolean = true
    var isDropCentered: Boolean = false

    protected lateinit var popupView: View
    protected lateinit var popupWindow: PopupWindow

    init {
        setPopup(context, layoutId)
        bind(popupView)
    }

    protected open fun setPopup(context: Context, @LayoutRes resId: Int) {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = layoutInflater.inflate(resId, null)
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.isOutsideTouchable = true
        popupWindow.isClippingEnabled = true
        popupWindow.elevation = elevation
    }

    protected fun getPopupDefaultRect(): Rect {
        return Rect().apply {
            left = 0
            top = 0
            right = popupView.measuredWidth
            bottom = popupView.measuredHeight
        }
    }

    protected fun checkAnchorRect(rect: Rect): Rect {
        return rect.apply {
            if (right == 0) {
                right = left + 2
            }

            if (left > right) {
                val temp = left
                left = right
                right = temp
            }

            if (bottom == 0) {
                bottom = top + 2
            }

            if (top > bottom) {
                val temp = top
                top = bottom
                bottom = temp
            }
        }
    }

    protected fun getPopupPosition(restrictRect: Rect, anchorRect: Rect, isOverlap: Boolean = true): Point {
        val offset = Point(margin, margin + bottomMargin)
        val popupRect = getPopupDefaultRect()

        val position: Rect = if (isOverlap) {
            UiUtils.getOverlapViewRect(anchorRect, popupRect, restrictRect, offset)
        } else {
            UiUtils.getDropViewRect(anchorRect, popupRect, restrictRect, offset, isDropDown, isDropCentered)
        }

        return Point(position.left, position.top)
    }

    open fun hide() {
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }

    open fun showInsideAt(view: View, positionRect: Rect) {
        checkAnchorRect(positionRect)
        val viewRect = UiUtils.getViewRectOnScreen(view)
        positionRect.offset(viewRect.left, viewRect.top)
        val offset = getPopupPosition(viewRect, positionRect, false)
        popupWindow.showAtLocation(view, Gravity.START or Gravity.TOP, offset.x, offset.y)
    }

    open fun showOverlap(view: View, restrictRect: Rect? = null) {
        val restrict = restrictRect ?: UiUtils.getScreenRect(view.context)
        val viewRect = UiUtils.getViewRectOnScreen(view)
        val offset = getPopupPosition(restrict, viewRect).apply {
            offset(-restrict.left, -restrict.top)
        }

        popupWindow.showAtLocation(view, Gravity.START or Gravity.TOP, offset.x, offset.y)
    }

    open fun showOverlap(view: View, activity: Activity) {
        val restrictRect = UiUtils.getActivityVisibleRect(activity)
        showOverlap(view, restrictRect)
    }

    open fun showDropAt(view: View, restrictRect: Rect? = null) {
        val restrict = restrictRect ?: UiUtils.getScreenRect(view.context)
        val viewRect = UiUtils.getViewRectOnScreen(view)
        val offset = getPopupPosition(restrict, viewRect, false).apply {
            offset(-restrict.left, -restrict.top)
        }

        popupWindow.showAtLocation(view, Gravity.START or Gravity.TOP, offset.x, offset.y)
    }

    open fun showDropAt(view: View, activity: Activity) {
        val restrictRect = UiUtils.getActivityVisibleRect(activity)
        showDropAt(view, restrictRect)
    }


    protected abstract fun bind(view: View)

}
