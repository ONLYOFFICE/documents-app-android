package lib.toolkit.base.ui.dialogs.common.holders

import android.view.View
import lib.toolkit.base.ui.dialogs.common.CommonDialog


class InfoHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {


    override fun show() {
        super.show()
        mFrameLayout.visibility = View.GONE
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.INFO

    inner class Builder {

        fun setTag(value: String?): Builder {
            mTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            mTopTitle = value
            return this
        }

        fun setBottomTitle(value: String?): Builder {
            mBottomTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            mCancelTitle = value
            return this
        }

        fun show() {
            dialog.show(getType())
        }
    }

}