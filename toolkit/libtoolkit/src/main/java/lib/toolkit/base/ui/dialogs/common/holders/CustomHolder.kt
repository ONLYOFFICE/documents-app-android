package lib.toolkit.base.ui.dialogs.common.holders

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import lib.toolkit.base.R
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import java.lang.ref.WeakReference

class CustomHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private var weakView: WeakReference<View>? = null
        private var weakListener: WeakReference<() -> Unit>? = null
    }

    override fun show() {
        super.show()
        weakView?.get()?.let { childView ->
            val layout = frameLayout.findViewById<FrameLayout>(R.id.dialogCustomLayout)
            layout.addView(childView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layout.isVisible = true
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.CUSTOM

    override fun save(state: Bundle) {
        super.save(state)
        frameLayout.findViewById<FrameLayout>(R.id.dialogCustomLayout).removeAllViews()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialogCommonAcceptButton -> {
                weakListener?.get()?.invoke()
                dialog.dismiss()
            }
            R.id.dialogCommonCancelButton -> {
                dialog.dismiss()
            }
        }
    }

    inner class Builder {

        fun setTopTitle(value: String?): Builder {
            topTitle = value
            return this
        }

        fun setAcceptTitle(value: String?): Builder {
            acceptTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            cancelTitle = value
            return this
        }

        fun setView(view: View): Builder {
            weakView = WeakReference<View>(view)
            return this
        }

        fun setAcceptClickListener(listener: () -> Unit): Builder {
            weakListener = WeakReference(listener)
            return this
        }

        fun show() {
            dialog.show(getType())
        }
    }
}