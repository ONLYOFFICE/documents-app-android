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
            mAcceptView.isEnabled = true
            mCancelView.isEnabled = true
            if (tintedAcceptTags.contains(mTag)) {
                val color = dialog.requireContext().getColor(R.color.colorLightRed)
                mAcceptView.setTextColor(color)
                mAcceptView.rippleColor = ColorStateList.valueOf(color).withAlpha(30)
            }
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.QUESTION

    inner class Builder {

        fun setTag(value: String?): Builder {
            mTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            mTopTitle = value
            return this
        }

        fun setQuestion(value: String?): Builder {
            mBottomTitle = value
            return this
        }

        fun setAcceptTitle(value: String?): Builder {
            mAcceptTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            mCancelTitle = value
            return this
        }

        fun setIsBackPress(isBackPress: Boolean = true): Builder {
            mIsBackPress = isBackPress
            return this
        }

        fun show() {
            dialog.show(getType())
        }
    }

}