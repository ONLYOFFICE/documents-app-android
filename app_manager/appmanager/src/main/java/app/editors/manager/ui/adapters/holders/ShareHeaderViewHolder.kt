package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.databinding.ListShareAddHeaderBinding
import app.editors.manager.mvp.models.ui.ShareHeaderUi

class ShareHeaderViewHolder(view: View) : ShareViewHolder<ShareHeaderUi>(view) {

    private val headerBinding = ListShareAddHeaderBinding.bind(view)

    override fun bind(item: ShareHeaderUi) {
        headerBinding.listShareAddHeaderTitle.text = item.title
    }
}