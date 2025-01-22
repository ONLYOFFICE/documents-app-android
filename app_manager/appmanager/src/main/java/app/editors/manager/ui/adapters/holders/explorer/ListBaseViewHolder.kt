package app.editors.manager.ui.adapters.holders.explorer

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer

abstract class ListBaseViewHolder<T : Item>(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<T>(view, adapter) {

    abstract val rootLayout: ConstraintLayout

    abstract val title: TextView
    abstract val subtitle: TextView
    abstract val contextButton: Button

    override fun bind(element: T) {
        initListeners()
        setElementClickable(element)
        initSelecting(element)
        title.text = element.title
        subtitle.text = getSubtitleText(element)
    }
    override fun initSelecting(element: T): Boolean {
        val isSelected = super.initSelecting(element)
        contextButton.isVisible = !adapter.isSelectMode
        return isSelected
    }

    private fun initListeners() {
        if (adapter.pickerMode is PickerMode.Ordering) {
            return
        }

        contextButton.setOnClickListener {
            adapter.mOnItemContextListener?.onItemContextClick(layoutPosition, icon)
        }
        with(rootLayout) {
            setOnClickListener {
                if (adapter.isTrash && !adapter.isSelectMode) {
                    adapter.mOnItemContextListener?.onItemContextClick(layoutPosition, icon)
                } else {
                    adapter.mOnItemClickListener?.onItemClick(this, layoutPosition)
                }
            }
            setOnLongClickListener { view ->
                adapter.mOnItemLongClickListener?.onItemLongClick(view, layoutPosition)
                return@setOnLongClickListener false
            }
        }
    }

    protected open fun getSubtitleText(element: T): String? {
        return StringUtils.getCloudItemInfo(
            context = adapter.context,
            item = element,
            state = adapter
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    protected fun initOrderingMode(dragIcon: View, buttonLayout: View) {
        contextButton.isVisible = false
        dragIcon.isVisible = true
        buttonLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                adapter.onDragStartListener.invoke(this)
            }
            true
        }
    }
}