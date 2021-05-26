package app.editors.manager.ui.adapters.share

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.ShareHeaderUi

class ShareHeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(shareHeaderUi: ShareHeaderUi) {
        view.findViewById<TextView>(R.id.list_share_settings_header_title).text = shareHeaderUi.title
    }
}