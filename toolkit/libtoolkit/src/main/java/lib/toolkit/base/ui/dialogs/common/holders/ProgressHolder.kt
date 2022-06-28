package lib.toolkit.base.ui.dialogs.common.holders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class ProgressHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private const val TAG_PROGRESS_COLOR = "TAG_PROGRESS_COLOR"
        private const val TAG_PROGRESS_VALUE = "TAG_PROGRESS_VALUE"
        private const val TAG_MAX_PROGRESS_VALUE = "TAG_MAX_PROGRESS_VALUE"
    }

    private var layout: ConstraintLayout? = null
    private var progressBarView: ProgressBar? = null
    private var progressTextView: AppCompatTextView? = null

    @ColorRes
    private var progressColor = 0
    private var progress = 0
    private var maxProgress = 0

    @SuppressLint("ResourceType")
    override fun init() {
        super.init()
        dialog.isCancelable = false
        dialog.view?.apply {
            layout = findViewById(R.id.dialogCommonProgressLayout)
            progressBarView = findViewById(R.id.dialogCommonProgressProgressBar)
            progressTextView = findViewById(R.id.dialogCommonProgressText)
        }
    }

    @SuppressLint("ResourceType")
    override fun show() {
        super.show()
        layout?.visibility = View.VISIBLE
        setViewProgress()

        if (progressColor > 0) {
            UiUtils.setProgressBarColorTint(checkNotNull(progressBarView), progressColor)
        }
    }

    override fun hide() {
        super.hide()
        layout?.visibility = View.GONE
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let {
            it.putInt(TAG_PROGRESS_COLOR, progressColor)
            it.putInt(TAG_PROGRESS_VALUE, progress)
            it.putInt(TAG_MAX_PROGRESS_VALUE, maxProgress)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let {
            progressColor = it.getInt(TAG_PROGRESS_COLOR)
            progress = it.getInt(TAG_PROGRESS_VALUE)
            maxProgress = it.getInt(TAG_MAX_PROGRESS_VALUE)
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.PROGRESS

    private fun setViewProgress() {
        if (progressBarView != null && progressTextView != null) {
            progressBarView?.max = maxProgress
            progressBarView?.progress = progress
            progressTextView?.text = "$progress %"
        }
    }

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

        fun setProgress(maxProgress: Int, progress: Int): Builder {
            this@ProgressHolder.progress = progress
            this@ProgressHolder.maxProgress = maxProgress
            return this
        }

        fun show() {
            dialog.show(getType())
        }

        fun update(maxProgress: Int, progress: Int) {
            setProgress(maxProgress, progress)
            setViewProgress()
        }
    }

}