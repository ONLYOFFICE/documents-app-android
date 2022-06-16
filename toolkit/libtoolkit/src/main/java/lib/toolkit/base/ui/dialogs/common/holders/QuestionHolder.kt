package lib.toolkit.base.ui.dialogs.common.holders

import android.content.res.ColorStateList
import android.view.View
import lib.toolkit.base.R
import lib.toolkit.base.ui.dialogs.common.CommonDialog


class QuestionHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    private val tintedAcceptTags = arrayOf("TAG_REMOVE", "TAG_LOGOUT")

    override fun show() {
        super.show()
        dialog.dialog!!.setCanceledOnTouchOutside(false)
        dialog.view?.post {
            mFrameLayout.visibility = View.GONE
            acceptView.isEnabled = true
            mCancelView.isEnabled = true
            if (tintedAcceptTags.contains(holderTag)) {
                val color = dialog.requireContext().getColor(R.color.colorLightRed)
                acceptView.setTextColor(color)
                acceptView.rippleColor = ColorStateList.valueOf(color).withAlpha(30)
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

        fun show() {
            dialog.show(getType())
        }
    }

}