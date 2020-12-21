package lib.toolkit.base.ui.views.animation


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver

class HeightValueAnimator(private val mView: View, private val mDuration: Int = DEFAULT_DURATION) {

    companion object {
        private const val DEFAULT_DURATION = 300
        private const val MAX_HEIGHT = 100
        private const val MIN_HEIGHT = 0
    }

    private inner class AnimationAdapter : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationStart(animation: Animator) {
            mView.visibility = View.VISIBLE
            onAnimationListener?.onAnimationStart(animation)
        }

        override fun onAnimationEnd(animation: Animator) {
            if (isHideView) {
                mView.visibility = if(isShow) View.VISIBLE else View.GONE
            }
            onAnimationListener?.onAnimationEnd(animation)
        }

        override fun onAnimationRepeat(animation: Animator?) {
            super.onAnimationRepeat(animation)
            onAnimationListener?.onAnimationRepeat(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
            super.onAnimationCancel(animation)
            onAnimationListener?.onAnimationCancel(animation)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val value = animation.animatedValue as Int
            mView.layoutParams.height = value
            mView.requestLayout()
            onAnimationUpdateListener?.onAnimationUpdate(animation)
        }
    }

    var isHideView = true
        private set

    var isShow = false
        private set

    val viewHeight: Int
        get() = mView.height

    var onAnimationListener: Animator.AnimatorListener? = null
    var onAnimationUpdateListener: ValueAnimator.AnimatorUpdateListener? = null
    var onLayoutTreeListener: ((View) -> Unit)? = null

    var viewMinHeight = MIN_HEIGHT
    var viewMaxHeight = MAX_HEIGHT
    var viewMeasuredHeight = 0

    private var mLayoutAnimation: ValueAnimator? = null
    private var mAnimationAdapter: AnimationAdapter

    init {
        mAnimationAdapter = AnimationAdapter()
        mView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                onLayoutTree()
            }
        })
    }

    protected fun onLayoutTree() {
        viewMeasuredHeight = mView.height
        viewMaxHeight = viewMeasuredHeight
        onLayoutTreeListener?.invoke(mView)
    }

    fun animate(isShow: Boolean, isHideView: Boolean = true) {
        clear()

        this@HeightValueAnimator.isShow = isShow
        this@HeightValueAnimator.isHideView = isHideView

        mLayoutAnimation = if (isShow) {
            ValueAnimator.ofInt(viewMinHeight, viewMaxHeight)
        } else {
            ValueAnimator.ofInt(viewMaxHeight, viewMinHeight)
        }.apply {
            duration = mDuration.toLong()
            addListener(mAnimationAdapter)
            addUpdateListener(mAnimationAdapter)
        }.also {
            it.start()
        }
    }

    fun animate(isShow: Boolean) {
        val newVisibility = if (isShow) View.VISIBLE else View.GONE
        val viewVisibility = mView.visibility
        if (newVisibility != viewVisibility) {
            animate(isShow, true)
        }
    }

    fun clear() {
        mLayoutAnimation?.apply {
            cancel()
            removeAllListeners()
            removeAllUpdateListeners()
        }
    }

}