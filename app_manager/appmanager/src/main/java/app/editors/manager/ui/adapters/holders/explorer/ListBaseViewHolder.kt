package app.editors.manager.ui.adapters.holders.explorer

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer

abstract class ListBaseViewHolder<T : Item>(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<T>(view, adapter) {

    abstract val rootLayout: ConstraintLayout

    //    abstract val selectIcon: ImageView
    abstract val title: TextView
    abstract val subtitle: TextView
    abstract val contextButton: Button

    //    private val colorSelected: Int by lazy {
//        view.context.getColor(lib.toolkit.base.R.color.colorOutline)
//    }

    override fun bind(element: T) {
        initListeners()
        initSelecting(element.isSelected)
        title.text = element.title
        subtitle.text = getSubtitleText(element)
    }

    private fun initSelecting(isSelected: Boolean) {
//        selectIcon.isVisible = adapter.isSelectMode
//        if (isSelected) {
//            rootLayout.setCardBackgroundColor(colorSelected)
//            selectIcon.setImageResource(R.drawable.ic_select_checked)
//        } else {
//            rootLayout.setCardBackgroundColor(null)
//            selectIcon.setImageResource(R.drawable.ic_select_not_checked)
//        }
    }

    private fun initListeners() {
        contextButton.setOnClickListener {
            adapter.mOnItemContextListener?.onItemContextClick(layoutPosition, icon)
        }
        with(rootLayout) {
            setOnClickListener { adapter.mOnItemClickListener?.onItemClick(this, layoutPosition) }
            setOnLongClickListener { view ->
                adapter.mOnItemLongClickListener?.onItemLongClick(view, layoutPosition)
                return@setOnLongClickListener false
            }
        }
    }

    private fun getSubtitleText(element: T): String? {
        return StringUtils.getCloudItemInfo(
            context = adapter.context,
            item = element,
            userId = adapter.accountId,
            sortBy = adapter.preferenceTool.sortBy,
            isSectionMy = adapter.isSectionMy
        )
    }
}