package lib.toolkit.base.ui.dialogs.common.holders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.annotation.ColorRes
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

    private lateinit var rootLayout: FrameLayout
    private lateinit var progressBarView: ProgressBar

    @ColorRes
    private var progressColor = 0
    private var progressType: ProgressType =
        ProgressType.HORIZONTAL

    @SuppressLint("ResourceType")
    override fun init() {
        super.init()
        dialog.dialog?.setCanceledOnTouchOutside(false)
        dialog.view?.apply {
            rootLayout = findViewById(R.id.dialogCommonWaitingLayout)
            progressBarView = when (progressType) {
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
        rootLayout.visibility = View.VISIBLE
        progressBarView.visibility = View.VISIBLE

        if (progressColor > 0) {
            UiUtils.setProgressBarColorDrawable(progressBarView, progressColor)
        }
    }

    override fun hide() {
        super.hide()
        rootLayout.visibility = View.GONE
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let {
            it.putInt(TAG_PROGRESS_COLOR, progressColor)
            it.putSerializable(TAG_PROGRESS_TYPE, progressType)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let {
            progressColor = it.getInt(TAG_PROGRESS_COLOR)
            progressType = it.getSerializable(TAG_PROGRESS_TYPE) as ProgressType
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.WAITING

    inner class Builder {

        fun setTag(value: String?): Builder {
            holderTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            topTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            cancelTitle = value
            return this
        }

        fun setProgressColor(@ColorRes colorRes: Int): Builder {
            progressColor = colorRes
            return this
        }

        fun setProgressType(progressType: ProgressType): Builder {
            this@WaitingHolder.progressType = progressType
            return this
        }

        fun setTextColor(textColor: Int): Builder {
            this@WaitingHolder.textColor = textColor
            return this
        }

        fun setTopTitleGravity(gravity: Int): Builder {
            topTitleGravity = gravity
            return this
        }

        fun setIsBackPress(isBack: Boolean = true): Builder {
            isBackPress = isBack
            return this
        }

        fun show() {
            dialog.show(CommonDialog.Dialogs.WAITING)
        }
    }

}