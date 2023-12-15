package app.editors.manager.ui.adapters.holders

import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import app.editors.manager.R

interface SelectableIcon {

    val selectableView: ViewGroup

    fun setSelected(isSelectMode: Boolean, isSelected: Boolean) {
        val context = selectableView.context
        with(selectableView) {
            background = if (isSelectMode)
                AppCompatResources.getDrawable(context, R.drawable.drawable_list_image_select_background) else null
            foreground = if (isSelected)
                AppCompatResources.getDrawable(context, R.drawable.drawable_list_image_select_foreground) else null
        }
    }
}