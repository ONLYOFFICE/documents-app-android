package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import app.editors.manager.R
import lib.toolkit.base.managers.utils.UiUtils.dpToPixel

class ConnectCloudView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.include_clouds_item, this)
    }

    private val defaultSize = dpToPixel(40f, context)

    private val imageView: ImageView? = findViewById(R.id.settingIcon)
    private val titleView: TextView? = findViewById(R.id.settingText)
    private val container: ConstraintLayout? = findViewById(R.id.container)

    var isVisible: Boolean
        get() {
            return visibility == VISIBLE
        }
        set(value) {
            visibility = if (value) {
                VISIBLE
            } else {
                GONE
            }
        }

    fun bind(@DrawableRes icon: Int, @StringRes title: Int, listener: () -> Unit) {
        imageView?.setSize()
        imageView?.setImageResource(icon)
        titleView?.setText(title)
        container?.setOnClickListener {
            listener()
        }
    }

    private fun ImageView.setSize(size: Float = defaultSize) {
        val params = layoutParams as LayoutParams
        params.height = size.toInt()
        params.width = size.toInt()
        layoutParams = params
    }

}