package lib.toolkit.base.ui.views.pager

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.view.children
import com.google.android.material.tabs.TabLayout
import lib.toolkit.base.R

class IconTabLayout : TabLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, style: Int) : super(context, attrs, style)

    private val iconSize: Int by lazy { resources.getDimension(R.dimen.image_size).toInt() }

    init {
        tabRippleColor = null
    }

    override fun addTab(tab: Tab, setSelected: Boolean) {
        super.addTab(tab, setSelected)
        for (view in tab.view.children) {
            if (view is ImageView) {
                view.layoutParams = view.layoutParams.apply {
                    height = iconSize
                    width = iconSize
                }
                view.requestLayout()
                break
            }
        }
    }

}