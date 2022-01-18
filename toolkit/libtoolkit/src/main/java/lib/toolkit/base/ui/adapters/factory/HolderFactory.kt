package lib.toolkit.base.ui.adapters.factory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import lib.toolkit.base.R
import lib.toolkit.base.ui.adapters.ItemClick
import lib.toolkit.base.ui.adapters.RecyclerItemClickListener
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.EmptyListViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

abstract class HolderFactory : (ViewGroup, Int) -> BaseViewHolder<ViewType> {

    protected val clicks = RecyclerItemClickListener()

    abstract fun createViewHolder(view: View, type: Int): BaseViewHolder<*>?

    @Suppress("UNCHECKED_CAST")
    final override fun invoke(viewGroup: ViewGroup, type: Int): BaseViewHolder<ViewType> {
        val view: View = viewGroup.inflate(type)
        return when (type) {
            R.layout.empty_list_item -> EmptyListViewHolder(view)
            else -> checkNotNull(createViewHolder(view, type)) {
                throw Error("Resources not found")
            }
        } as BaseViewHolder<ViewType>
    }

    fun clickPosition(vararg viewType: Int): Observable<Int> {
        return clicks.filter { it.viewType in viewType }.map(ItemClick::position)
    }

    fun clickPosition(viewType: Int, viewId: Int): Observable<Int> {
        return clicks.filter { it.viewType == viewType && viewId == it.view.id }.map(ItemClick::position)
    }

}

@Suppress("UNCHECKED_CAST")
fun <T : View> View.inflate(res: Int, root: ViewGroup? = this as? ViewGroup): T {
    return LayoutInflater.from(context).inflate(res, root, false) as T
}