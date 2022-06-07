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
            holderTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            topTitle = value
            return this
        }

        fun setBottomTitle(value: String?): Builder {
            bottomTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            cancelTitle = value
            return this
        }

        fun show() {
            dialog.show(getType())
        }
    }

}