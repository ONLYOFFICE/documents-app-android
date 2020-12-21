package app.editors.manager.ui.views.pager;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class PagingViewPager extends ViewPager {

    public static final String TAG = PagingViewPager.class.getSimpleName();

    public interface OnPagerListener {
        void onPageScroll(boolean isActive);
        void onStartScroll();
    }

    private boolean mIsPaging = true;

    public PagingViewPager(Context context) {
        super(context);
    }

    public PagingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mIsPaging && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return mIsPaging && super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public void setPaging(boolean isPaging) {
        mIsPaging = isPaging;
    }

    public boolean isPaging() {
        return mIsPaging;
    }

}