package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import lib.toolkit.base.databinding.ListItemHeaderBinding
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class ShareHeaderViewHolder(view: View) : BaseViewHolder<ShareHeaderUi>(view) {

    override fun bind(item: ShareHeaderUi) {
        with(ListItemHeaderBinding.bind(view)) {
            title.text = item.title
        }
    }
}