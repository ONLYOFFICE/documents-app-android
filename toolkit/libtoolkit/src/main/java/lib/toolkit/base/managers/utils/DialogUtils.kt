package lib.toolkit.base.managers.utils

import android.content.Context
import android.util.Size
import lib.toolkit.base.ui.dialogs.base.BaseDialog

object DialogUtils {
    fun getWidth(context: Context): Int {
        with(context) {
            val sizes = Size(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            val size = sizes.width.coerceAtMost(sizes.height)
            return (size * if (UiUtils.isTablet(this)) {
                BaseDialog.WIDTH_TABLET_PERCENT
            } else {
                BaseDialog.WIDTH_PHONE_PERCENT
            }).toInt()
        }
    }
}