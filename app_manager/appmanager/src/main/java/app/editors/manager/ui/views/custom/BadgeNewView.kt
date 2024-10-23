package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.cardview.widget.CardView
import lib.toolkit.base.R

class BadgeNewView : CardView {

    var number: Int = 0
        set(value) {
            updateText(value)
            invalidate()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        cardElevation = 0f
        inflate(context, app.editors.manager.R.layout.view_layout_badge_new, this)
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            app.editors.manager.R.styleable.BadgeNewView,
            0,
            0
        )
        try {
            val number = typedArray.getInt(app.editors.manager.R.styleable.BadgeNewView_number, -1)
            updateText(number)
        } finally {
            typedArray.recycle()
        }
    }

    private fun updateText(number: Int) {
        val textView = findViewById<TextView>(app.editors.manager.R.id.badgeText)
        if (number > 0) {
            val text = number.toString()
            textView.text = text
            if (text.length == 1) textView.layoutParams = textView.layoutParams.apply { width = 0 }
            radius = resources.getDimension(R.dimen.material3_container_radius)
        } else {
            textView.text = "new"
            radius = resources.getDimension(R.dimen.default_corner_radius_small)
        }
    }
}