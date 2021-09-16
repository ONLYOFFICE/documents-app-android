package lib.toolkit.base.ui.adapters

import lib.toolkit.base.R
import lib.toolkit.base.ui.adapters.holder.ViewType

class EmptyListItem(val text: String) : ViewType {

    override val viewType: Int
        get() = R.layout.empty_list_item

}