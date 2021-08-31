package app.editors.manager.ui.adapters.share

import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.ui.adapters.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAdapter(
    factory: ShareHolderFactory,
) : BaseViewTypeAdapter<ViewType>(factory) {

    fun removeHeader(item: ShareHeaderUi) {
        removeItem(item)
    }
}