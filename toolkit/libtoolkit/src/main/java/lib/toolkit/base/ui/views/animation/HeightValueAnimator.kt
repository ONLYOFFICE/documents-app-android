package lib.toolkit.base.ui.views.animation


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver

class HeightValueAnimator(private val view: View, private val duration: Int = DEFAULT_DURATION) {

    companion object {
        private const val DEFAULT_DURATION = 300
        private const val MAX_HEIGHT = 100
        private const val MIN_HEIGHT = 0
    }

    private inner class AnimationAdapter : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationStart(animation: Animator) {
            view.visibility = View.VISIBLE
            onAnimationListener?.onAnimationStart(animation)
        }

        override fun onAnimationEnd(animation: Animator) {
            if (isHideView) {
                view.visibility = if(isShow) View.VISIBLE else View.GONE
            }
            onAnimationListener?.onAnimationEnd(animation)
        }

        override fun onAnimationRepeat(animation: Animator) {
            super.onAnimationRepeat(animation)
            onAnimationListener?.onAnimationRepeat(animation)
        }

        override fun onAnimationCancel(animation: Animator) {
            super.onAnimationCancel(animation)
            onAnimationListener?.onAnimationCancel(animation)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val value = animation.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
            onAnimationUpdateListener?.onAnimationUpdate(animation)
        }
    }

    var isHideView = true
        private set

    var isShow = false
        private set

    val viewHeight: Int
        get() = view.height

    var onAnimationListener: Animator.AnimatorListener? = null
    var onAnimationUpdateListener: ValueAnimator.AnimatorUpdateListener? = null
    var onLayoutTreeListener: ((View) -> Unit)? = null

    var viewMinHeight = MIN_HEIGHT
    var viewMaxHeight = MAX_HEIGHT
    var viewMeasuredHeight = 0

    private var layoutAnimation: ValueAnimator? = null
    private var animationAdapter: AnimationAdapter = AnimationAdapter()

    init {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                onLayoutTree()
            }
        })
    }

    protected fun onLayoutTree() {
        viewMeasuredHeight = view.height
        viewMaxHeight = viewMeasuredHeight
        onLayoutTreeListener?.invoke(view)
    }

    fun animate(isShow: Boolean, isHideView: Boolean = true) {
        clear()

        this@HeightValueAnimator.isShow = isShow
        this@HeightValueAnimator.isHideView = isHideView

        layoutAnimation = if (isShow) {
            ValueAnimator.ofInt(viewMinHeight, viewMaxHeight)
        } else {
            ValueAnimator.ofInt(viewMaxHeight, viewMinHeight)
        }.apply {
            duration = duration.toLong()
            addListener(animationAdapter)
            addUpdateListener(animationAdapter)
        }.also {
            it.start()
        }
    }

    fun animate(isShow: Boolean) {
        val newVisibility = if (isShow) View.VISIBLE else View.GONE
        val viewVisibility = view.visibility
        if (newVisibility != viewVisibility) {
            animate(isShow, true)
        }
    }

    fun clear() {
        layoutAnimation?.apply {
            cancel()
            removeAllListeners()
            removeAllUpdateListeners()
        }
    }

}