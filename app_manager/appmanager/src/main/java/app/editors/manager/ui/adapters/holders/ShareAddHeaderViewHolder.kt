package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.databinding.ListShareAddHeaderBinding
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAddHeaderViewHolder<T: ViewType>(view: View)
    : BaseViewHolder<T>(view) {

    private var headerBinding = ListShareAddHeaderBinding.bind(view)

    override fun bind(item: T) {
        headerBinding.listShareAddHeaderTitle.text = (item as ShareHeaderUi).title
    }
}