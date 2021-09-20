package lib.toolkit.base.ui.views.animation

import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Transformation

fun View.expand(duration: Long = 200) {
    measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    val targetHeight = measuredHeight

    visibility = View.VISIBLE
    val animation: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            layoutParams.height =
                if (interpolatedTime == 1f) WindowManager.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    animation.duration = duration
    startAnimation(animation)
}

fun View.collapse() {
    val initialHeight = measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height =
                    initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    animation.duration = 200
    startAnimation(animation)
}