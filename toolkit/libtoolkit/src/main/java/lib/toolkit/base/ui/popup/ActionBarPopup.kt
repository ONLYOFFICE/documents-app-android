package lib.toolkit.base.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lib.toolkit.base.R
import lib.toolkit.base.databinding.ActionPopupItemArrowBinding
import lib.toolkit.base.databinding.ActionPopupItemListBinding
import lib.toolkit.base.databinding.ActionPopupItemTopbarBinding
import lib.toolkit.base.databinding.ActionPopupLayoutBinding
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.adapters.factory.inflate

abstract class BasePopupItem(
    open val title: Int,
    val withDivider: Boolean = false,
    val items: List<BasePopupItem> = listOf()
) {

    data class TobBar(override val title: Int) : BasePopupItem(title)
}

@Suppress("UNCHECKED_CAST")
@SuppressLint("InflateParams")
abstract class ActionBarPopup<T : BasePopupItem>(
    context: Context,
    private val items: List<T> = listOf(),
    clickListener: (T) -> Unit
) : PopupWindow(
    null,
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT,
    true
) {

    companion object {

        const val POPUP_ITEM_NONE = 0
        private const val POPUP_ITEM_WITH_ITEMS = 1
        private const val POPUP_ITEM_TOP_BAR = 2

        fun <T : BasePopupItem> List<T>.filter(excluded: List<T>): List<T> {
            return filter { !excluded.contains(it) }
        }
    }

    private val margin = context.resources.getDimension(R.dimen.default_margin_medium).toInt()
    private val shape = ContextCompat.getDrawable(context, R.drawable.shape_action_bar_popup)
    private var viewBinding: ActionPopupLayoutBinding? = null

    protected open val popupAdapter: PopupAdapter<T> = PopupAdapter {
        clickListener(it)
        dismiss()
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewBinding = ActionPopupLayoutBinding.inflate(inflater).also { binding ->
            contentView = binding.root
            binding.popupView.layoutManager = LinearLayoutManager(context)
            binding.popupView.itemAnimator = DefaultItemAnimator()
        }
        animationStyle = R.style.MainPopupAnimation
        isOutsideTouchable = true
        isClippingEnabled = true
        elevation = context.resources.getDimension(R.dimen.elevation_height_popup)
    }

    fun show(view: View) {
        val horizontalGravity = if (UiUtils.isRTL()) Gravity.START else Gravity.END
        val insets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            view.rootWindowInsets.displayCutout?.safeInsetTop else 0
        viewBinding?.popupView?.adapter = popupAdapter.also { it.items = items }
        setBackgroundDrawable(shape)
        showAtLocation(view, horizontalGravity or Gravity.TOP, margin, margin + (insets ?: 0))
    }

    override fun dismiss() {
        viewBinding = null
        super.dismiss()
    }

    protected open class PopupAdapter<T : BasePopupItem>(private val clickListener: (T) -> Unit) :
        RecyclerView.Adapter<PopupItemViewHolder<out T>>() {

        var items: List<T> = emptyList()
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        private var rootItems: List<T> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopupItemViewHolder<out T> {
            return when (viewType) {
                POPUP_ITEM_WITH_ITEMS -> PopupArrowItemViewHolder(parent.inflate(R.layout.action_popup_item_arrow))
                POPUP_ITEM_TOP_BAR -> PopupItemTopBarViewHolder(
                    view = parent.inflate(R.layout.action_popup_item_topbar),
                    onBack = { items = rootItems }
                )
                else -> PopupItemViewHolder(parent.inflate(R.layout.action_popup_item_list))
            }
        }

        override fun onBindViewHolder(holder: PopupItemViewHolder<out T>, position: Int) {
            (holder as? PopupItemViewHolder<T>)?.let {
                val item = items[position]
                holder.bind(item, position == itemCount - 1)
                holder.itemView.setOnClickListener {
                    if (item !is BasePopupItem.TobBar && item.items.isEmpty()) {
                        clickListener.invoke(item)
                    } else if (item.items.isNotEmpty()) {
                        rootItems = items
                        items = listOf(BasePopupItem.TobBar(item.title))
                            .plus(item.items) as List<T>
                    }
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when {
                items[position].items.isNotEmpty() -> POPUP_ITEM_WITH_ITEMS
                items[position] is BasePopupItem.TobBar -> POPUP_ITEM_TOP_BAR
                else -> POPUP_ITEM_NONE
            }
        }

        override fun getItemCount(): Int = items.size
    }

    protected open class PopupItemViewHolder<T : BasePopupItem>(
        private val view: View
    ) : RecyclerView.ViewHolder(view) {

        open fun bind(item: T, last: Boolean) {
            with(ActionPopupItemListBinding.bind(view)) {
                title.text = view.context.getText(item.title)
                divider.isVisible = item.withDivider && !last
                order.isVisible = false
            }
        }
    }

    protected open class PopupArrowItemViewHolder<T : BasePopupItem>(
        private val view: View
    ) : PopupItemViewHolder<T>(view) {

        override fun bind(item: T, last: Boolean) {
            with(ActionPopupItemArrowBinding.bind(view)) {
                title.text = view.context.getText(item.title)
                divider.isVisible = item.withDivider && !last
            }
        }
    }

    protected open class PopupItemTopBarViewHolder<T : BasePopupItem>(
        private val view: View,
        private val onBack: () -> Unit,
    ) : PopupItemViewHolder<T>(view) {

        override fun bind(item: T, last: Boolean) {
            with(ActionPopupItemTopbarBinding.bind(view)) {
                title.text = view.context.getText(item.title)
                backButton.setOnClickListener { onBack.invoke() }
            }
        }
    }
}