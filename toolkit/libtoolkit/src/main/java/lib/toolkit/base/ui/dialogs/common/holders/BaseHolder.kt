package lib.toolkit.base.ui.dialogs.common.holders

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import lib.toolkit.base.R
import lib.toolkit.base.ui.dialogs.common.CommonDialog


abstract class BaseHolder(private val dialog: CommonDialog) : CommonDialog.ViewHolder,
        View.OnClickListener {

    companion object {
        protected const val TAG_USER_TAG = "TAG_USER_TAG"
        protected const val TAG_TOP_TITLE = "TAG_TOP_TITLE"
        protected const val TAG_BOTTOM_TITLE = "TAG_BOTTOM_TITLE"
        protected const val TAG_ACCEPT_TITLE = "TAG_ACCEPT_TITLE"
        protected const val TAG_CANCEL_TITLE = "TAG_CANCEL_TITLE"
    }

    protected var mOnClickListener: CommonDialog.OnClickListener? = null
    protected lateinit var mFrameLayout: FrameLayout
    protected lateinit var mTopTitleView: AppCompatTextView
    protected lateinit var mBottomTitleView: AppCompatTextView
    protected lateinit var mAcceptView: MaterialButton
    protected lateinit var mCancelView: MaterialButton

    protected var mTag: String? = null
    protected var mTopTitle: String? = null
    protected var mBottomTitle: String? = null
    protected var mAcceptTitle: String? = null
    protected var mCancelTitle: String? = null
    protected var mTextColor: Int = 0
    protected var mTopTitleGravity: Int = Gravity.START
    protected var mIsBackPress: Boolean = true

    protected val colorPrimary: Int
        get() {
            val typedValue = TypedValue()
            dialog.requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            return typedValue.data
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialogCommonAcceptButton -> {
                mOnClickListener?.onAcceptClick(getType(), getValue(), mTag)
            }
            R.id.dialogCommonCancelButton -> {
                mOnClickListener?.onCancelClick(getType(), mTag)
                hide()
            }
        }
    }

    override fun init() {
        dialog.isCancelable = true
        dialog.view?.apply {
            mFrameLayout = findViewById(R.id.dialogCommonFrameLayout)
            mTopTitleView = findViewById(R.id.dialogCommonTopTitleText)
            mBottomTitleView = findViewById(R.id.dialogCommonBottomTitleText)
            mAcceptView = findViewById(R.id.dialogCommonAcceptButton)
            mCancelView = findViewById(R.id.dialogCommonCancelButton)
        }
        setTint()
    }

    override fun show() {
        mFrameLayout.visibility = View.VISIBLE
        mAcceptView.setOnClickListener(this)
        mCancelView.setOnClickListener(this)

        if (mTopTitle.isNullOrBlank()) {
            mTopTitleView.visibility = View.GONE
        } else {
            mTopTitleView.visibility = View.VISIBLE
            mTopTitleView.text = mTopTitle
            mTopTitleView.gravity = mTopTitleGravity
        }

        if (mBottomTitle.isNullOrBlank()) {
            mBottomTitleView.visibility = View.GONE
        } else {
            mBottomTitleView.visibility = View.VISIBLE
            mBottomTitleView.text = mBottomTitle
        }

        if (mAcceptTitle.isNullOrBlank()) {
            mAcceptView.visibility = View.GONE
        } else {
            mAcceptView.visibility = View.VISIBLE
            mAcceptView.text = mAcceptTitle
        }

        if (mCancelTitle.isNullOrBlank()) {
            mCancelView.visibility = View.GONE
        } else {
            mCancelView.visibility = View.VISIBLE
            mCancelView.text = mCancelTitle
        }

        dialog.view?.let { TransitionManager.beginDelayedTransition(it as ViewGroup, Fade()) }
    }

    override fun hide() {
        mOnClickListener = null
    }

    override fun setClickListener(listener: CommonDialog.OnClickListener?) {
        mOnClickListener = listener
    }

    override fun save(state: Bundle) {
        state.let { bundle ->
            bundle.putString(TAG_USER_TAG, mTag)
            bundle.putString(TAG_TOP_TITLE, mTopTitle)
            bundle.putString(TAG_BOTTOM_TITLE, mBottomTitle)
            bundle.putString(TAG_ACCEPT_TITLE, mAcceptTitle)
            bundle.putString(TAG_CANCEL_TITLE, mCancelTitle)
        }
    }

    override fun restore(state: Bundle) {
        state.let { bundle ->
            mTag = bundle.getString(TAG_USER_TAG)
            mTopTitle = bundle.getString(TAG_TOP_TITLE)
            mBottomTitle = bundle.getString(TAG_BOTTOM_TITLE)
            mAcceptTitle = bundle.getString(TAG_ACCEPT_TITLE)
            mCancelTitle = bundle.getString(TAG_CANCEL_TITLE)
        }
    }

    override fun getTag(): String? {
        return mTag
    }

    override fun isBackPress(): Boolean {
        return mIsBackPress
    }

    protected open fun setTint() {
        val colorDisabled = dialog.requireContext().getColor(R.color.colorOnSurface)

        arrayOf(mCancelView, mAcceptView).forEach { view ->
            view.rippleColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 60))
            view.setTextColor(
                ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_enabled),
                        intArrayOf(-android.R.attr.state_enabled)
                    ), intArrayOf(colorPrimary, ColorUtils.setAlphaComponent(colorDisabled, 60))
                )
            )
        }
    }
}