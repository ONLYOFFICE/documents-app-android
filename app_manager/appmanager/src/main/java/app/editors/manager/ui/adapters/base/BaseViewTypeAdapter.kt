package app.editors.manager.ui.adapters.base

import androidx.recyclerview.widget.DiffUtil.DiffResult
import lib.toolkit.base.ui.adapters.ViewTypeAdapter
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.ViewType

open class BaseViewTypeAdapter<T : ViewType>(holderFactory: HolderFactory)
    : ViewTypeAdapter<T>(holderFactory) {

    private var items: MutableList<T> = mutableListOf()

    override var itemsList: List<T>
        get() = items
        set(value) {
            items.clear()
            items.addAll(value)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItems(list: List<T>) {
        items.addAll(list)
        notifyItemRangeInserted(items.size, list.size)
    }

    fun addItemsAtTop(list: List<T>) {
        items.addAll(0, list)
        notifyItemRangeInserted(0, list.size)
    }

    fun setItems(list: List<T>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setItem(item: T, position: Int) {
        if (getItem(position) != null) {
            items[position] = item
            notifyItemChanged(position)
        }
    }

    fun addItem(item: T): Int {
        items.add(item)
        notifyItemInserted(items.size)
        return items.size
    }

    fun addItemAtTop(item: T) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun getItemList(): List<T> {
        return items
    }

    fun getItem(index: Int): T? {
        return if (index >= 0 && index < items.size) items[index] else null
    }

    fun clear() {
        items.clear()
        notifyItemRangeRemoved(0, items.size)
    }

    fun removeItem(position: Int) {
        if (getItem(position) != null) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun removeItem(item: T) {
        if (items.isNotEmpty() && items.contains(item)) {
            notifyItemRemoved(items.indexOf(item))
            items.remove(item)
        }
    }

    fun removeItems(items: List<T>) {
        if (items.isNotEmpty()) {
            for (item in items) {
                this.items.remove(item)
                notifyItemRemoved(this.items.indexOf(item))
            }
        }
    }

    fun updateItem(item: T): Int {
        if (items.isNotEmpty()) {
            val position = items.indexOf(item)
            if (position != -1) {
                items[position] = item
                notifyItemChanged(position, item)
            }
            return position
        }
        return 0
    }

    fun updateItem(item: T, position: Int) {
        if (items.isNotEmpty()) {
            if (position != -1) {
                items[position] = item
                notifyItemChanged(position, item)
            }
        }
    }

    fun set(list: List<T>, result: DiffResult) {
        items.clear()
        items.addAll(list)
        result.dispatchUpdatesTo(this)
    }

    fun setData(list: MutableList<T>) {
        items = list
    }
}