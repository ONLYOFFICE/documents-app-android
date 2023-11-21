package app.editors.manager.ui.views.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import app.editors.manager.R
import com.google.android.material.button.MaterialButton


class AccessIconButton : MaterialButton {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val attr = intArrayOf(com.google.android.material.R.attr.icon)
        val a = context.obtainStyledAttributes(attrs, attr)
        val icon = a.getResourceId(0, -1)
        a.recycle()
        setIconResource(icon)
    }

    override fun setIconResource(iconResourceId: Int) {
        icon = AppCompatResources.getDrawable(context, iconResourceId)
    }

    override fun setIcon(icon: Drawable?) {
        super.setIcon(addArrow(icon))
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style)

    private fun addArrow(drawable: Drawable?): Drawable {
        val arrow = AppCompatResources.getDrawable(context, R.drawable.ic_drawer_menu_header_arrow)
        return LayerDrawable(arrayOf(drawable, arrow)).apply {
            setLayerGravity(0, Gravity.START or Gravity.CENTER_VERTICAL)
            setLayerGravity(1, Gravity.END or Gravity.CENTER_VERTICAL)
            setLayerInsetEnd(0, 10)
            setLayerInsetEnd(1, -15)
            setLayerSize(0, 60, 60)
            setLayerSize(1, 15, 12)
        }
    }
}