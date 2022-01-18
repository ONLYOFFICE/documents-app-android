package lib.toolkit.base.ui.adapters

import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.ViewType

class Adapter<T: ViewType>(holderFactory: HolderFactory) : ViewTypeAdapter<T>(holderFactory) {

    private val items: MutableList<T> = mutableListOf()

    override var itemsList: List<T>
        get() = items
        set(value) {
            items.clear()
            items.addAll(value)
            notifyDataSetChanged()
        }
}