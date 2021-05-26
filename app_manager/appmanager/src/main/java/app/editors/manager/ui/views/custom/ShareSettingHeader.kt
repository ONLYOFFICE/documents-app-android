package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import app.editors.manager.R

class ShareSettingHeader @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.include_share_settings_header, this)
    }

    private val titleText = findViewById<TextView>(R.id.share_settings_external_access_title)
    private val infoTitle = findViewById<TextView>(R.id.share_settings_external_access_info_title)
    private val copyButton = findViewById<Button>(R.id.share_settings_external_copy_link)
    private val sendButton = findViewById<Button>(R.id.share_settings_external_send_link)
    private val imageLayout = findViewById<ConstraintLayout>(R.id.share_settings_access_button_layout)
    private val image = findViewById<ImageView>(R.id.button_popup_image)

    fun setListener(listener: (view:View) -> Unit) {
        copyButton.setOnClickListener {
            listener.invoke(it)
        }
        sendButton.setOnClickListener {
            listener.invoke(it)
        }
        imageLayout.setOnClickListener {
            listener.invoke(it)
        }
    }

    fun setImage() {

    }

}