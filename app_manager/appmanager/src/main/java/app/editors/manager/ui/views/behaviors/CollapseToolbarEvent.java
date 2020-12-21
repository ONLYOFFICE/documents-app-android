package app.editors.manager.ui.views.behaviors;

import com.google.android.material.appbar.AppBarLayout;

public class CollapseToolbarEvent implements AppBarLayout.OnOffsetChangedListener {

    public interface OnCollapseToolbar {
        void onToolbarExpand();
        void onToolbarChange(int verticalOffset);
        void onToolbarCollapse();
    }

    private enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    protected final OnCollapseToolbar mOnCollapseToolbar;
    protected State mCurrentState = State.IDLE;

    public CollapseToolbarEvent(OnCollapseToolbar onCollapseToolbar) {
        mOnCollapseToolbar = onCollapseToolbar;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mOnCollapseToolbar != null) {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    mOnCollapseToolbar.onToolbarCollapse();
                    mCurrentState = State.COLLAPSED;
                }
            } else if (verticalOffset == 0) {
                if (mCurrentState != State.EXPANDED) {
                    mOnCollapseToolbar.onToolbarExpand();
                    mCurrentState = State.EXPANDED;
                }
            } else {
                if (mCurrentState != State.IDLE) {
                    mOnCollapseToolbar.onToolbarChange(verticalOffset);
                    mCurrentState = State.IDLE;
                }
            }
        }
    }

}
