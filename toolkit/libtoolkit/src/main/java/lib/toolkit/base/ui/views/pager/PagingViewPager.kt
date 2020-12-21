package lib.toolkit.base.ui.views.pager


import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

class PagingViewPager : ViewPager {

    companion object {
        val TAG = PagingViewPager::class.java.simpleName!!
    }

    var isPaging = true

    interface OnPagerListener {
        fun onPageScroll(isActive: Boolean)
        fun onStartScroll()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return isPaging && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return try {
            isPaging && super.onInterceptTouchEvent(event)
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

}