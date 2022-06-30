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

    protected var onClickListener: CommonDialog.OnClickListener? = null
    protected lateinit var frameLayout: FrameLayout
    protected lateinit var topTitleView: AppCompatTextView
    protected lateinit var bottomTitleView: AppCompatTextView
    protected lateinit var acceptView: MaterialButton
    protected lateinit var cancelView: MaterialButton

    protected var holderTag: String? = null
    protected var topTitle: String? = null
    protected var bottomTitle: String? = null
    protected var acceptTitle: String? = null
    protected var cancelTitle: String? = null
    protected var textColor: Int = 0
    protected var topTitleGravity: Int = Gravity.START
    override var isBackPress: Boolean = true

    protected val colorPrimary: Int
        get() {
            val typedValue = TypedValue()
            dialog.requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            return typedValue.data
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialogCommonAcceptButton -> {
                onClickListener?.onAcceptClick(getType(), getValue(), holderTag)
            }
            R.id.dialogCommonCancelButton -> {
                onClickListener?.onCancelClick(getType(), holderTag)
                hide()
            }
        }
    }

    override fun init() {
        dialog.isCancelable = true
        dialog.view?.apply {
            frameLayout = findViewById(R.id.dialogCommonFrameLayout)
            topTitleView = findViewById(R.id.dialogCommonTopTitleText)
            bottomTitleView = findViewById(R.id.dialogCommonBottomTitleText)
            acceptView = findViewById(R.id.dialogCommonAcceptButton)
            cancelView = findViewById(R.id.dialogCommonCancelButton)
        }
        setTint()
    }

    override fun show() {
        frameLayout.visibility = View.VISIBLE
        acceptView.setOnClickListener(this)
        cancelView.setOnClickListener(this)

        if (topTitle.isNullOrBlank()) {
            topTitleView.visibility = View.GONE
        } else {
            topTitleView.visibility = View.VISIBLE
            topTitleView.text = topTitle
            topTitleView.gravity = topTitleGravity
        }

        if (bottomTitle.isNullOrBlank()) {
            bottomTitleView.visibility = View.GONE
        } else {
            bottomTitleView.visibility = View.VISIBLE
            bottomTitleView.text = bottomTitle
        }

        if (acceptTitle.isNullOrBlank()) {
            acceptView.visibility = View.GONE
        } else {
            acceptView.visibility = View.VISIBLE
            acceptView.text = acceptTitle
        }

        if (cancelTitle.isNullOrBlank()) {
            cancelView.visibility = View.GONE
        } else {
            cancelView.visibility = View.VISIBLE
            cancelView.text = cancelTitle
        }

        dialog.view?.let { TransitionManager.beginDelayedTransition(it as ViewGroup, Fade()) }
    }

    override fun hide() {
        onClickListener = null
    }

    override fun setClickListener(listener: CommonDialog.OnClickListener?) {
        onClickListener = listener
    }

    override fun save(state: Bundle) {
        state.let { bundle ->
            bundle.putString(TAG_USER_TAG, holderTag)
            bundle.putString(TAG_TOP_TITLE, topTitle)
            bundle.putString(TAG_BOTTOM_TITLE, bottomTitle)
            bundle.putString(TAG_ACCEPT_TITLE, acceptTitle)
            bundle.putString(TAG_CANCEL_TITLE, cancelTitle)
        }
    }

    override fun restore(state: Bundle) {
        state.let { bundle ->
            holderTag = bundle.getString(TAG_USER_TAG)
            topTitle = bundle.getString(TAG_TOP_TITLE)
            bottomTitle = bundle.getString(TAG_BOTTOM_TITLE)
            acceptTitle = bundle.getString(TAG_ACCEPT_TITLE)
            cancelTitle = bundle.getString(TAG_CANCEL_TITLE)
        }
    }

    override fun getTag(): String? {
        return holderTag
    }

    protected open fun setTint() {
        val colorDisabled = dialog.requireContext().getColor(R.color.colorOnSurface)

        arrayOf(cancelView, acceptView).forEach { view ->
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