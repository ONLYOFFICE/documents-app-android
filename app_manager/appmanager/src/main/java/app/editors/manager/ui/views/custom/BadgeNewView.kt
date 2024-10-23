package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import lib.toolkit.base.R

class BadgeNewView : CardView {

    var text: String? = null
        set(value) {
            updateText(value)
            invalidate()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        cardElevation = 0f
        radius = resources.getDimension(R.dimen.material3_container_radius)
        inflate(context, app.editors.manager.R.layout.view_layout_badge_new, this)
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            app.editors.manager.R.styleable.BadgeNewView,
            0,
            0
        )
        try {
            val text = typedArray.getString(app.editors.manager.R.styleable.BadgeNewView_text)
            updateText(text)
        } finally {
            typedArray.recycle()
        }
    }

    private fun updateText(text: String?) {
        isVisible = !text.isNullOrEmpty()
        findViewById<TextView>(app.editors.manager.R.id.badgeText).apply {
            if (text != null) {
                this.text = text
                if (text.length == 1) layoutParams = layoutParams.apply { width = 0 }
            }
        }
    }
}