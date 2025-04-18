package app.editors.manager.ui.views.badge

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.UiFormFillingStatus

class BadgeCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardView: CardView
    private val textView: TextView

    private var badgeText: String = ""
    private var badgeColor: Int = 0


    init {
        val view = inflate(context, R.layout.view_badge_card, this)
        cardView = view.findViewById(R.id.badgeCard)
        textView = view.findViewById(R.id.badgeText)

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BadgeCardView)

            try {
                badgeText = typedArray.getString(R.styleable.BadgeCardView_badgeText) ?: ""
                badgeColor = typedArray.getColor(
                    R.styleable.BadgeCardView_badgeColor,
                    ContextCompat.getColor(context, lib.toolkit.base.R.color.colorFormYourTurn)
                )
            } finally {
                typedArray.recycle()
            }
        }
        applyAttributes()
    }

    private fun applyAttributes() {
        textView.text = badgeText
        cardView.setCardBackgroundColor(badgeColor)
    }

    fun setBadgeText(text: String) {
        badgeText = text
        textView.text = text
    }

    fun setBadgeColor(color: Int) {
        badgeColor = color
        cardView.setCardBackgroundColor(color)
    }
}

fun BadgeCardView.setFormStatus(status: UiFormFillingStatus) {
    if (status != UiFormFillingStatus.None) {
        setBadgeText(context.getString(status.textRes))
        setBadgeColor(context.getColor(status.colorRes))
        isVisible = true
    } else {
        isVisible = false
    }
}