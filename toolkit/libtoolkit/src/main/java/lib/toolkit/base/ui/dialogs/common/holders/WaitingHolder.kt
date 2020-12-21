package lib.toolkit.base.ui.dialogs.common.holders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class WaitingHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private const val TAG_PROGRESS_COLOR = "TAG_PROGRESS_COLOR"
        private const val TAG_PROGRESS_TYPE = "TAG_PROGRESS_TYPE"
    }

    enum class ProgressType {
        CIRCLE, HORIZONTAL
    }

    private lateinit var mLayout: ConstraintLayout
    private lateinit var mProgressBarView: ProgressBar

    @ColorRes
    private var mProgressColor = 0
    private var mProgressType: ProgressType =
        ProgressType.HORIZONTAL

    @SuppressLint("ResourceType")
    override fun init() {
        super.init()
        dialog.dialog?.setCanceledOnTouchOutside(false)
        dialog.view?.apply {
            mLayout = findViewById(R.id.dialogCommonWaitingLayout)
            mProgressBarView = when (mProgressType) {
                ProgressType.CIRCLE -> {
                    findViewById(R.id.dialogCommonWaitingProgressBarCircle)
                }

                ProgressType.HORIZONTAL -> {
                    findViewById(R.id.dialogCommonWaitingProgressBar)
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    override fun show() {
        super.show()
        mLayout.visibility = View.VISIBLE
        mProgressBarView.visibility = View.VISIBLE

        if (mProgressColor > 0) {
            UiUtils.setProgressBarColorDrawable(mProgressBarView, mProgressColor)
        }
    }

    override fun hide() {
        super.hide()
        mLayout.visibility = View.GONE
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let {
            it.putInt(TAG_PROGRESS_COLOR, mProgressColor)
            it.putSerializable(TAG_PROGRESS_TYPE, mProgressType)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state?.let {
            mProgressColor = it.getInt(TAG_PROGRESS_COLOR)
            mProgressType = it.getSerializable(TAG_PROGRESS_TYPE) as ProgressType
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.WAITING

    inner class Builder {

        fun setTag(value: String?): Builder {
            mTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            mTopTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            mCancelTitle = value
            return this
        }

        fun setProgressColor(@ColorRes colorRes: Int): Builder {
            mProgressColor = colorRes
            return this
        }

        fun setProgressType(progressType: ProgressType): Builder {
            mProgressType = progressType
            return this
        }

        fun setTextColor(textColor: Int): Builder {
            mTextColor = textColor
            return this
        }

        fun setTopTitleGravity(gravity: Int): Builder {
            mTopTitleGravity = gravity
            return this
        }

        fun setIsBackPress(isBackPress: Boolean = true): Builder {
            mIsBackPress = isBackPress
            return this
        }

        fun show() {
            dialog.show(CommonDialog.Dialogs.WAITING)
        }
    }

}