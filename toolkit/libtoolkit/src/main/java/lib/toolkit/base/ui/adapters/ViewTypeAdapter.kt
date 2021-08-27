package lib.toolkit.base.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.ViewType

abstract class ViewTypeAdapter<T : ViewType>(private val holderFactory: HolderFactory) :
    RecyclerView.Adapter<BaseViewHolder<ViewType>>() {

    abstract var itemsList: List<T>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ViewType> =
        holderFactory(parent, viewType)

    override fun onBindViewHolder(holder: BaseViewHolder<ViewType>, position: Int) {
        holder.bind(itemsList[position])
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ViewType>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.bind(itemsList[position], payloads)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = itemsList.count()

    override fun getItemViewType(position: Int): Int {
        return itemsList[position].viewType
    }

}