package lib.toolkit.base.ui.recycler

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import lib.toolkit.base.ui.adapters.Adapter
import lib.toolkit.base.ui.adapters.ViewTypeAdapter
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.ViewType

interface Recycler<T : ViewType> {

    companion object {

        operator fun <T : ViewType> invoke(
            recyclerView: RecyclerView,
            adapter: ViewTypeAdapter<T>
        ): Recycler<T> {
            return RecyclerImpl(recyclerView, adapter)
        }

        // Если нужен простой адаптер, то просто отдаём HolderFactory
        operator fun <T : ViewType> invoke(
            recyclerView: RecyclerView,
            holderFactory: HolderFactory
        ): Recycler<T> {
            return RecyclerImpl(recyclerView, Adapter(holderFactory))
        }

    }

    val recyclerView: RecyclerView
    val adapter: ViewTypeAdapter<T>

    fun setItems(items: List<T>)
    fun <R : ViewType> clickedItem(vararg viewType: Int): Observable<R>
    fun <R : ViewType> clickedItemViewId(viewType: Int, viewId: Int): Observable<R>

}

internal class RecyclerImpl<T : ViewType>(
    override val recyclerView: RecyclerView,
    override val adapter: ViewTypeAdapter<T>
) : Recycler<T> {

    init {
        recyclerView.adapter = adapter
    }

    override fun setItems(items: List<T>) {
        adapter.itemsList = items
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : ViewType> clickedItem(vararg viewType: Int): Observable<R> {
        return adapter.holderFactory.clickPosition(*viewType).map { adapter.itemsList[it] as R }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : ViewType> clickedItemViewId(viewType: Int, viewId: Int): Observable<R> {
        return adapter.holderFactory.clickPosition(viewType, viewId).map { adapter.itemsList[it] as R }
    }
}