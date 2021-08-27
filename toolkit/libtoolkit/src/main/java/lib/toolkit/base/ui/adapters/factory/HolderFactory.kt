package lib.toolkit.base.ui.adapters.factory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

abstract class HolderFactory : (ViewGroup, Int) -> BaseViewHolder<ViewType> {

    abstract fun createViewHolder(view: View, type: Int): BaseViewHolder<*>?

    @Suppress("UNCHECKED_CAST")
    final override fun invoke(viewGroup: ViewGroup, type: Int): BaseViewHolder<ViewType> {
        val view: View = viewGroup.inflate(type)
        return checkNotNull(createViewHolder(view, type)) {
            throw Error("Resources not found")
        } as BaseViewHolder<ViewType>
    }

}

@Suppress("UNCHECKED_CAST")
fun <T : View> View.inflate(res: Int, root: ViewGroup? = this as? ViewGroup): T {
    return LayoutInflater.from(context).inflate(res, root, false) as T
}