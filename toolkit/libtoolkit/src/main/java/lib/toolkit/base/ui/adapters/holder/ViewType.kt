package lib.toolkit.base.ui.adapters.holder

interface ViewType {
    abstract val viewType:  Int
    abstract fun getItemId(): String
    abstract fun getItemName(): String
}

fun ViewType.position(list: List<ViewType>) = list.indexOf(this)