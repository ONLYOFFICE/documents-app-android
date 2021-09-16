package lib.toolkit.base.ui.adapters.holder

import android.view.View
import android.widget.TextView
import lib.toolkit.base.R
import lib.toolkit.base.ui.adapters.EmptyListItem

class EmptyListViewHolder(view: View): BaseViewHolder<EmptyListItem>(view) {

    private val title = view.findViewById<TextView>(R.id.text)

    override fun bind(item: EmptyListItem) {
        title.text = item.text
    }

}