package app.editors.manager.managers.tools

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import lib.toolkit.base.R
import lib.toolkit.base.databinding.ActionPopupItemListBinding
import lib.toolkit.base.databinding.ActionPopupItemTopbarBinding
import lib.toolkit.base.ui.adapters.factory.inflate
import lib.toolkit.base.ui.popup.IActionMenuAdapter

class ActionMenuAdapter(
    private val onClick: (ActionMenuItem) -> Unit,
    override var onDismiss: () -> Unit = {},
) : RecyclerView.Adapter<ActionMenuAdapter.PopupItemViewHolder>(), IActionMenuAdapter<ActionMenuItem> {

    companion object {

        private const val POPUP_ITEM_NONE = 0
        private const val POPUP_ITEM_SORT = 1
        private const val POPUP_ITEM_ARROW = 2
        private const val POPUP_ITEM_TOP_BAR = 3
        private const val POPUP_ITEM_DIVIDER = 4
    }

    private var items: MutableList<ActionMenuItem> = mutableListOf()
    private var rootItems: List<ActionMenuItem> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    override fun setItems(items: List<ActionMenuItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopupItemViewHolder {
        return when (viewType) {
            POPUP_ITEM_NONE -> PopupItemViewHolder(parent.inflate(R.layout.action_popup_item_list))
            POPUP_ITEM_SORT -> PopupItemSortViewHolder(parent.inflate(R.layout.action_popup_item_list))
            POPUP_ITEM_ARROW -> PopupItemViewHolder(parent.inflate(R.layout.action_popup_item_arrow))
            POPUP_ITEM_DIVIDER -> PopupItemViewHolder(parent.inflate(R.layout.action_popup_item_divider))
            POPUP_ITEM_TOP_BAR -> PopupItemTopBarViewHolder(
                view = parent.inflate(R.layout.action_popup_item_topbar),
                onBack = { setItems(rootItems) }
            )
            else -> error("viewType not found")
        }
    }

    override fun onBindViewHolder(holder: PopupItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        if (item !is ActionMenuItem.TopBar) {
            holder.itemView.setOnClickListener {
                when (item) {
                    is ActionMenuItem.Arrow -> {
                        check(item.items.isNotEmpty()) { "items must not be empty" }
                        rootItems = ArrayList(items)
                        setItems(item.items.toMutableList().apply { add(0, ActionMenuItem.TopBar(item.title)) })
                    }
                    else -> {
                        onClick.invoke(item)
                        onDismiss.invoke()
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item) {
            is ActionMenuItem.None -> POPUP_ITEM_NONE
            is ActionMenuItem.Arrow -> POPUP_ITEM_ARROW
            is ActionMenuItem.Sort -> POPUP_ITEM_SORT
            is ActionMenuItem.TopBar -> POPUP_ITEM_TOP_BAR
            is ActionMenuItem.Divider -> POPUP_ITEM_DIVIDER
        }
    }

    override fun getItemCount(): Int = items.size

    override val adapter: RecyclerView.Adapter<*>
        get() = this

    open class PopupItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        open fun bind(item: ActionMenuItem) {
            when (item) {
                is ActionMenuItem.Arrow, is ActionMenuItem.None -> {
                    view.findViewById<TextView>(R.id.title)?.text = view.context.getText(item.title)
                }
                else -> Unit
            }
        }
    }

    class PopupItemTopBarViewHolder(
        private val view: View,
        private val onBack: () -> Unit,
    ) : PopupItemViewHolder(view) {

        override fun bind(item: ActionMenuItem) {
            with(ActionPopupItemTopbarBinding.bind(view)) {
                title.text = view.context.getText(item.title)
                backButton.setOnClickListener { onBack.invoke() }
            }
        }
    }

    class PopupItemSortViewHolder(private val view: View) : PopupItemViewHolder(view) {

        override fun bind(item: ActionMenuItem) {
            if (item is ActionMenuItem.Sort) {
                with(ActionPopupItemListBinding.bind(view)) {
                    title.text = view.context.getText(item.title)
                    order.isVisible = item.active
                    order.isSelected = item.asc
                }
            }
        }
    }
}