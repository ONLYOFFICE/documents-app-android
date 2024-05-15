package lib.toolkit.base.ui.dialogs.common.holders

import android.view.View
import androidx.fragment.app.FragmentManager
import lib.toolkit.base.R
import lib.toolkit.base.ui.dialogs.common.CommonDialog


class QuestionHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    private var errorTint: Boolean = false

    override fun show() {
        super.show()
        dialog.dialog!!.setCanceledOnTouchOutside(false)
        dialog.view?.post {
            frameLayout.visibility = View.GONE
            acceptView.isEnabled = true
            cancelView.isEnabled = true
            if (errorTint) {
                val color = dialog.requireContext().getColor(R.color.colorError)
                acceptView.setTextColor(color)
            }
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.QUESTION

    inner class Builder {

        fun setTag(value: String?): Builder {
            holderTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            topTitle = value
            return this
        }

        fun setQuestion(value: String?): Builder {
            bottomTitle = value
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

        fun setIsBackPress(isBack: Boolean = true): Builder {
            isBackPress = isBack
            return this
        }

        fun setAcceptErrorTint(errorTint: Boolean): Builder {
            this@QuestionHolder.errorTint = errorTint
            return this
        }

        fun show(fragmentManager: FragmentManager) {
            dialog.show(fragmentManager, getType())
        }
    }

}