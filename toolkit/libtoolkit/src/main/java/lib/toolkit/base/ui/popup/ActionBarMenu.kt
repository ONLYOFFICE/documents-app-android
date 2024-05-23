package lib.toolkit.base.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lib.toolkit.base.R
import lib.toolkit.base.databinding.ActionPopupLayoutBinding
import lib.toolkit.base.managers.utils.UiUtils

interface IActionMenuItem {

    val title: Int
}

interface IActionMenuAdapter<out T : IActionMenuItem> {

    val adapter: RecyclerView.Adapter<*>
    var onDismiss: () -> Unit
    fun setItems(items: List<@UnsafeVariance T>)
}

@SuppressLint("InflateParams")
class ActionBarMenu(
    context: Context,
    private val adapter: IActionMenuAdapter<IActionMenuItem>,
    private val items: List<IActionMenuItem> = listOf(),
) : PopupWindow(context) {

    private val margin = context.resources.getDimension(R.dimen.default_margin_medium).toInt()
    private val shape = ContextCompat.getDrawable(context, R.drawable.shape_action_bar_popup)
    private var viewBinding: ActionPopupLayoutBinding? = null

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
        viewBinding?.popupView?.adapter = adapter.adapter.also {
            adapter.setItems(items)
            adapter.onDismiss = ::dismiss
        }
        setBackgroundDrawable(shape)
        showAtLocation(view, horizontalGravity or Gravity.TOP, margin, margin + (insets ?: 0))
    }

    override fun dismiss() {
        viewBinding = null
        super.dismiss()
    }
}