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

    private lateinit var mLayout: ConstraintLayout
    private lateinit var mProgressBarView: ProgressBar
    private lateinit var mProgressTextView: AppCompatTextView

    @ColorRes
    private var mProgressColor = 0
    private var mProgress = 0
    private var mMaxProgress = 0

    @SuppressLint("ResourceType")
    override fun init() {
        super.init()
        dialog.isCancelable = false
        dialog.view?.apply {
            mLayout = findViewById(R.id.dialogCommonProgressLayout)
            mProgressBarView = findViewById(R.id.dialogCommonProgressProgressBar)
            mProgressTextView = findViewById(R.id.dialogCommonProgressText)
        }
    }

    @SuppressLint("ResourceType")
    override fun show() {
        super.show()
        mLayout.visibility = View.VISIBLE
        setViewProgress()

        if (mProgressColor > 0) {
            UiUtils.setProgressBarColorTint(mProgressBarView, mProgressColor)
        }
    }

    override fun hide() {
        super.hide()
        mLayout.visibility = View.GONE
    }

    override fun save(state: Bundle) {
        super.save(state)
        state?.let {
            it.putInt(TAG_PROGRESS_COLOR, mProgressColor)
            it.putInt(TAG_PROGRESS_VALUE, mProgress)
            it.putInt(TAG_MAX_PROGRESS_VALUE, mMaxProgress)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state?.let {
            mProgressColor = it.getInt(TAG_PROGRESS_COLOR)
            mProgress = it.getInt(TAG_PROGRESS_VALUE)
            mMaxProgress = it.getInt(TAG_MAX_PROGRESS_VALUE)
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.PROGRESS

    private fun setViewProgress() {
        if (mProgressBarView != null && mProgressTextView != null) {
            mProgressBarView.max = mMaxProgress
            mProgressBarView.progress = mProgress
            mProgressTextView.text = "$mProgress %"
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
            mProgressColor = colorRes
            return this
        }

        fun setProgress(maxProgress: Int, progress: Int): Builder {
            mProgress = progress
            mMaxProgress = maxProgress
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