package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer

abstract class GridBaseViewHolder<T : Item>(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<T>(view, adapter) {

    abstract val rootLayout: CardView
    abstract val title: TextView
    abstract val subtitle: TextView

    private val colorRootLayoutSelected: Int by lazy {
        view.context.getColor(lib.toolkit.base.R.color.colorOutline)
    }

    override fun bind(element: T) {
        initListeners()
        initSelecting(element)
        setElementClickable(element)
        title.text = element.title
        subtitle.text = getSubtitleText(element)
    }

    override fun initSelecting(element: T): Boolean {
        val isSelected = super.initSelecting(element)
        if (isSelected) {
            rootLayout.setCardBackgroundColor(colorRootLayoutSelected)
        } else {
            rootLayout.setCardBackgroundColor(null)
        }
        return isSelected
    }

    private fun initListeners() {
        if (adapter.pickerMode is PickerMode.Ordering) {
            return
        }

        with(rootLayout) {
            setOnClickListener {
                if (adapter.isTrash && !adapter.isSelectMode) {
                    adapter.mOnItemContextListener?.onItemContextClick(layoutPosition, icon)
                } else {
                    adapter.mOnItemClickListener?.onItemClick(this, layoutPosition)
                }
            }
            setOnLongClickListener {
                if (adapter.pickerMode != PickerMode.Folders) {
                    adapter.mOnItemContextListener?.onItemContextClick(layoutPosition, icon)
                }
                return@setOnLongClickListener true
            }
        }
    }

}