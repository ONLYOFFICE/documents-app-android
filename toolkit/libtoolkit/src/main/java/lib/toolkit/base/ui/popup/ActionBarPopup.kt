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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lib.toolkit.base.R
import lib.toolkit.base.databinding.ActionPopupItemListBinding
import lib.toolkit.base.databinding.ActionPopupLayoutBinding
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.adapters.factory.inflate

abstract class BasePopupItem(val title: Int, val withDivider: Boolean = false)

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
    private val margin = context.resources.getDimension(R.dimen.default_margin_medium).toInt()
    private val shape = ContextCompat.getDrawable(context, R.drawable.shape_action_bar_popup)
    private var viewBinding: ActionPopupLayoutBinding? = null

    protected open val popupAdapter: PopupAdapter<T> = PopupAdapter {
        clickListener(it)
        dismiss()
    }

    companion object {
        fun <T : BasePopupItem> List<T>.filter(excluded: List<T>): List<T> {
            return filter { !excluded.contains(it) }
        }
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewBinding = ActionPopupLayoutBinding.inflate(inflater).also { binding ->
            contentView = with(binding) {
                popupView.layoutManager = LinearLayoutManager(context)
                root
            }

            animationStyle = R.style.MainPopupAnimation
            isOutsideTouchable = true
            isClippingEnabled = true
            elevation = context.resources.getDimension(R.dimen.elevation_height_popup)
        }

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
        RecyclerView.Adapter<PopupItemViewHolder<T>>() {

        var items: List<T> = emptyList()
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopupItemViewHolder<T> {
            return PopupItemViewHolder(parent.inflate(R.layout.action_popup_item_list))
        }

        override fun onBindViewHolder(holder: PopupItemViewHolder<T>, position: Int) {
            (holder as? PopupItemViewHolder<T>)?.let {
                val item = items[position]
                holder.bind(item, position == itemCount - 1)
                holder.itemView.setOnClickListener {
                    clickListener(item)
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }

    protected open class PopupItemViewHolder<T : BasePopupItem>(private val view: View) :
        RecyclerView.ViewHolder(view) {
        open fun bind(item: T, last: Boolean) {
            with(ActionPopupItemListBinding.bind(view)) {
                title.text = view.context.getText(item.title)
                divider.isVisible = item.withDivider && !last
                order.isVisible = false
            }
        }

    }
}