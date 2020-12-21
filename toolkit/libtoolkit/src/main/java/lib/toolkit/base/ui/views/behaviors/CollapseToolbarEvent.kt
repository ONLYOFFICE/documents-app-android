package lib.toolkit.base.ui.views.behaviors

import com.google.android.material.appbar.AppBarLayout

class CollapseToolbarEvent(private val mOnCollapseToolbar: OnCollapseToolbar?) : AppBarLayout.OnOffsetChangedListener {

    enum class State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    protected var mCurrentState =
        State.IDLE

    interface OnCollapseToolbar {
        fun onToolbarExpand()
        fun onToolbarChange(verticalOffset: Int)
        fun onToolbarCollapse()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        mOnCollapseToolbar?.let {
            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                if (mCurrentState != State.COLLAPSED) {
                    it.onToolbarCollapse()
                    mCurrentState =
                        State.COLLAPSED
                }
            } else if (verticalOffset == 0) {
                if (mCurrentState != State.EXPANDED) {
                    it.onToolbarExpand()
                    mCurrentState =
                        State.EXPANDED
                }
            } else {
                if (mCurrentState != State.IDLE) {
                    it.onToolbarChange(verticalOffset)
                    mCurrentState =
                        State.IDLE
                }
            }
        }
    }

}
