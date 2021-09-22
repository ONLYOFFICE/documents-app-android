package lib.toolkit.base.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observer

data class ItemClick(val viewType: Int, val position: Int, val view: View)

interface RecyclerHolderClickListener {
    fun accept(holder: BaseViewHolder<*>)
    fun accept(view: View, holder: BaseViewHolder<*>)
}

class RecyclerItemClickListener : Observable<ItemClick>(), RecyclerHolderClickListener {

    val source = PublishRelay.create<ItemClick>()

    override fun accept(holder: BaseViewHolder<*>) {
        holder.itemView.setOnClickListener {
            if (holder.absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                source.accept(ItemClick(holder.itemViewType, holder.absoluteAdapterPosition, it))
            }
        }
    }

    override fun accept(view: View, holder: BaseViewHolder<*>) {
        view.setOnClickListener {
            if (holder.absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                source.accept(ItemClick(holder.itemViewType, holder.absoluteAdapterPosition, it))
            }
        }
    }

    override fun subscribeActual(observer: Observer<in ItemClick>) {
        source.subscribe(observer)
    }

}