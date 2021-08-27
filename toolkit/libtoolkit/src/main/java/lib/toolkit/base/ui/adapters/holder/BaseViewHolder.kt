package lib.toolkit.base.ui.adapters.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class BaseViewHolder<T: ViewType>(val view: View): RecyclerView.ViewHolder(view) {

    open fun bind(item: T) = Unit

    open fun bind(item: T, payloads: List<Any>) = Unit

}