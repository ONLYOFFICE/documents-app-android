package lib.toolkit.base.managers.utils

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class SwipeEventUtils : View.OnTouchListener {

    private var swipeCallback: SwipeCallback? = null
    private var swipeSingleCallback: SwipeSingleCallback? = null
    private var detectSwipeDirection: SwipeDirection? = null

    private var x1: Float = 0.toFloat()
    private var x2: Float = 0.toFloat()
    private var y1: Float = 0.toFloat()
    private var y2: Float = 0.toFloat()
    private var view: View? = null

    private fun detect() {
        view!!.setOnTouchListener(this)
    }

    private fun detectSingle(direction: SwipeDirection) {
        this.detectSwipeDirection = direction
        view!!.setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                y2 = event.y
                var direction: SwipeDirection? = null

                val xDiff = x2 - x1
                val yDiff = y2 - y1
                direction = if (abs(xDiff) > abs(yDiff)) {
                    if (x1 < x2) {
                        SwipeDirection.RIGHT
                    } else {
                        SwipeDirection.LEFT
                    }
                } else {
                    if (y1 > y2) {
                        SwipeDirection.TOP
                    } else {
                        SwipeDirection.BOTTOM
                    }
                }

                // Only trigger the requested event only if there
                if (detectSwipeDirection != null && swipeSingleCallback != null) {
                    if (direction == detectSwipeDirection) {
                        swipeSingleCallback!!.onSwipe()
                    }
                } else {
                    if (direction == SwipeDirection.RIGHT) {
                        swipeCallback!!.onSwipeRight()
                    }
                    if (direction == SwipeDirection.LEFT) {
                        swipeCallback!!.onSwipeLeft()
                    }
                    if (direction == SwipeDirection.TOP) {
                        swipeCallback!!.onSwipeTop()
                    }
                    if (direction == SwipeDirection.BOTTOM) {
                        swipeCallback!!.onSwipeBottom()
                    }
                }
            }
        }
        return false
    }

    enum class SwipeDirection {
        TOP, RIGHT, BOTTOM, LEFT
    }

    interface SwipeCallback {
        fun onSwipeTop()

        fun onSwipeRight()

        fun onSwipeBottom()

        fun onSwipeLeft()
    }

    interface SwipeSingleCallback {
        fun onSwipe()
    }

    companion object {

        private fun newInstance(): SwipeEventUtils {
            return SwipeEventUtils()
        }

        @JvmStatic
        fun detect(view: View, swipeCallback: SwipeCallback) {
            val evt = newInstance()
            evt.swipeCallback = swipeCallback
            evt.view = view
            evt.detect()
        }

        @JvmStatic
        fun detectTop(view: View, swipeSingleCallback: SwipeSingleCallback) {
            val evt = newInstance()
            evt.swipeSingleCallback = swipeSingleCallback
            evt.view = view
            evt.detectSingle(SwipeDirection.TOP)
        }

        @JvmStatic
        fun detectRight(view: View, swipeSingleCallback: SwipeSingleCallback) {
            val evt = newInstance()
            evt.swipeSingleCallback = swipeSingleCallback
            evt.view = view
            evt.detectSingle(SwipeDirection.RIGHT)
        }

        @JvmStatic
        fun detectBottom(view: View, swipeSingleCallback: SwipeSingleCallback) {
            val evt = newInstance()
            evt.swipeSingleCallback = swipeSingleCallback
            evt.view = view
            evt.detectSingle(SwipeDirection.BOTTOM)
        }

        @JvmStatic
        fun detectLeft(view: View, swipeSingleCallback: SwipeSingleCallback) {
            val evt = newInstance()
            evt.swipeSingleCallback = swipeSingleCallback
            evt.view = view
            evt.detectSingle(SwipeDirection.LEFT)
        }
    }
}