package lib.toolkit.base.ui.dialogs.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isInvisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity


abstract class BaseDialog : DialogFragment(), DialogInterface.OnShowListener,
    BaseInterfaceDialog {


    companion object {
        const val WIDTH_TABLET_PERCENT = 0.7f
        const val WIDTH_PHONE_PERCENT = 0.9f
        protected const val MARGIN_OFFSET = 4
    }

    protected lateinit var baseActivity: BaseActivity
    protected var closeHandler: Handler = Handler(Looper.getMainLooper())
    protected var anchorView: View? = null
    protected var marginOffset: Int = 0
    protected var isPercentWidth: Boolean = true


    protected val anchorSize: Rect?
        get() = anchorView?.let {
            Rect().apply {
                it.getGlobalVisibleRect(this)
            }
        }

    protected val dialogSize: Rect?
        get() = dialog?.window?.attributes?.let { layoutParams ->
            viewSize?.let { viewSize ->
                Rect(layoutParams.x, layoutParams.y, layoutParams.x + viewSize.width, layoutParams.y + viewSize.height)
            }
        }

    protected val viewSize: Size?
        get() = dialog?.let {
            Size(it.window!!.decorView.width, it.window!!.decorView.height)
        }

    protected val screenSize: Size
        get() {
            return if (activity != null) {
                Size(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            } else {
                Size(0, 0)
            }
        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            baseActivity = context as BaseActivity
        } catch (e: ClassCastException) {
            throw RuntimeException("${BaseDialog::class.java.simpleName} - must implement - ${BaseActivity::class.java.simpleName}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marginOffset = (MARGIN_OFFSET * resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFragmentIsEmpty()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return getCustomDialog().apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.decorView?.setPadding(0, 0, 0, 0)
            setCanceledOnTouchOutside(true)
            setOnShowListener(this@BaseDialog)
            setVisibleView(window?.decorView)
        }
    }

    private fun setVisibleView(decorView: View?) {
        if (anchorView != null && isTablet() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView?.isInvisible = true
        }
    }

    //TODO check do not crash
    override fun onBackPressed(): Boolean {
        if (requireActivity().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && isAdded) {
            dismiss()
        } else {
            dismissAllowingStateLoss()
        }
        return false
    }

    override fun onDialogAdded() {

    }

    override fun onShowContentFragment() {

    }

    override fun onShow(dialogInterface: DialogInterface) {
        if (anchorView != null) {
            setDialogAnchorPosition()
        } else {
            setLayout()
        }
    }

    protected fun getCustomDialog(): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                this@BaseDialog.onBackPressed()
            }
        }
    }

    protected open fun setLayout() {
        val sizes = screenSize
        val size = Math.min(sizes.width, sizes.height)
        if (activity != null && isAdded) {
            val width: Int = (size * if (UiUtils.isTablet(requireActivity())) {
                WIDTH_TABLET_PERCENT
            } else {
                WIDTH_PHONE_PERCENT
            }).toInt()

            dialog?.window?.apply {
                setGravity(Gravity.CENTER)
                setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        closeHandler.removeCallbacksAndMessages(null)
        manager.findFragmentByTag(tag)?.let { dialog ->
            try {
                if (dialog.isAdded) {
                    onDialogAdded()
                } else {
                    if (requireActivity().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        super.show(manager.beginTransaction().remove(dialog), tag)
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                //TODO already added?? Возможно интсанс фрагмента остаётся в стеке.
            }
        } ?: run {
            super.show(manager, tag)
        }
    }

    protected fun setDialogAnchorPosition() {
        if (anchorView != null && dialog != null) {
            val offset = Point(marginOffset, marginOffset)
            val restrict = UiUtils.getWindowVisibleRect(requireActivity().window.decorView)
            val position = UiUtils.getOverlapViewRect(anchorSize!!, dialogSize!!, restrict, offset)

            dialog?.window?.let { window ->
                window.setGravity(Gravity.START or Gravity.LEFT or Gravity.TOP)
                window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                window.attributes = dialog!!.window!!.attributes.apply {
                    x = position.left
                    y = position.top - UiUtils.getStatusBarHeightMeasure(requireActivity())
                }
                if (window.decorView.isInvisible) {
                    window.decorView.isInvisible = false
                }
            }
        }
    }

    fun setAnchor(view: View?) {
        anchorView = view
    }

    protected fun showKeyboard(editText: AppCompatEditText) {
        KeyboardUtils.showKeyboard(editText)
    }

    protected fun hideKeyboard(editText: AppCompatEditText) {
        KeyboardUtils.hideKeyboard(editText)
    }

    protected fun isTablet(): Boolean {
        return UiUtils.isTablet(requireContext())
    }

    protected fun isLandscape(): Boolean {
        return UiUtils.isLandscape(requireContext())
    }

    protected fun showFragmentIsEmpty() {
        if (childFragmentManager.fragments.isEmpty()) {
            onShowContentFragment()
        }
    }

}
