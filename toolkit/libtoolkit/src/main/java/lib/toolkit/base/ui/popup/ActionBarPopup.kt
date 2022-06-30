package lib.toolkit.base.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import lib.toolkit.base.R

open class ActionBarPopupItem(val title: Int)

@SuppressLint("InflateParams")
abstract class ActionBarPopup(context: Context) : PopupWindow(
    null, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
) {
    private val margin = context.resources.getDimension(R.dimen.default_margin_large).toInt()
    private val shape = ContextCompat.getDrawable(context, R.drawable.shape_action_bar_popup)

    protected val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        animationStyle = R.style.MainPopupAnimation
        isOutsideTouchable = true
        isClippingEnabled = true
        elevation = context.resources.getDimension(R.dimen.elevation_height_popup)
    }

    fun show(view: View) {
        setBackgroundDrawable(shape)
        showAtLocation(view, Gravity.END or Gravity.TOP, margin / 2, margin * 2)
    }

    open fun hide() {
        dismiss()
    }
}