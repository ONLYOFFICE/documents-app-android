package app.editors.manager.ui.adapters.share

import android.view.InflateException
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.ui.adapters.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import lib.toolkit.base.managers.extensions.inflate
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAdapter(
    factory: ShareHolderFactory,
) : BaseViewTypeAdapter<ViewType>(factory) {

    fun removeHeader(title: String) {
        for (item in itemsList) {
            if (item is Header) {
                if (item.title == title) {
                    removeItem(item)
                    return
                }
            }
        }
    }

}