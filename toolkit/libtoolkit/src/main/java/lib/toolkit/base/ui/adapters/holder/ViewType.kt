package lib.toolkit.base.ui.adapters.holder

interface ViewType {
    val viewType:  Int
}

fun ViewType.position(list: List<ViewType>) = list.indexOf(this)